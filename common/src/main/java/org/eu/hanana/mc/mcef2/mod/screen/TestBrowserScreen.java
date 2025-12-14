package org.eu.hanana.mc.mcef2.mod.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.eu.hanana.mc.mcef2.mod.cef.CefUtil;
import org.eu.hanana.mc.mcef2.mod.screen.widget.WebViewWidget;

public class TestBrowserScreen extends Screen {
    protected WebViewWidget webViewWidget;
    public TestBrowserScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        if (webViewWidget==null){
            webViewWidget= new WebViewWidget(this,30,20,200,200,Component.empty());
        }
        this.addRenderableWidget(webViewWidget);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        webViewWidget.mouseMoved(mouseX,mouseY);
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
