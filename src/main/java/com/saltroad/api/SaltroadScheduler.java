package com.saltroad.api;

import com.saltroad.SaltroadMod;
import com.saltroad.accessor.ItemQueueAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SaltroadScheduler {

    private static final SaltroadAPI API = new SaltroadAPI();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int SEND_DELAY_MINS = 5;

    public static void startScheduledTask() {
        scheduler.scheduleAtFixedRate(SaltroadScheduler::processMarketAndLootItems, 1, SEND_DELAY_MINS, TimeUnit.MINUTES);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            SaltroadScheduler.stopScheduledTask();
        });
    }

    public static void stopScheduledTask() {
        processMarketAndLootItems();
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(SEND_DELAY_MINS, TimeUnit.MINUTES)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private static void processMarketAndLootItems() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            ItemQueueAccessor accessor = (ItemQueueAccessor) minecraft.getConnection();

            if (!accessor.getQueuedMarketItems().isEmpty()) {
                API.sendTradeMarketResults(accessor.getQueuedMarketItems());
                accessor.getQueuedMarketItems().clear();
            }
        }
    }
}
