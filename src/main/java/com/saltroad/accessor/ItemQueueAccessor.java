package com.saltroad.accessor;

import com.saltroad.model.item.TradeMarketItem;

import java.util.List;
import java.util.Map;

public interface ItemQueueAccessor {
    List<TradeMarketItem> getQueuedMarketItems();
}