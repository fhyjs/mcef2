package org.eu.hanana.mc.mcef2.mod.neoforge;

import me.friwi.jcefmaven.EnumProgress;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import org.apache.commons.lang3.NotImplementedException;
import org.eu.hanana.mc.mcef2.MCEFMod;
import org.eu.hanana.mc.mcef2.mod.InstallerMsgOutput;

import java.util.Objects;

public class InstallerMsgOutputImpl extends InstallerMsgOutput {
    private ProgressMeter lastBar=null;
    public static InstallerMsgOutput getInstance(){
        return new InstallerMsgOutputImpl();
    }
    @Override
    public void handleProgress(String state, float percent) {
        Objects.requireNonNull(state, "state cannot be null");
        if (percent != -1f && (percent < 0f || percent > 100f)) {
            throw new RuntimeException("percent has to be -1f or between 0f and 100f. Got " + percent + " instead");
        }
        MCEFMod.LOGGER.info("{} |> {}", state, percent == -1f ? "In progress..." : percent);
        if (lastBar!=null){
            if (percent>1){
                lastBar.setAbsolute((int) percent);
            }
        }
        if (percent==-1){
            if (lastBar!=null){
                StartupNotificationManager.popBar(lastBar);
                lastBar=null;
            }
            if (!state.equals(EnumProgress.INITIALIZED.name())){
                lastBar = StartupNotificationManager.prependProgressBar("MCEF: "+state, 100);
            }
            StartupNotificationManager.addModMessage(state);
        }else if (percent>=100f){
            if (lastBar!=null){
                StartupNotificationManager.popBar(lastBar);
                lastBar=null;
            }
        }
    }
    public void onError(Exception e)  {
        MCEFMod.LOGGER.error("Error Installing cef core",e);
        throw new RuntimeException(e);
    }
}