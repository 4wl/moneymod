package wtf.moneymod.client.utility;

import net.minecraft.client.Minecraft;

public interface Globals {

    Minecraft mc = Minecraft.getMinecraft();

    default boolean nullCheck() {
        return mc.player == null || mc.world == null;
    }

}
