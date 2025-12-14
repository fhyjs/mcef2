package org.eu.hanana.mc.mcef2.cef;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandler;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.eu.hanana.mc.mcef2.cef.event.ICefMessageRouterEvent;

public class CefMessageRouterHandlerImpl extends CefMessageRouterHandlerAdapter {
    private static CefMessageRouterHandlerImpl INSTANCE;

    public static CefMessageRouterHandlerImpl getINSTANCE() {
        return INSTANCE;
    }

    public final Event<ICefMessageRouterEvent> cefMessageRouterEvent  = EventFactory.createEventResult();
    public CefMessageRouterHandlerImpl(){
        INSTANCE=this;
    }
    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        Boolean value = cefMessageRouterEvent.invoker().onQuery(browser, frame, queryId, request, persistent, callback).value();
        if (value==null) value=false;
        return super.onQuery(browser, frame, queryId, request, persistent, callback)||value;
    }

    @Override
    public void onQueryCanceled(CefBrowser browser, CefFrame frame, long queryId) {
        super.onQueryCanceled(browser, frame, queryId);
        cefMessageRouterEvent.invoker().onQueryCanceled(browser, frame, queryId);
    }
}
