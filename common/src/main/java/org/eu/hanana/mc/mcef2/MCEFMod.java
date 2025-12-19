package org.eu.hanana.mc.mcef2;

import com.mojang.logging.LogUtils;
import dev.architectury.event.Event;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientScreenInputEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import me.friwi.jcefmaven.CefBuildInfo;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserFactory;
import org.cef.browser.CefFrame;
import org.cef.callback.CefJSDialogCallback;
import org.cef.handler.CefJSDialogHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.misc.BoolRef;
import org.eu.hanana.mc.mcef2.cef.CefBrowserMC;
import org.eu.hanana.mc.mcef2.cef.CelInstaller;
import org.eu.hanana.mc.mcef2.mod.InstallerMsgOutput;
import org.eu.hanana.mc.mcef2.mod.cef.CefUtil;
import org.eu.hanana.mc.mcef2.mod.client.Mcef2KeyBindingsRegister;
import org.eu.hanana.mc.mcef2.mod.screen.TestBrowserScreen;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Random;

import static dev.architectury.utils.Env.CLIENT;

public final class MCEFMod {
    public static CefApp cefApp;
    public static CefClient cefClient;
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "mcef2";

    public static void init() {
        LOGGER.info("MCEF2 Init");
        var cefBuilder = CelInstaller.getBuilder();
        cefBuilder.getCefSettings().log_severity= CefSettings.LogSeverity.LOGSEVERITY_VERBOSE;
        cefBuilder.getCefSettings().user_agent_product="MCEF2";
        var installMsgOut = InstallerMsgOutput.getInstance();
        cefBuilder.setProgressHandler(installMsgOut);
        try {
            cefBuilder.install();
        } catch (IOException | UnsupportedPlatformException e) {
            installMsgOut.onError(e);
        }
        cefBuilder.getCefSettings().windowless_rendering_enabled=true;
        cefApp = null;
        try {
            cefApp = CefUtil.buildCefApp(cefBuilder);
        } catch (Exception e) {
            installMsgOut.onError(e);
        }
        cefClient = CefUtil.createClient(cefApp);

        //var browserMC = new CefBrowserMC(client,"https://baidu.com/",null,null,null,null);

        ClientGuiEvent.RENDER_POST.register((screen, guiGraphics, i, i1, deltaTracker) -> {
            if (screen instanceof TitleScreen){
                //Minecraft.getInstance().setScreen(new TestBrowserScreen());
            }
        });
        LOGGER.info("Env: {}",Platform.getEnvironment());
        if (Platform.getEnvironment()==CLIENT){

            Mcef2KeyBindingsRegister.reg();
            ClientLifecycleEvent.CLIENT_STOPPING.register(instance -> {
                cefApp.dispose();
            });
        }else {
            LifecycleEvent.SERVER_STOPPING.register(instance -> {
                cefApp.dispose();
            });
        }

    }
}
