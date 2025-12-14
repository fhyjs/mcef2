package org.eu.hanana.mc.mcef2.cef;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.events.common.EntityEvent;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefSchemeRegistrar;
import org.cef.handler.CefAppHandler;
import org.eu.hanana.mc.mcef2.cef.event.ICefAppHandlerEvent;

public class CefAppHandlerImpl extends MavenCefAppHandlerAdapter {
    public final Event<ICefAppHandlerEvent> cefAppEvent = EventFactory.createEventResult();
    public CefApp cefApp;
    @Override
    public boolean onAlreadyRunningAppRelaunch(CefCommandLine command_line, String current_directory) {
        return super.onAlreadyRunningAppRelaunch(command_line, current_directory)||cefAppEvent.invoker().onAlreadyRunningAppRelaunch(command_line, current_directory).value();
    }

    @Override
    public boolean onBeforeTerminate() {
        return super.onBeforeTerminate()||cefAppEvent.invoker().onBeforeTerminate().value();
    }

    @Override
    public void onContextInitialized() {
        super.onContextInitialized();
        cefAppEvent.invoker().onContextInitialized();
    }

    @Override
    public void onRegisterCustomSchemes(CefSchemeRegistrar registrar) {
        super.onRegisterCustomSchemes(registrar);
        cefAppEvent.invoker().onRegisterCustomSchemes(registrar);
    }

    @Override
    public void onScheduleMessagePumpWork(long delay_ms) {
        super.onScheduleMessagePumpWork(delay_ms);
        cefAppEvent.invoker().onScheduleMessagePumpWork(delay_ms);
    }
    @Override
    public void stateHasChanged(CefApp.CefAppState state) {
        super.stateHasChanged(state);
        cefAppEvent.invoker().stateHasChanged(state);
    }
}
