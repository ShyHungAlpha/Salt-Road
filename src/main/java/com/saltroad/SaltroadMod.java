package com.saltroad;

import com.sun.tools.javac.Main;
import com.wynntils.utils.mc.McUtils;
import com.saltroad.api.SaltroadScheduler;
import com.saltroad.config.ConfigManager;
import com.saltroad.config.ConfigScreen;
import com.saltroad.model.keymapping.StickyKeyMapping;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SaltroadMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("saltroad");
	public static final Optional<ModContainer> SALTROAD_INSTANCE = FabricLoader.getInstance().getModContainer("saltroad");
	public static String SALTROAD_VERSION;
	public static String SALTROAD_MOD_NAME;

	private static boolean IS_DEV = false;

	@Override
	public void onInitializeClient() {
		if (SALTROAD_INSTANCE.isEmpty()) {
			error("Could not find Salt Road in Fabric Loader!");
			return;
		}
		SALTROAD_VERSION = SALTROAD_INSTANCE.get().getMetadata().getVersion().getFriendlyString();
		SALTROAD_MOD_NAME = SALTROAD_INSTANCE.get().getMetadata().getName();

		SaltroadScheduler.startScheduledTask();

		ConfigManager.getInstance().loadConfig();
		registerKeyBinds();

		LOGGER.info("Initialized Salt Road with version {}", SALTROAD_VERSION);
	}

	private static void registerKeyBinds() {
		KeyMapping openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.saltroad.open_config",
				GLFW.GLFW_KEY_N,
				"category.saltroad.keybinding"
		));

		StickyKeyMapping priceTooltipKey = (StickyKeyMapping) KeyBindingHelper.registerKeyBinding(new StickyKeyMapping(
				"key.saltroad.toggle_tooltips",
				GLFW.GLFW_KEY_PERIOD,
				"category.saltroad.keybinding",
				() -> true
		));

		StickyKeyMapping boxedPriceTooltipKey = (StickyKeyMapping) KeyBindingHelper.registerKeyBinding(new StickyKeyMapping(
				"key.saltroad.toggle_boxed_item_tooltips",
				GLFW.GLFW_KEY_COMMA,
				"category.saltroad.keybinding",
				() -> true
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openConfigKey.consumeClick()) {
				Minecraft.getInstance().setScreen(ConfigScreen.createConfigScreen(Minecraft.getInstance().screen));
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(priceTooltipKey.hasStateChanged()) {
				ConfigManager config = ConfigManager.getInstance();
				config.setShowTooltips(!config.isShowTooltips());
				config.saveConfig();

				Component message;
				if(config.isShowTooltips()) {
					message = Component.literal("[Salt Road] Trade Market item tooltips enabled").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
				} else {
					message = Component.literal("[Salt Road] Trade Market item tooltips disabled").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
				}


				McUtils.sendMessageToClient(message);
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(boxedPriceTooltipKey.hasStateChanged()) {
				ConfigManager config = ConfigManager.getInstance();
				config.setShowBoxedItemTooltips(!config.isShowBoxedItemTooltips());
				config.saveConfig();

				Component message;
				if(config.isShowBoxedItemTooltips()) {
					message = Component.literal("[Salt Road] Trade Market boxed item tooltips enabled").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
				} else {
					message = Component.literal("[Salt Road] Trade Market boxed item tooltips disabled").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
				}

				McUtils.sendMessageToClient(message);
			}
		});
	}

	public static void debug(String msg) {
		LOGGER.debug(msg);
	}

	public static void info(String msg) {
		LOGGER.info(msg);
	}

	public static void warn(String msg) {
		LOGGER.warn(msg);
	}

	public static void warn(String msg, Throwable t) {
		LOGGER.warn(msg, t);
	}

	public static void error(String msg) {
		LOGGER.error(msg);
	}

	public static void error(String msg, Throwable t) {
		LOGGER.error(msg, t);
	}

	public static boolean isDev() {
		return IS_DEV;
	}
}