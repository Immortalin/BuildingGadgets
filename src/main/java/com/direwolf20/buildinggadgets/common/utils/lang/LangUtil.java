package com.direwolf20.buildinggadgets.common.utils.lang;

import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public final class LangUtil {
    private LangUtil() {}
    public static String getLangKeyPrefix(String type, String... args) {
        return getLangKey(type, args) + ".";
    }

    public static String getLangKey(String type, String... args) {
        return String.join(".", type, Reference.MODID, String.join(".", args));
    }

    public static String getFormattedBlockName(IBlockState block) {
        return getFormattedBlockName(block.getBlock());
    }

    public static String getFormattedBlockName(Block block) {
        return block.getNameTextComponent().getFormattedText();
    }

}