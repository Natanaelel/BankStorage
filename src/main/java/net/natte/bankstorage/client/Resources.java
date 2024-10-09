package net.natte.bankstorage.client;

import net.minecraft.resources.ResourceLocation;
import net.natte.bankstorage.util.Util;

public class Resources {
    public static final ResourceLocation BACKGROUND_9x1 = Util.ID("textures/gui/9x1.png");
    public static final ResourceLocation BACKGROUND_9x2 = Util.ID("textures/gui/9x2.png");
    public static final ResourceLocation BACKGROUND_9x3 = Util.ID("textures/gui/9x3.png");
    public static final ResourceLocation BACKGROUND_9x4 = Util.ID("textures/gui/9x4.png");
    public static final ResourceLocation BACKGROUND_9x5 = Util.ID("textures/gui/9x5.png");
    public static final ResourceLocation BACKGROUND_9x6 = Util.ID("textures/gui/9x6.png");
    public static final ResourceLocation BACKGROUND_9x7 = Util.ID("textures/gui/9x7.png");
    public static final ResourceLocation BACKGROUND_9x8 = Util.ID("textures/gui/9x8.png");
    public static final ResourceLocation BACKGROUND_9x9 = Util.ID("textures/gui/9x9.png");
    public static final ResourceLocation BACKGROUND_9x10 = Util.ID("textures/gui/9x10.png");
    public static final ResourceLocation BACKGROUND_9x11 = Util.ID("textures/gui/9x11.png");
    public static final ResourceLocation BACKGROUND_9x12 = Util.ID("textures/gui/9x12.png");

    public static final ResourceLocation NULL_TEXTURE = Util.ID("textures/this_file_does_not_exist");


    private static final ResourceLocation[] BACKGROUNDS = {
            NULL_TEXTURE,
            BACKGROUND_9x1,
            BACKGROUND_9x2,
            BACKGROUND_9x3,
            BACKGROUND_9x4,
            BACKGROUND_9x5,
            BACKGROUND_9x6,
            BACKGROUND_9x7,
            BACKGROUND_9x8,
            BACKGROUND_9x9,
            BACKGROUND_9x10,
            BACKGROUND_9x11,
            BACKGROUND_9x12
    };

    public static ResourceLocation backGround(int rows) {
        assert 0 <= rows && rows < BACKGROUNDS.length;
        return BACKGROUNDS[rows];
    }
}
