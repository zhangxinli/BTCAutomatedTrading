package org.btctrading.strategy.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.btctrading.client.TradeClient;
import org.btctrading.strategy.StrategyFactory;

import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;

public class EMAStrategy extends StrategyMonitor {
	private final List<LimitOrder> ordersPlaced;

	private final List<Ticker> tickers;
	private final int minTikcers;
	private final int maxTikcers;
	private Ticker preTicker;
	private int shortEMASizes;
	private BigDecimal shortEMA;
	private BigDecimal expsShortEMA;

	private final List<BigDecimal> shortEMAHistoric;

	private BigDecimal EMAExpLong;
	private BigDecimal EMALong;

	private BigDecimal lastASK;
	private BigDecimal lastBig;

	private boolean onlyMyWin;

	private int time;
	private int lastOpTime;
	private int limitOrderCount;
	private static final int MAXTICKERS = 100;
	private static final int MINTICKERS = 80;
	
	 private static final int MIN_TICKER_BETWEEN_ORDERS = 12;

	private static final int CHECK_IF_DEAD_ORDERS_FREQ = 300;

	private static final int MAX_MINUTES_ORDERS_TO = 15;
	
	private static final BigDecimal MIN_DIFFERENCE_SHORT_AND_LONG_EMA_TO_OP = BigDecimal.valueOf(0.8);

	private static final double MIN_AVAILABLE_BTC_TO_OP = 0.001;
    private static final double MIN_AVAILABLE_CNY_TO_OP = 0.1;
    
	public EMAStrategy(TradeClient tradeClient, StrategyFactory strategyFactory) {
		super(tradeClient, strategyFactory);
		this.tickers = new ArrayList<Ticker>();
		this.shortEMAHistoric = new ArrayList<BigDecimal>();
		this.ordersPlaced = new ArrayList<LimitOrder>();
		this.minTikcers = MAXTICKERS;
		this.maxTikcers = MAXTICKERS;
		this.shortEMASizes = MAXTICKERS + 1;
		this.onlyMyWin = false;
		this.time = 0;
		this.limitOrderCount = 0;
	}

	public EMAStrategy(TradeClient tradeClient,
			StrategyFactory strategyFactory, int min, int max, boolean onlyWin) {
		super(tradeClient, strategyFactory);
		this.tickers = new ArrayList<Ticker>();
		this.shortEMAHistoric = new ArrayList<BigDecimal>();
		this.ordersPlaced = new ArrayList<LimitOrder>();
		this.minTikcers = min;
		this.maxTikcers = max;
		this.shortEMASizes = minTikcers + 1;
		this.onlyMyWin = onlyWin;
		this.time = 0;
		this.limitOrderCount = 0;
	}

	@Override
	public void receiveTicker(Ticker ticker) {
		super.receiveTicker(ticker);
		checkIfDeadLimitOrders();
		lastOpTime++;
		if (preTicker == null) {
			initTickers(ticker);
		} else {
			addTicker(ticker);
			processTicker(ticker);
		}
		order(ticker) ;
	}

	private void order(Ticker ticker) {
		if (shortEMA == null || EMALong == null) {
			return;
		}
		if(shortEMA.subtract(EMALong).abs().compareTo(MIN_DIFFERENCE_SHORT_AND_LONG_EMA_TO_OP)==-1&&lastOpTime>MIN_TICKER_BETWEEN_ORDERS){
			lastOpTime=0 ;
			OrderType type = getOrderType(ticker) ;
			BigDecimal amount ;
			if(type==OrderType.BID&&(lastASK==null||!onlyMyWin||lastASK.compareTo(ticker.getBid())==1)&&tradeClient.getCurrencyBalance().compareTo(BigDecimal.valueOf(MIN_AVAILABLE_CNY_TO_OP))==1){
				BigDecimal price = ticker.getBid() ;
				amount= tradeClient.getCurrencyBalance().divide(price,40,BigDecimal.ROUND_DOWN) ;
				logger.info("在"+price+"买入"+amount);
				placeOrder(type, amount, price) ;
				lastBig=price ;
			}else if(type==OrderType.ASK&&(lastBig==null||!onlyMyWin||lastBig.compareTo(ticker.getAsk())==-1)&&tradeClient.getBitCoinBalance().compareTo(BigDecimal.valueOf(MIN_AVAILABLE_BTC_TO_OP))==1){
				BigDecimal price = ticker.getAsk() ;
				amount =tradeClient.getBitCoinBalance() ;
				logger.info("在"+price+"卖出"+amount);
				placeOrder(type, amount, price) ;
				lastASK =price ;
				
			}
		}
	}

