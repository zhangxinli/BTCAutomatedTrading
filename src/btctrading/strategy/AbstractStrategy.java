package org.btctrading.strategy;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.btctrading.client.TradeClient;

import com.xeiam.xchange.dto.Order.OrderType;

public abstract class AbstractStrategy implements Strategy {
	protected static final Logger logger = Logger
			.getLogger(AbstractStrategy.class);

	public final TradeClient tradeClient;

	public AbstractStrategy(TradeClient tradeClient) {
		this.tradeClient = tradeClient;
	}

	protected String placeOrder(OrderType orderType, BigDecimal amount,
			BigDecimal price) {
		return tradeClient.placeOrder(orderType, amount, price);
	}
}
