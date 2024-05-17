package net.natte.bankstorage.rendering;

import net.minecraft.client.MinecraftClient;

public class ItemCountUtils {

	private static final String[] POWER = { "K", "M", "B", "T" };

	public static String toConsiseString(int count) {
		int index = 0;
		if (count > 9999) {
			while (count >= 1000) {
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
	 * 1 == vanilla
	 */
	public static float scale(String string) {
		MinecraftClient client = MinecraftClient.getInstance();
		int guiScale = (int) client.getWindow().getScaleFactor();
		float width = client.textRenderer.getTextHandler().getWidth(string);
		float scale = 16f / width;
		float step = Math.max(3, guiScale);
		return Math.min(1f, scaledFloor(scale, step));
	}

	private static float scaledFloor(float value, float step) {
		return ((float) (int) (value * step)) / step;
	}
}
