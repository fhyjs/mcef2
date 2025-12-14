package org.eu.hanana.mc.mcef2.test;

import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.browser.CefRequestContext;
import org.cef.callback.CefJSDialogCallback;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefJSDialogHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.cef.misc.BoolRef;
import org.eu.hanana.mc.mcef2.cef.CefBrowserMC;
import org.eu.hanana.mc.mcef2.cef.CefRenderStandaloneLwjglWr;
import org.eu.hanana.mc.mcef2.cef.CelInstaller;
import org.eu.hanana.mc.mcef2.mod.cef.CefUtil;
import org.lwjgl.glfw.GLFW;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;

public class CefBrowserMC_Test {

    private static final Logger log = LogManager.getLogger(CefBrowserMC_Test.class);

    public static void main(String[] args) throws Exception {
        // 初始化 JCEF
        var cefBuilder = CelInstaller.getBuilder();
        cefBuilder.install();
        cefBuilder.getCefSettings().log_severity = CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;
        cefBuilder.getCefSettings().windowless_rendering_enabled=true;
        CefApp cefApp = null;
        cefApp = CefUtil.buildCefApp(cefBuilder);
        CefClient client = CefUtil.createClient(cefApp);
        CefRenderStandaloneLwjglWr renderStandaloneLwjglWr;
        // 创建我们的自定义浏览器
        CefApp finalCefApp1 = cefApp;
        
        CefBrowserMC browserMC = new CefBrowserMC(
                client,
                "classpath://cef_test/welcome.html",
                false,
                CefRequestContext.getGlobalContext(),
                renderStandaloneLwjglWr=new CefRenderStandaloneLwjglWr(true){
                    public boolean destroyed = false;
                    @Override
                    public void destroy() {
                        if (destroyed) return;
                        destroyed=true;
                        super.destroy();
                        GLFW.glfwTerminate();
                        new Thread(() -> {
                            finalCefApp1.dispose(); // 等 CEF 线程干净退出
                            System.exit(0);
                        }, "Shutdown-Thread").start();

                    }
                }
        );
        browserMC.setCloseAllowed();
        browserMC.createImmediately();
        browserMC.setFocus(true);
        browserMC.wasResized_(1000,1000);
        renderStandaloneLwjglWr.addTask(()->{
            Field renderThread = null;
            try {
                renderThread = RenderSystem.class.getDeclaredField("renderThread");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            renderThread.setAccessible(true);
            try {
                renderThread.set(null,Thread.currentThread());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        CefApp finalCefApp = cefApp;
        renderStandaloneLwjglWr.cefBrowserMC=browserMC;
        renderStandaloneLwjglWr.addTask(()->{
            browserMC.reload();;
            //browserMC.openDevTools();
        });
        renderStandaloneLwjglWr.run();
    }
}