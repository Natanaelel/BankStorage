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
		if(Util.isDebugMode){
			if (string.length() > 3) {
				return 5f / 8f;
			} else if (string.length() == 3) {
				return 6f / 8f;
			}
			return 1f;
		}
		return newScale(string);
	}

	private static float newScale(String string) {
		MinecraftClient client = MinecraftClient.getInstance();
        int guiScale = (int) client.getWindow().getScaleFactor();
        float width = client.textRenderer.getTextHandler().getWidth(string);
        if (guiScale == 1)
            return width / 6.0 >= 4 ? 0.7f : 1f;

        if (width == 0) // *should* tm never be 0 but why not
            return 1f;
        float scale = 16f / width;
        float step = Math.max(3, guiScale);
        return Math.min(1f, scaledFloor(scale, step));
	}

	private static float scaledFloor(float value, float step) {
        return ((float) (int) (value * step)) / step;
    }
}
