package com.divinedoll.edgerunners;

import com.divinedoll.edgerunners.effect.ModEffects;
import com.divinedoll.edgerunners.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgerunnersMod implements ModInitializer {
    public static final String MOD_ID = "edgerunners";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEffects.register();
        ModItems.register();
        LOGGER.info("Edgerunners cargado.");
    }
}
