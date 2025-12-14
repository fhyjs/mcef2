package org.eu.hanana.mc.mcef2.cef.event;

import dev.architectury.event.EventResult;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;

public interface ICefMessageRouterEvent {
    default EventResult onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {return EventResult.pass();}
    default EventResult onQueryCanceled(CefBrowser browser, CefFrame frame, long queryId){return EventResult.pass();}
}
