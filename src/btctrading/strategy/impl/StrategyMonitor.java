package org.btctrading.strategy.impl;

import java.math.BigDecimal;

import org.btctrading.client.TradeClient;
import org.btctrading.strategy.AbstractStrategy;
import org.btctrading.strategy.StrategyFactory;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Ticker;

public class StrategyMonitor extends AbstractStrategy {
	private final StrategyFactory strategyFactory;

	private final int MARKET_TREND_OP = 4;

	private int marketTrend;
	private BigDecimal myMoney;
	private BigDecimal lastUsedBid = BigDecimal.valueOf(0);

	public StrategyMonitor(TradeClient tradeClient,
			StrategyFactory strategyFactory) {
		super(tradeClient);
		this.strategyFactory = strategyFactory;
		marketTrend = 0;

	}

	/**
	 * 一种交易策略连续4次钱数少于总钱数，切换交易策略
	 */
	public void receiveTicker(Ticker ticker) {
		if (!(this.strategyFactory.getTradingStrategy() instanceof TurtleStrategy)) {
			return;
		}
		BigDecimal money = tradeClient.getBitCoinBalance()
				.multiply(lastUsedBid).add(tradeClient.getCurrencyBalance());
		if (myMoney == null) {
			myMoney = money;
		} else if (myMoney.compareTo(money) != 0) {
			if (myMoney.compareTo(money) == 1) {
				marketTrend--;
			} else {
				marketTrend++;
			}
			myMoney = money;
			if (Math.abs(marketTrend) == MARKET_TREND_OP) {
				changeStrategy();
				marketTrend = 0;
			}
		}

	}

	@Override
	protected String placeOrder(Order.OrderType orderType, BigDecimal amount,
			BigDecimal price) {
		// lastUsedBid = ticker.getBid();
		String orderResult = super.placeOrder(orderType, amount, price);
		return orderResult;
	}

	private void changeStrategy() {
		if (strategyFactory.getTradingStrategy() instanceof TurtleStrategy) {
			TurtleStrategy strategy = (TurtleStrategy) strategyFactory
					.getTradingStrategy();
			int newTurtleSpeed;
			int newOpAmount;
			if (marketTrend > 0) { // doing bad
				newTurtleSpeed = strategy.getTurtleSpeed() - 1;
				newOpAmount = strategy.getOpAmount() + 1;
			} else { // doing good
				newTurtleSpeed = strategy.getTurtleSpeed() + 1;
				newOpAmount = strategy.getOpAmount() - 1;
			}
			if (newOpAmount > 4) {
				newOpAmount = 4;
			}
			if (newOpAmount <= 0) {
				newOpAmount = 1;
			}
			if (newTurtleSpeed <= 1) {
				newTurtleSpeed = 2;
			}
			StrategyFactory sf = new StrategyFactory(tradeClient);
			sf.switchStrategy(sf.buildTurtleStrategy(tradeClient, newOpAmount,
					newTurtleSpeed));
		}
	}

}
