package org.eu.hanana.mc.mcef2.mod.screen.widget;

import com.google.errorprone.annotations.MustBeClosed;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefRequestContext;
import org.cef.handler.*;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;
import org.eu.hanana.mc.mcef2.MCEFMod;
import org.eu.hanana.mc.mcef2.cef.CefBrowserMC;
import org.eu.hanana.mc.mcef2.cef.CefRendererLwjgl;
import org.eu.hanana.mc.mcef2.mixin.MixinScreen;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import sun.misc.Unsafe;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ListIterator;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public class WebViewWidget extends AbstractWidget implements Closeable {
    private static final Logger log = LogManager.getLogger(WebViewWidget.class);
    private final Screen screen;
    protected CefBrowserMC cefBrowserMC;
    protected CefRendererLwjgl cefRendererLwjgl;
    @SuppressWarnings("unchecked")
    @MustBeClosed
    public WebViewWidget(Screen screen, int x, int y, int width, int height, Component message) {
        this(screen,x,y,width,height,message,"classpath://cef_test/welcome.html");
    }
    public WebViewWidget(Screen screen, int x, int y, int width, int height, Component message,String url) {
        super(x, y, width, height, message);
        this.screen=screen;
        MCEFMod.LOGGER.info("Created a webview in screen:{} ,class:{}", screen.getTitle().getString(),screen.getClass());
        cefBrowserMC=new CefBrowserMC(MCEFMod.cefClient,url,true, CefRequestContext.getGlobalContext(),cefRendererLwjgl=new CefRendererLwjgl(true));
        try {
            ((ArrayList<WebViewWidget>) screen.getClass().getMethod("mcef2$GetWebViewWidgets").invoke(screen)).add(this);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        cefBrowserMC.createImmediately();
        resize0(width,height);
        //cefBrowserMC.openDevTools();
        cefBrowserMC.setFocus(true);
        cefBrowserMC.setCloseAllowed();
        cefBrowserMC.reload();
    }

    public CefBrowserMC getCefBrowserMC() {
        return cefBrowserMC;
    }

    protected void resize0(int w, int h){
        int realPixelWidth  = (int) (getScaleW(w));
        int realPixelHeight = (int) (getScaleH(h));
        if (realPixelHeight<1||realPixelWidth<1){
            log.warn("size <1");
            return;
        }
        cefBrowserMC.wasResized_(realPixelWidth,realPixelHeight);
    }
    private double getScaleH(int h){
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        return h/(window.getGuiScaledHeight()/(window.getHeight()*1d));
    }
    private double getScaleW(int w){
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        return w/(window.getGuiScaledWidth()/(window.getWidth()*1d));
    }
    private double getScaleX() { return getScaleW(getWidth()) / getWidth(); }
    private double getScaleY() { return getScaleH(getHeight()) / getHeight(); }
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        if (cefBrowserMC.isLoading()){
            guiGraphics.fill(getX(),getY(),width+getX(),height+getY(),0xff3fffff);
            guiGraphics.drawString(Minecraft.getInstance().fontFilterFishy,"WebView is loading... ",width/2-10,height/3,0,false);
        }
        //GL11.glEnable(GL_TEXTURE_2D);
        cefBrowserMC.mcefUpdate();
        cefRendererLwjgl.setGuiGraphics(guiGraphics);
        cefRendererLwjgl.render(getX(),getY(),width+getX(),height+getY());
        //GL11.glDisable(GL_TEXTURE_2D);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        cefBrowserMC.mouseMoved((int) ((mouseX-getX())*getScaleX()),  (int) ((mouseY-getY())*getScaleY()),0);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        cefBrowserMC.mouseDragged((int) ((mouseX-getX())*getScaleX()), (int) ((mouseY-getY())*getScaleY()),button,dragX,dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        cefBrowserMC.mouseInteracted((int) ((mouseX-getX())*getScaleX()), (int) ((mouseY-getY())*getScaleY()),0,button,false,0);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        cefBrowserMC.mouseInteracted((int) ((mouseX-getX())*getScaleX()), (int) ((mouseY-getY())*getScaleY()),0,button,true,0);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (cefBrowserMC != null) {
            int localX = (int) (mouseX - getX());
            int localY = (int) (mouseY - getY());

            // Minecraft scrollY = +1(向上), -1(向下)
            // CEF 需要像素值，标准是 ×120
            int delta = (int) (scrollY * 120);

            cefBrowserMC.mouseScrolled(localX, localY, 0, delta, 1);
            return true; // 消费事件，不要让原版继续滚动
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        cefBrowserMC.keyEventByKeyCode(keyCode,scanCode,0,true);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        cefBrowserMC.keyEventByKeyCode(keyCode,scanCode,0,false);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        cefBrowserMC.keyTyped(codePoint,modifiers);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        resize0(width,height);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        resize0(width,getHeight());
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        resize0(getWidth(),height);
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return super.getCurrentFocusPath();
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }

    @Override
    public void close()  {
        cefBrowserMC.close();
    }
}
