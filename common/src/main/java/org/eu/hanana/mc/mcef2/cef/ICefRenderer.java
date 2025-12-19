package org.eu.hanana.mc.mcef2.cef;

import com.google.gson.Gson;
import dev.architectury.event.EventResult;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefRequestContext;
import org.cef.callback.CefJSDialogCallback;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefJSDialogHandler;
import org.cef.misc.BoolRef;
import org.eu.hanana.mc.mcef2.cef.event.ICefMessageRouterEvent;
import org.eu.hanana.mc.mcef2.cef.jsobj.AlertResult;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.glfw.GLFW.*;

public interface ICefRenderer {
    default void onJsAlert(CefBrowserMC browser, String originUrl, CefJSDialogHandler.JSDialogType dialogType, String messageText, String defaultPromptText, CefJSDialogCallback callback, BoolRef suppressMessage){
        var render = new CefRenderStandaloneLwjglWr(true);
        //suppressMessage.set(true);
        var cmrh = (new ICefMessageRouterEvent() {
            @Override
            public EventResult onQuery(CefBrowser browser1, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback1) {
                AlertResult alertResult = null;
                try{
                    alertResult=new Gson().fromJson(request,AlertResult.class);
                }catch (Exception e){
                    callback1.failure(-1,e.toString());
                    return EventResult.pass();
                }
                callback1.success("");
                if (browser.hashCode()==alertResult.hash){
                    callback.Continue(alertResult.ok,alertResult.value);
                }
                render.addTask(()->browser1.close(false));
                return EventResult.pass();
            }
        });
        CefMessageRouterHandlerImpl.getINSTANCE().cefMessageRouterEvent.register(cmrh);
        var page = "";
        if (dialogType.equals(CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_ALERT)) {
            page="alert.html";
        }else if (dialogType.equals(CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_CONFIRM)) {
            page="confirm.html";
        }else if (dialogType.equals(CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_PROMPT)) {
            page="prompt.html";
        }
        CefBrowserMC browserMC = new CefBrowserMC(browser.getClient(), "classpath://cef_test/page/%s?browser=%d&message=%s&prompt=%s".formatted(page,browser.hashCode(),messageText,defaultPromptText), false, CefRequestContext.getGlobalContext(), render){
            @Override
            public void createImmediately() {
                new Thread(render::run).start();
                super.createImmediately();
            }

            @Override
            public synchronized void onBeforeClose() {
                super.onBeforeClose();
                callback.Continue(false,"");
                CefMessageRouterHandlerImpl.getINSTANCE().cefMessageRouterEvent.unregister(cmrh);
            }
        };
        render.addTask(()->{
            glfwSetWindowSize(render.window, 450, 300);
            glfwSetWindowAttrib(render.window, GLFW_FLOATING, GLFW_TRUE);
        });
        render.cefBrowserMC=browserMC;
        browserMC.createImmediately();
    }
    void render(double x1, double y1, double x2, double y2);

    void destroy();

    void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender);

    void onPopupSize(Rectangle var1);

    void onPopupClosed();

    default void onTitleChange(CefBrowserMC cefBrowserMC, String title){}

    CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution);
}