package org.eu.hanana.mc.mcef2.cef;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class CefTexture extends AbstractTexture {
    public CefRendererLwjgl cefRendererLwjgl;

    public CefTexture(CefRendererLwjgl cefRendererLwjgl) {
        this.cefRendererLwjgl=cefRendererLwjgl;
    }

    @Override
    public int getId() {
        return cefRendererLwjgl.texture_id_;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {

    }

    @Override
    public void close() {
        super.close();
    }
}
