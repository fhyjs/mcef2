package org.eu.hanana.mc.mcef2.mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.eu.hanana.mc.mcef2.mod.screen.WebViewScreenScreen;

public class Mcef2KeyBindingsRegister {
    public static final KeyMapping OPEN_BOWSER = new KeyMapping(
            "key.mcef2.openbsr", // The translation key of the name shown in the Controls screen
            InputConstants.Type.KEYSYM, // This key mapping is for Keyboards by default
            InputConstants.KEY_F10, // The default keycode
            "category.mcef.key" // The category translation key used to categorize in the Controls screen
    );
    public static void reg(){
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            while (OPEN_BOWSER.consumeClick()) {
                Minecraft.getInstance().setScreen(new WebViewScreenScreen());
            }
        });
    }
}
