package ganymedes01.zzzzzcustomconfigs.files;

import ganymedes01.zzzzzcustomconfigs.lib.ConfigFile;
import ganymedes01.zzzzzcustomconfigs.xml.XMLNode;
import ganymedes01.zzzzzcustomconfigs.xml.XMLParser;

import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import cpw.mods.fml.common.Loader;

public class Thaumcraft4 extends ConfigFile {

	private static String header = "Examples:\n";
	static {
		header += "<aspects>\n";
		header += "\t<stack>minecraft:diamond 1 0</stack>\n";
		header += "\t<aspect1>terra 1</aspect1>\n";
		header += "\t<aspect2>aer 5</aspect2>\n";
		header += "</aspects>\n";
		header += "\n";
		header += "Aspects: ";
		for (Entry<String, Aspect> entry : Aspect.aspects.entrySet())
			header += entry.getKey() + ", ";
		header = header.substring(0, header.length() - 2);
	}

	public Thaumcraft4() {
		super("Thaumcraft4", header);
	}

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		for (XMLNode node : xmlNode.getNodes())
			if (node.getName().equals("aspects")) {
				Object input = XMLParser.parseNode(node.getNode("stack"));
				AspectList aspects = new AspectList();
				for (XMLNode n : node.getNodes())
					if (n.getName().startsWith("aspect")) {
						String[] data = n.getValue().split(" ");
						aspects.add(Aspect.getAspect(data[0]), Integer.parseInt(data[1]));
					}

				if (input instanceof ItemStack)
					ThaumcraftApi.registerComplexObjectTag((ItemStack) input, aspects);
				else if (input instanceof String)
					ThaumcraftApi.registerObjectTag((String) input, aspects);
				else
					throw new RuntimeException("Invalid object type. Must be ItemStack or String");
			}
	}

	@Override
	public void postInit() {
	}

	@Override
	public boolean isEnabled() {
		return Loader.isModLoaded("Thaumcraft");
	}

	public static void registerAspects(Logger logger, String line) {
		String[] data = line.split("=");

		String[] idMeta = data[0].trim().split(":");
		String aspects = data[1].trim();

		Item item = (Item) Item.itemRegistry.getObject(idMeta[0].trim());
		int meta = getInt(idMeta[1]);

		ThaumcraftApi.registerObjectTag(new ItemStack(item, 1, meta), getAspects(aspects));
		logger.log(Level.INFO, "Registered aspects for " + idMeta[0]);
	}

	private static AspectList getAspects(String line) {
		AspectList list = new AspectList();
		String[] data = line.split(",");

		for (String s : data) {
			String[] asp = s.split(":");
			list.add(Aspect.getAspect(asp[0].toLowerCase().trim()), getInt(asp[1]));
		}

		return list;
	}

	private static int getInt(String s) {
		return Integer.parseInt(s.trim());
	}
}