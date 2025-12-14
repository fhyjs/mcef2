package org.eu.hanana.mc.mcef2.cef.event;

import dev.architectury.event.EventResult;
import org.cef.CefApp;
import org.cef.callback.CefCommandLine;
import org.cef.callback.CefSchemeRegistrar;

public interface ICefAppHandlerEvent {
    default EventResult onBeforeTerminate(){return EventResult.pass();}
    default EventResult stateHasChanged(CefApp.CefAppState state){return EventResult.pass();}
    default EventResult  onRegisterCustomSchemes(CefSchemeRegistrar registrar){return EventResult.pass();}
    default EventResult onContextInitialized(){return EventResult.pass();}
    default EventResult onScheduleMessagePumpWork(long delay_ms){return EventResult.pass();}
    default EventResult onAlreadyRunningAppRelaunch(CefCommandLine command_line, String current_directory){return EventResult.pass();}
}
