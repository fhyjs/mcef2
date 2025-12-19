package org.eu.hanana.mc.mcef2.mod.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;
import org.eu.hanana.mc.mcef2.MCEFMod;
import org.eu.hanana.mc.mcef2.cef.event.ICefLoadEvent;
import org.eu.hanana.mc.mcef2.mod.cef.CefUtil;
import org.eu.hanana.mc.mcef2.mod.screen.widget.WebViewWidget;

public class WebViewScreenScreen extends Screen {
    public static String lastUrl="classpath://cef_test/welcome.html";
    protected WebViewWidget webViewWidget;
    protected ICefLoadEvent event = new ICefLoadEvent() {
        @Override
        public EventResult onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
            backBtn.active=canGoBack;
            frontBtn.active=canGoForward;
            editBoxText.setValue(browser.getURL());
            return EventResult.pass();
        }

        @Override
        public EventResult onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {
            editBoxText.setValue(browser.getURL());
            editBoxText.setTextColor(0xffefef14);
            return EventResult.pass();
        }

        @Override
        public EventResult onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
            editBoxText.setTextColor(0xffffffff);
            return EventResult.pass();
        }

        @Override
        public EventResult onLoadError(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode, String errorText, String failedUrl) {
            return EventResult.pass();
        }
    };
    protected Button backBtn,frontBtn,reflashBtn;
    public EditBox editBoxText;
    public WebViewScreenScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        if (webViewWidget==null){
            webViewWidget= new WebViewWidget(this,13,33, (int) (380/minecraft.getWindow().getGuiScale()),200,Component.empty(),lastUrl);
            CefUtil.getCefLoadEvent().register(event);
        }
        webViewWidget.setSize(getRectangle().width()-30, getRectangle().height()-40);
        this.addRenderableWidget(webViewWidget);
        addRenderableWidget(backBtn=Button.builder(Component.literal("<-"),(button)->{
            webViewWidget.getCefBrowserMC().goBack();
        }).bounds(15,10,20,20).build());
        addRenderableWidget(reflashBtn=Button.builder(Component.literal("R"),(button)->{
            webViewWidget.getCefBrowserMC().reload();
        }).bounds(40,10,20,20).build());
        addRenderableWidget(frontBtn=Button.builder(Component.literal("->"),(button)->{
            webViewWidget.getCefBrowserMC().goForward();
        }).bounds(65,10,20,20).build());
        addRenderableWidget(editBoxText=new EditBox(font,100,10, (int) (getRectangle().width()*0.7),20,Component.empty()));
        editBoxText.setMaxLength(Integer.MAX_VALUE);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode== InputConstants.KEY_RETURN||keyCode== InputConstants.KEY_NUMPADENTER){
            if (editBoxText.isFocused()){
                webViewWidget.getCefBrowserMC().loadURL(editBoxText.getValue());
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        webViewWidget.mouseMoved(mouseX,mouseY);
    }

    @Override
    public void onClose() {
        CefUtil.getCefLoadEvent().unregister(event);
        lastUrl=webViewWidget.getCefBrowserMC().getURL();
        super.onClose();
    }
}
