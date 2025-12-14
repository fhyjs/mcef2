package org.eu.hanana.mc.mcef2.mod.fabric;

import me.friwi.jcefmaven.EnumProgress;
import org.eu.hanana.mc.mcef2.MCEFMod;
import org.eu.hanana.mc.mcef2.mod.InstallerMsgOutput;

import java.util.Objects;

public class InstallerMsgOutputImpl extends InstallerMsgOutput {
    @Override
    public void handleProgress(String state, float percent) {
        Objects.requireNonNull(state, "state cannot be null");
        if (percent != -1f && (percent < 0f || percent > 100f)) {
            throw new RuntimeException("percent has to be -1f or between 0f and 100f. Got " + percent + " instead");
        }
        MCEFMod.LOGGER.info("{} |> {}", state, percent == -1f ? "In progress..." : percent);
    }
    public void onError(Exception e)  {
        MCEFMod.LOGGER.error("Error Installing cef core",e);
        throw new RuntimeException(e);
    }

    public static InstallerMsgOutput getInstance() {
        return new InstallerMsgOutputImpl();
    }
}