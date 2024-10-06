package com.saltroad.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.wynntils.core.components.Models;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.McUtils;
import com.saltroad.SaltroadMod;
import com.saltroad.model.item.TradeMarketItem;
import com.saltroad.model.item.TradeMarketItemPriceInfo;
import com.saltroad.util.HttpUtil;
import net.minecraft.world.item.ItemStack;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SaltroadAPI {
    private static final ObjectMapper objectMapper = createObjectMapper();

    public void sendTradeMarketResults(List<TradeMarketItem> marketItems) {
        if (marketItems.isEmpty()) return;

        URI endpointURI;

        endpointURI = URI.create("https://salt-road.xyz/api/trademarket/items");

        HttpUtil.sendHttpPostRequest(endpointURI, serializeData(marketItems));
    }

    public TradeMarketItemPriceInfo fetchItemPrices(ItemStack item) {
        return Models.Item.asWynnItem(item, GearItem.class)
                .map(gearItem -> fetchItemPrices(gearItem.getName()))
                .orElse(null);
    }

    public TradeMarketItemPriceInfo fetchItemPrices(String itemName) {
        String playerName = McUtils.playerName();
        try {
            final String encodedItemName = URLEncoder.encode(itemName, StandardCharsets.UTF_8).replace("+", "%20");

            URI endpointURI;

            endpointURI = URI.create("https://salt-road.xyz/api/trademarket/item/"+encodedItemName+"/price");

            HttpResponse<String> response = HttpUtil.sendHttpGetRequest(endpointURI);

            if (response.statusCode() == 200) {
                return parsePriceInfoResponse(response.body());
            } else if (response.statusCode() == 404) {
                return null;
            } else {
                SaltroadMod.error("Failed to fetch item price from API: " + response.body());
                return null;
            }
        } catch (Exception e) {
            SaltroadMod.error("Failed to initiate item price fetch {}", e);
            return null;
        }
    }

    private String serializeData(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            SaltroadMod.LOGGER.error("Failed to serialize data", e);
            return "[]";
        }
    }

    private TradeMarketItemPriceInfo parsePriceInfoResponse(String responseBody) {
        try {
            List<TradeMarketItemPriceInfo> priceInfoList = objectMapper.readValue(responseBody, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            return priceInfoList.isEmpty() ? null : priceInfoList.getFirst();
        } catch (JsonProcessingException e) {
            SaltroadMod.error("Failed to parse item price response {}", e);
            return null;
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        return mapper;
    }
}