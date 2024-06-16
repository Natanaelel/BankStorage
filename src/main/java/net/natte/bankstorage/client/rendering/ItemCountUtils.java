package net.natte.bankstorage.client.rendering;

import net.minecraft.client.Minecraft;

public class ItemCountUtils {

    private static final String[] POWER = {"k", "M", "B", "T"};

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
    private static float scale = 1f;
    private static int i = 0;

    public static float scale() {
        // cache "expensive" calculation because why not
        if (i++ % 1000 == 0) {
            Minecraft client = Minecraft.getInstance();
            int guiScale = (int) client.getWindow().getGuiScale();
            scale = Math.max(1f, (int) (0.7f * guiScale)) / guiScale;
        }
        return scale;
    }
}
