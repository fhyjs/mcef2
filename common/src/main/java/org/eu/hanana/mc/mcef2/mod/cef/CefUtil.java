package org.eu.hanana.mc.mcef2.mod.cef;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.architectury.event.EventResult;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefBuildInfo;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.*;
import org.cef.callback.*;
import org.cef.handler.*;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;
import org.eu.hanana.mc.mcef2.cef.*;
import org.eu.hanana.mc.mcef2.cef.dev.DevSearchItem;
import org.eu.hanana.mc.mcef2.cef.event.ICefAppHandlerEvent;
import org.eu.hanana.mc.mcef2.mixin.MixinScreen;
import org.eu.hanana.mc.mcef2.mod.screen.widget.WebViewWidget;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ListIterator;

public class CefUtil {
    private static CefAppHandlerImpl cefAppHandler = null;
    public static int getRandomPort() {
        try (ServerSocket socket = new ServerSocket(0)) { // 0 表示随机可用端口
            return socket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
            return 9222; // 失败回退
        }
    }
    @Environment(EnvType.CLIENT)
    public static void setAutoCloseWebViewEnabled(boolean enabled, Screen screen) {
        try {
            screen.getClass().getField("mcef2$AutoCloseWebViews").set(screen,enabled);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static CefAppHandlerImpl getCefAppHandler() {
        return cefAppHandler;
    }
    @ApiStatus.Internal
    public static CefClient createClient(CefApp cefApp){
        var client = cefApp.createClient();
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadStart(CefBrowser browser, CefFrame frame_, CefRequest.TransitionType transitionType) {
                CefDevToolsClient devToolsClient = browser.getDevToolsClient();
                devToolsClient.executeDevToolsMethod("Page.enable");
                devToolsClient.addEventListener(new CefDevToolsClient.EventListener() {
                    @Override
                    public void onEvent(String eventName, String messageAsJson) {
                    if (!"Page.frameNavigated".equals(eventName)) return;
                    var gson = new Gson();
                    // 使用 Gson 解析 JSON
                    JsonObject json = gson.fromJson(messageAsJson, JsonObject.class);
                    JsonObject frame = json.getAsJsonObject("frame");

                    // 只处理主 frame
                    if (frame.has("parentId")) return;

                    // JS 注入语句
                    String js = """
                        (function() {
                            if (!window.__JCEF_BROWSER_ID__) {
                                window.__JCEF_BROWSER_ID__ = "%s";
                            }
                            console.log("__JCEF_BROWSER_ID__:"+window.__JCEF_BROWSER_ID__);
                        })();
                        """.formatted(browser.hashCode());

                    // 构建参数对象
                    JsonObject args = new JsonObject();
                    args.addProperty("expression", js);
                    args.addProperty("includeCommandLineAPI", false);

                    // 注入 JS
                    new Thread(()->{
                        devToolsClient.executeDevToolsMethod("Runtime.evaluate", gson.toJson(args)).join();
                        devToolsClient.removeEventListener(this);
                    }).start();
                }});


                super.onLoadStart(browser, frame_, transitionType);
            }
        });
        client.addJSDialogHandler(new CefJSDialogHandlerAdapter() {
            @Override
            public boolean onJSDialog(CefBrowser browser, String origin_url, JSDialogType dialog_type, String message_text, String default_prompt_text, CefJSDialogCallback callback, BoolRef suppress_message) {
                if (browser instanceof CefBrowserMC cefBrowserMC){
                    cefBrowserMC.onJSDialog(cefBrowserMC,origin_url,dialog_type,message_text,default_prompt_text,callback,suppress_message);
                    return true;
                }
                return super.onJSDialog(browser, origin_url, dialog_type, message_text, default_prompt_text, callback, suppress_message);
            }
        });
        client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
                if (browser instanceof CefBrowserMC cefBrowserMC){
                    return cefBrowserMC.onBeforePopup(browser, frame, target_url, target_frame_name);
                }
                return super.onBeforePopup(browser, frame, target_url, target_frame_name);
            }
        });
        client.addDisplayHandler(new CefDisplayHandlerAdapter() {

            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                if (browser instanceof CefBrowserMC cefBrowserMC){
                    cefBrowserMC.onTitleChange(cefBrowserMC, title);
                }

                // 例如：同步到窗口标题 / UI
                // glfwSetWindowTitle(window, title);
            }
        });
        client.addContextMenuHandler(new CefContextMenuHandlerAdapter() {

            @Override
            public void onBeforeContextMenu(
                    CefBrowser browser,
                    CefFrame frame,
                    CefContextMenuParams params,
                    CefMenuModel model) {

                // 清空默认菜单（关键）
                model.clear();
            }

            @Override
            public boolean onContextMenuCommand(
                    CefBrowser browser,
                    CefFrame frame,
                    CefContextMenuParams params,
                    int commandId,
                    int eventFlags) {

                return false;
            }

            @Override
            public void onContextMenuDismissed(CefBrowser browser, CefFrame frame) {
            }
        });
        client.addDragHandler((browser, dragData, mask) -> {

            // 返回 false = 允许拖拽
            return false;
        });
        CefMessageRouter router = CefMessageRouter.create(new CefMessageRouter.CefMessageRouterConfig());
        router.addHandler(new CefMessageRouterHandlerImpl(), true);

        client.addMessageRouter(router);
        return client;
    }
    @ApiStatus.Internal
    public static CefApp buildCefApp(CefAppBuilder builder) throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        if (cefAppHandler!=null){
            throw new IllegalStateException("CefApp already built!");
        }

        //builder.getCefSettings().windowless_rendering_enabled=true;
        builder.setAppHandler(cefAppHandler=new CefAppHandlerImpl());

        cefAppHandler.cefAppEvent.register(new ICefAppHandlerEvent() {
            @Override
            public EventResult onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
                // 使用 CefSchemeRegistrar 注册 scheme
                registrar.addCustomScheme(
                        "classpath",      // scheme 名称
                        true,            // isStandard
                        false,             // isLocal
                        false,            // isDisplayIsolated
                        true,            // isSecure
                        true,             // isCorsEnabled
                        true,            // isCspBypassing
                        true              // is_fetch_enabled
                );
                return EventResult.pass();
            }

            @Override
            public EventResult onContextInitialized() {
                getCefAppHandler().cefApp.registerSchemeHandlerFactory("classpath","",new ClasspathSchemeHandlerFactory());
                return EventResult.pass();
            }
        });

        CefApp build = builder.build();
        cefAppHandler.cefApp=build;
        return build;
    }
    public static int getDebugPort(){
        return CelInstaller.getDebugPort();
    }
    public static List<DevSearchItem> getDevSearchResults(){
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:%d/json".formatted(getDebugPort())))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            //noinspection unchecked
            return (List<DevSearchItem>) new Gson().fromJson(response.body(), TypeToken.getParameterized(List.class,DevSearchItem.class));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    };
}
