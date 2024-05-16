package net.natte.bankstorage.rendering;

import net.minecraft.client.MinecraftClient;
import net.natte.bankstorage.util.Util;

public class ItemCountUtils {

	private static final String[] POWER = { "K", "M", "B", "T" };

	public static String toConsiseString(int count) {
		int index = 0;
		if (count > 9999) {
			while (count / 1000 != 0) {
				count /= 1000;
				index++;
			}
		}

		if (index > 0) {
			return count + POWER[index - 1];
		} else {
			return String.valueOf(count);
		}
	}

	/**
	 * How big or small to render the item count text
	 * 
	 * @return 1 == vanilla
	 */
	public static float scale(String string) {
		MinecraftClient client = MinecraftClient.getInstance();
		int guiScale = client.options.getGuiScale().getValue();
		// return new float[][]{new float[]{0}}[guiScale][0];
		
		// System.out.println(string);
		// float factorDependingOnTextLength = string.length() >= 4 ? 3f/5f : string.length() >= 3 ? 5f/6f : 1f;
		int length = string.length();
		float factorDependingOnTextLength = length >= 4 ? 2f/3f : length >= 3 ? 0.9f : 1f;
		return Util.isDebugMode ? 5f / 8f : guiScale == 1 ? length >= 4 ? 0.7f : length >= 3 ? 1f : 1f : guiScale == 2 ? length >= 4 ? 4f/6f : length >= 3 ? 5f/6f : 1f : Math.max(1f, (float)(int)(guiScale * factorDependingOnTextLength)) / guiScale;
		// if (string.length() > 3) {
		// } else if (string.length() == 3) {
		// 	// return 6f / 8f;
		// 	// return (guiScale - 1f) / guiScale;
		// 	return Util.isDebugMode ?  5f / 8f : (guiScale - 1f) / guiScale;
		// }
		// return 1f;
	}
}
