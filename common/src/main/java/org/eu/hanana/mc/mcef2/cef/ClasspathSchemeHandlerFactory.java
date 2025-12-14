package org.eu.hanana.mc.mcef2.cef;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ClasspathSchemeHandlerFactory implements CefSchemeHandlerFactory {
    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {

        return new CefResourceHandlerAdapter() {
            private InputStream stream;

            @Override
            public boolean processRequest(CefRequest request, CefCallback callback) {
                try {
                    // 去掉协议前缀 classpath://
                    String path = request.getURL().substring("classpath://".length());
                    if (path.contains("?")){
                        path=path.split("\\?")[0];
                    }
                    stream = getClass().getClassLoader().getResourceAsStream(path);
                    callback.Continue();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public void getResponseHeaders(CefResponse response, IntRef responseLength,
                                           StringRef redirectUrl) {
                response.setMimeType("text/html");
                if (stream != null) {
                    try {
                        responseLength.set(stream.available());
                    } catch (Exception e) {
                        responseLength.set(0);
                    }
                    response.setStatus(200);
                } else {
                    response.setStatus(404);
                    responseLength.set(0);
                }
            }

            @Override
            public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead,
                                        CefCallback callback) {
                try {
                    if (stream == null) return false;
                    int len = stream.read(dataOut, 0, bytesToRead);
                    if (len == -1) return false;
                    bytesRead.set(len);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public void cancel() {
                try {
                    if (stream != null) stream.close();
                } catch (Exception ignored) {}
            }
        };
    }

}
