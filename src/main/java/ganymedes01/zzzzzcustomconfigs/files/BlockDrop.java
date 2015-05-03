package ganymedes01.zzzzzcustomconfigs.files;

import ganymedes01.zzzzzcustomconfigs.lib.ConfigFile;
import ganymedes01.zzzzzcustomconfigs.xml.XMLBuilder;
import ganymedes01.zzzzzcustomconfigs.xml.XMLNode;
import ganymedes01.zzzzzcustomconfigs.xml.XMLParser;
import ganymedes01.zzzzzcustomconfigs.xml.XMLParser.NodeType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.oredict.OreDictionary;

public class BlockDrop extends ConfigFile {

	private static List<BlockBundle> bundles = new LinkedList<BlockBundle>();

	private static String header = "Examples:\n\n";
	static {
		header += "Use this file to add/remove drops from harvestable blocks\n";
		header += "This may not work for every block. Modded blocks depend on their owners allowing this to happen and some vanilla ones have been bugged forever.\n\n";

		header += "The following shows how to make the Block of Gold drop 9 ingots instead of itself";
		XMLBuilder builder = new XMLBuilder(Block.blockRegistry.getNameForObject(Blocks.gold_block));
		builder.makeEntry("drop0", new ItemStack(Items.gold_ingot, 9)).addProperty("chance", "1.0");
		builder.makeEntry("blacklist0", new ItemStack(Blocks.gold_block));
		header += builder.toNode().addProperty("meta", "0").toString() + "\n\n";
	}

	public BlockDrop() {
		super("BlockDrops", header);
	}

	@Override
	public void init() {
		for (XMLNode node : xmlNode.getNodes()) {
			Block block = Block.getBlockFromName(node.getName());
			int meta = Integer.parseInt(node.getProperty("meta"));
			List<StackWithChance> drops = new ArrayList<StackWithChance>();
			List<ItemStack> blacklist = new ArrayList<ItemStack>();

			for (XMLNode n : node.getNodes())
				if (n.getName().startsWith("drop")) {
					ItemStack stack = XMLParser.parseItemStackNode(n, NodeType.OUTPUT);
					float chance = Float.parseFloat(n.getProperty("chance"));
					drops.add(new StackWithChance(stack, chance));
				}

			for (XMLNode n : node.getNodes())
				if (n.getName().startsWith("blacklist")) {
					ItemStack stack = XMLParser.parseItemStackNode(n, NodeType.N_A);
					blacklist.add(stack);
				}

			bundles.add(new BlockBundle(block, meta, drops.toArray(new StackWithChance[drops.size()]), blacklist.toArray(new ItemStack[blacklist.size()])));
		}
	}

	@Override
	public void postInit() {
	}

	@Override
	public void serverStarting() {
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public static void onBlockHarvest(HarvestDropsEvent event) {
		for (BlockBundle bundle : bundles)
			if (bundle.block == event.block && (bundle.meta == event.blockMetadata || bundle.meta == OreDictionary.WILDCARD_VALUE)) {
				event.dropChance = 1;

				for (StackWithChance stack : bundle.drops)
					if (event.world.rand.nextFloat() <= stack.chance)
						event.drops.add(stack.stack.copy());

				List<ItemStack> toRemove = new ArrayList<ItemStack>();
				for (ItemStack drop : event.drops)
					for (ItemStack blacklisted : bundle.blacklist)
						if (areStacksTheSame(blacklisted, drop))
							toRemove.add(drop);
				for (ItemStack drop : toRemove)
					event.drops.remove(drop);
			}
	}

	private static boolean areStacksTheSame(ItemStack target, ItemStack stack) {
		return OreDictionary.itemMatches(target, stack, false) && ItemStack.areItemStackTagsEqual(target, stack);
	}

	private static class BlockBundle {

		final Block block;
		final int meta;
		final StackWithChance[] drops;
		final ItemStack[] blacklist;

		BlockBundle(Block block, int meta, StackWithChance[] drops, ItemStack[] blacklist) {
			this.block = block;
			this.meta = meta;
			this.drops = drops;
			this.blacklist = blacklist;

			if (meta != OreDictionary.WILDCARD_VALUE && (meta < 0 || meta > 15))
				throw new IllegalArgumentException("Metadatas can only go from 0 to 15 so " + meta + " is not a valid value.");
		}
	}

	private static class StackWithChance {

		final ItemStack stack;
		final float chance;

		StackWithChance(ItemStack stack, float chance) {
			this.stack = stack;
			this.chance = chance;
		}
	}
}