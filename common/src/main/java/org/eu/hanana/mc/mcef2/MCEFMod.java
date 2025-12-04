package org.eu.hanana.mc.mcef2;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class MCEFMod {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "mcef2";

    public static void init() {
        LOGGER.info("MCEF2 Init");
    }
}