	private OrderType getOrderType(Ticker ticker) {
		int high = 0;
		int low = 0;
		for (BigDecimal EMAShort : shortEMAHistoric) {
			if (shortEMA.compareTo(expsShortEMA) == 1) {
				low++;
			} else {
				high++;
			}
		}
		if (high > low) {
			return OrderType.ASK;
		} else if (low > high) {
			return OrderType.BID;
		}
		return null;
	}

	private void addTicker(Ticker ticker) {
		if (tickers.size() == maxTikcers) {
			tickers.remove(tickers.size() - 1);
		}
		tickers.add(0, ticker);
	}

	private void processTicker(Ticker ticker) {
		if (shortEMA == null) {
			shortEMA = ticker.getLast();
		} else {
			shortEMA = ticker
					.getLast()
					.multiply(expsShortEMA)
					.add(shortEMA.multiply(BigDecimal.valueOf(1).subtract(
							expsShortEMA)));
			if (shortEMAHistoric.size() == MAX_MINUTES_ORDERS_TO) {
				shortEMAHistoric.remove(shortEMAHistoric.size() - 1);
			}
			shortEMAHistoric.add(0, shortEMA);
		}
		EMALong = ticker
				.getLast()
				.multiply(EMAExpLong)
				.add(EMALong.multiply(BigDecimal.valueOf(1)
						.subtract(EMAExpLong)));
		preTicker = ticker;
	}

	private void initTickers(Ticker ticker) {
		preTicker = ticker;
		expsShortEMA = BigDecimal.valueOf((double) 2 / (shortEMASizes + 1));
		EMAExpLong = BigDecimal.valueOf((double) 2 / (maxTikcers + 1));
		EMALong = ticker.getLast();
		tickers.add(0, ticker);
	}

	private void checkIfDeadLimitOrders() {
		if (limitOrderCount < CHECK_IF_DEAD_ORDERS_FREQ) {
			limitOrderCount++;
			return;
		}
		limitOrderCount++;
		OpenOrders myOpenOrders = tradeClient.getOpenOrders();
		if (myOpenOrders == null || myOpenOrders.getOpenOrders() == null
				|| myOpenOrders.getOpenOrders().size() == 0) {
			return;
		}
		Date time = new Date();
		for (LimitOrder lo : myOpenOrders.getOpenOrders()) {
			int timePlaced = (int) (time.getTime() - lo.getTimestamp()
					.getTime());
			int min = timePlaced / 60 / 1000;
			if (min > MAX_MINUTES_ORDERS_TO) {
				boolean cancelled = tradeClient.cancelLimitOrder(lo);
				logger.info("limit 取消");
			} else {
				logger.debug("limit placed on time" + lo);
			}
			if (!ordersPlaced.contains(lo)) {
				ordersPlaced.add(lo);
			}
			for (int orderPlacedIndex = 0; orderPlacedIndex < ordersPlaced
					.size(); orderPlacedIndex++) {
				LimitOrder order = ordersPlaced.get(orderPlacedIndex);
				if (!myOpenOrders.getOpenOrders().contains(order)) {
					ordersPlaced.remove(order);
				}
			}
		}

	}
}
