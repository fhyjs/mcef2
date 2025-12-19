package org.eu.hanana.mc.mcef2.cef;



import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandler;
import org.cef.misc.BoolRef;
import org.eu.hanana.mc.mcef2.MCEFMod;
import org.eu.hanana.mc.mcef2.test.CefTest;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.opengl.ARBInternalformatQuery2.GL_TEXTURE_2D;
import static org.lwjgl.opengl.EXTBGRA.GL_BGRA_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;


public class CefRendererLwjgl implements ICefRenderer {
    private final boolean transparent_;

    public int texture_id_ = 0;
    private int view_width_ = 0;
    protected GuiGraphics guiGraphics;
    private int view_height_ = 0;
    protected CefTexture cefTexture = new CefTexture(this);
    private Rectangle popup_rect_ = new Rectangle(0, 0, 0, 0);
    private Rectangle original_popup_rect_ = new Rectangle(0, 0, 0, 0);
    protected ResourceLocation texRl = ResourceLocation.fromNamespaceAndPath(MCEFMod.MOD_ID,"cef_render/tex/"+hashCode());
    public CefRendererLwjgl(boolean transparent) {

        transparent_ = transparent;
        initialize();

    }
    @Override
    public CompletableFuture<BufferedImage> createScreenshot(boolean nativeResolution) {
        CompletableFuture<BufferedImage> future = new CompletableFuture<>();

        // 提交任务到 GL 线程，确保读取纹理安全
        Minecraft.getInstance().doRunTask(() -> {
            try {
                if (texture_id_ == 0 || view_width_ == 0 || view_height_ == 0) {
                    future.completeExceptionally(new IllegalStateException("No texture available"));
                    return;
                }

                // 绑定纹理
                glBindTexture(GL_TEXTURE_2D, texture_id_);

                // 读取像素数据
                ByteBuffer buffer = ByteBuffer.allocateDirect(view_width_ * view_height_ * 4).order(ByteOrder.nativeOrder());
                glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA_EXT, GL_UNSIGNED_BYTE, buffer);

                BufferedImage image = new BufferedImage(view_width_, view_height_, BufferedImage.TYPE_INT_ARGB);

                for (int y = 0; y < view_height_; y++) {
                    for (int x = 0; x < view_width_; x++) {
                        int i = ((view_height_ - 1 - y) * view_width_ + x) * 4;
                        int b = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int r = buffer.get(i + 2) & 0xFF;
                        int a = buffer.get(i + 3) & 0xFF;
                        int pixel = (a << 24) | (r << 16) | (g << 8) | b;
                        image.setRGB(x, y, pixel);
                    }
                }

                glBindTexture(GL_TEXTURE_2D, 0);
                future.complete(image);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }
    protected void initialize() {
        //GlStateManager.enableTexture2D();
        texture_id_ = GlStateManager._genTexture();

        RenderSystem.bindTexture(texture_id_);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        RenderSystem.bindTexture(0);
        Minecraft.getInstance().getTextureManager().register(texRl,cefTexture);
    }
    protected boolean destroyed=false;
    @Override
    public void destroy() {
        if (destroyed ) return;
        destroyed=true;
        RenderSystem.recordRenderCall(()->{
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            try {
                Field byPath = TextureManager.class.getDeclaredField("byPath");
                byPath.setAccessible(true);
                ((Map<ResourceLocation, AbstractTexture>) byPath.get(textureManager)).remove(texRl);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            if(texture_id_ != 0) {
                RenderSystem.deleteTexture(texture_id_);
                texture_id_ = 0;
            }
        });
    }

    @SuppressWarnings("removal")
    @Override
    protected void finalize() {
        destroy(); // NO MEMORY LEAKS!
    }

    public void setGuiGraphics(GuiGraphics guiGraphics) {
        this.guiGraphics = guiGraphics;
    }
        @Override
    public void render(double x1, double y1, double x2, double y2) {
        if(view_width_ == 0 || view_height_ == 0)
            return;
        if (guiGraphics!=null) {
            int drawX = (int) x1;
            int drawY = (int) y1;
            int drawW = (int) (x2 - x1);
            int drawH = (int) (y2 - y1);

            guiGraphics.blit(
                    texRl,
                    drawX, drawY,             // 目标位置
                    drawW, drawH,             // ❗目标缩放后的宽高
                    0, 0,                     // UV 起点
                    view_width_, view_height_,// 真实纹理大小
                    view_width_, view_height_ // UV 坐标系大小
            );
        }

    }

    @Override
    public void onPopupSize(Rectangle rect) {
        if(rect.width <= 0 || rect.height <= 0)
            return;
        original_popup_rect_ = rect;
        popup_rect_ = getPopupRectInWebView(original_popup_rect_);
    }

    protected Rectangle getPopupRectInWebView(Rectangle rc) {
        // if x or y are negative, move them to 0.
        if(rc.x < 0)
            rc.x = 0;
        if(rc.y < 0)
            rc.y = 0;
        // if popup goes outside the view, try to reposition origin
        if(rc.x + rc.width > view_width_)
            rc.x = view_width_ - rc.width;
        if(rc.y + rc.height > view_height_)
            rc.y = view_height_ - rc.height;
        // if x or y became negative, move them to 0 again.
        if(rc.x < 0)
            rc.x = 0;
        if(rc.y < 0)
            rc.y = 0;
        return rc;
    }

    @Override
    public void onPopupClosed() {
        popup_rect_.setBounds(0, 0, 0, 0);
        original_popup_rect_.setBounds(0, 0, 0, 0);
    }

    @Override
    public void onPaint(boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height, boolean completeReRender) {
        // nothing to update


        if (!popup) {
            if (dirtyRects.length == 0)
                return;
            if (view_width_ != width || view_height_ != height) {
                view_width_ = width;
                view_height_ = height;
                // upload full texture
                // this also sets up the texture size and creates the texture
                if (transparent_) {
                    RenderSystem.enableBlend();
                }

                RenderSystem.bindTexture(texture_id_);
                RenderSystem.pixelStore(GL_UNPACK_ROW_LENGTH, width);
                RenderSystem.pixelStore(GL_UNPACK_SKIP_PIXELS, 0);
                RenderSystem.pixelStore(GL_UNPACK_SKIP_ROWS, 0);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
                //renderer.onPaint(buffer, width, height);
            } else {
                if (texture_id_ == 0) return;
                RenderSystem.bindTexture(texture_id_);
                RenderSystem.pixelStore(GL_UNPACK_ROW_LENGTH, width);
                for (Rectangle dirtyRect : dirtyRects) {
                    GlStateManager._pixelStore(GL_UNPACK_SKIP_PIXELS, dirtyRect.x);
                    GlStateManager._pixelStore(GL_UNPACK_SKIP_ROWS, dirtyRect.y);
                    glTexSubImage2D(GL_TEXTURE_2D, 0,  dirtyRect.x, dirtyRect.y, dirtyRect.width, dirtyRect.height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
                    //renderer.onPaint(buffer, dirtyRect.x, dirtyRect.y, dirtyRect.width, dirtyRect.height);
                }


            }
        }  else {
            if (popup_rect_.width <= 0 || popup_rect_.height <= 0)
                return;

            int x = popup_rect_.x;
            int y = popup_rect_.y;

            RenderSystem.bindTexture(texture_id_);

            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

            // 直接把 popup buffer 上传到主纹理的 (x,y)
            glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    x,
                    y,
                    width,     // popup 自己的 width
                    height,    // popup 自己的 height
                    GL_BGRA,
                    GL_UNSIGNED_BYTE,
                    buffer
            );
        }

        RenderSystem.bindTexture(0);
    }
    public static void saveTextureAsPng(int textureId, int width, int height, Path output) {
        // 绑定纹理
        RenderSystem.bindTexture(textureId);

        // 创建 byte buffer（BGRA，每像素4字节）
        int size = width * height * 4;
        ByteBuffer buffer = MemoryUtil.memAlloc(size);

        // 为读取做准备
        glPixelStorei(GL_PACK_ALIGNMENT, 1);

        // 从 GPU 纹理读取像素数据
        glGetTexImage(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                buffer
        );

        // 写入 PNG
        try {
            // 让 Java 读取 ByteBuffer
            byte[] bytes = new byte[size];
            buffer.get(bytes);
            buffer.flip();

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // OpenGL origin 在左下 -> PNG 需要左上
            int index = 0;
            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    int r = bytes[index] & 0xFF;
                    int g = bytes[index + 1] & 0xFF;
                    int b = bytes[index + 2] & 0xFF;
                    int a = bytes[index + 3] & 0xFF;

                    int argb = (a << 24) | (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, argb);

                    index += 4;
                }
            }

            ImageIO.write(image, "png", output.toFile());
            System.out.println("Saved texture to: " + output);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }
}