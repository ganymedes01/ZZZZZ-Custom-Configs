package ganymedes01.zzzzzcustomconfigs.files;

import ganymedes01.zzzzzcustomconfigs.lib.ConfigFile;
import ganymedes01.zzzzzcustomconfigs.xml.XMLNode;
import ganymedes01.zzzzzcustomconfigs.xml.XMLParser;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public class Smelting extends ConfigFile {

	public static List<ItemStack> addedInputs = new LinkedList<ItemStack>();
	private static String header = "Examples:\n\n";
	static {
		header += "<recipe>\n";
		header += "\t<output>minecraft:diamond 2 0</output>\n";
		header += "\t<input>minecraft:diamond_sword 1 0</input>\n";
		header += "\t<xp>1.0</xp>\n";
		header += "</recipe>";
	}

	public Smelting() {
		super("Smelting", header);
	}

	@Override
	public void preInit() {
		for (XMLNode node : xmlNode.getNodes())
			if (node.getName().equals("recipe")) {
				ItemStack output = XMLParser.parseItemStackNode(node.getNode("output"));
				ItemStack input = XMLParser.parseItemStackNode(node.getNode("input"));
				float xp = Float.parseFloat(node.getNode("xp").getValue());

				GameRegistry.addSmelting(input, output, xp);
				addedInputs.add(input);
			}
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}