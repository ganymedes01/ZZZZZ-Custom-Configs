package ganymedes01.zzzzzcustomconfigs.files;

import ganymedes01.zzzzzcustomconfigs.lib.ConfigFile;
import ganymedes01.zzzzzcustomconfigs.xml.XMLBuilder;
import ganymedes01.zzzzzcustomconfigs.xml.XMLNode;
import ganymedes01.zzzzzcustomconfigs.xml.XMLParser;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.TerrainGen;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class OreSpawn extends ConfigFile {

	private static String header = "Examples:\n\n";
	static {
		XMLBuilder builder = new XMLBuilder("add");
		builder.makeEntry("ore", new ItemStack(Blocks.sponge));
		builder.makeEntry("target", new ItemStack(Blocks.stone));
		builder.makeEntry("veinSize", 5);
		builder.makeEntry("veinCount", 10);
		builder.makeEntry("minY", 0);
		builder.makeEntry("maxY", 256);
		header += "The following adds SPONGES to generate on smooth stone, in veins with 5 blocks at most, up to 10 veins in one chunk, with a minimum Y level of 0, and a maximum Y level of 256";
		header += builder.toString() + "\n\n";

		builder = new XMLBuilder("remove");
		builder.makeEntries("type", new Object[] { "coal", "diamond", "iron" });
		header += "The following shows how to remove a VANILLA ore from being generated. There is no way to block ores added by other mods.\n";
		header += "Ore types:\nCOAL\nDIAMOND\nDIRT\nGOLD\nGRAVEL\nIRON\nLAPIS\nREDSTONE\nQUARTZ\n";
		header += builder.toString();
	}

	private static final List<GenerateMinable.EventType> removedOres = new LinkedList<GenerateMinable.EventType>();

	public OreSpawn() {
		super("OreSpawn", header);
	}

	@Override
	public void init() {
		for (XMLNode node : xmlNode.getNodes())
			if (node.getName().equals("add")) {
				ItemStack oreStack = XMLParser.parseItemStackNode(node.getNode("ore"));
				int veinSize = Integer.parseInt(XMLParser.parseStringNode(node.getNode("veinSize")));
				int veinCount = Integer.parseInt(XMLParser.parseStringNode(node.getNode("veinCount")));
				int minY = Integer.parseInt(XMLParser.parseStringNode(node.getNode("minY")));
				int maxY = Integer.parseInt(XMLParser.parseStringNode(node.getNode("maxY")));
				ItemStack targetStack = XMLParser.parseItemStackNode(node.getNode("target"));

				Block ore = Block.getBlockFromItem(oreStack.getItem());
				Block target = Block.getBlockFromItem(targetStack.getItem());
				if (ore == Blocks.air || target == Blocks.air)
					throw new IllegalArgumentException("Argument passed must be a block, not an item: " + oreStack);

				WorldGenMinable generator = new WorldGenMinable(ore, oreStack.getItemDamage(), veinSize, target);
				MineableOre.makeMineable(generator, veinCount, minY, maxY);
			} else if (node.getName().equals("remove")) {
				for (XMLNode n : node.getNodes())
					if (n.getName().startsWith("type")) {
						String type = XMLParser.parseStringNode(n).toUpperCase();
						try {
							GenerateMinable.EventType eventType = GenerateMinable.EventType.valueOf(type);
							removedOres.add(eventType);
						} catch (Exception e) {
							throw new IllegalArgumentException("Invalid ore type: " + type);
						}
					}
			} else
				throw new IllegalArgumentException("Invalid operation: " + node.getName());
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

	public static void onPostOreGen(World world, Random rand, int chunkX, int chunkZ) {
		for (MineableOre ore : MineableOre.ores)
			if (world.provider.dimensionId == 0) {
				WorldGenMinable generator = ore.generator;

				if (TerrainGen.generateOre(world, rand, generator, chunkX, chunkZ, GenerateMinable.EventType.CUSTOM))
					for (int i = 0; i < ore.veinCount; i++) {
						int x = chunkX + rand.nextInt(16);
						int y = ore.minY + rand.nextInt(ore.maxY);
						int z = chunkZ + rand.nextInt(16);

						generator.generate(world, rand, x, y, z);
					}
			}
	}

	public static void onOreGen(OreGenEvent.GenerateMinable event) {
		if (removedOres.contains(event.type))
			event.setResult(Result.DENY);
	}

	private static class MineableOre {

		static final List<MineableOre> ores = new LinkedList<MineableOre>();

		final WorldGenMinable generator;
		final int veinCount, minY, maxY;

		static void makeMineable(WorldGenMinable generator, int veinCount, int minY, int maxY) {
			ores.add(new MineableOre(generator, veinCount, minY, maxY));
		}

		MineableOre(WorldGenMinable generator, int veinCount, int minY, int maxY) {
			this.generator = generator;
			this.veinCount = veinCount;
			this.minY = minY;
			this.maxY = maxY;
		}
	}
}