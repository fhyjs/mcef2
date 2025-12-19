package org.eu.hanana.mc.mcef2.cef.event;

import dev.architectury.event.EventResult;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;

public interface ICefLoadEvent {
    EventResult onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward);
    EventResult onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType);
    EventResult onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode);
    EventResult onLoadError(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode, String errorText, String failedUrl);
}
