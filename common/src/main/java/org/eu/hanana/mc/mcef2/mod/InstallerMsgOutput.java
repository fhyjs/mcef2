package org.eu.hanana.mc.mcef2.mod;

import dev.architectury.injectables.annotations.ExpectPlatform;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.IProgressHandler;
import org.apache.commons.lang3.NotImplementedException;

public abstract class InstallerMsgOutput implements IProgressHandler {
    @ExpectPlatform
    public static InstallerMsgOutput getInstance(){
        throw new NotImplementedException("Not Impl!");
    }
    @Override
    public final void handleProgress(EnumProgress state, float percent) {
        handleProgress(state.name(),percent);
    };
    public abstract void handleProgress(String state, float percent);
    public abstract void onError(Exception e);
}
