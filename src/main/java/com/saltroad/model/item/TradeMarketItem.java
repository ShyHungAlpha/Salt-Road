package com.saltroad.model.item;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.trademarket.type.TradeMarketPriceInfo;
import com.wynntils.utils.mc.McUtils;
import com.saltroad.SaltroadMod;
import com.saltroad.util.TradeMarketPriceParser;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TradeMarketItem {
    private SimplifiedGearItem item;
    private int listingPrice;
    private int amount;
    private String playerName;
    private String modVersion;

    public TradeMarketItem(GearItem item, int listingPrice, int amount) {
        this.item = new SimplifiedGearItem(item);
        this.listingPrice = listingPrice;
        this.amount = amount;
        this.playerName = McUtils.playerName();
        this.modVersion = SaltroadMod.SALTROAD_VERSION;
    }

    public static TradeMarketItem createTradeMarketItem(ItemStack item) {
        Optional<GearItem> gearItemOptional = Models.Item.asWynnItem(item, GearItem.class);
        if(gearItemOptional.isPresent()) {
            GearItem gearItem = gearItemOptional.get();
            TradeMarketPriceInfo priceInfo = TradeMarketPriceParser.calculateItemPriceInfo(item);

            if (priceInfo != TradeMarketPriceInfo.EMPTY) {
                return new TradeMarketItem(gearItem, priceInfo.price(), priceInfo.amount());
            }
        }

        return null;
    }

    public SimplifiedGearItem getItem() {
        return item;
    }

    public int getListingPrice() {
        return listingPrice;
    }

    public int getAmount() {
        return amount;
    }

    public String getPlayerName() { return playerName; }

    public String getModVersion() { return modVersion; }
}
