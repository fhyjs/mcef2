package org.eu.hanana.mc.mcef2.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.eu.hanana.mc.mcef2.MCEFMod;
import org.eu.hanana.mc.mcef2.cef.CefBrowserMC;
import org.eu.hanana.mc.mcef2.mod.screen.widget.WebViewWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler implements Renderable {
    @Unique
    public List<WebViewWidget> mcef2$GetWebViewWidgets() {
        if (mcef2$WebViewWidgets == null) {
            mcef2$WebViewWidgets = new ArrayList<>();
        }
        return mcef2$WebViewWidgets;
    }
    @Unique
    private List<WebViewWidget> mcef2$WebViewWidgets = new ArrayList<>();
    @Unique
    public boolean mcef2$AutoCloseWebViews = true;
    @Inject(method = {"onClose"}, at = @At("HEAD"))
    public void onClose(CallbackInfo ci){
        if (mcef2$AutoCloseWebViews) {
            mcef2$WebViewWidgets.forEach(webViewWidget -> {
                webViewWidget.close();
                MCEFMod.LOGGER.info("VebView Closed");
            });
        }
    }
    @Inject(method = {"resize"}, at = @At("HEAD"))
    public void resize(Minecraft minecraft, int width, int height,CallbackInfo ci){
        mcef2$WebViewWidgets.forEach(webViewWidget -> {
            webViewWidget.setSize(webViewWidget.getWidth(),webViewWidget.getHeight());
        });

    }
}
