package org.btctrading.strategy.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.btctrading.client.TradeClient;
import org.btctrading.strategy.StrategyFactory;

import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;

public class TurtleStrategy extends StrategyMonitor {
	protected static final Logger logger = Logger.getLogger(TurtleStrategy.class) ;
	
	private static final int CHECK_DEADORDERS_FREQ = 10;
	private static final int MAC_MINUTES_TOO_PROCESSED =30 ;

	private final int turtleSpeed;
	private final int opAmount;
	private final List<Ticker> historicData;
	private int limitOrderCount;

	public TurtleStrategy(StrategyFactory strategyFactory,
			TradeClient tradeClient, int turtleSpeed, int opAmount) {
		super(tradeClient, strategyFactory);
		this.turtleSpeed = turtleSpeed;
		this.opAmount = opAmount;
		this.historicData = new ArrayList<Ticker>();
		this.limitOrderCount = 0;
	}

	@Override
	public void receiveTicker(Ticker ticker) {
		super.receiveTicker(ticker);
		checkIfDeadLimitOrders(); 
		if (historicData.size()==turtleSpeed) {
			checkIfProfitByASK(ticker);
			checkIfProtitByBit(ticker);
		}
		addHistoric(ticker);

	}
	private void addHistoric(Ticker ticker){
		if(historicData.size()==turtleSpeed){
			historicData.remove(historicData.size()-1) ;
		}
		historicData.add(0, ticker);
	}
	private void checkIfProtitByBit(Ticker ticker){
		BigDecimal mCurrency = tradeClient.getCurrencyBalance() ;
		if(mCurrency.compareTo(BigDecimal.ZERO)==1&&shouldBit(ticker)){
			BigDecimal amountToBuy = mCurrency.divide(BigDecimal.valueOf(opAmount)) ;
			placeOrder(OrderType.BID, amountToBuy, ticker.getBid()) ;
			historicData.clear();
		}
	}
	private boolean shouldBit(Ticker ticker) {
		if(historicData.isEmpty()) return false ;
		for(Ticker historicTikcer:historicData){
			BigDecimal previuseBid= historicTikcer.getBid() ;
			if(previuseBid.doubleValue()>=ticker.getBid().doubleValue()){
				logger.debug("无法找到订单+"+previuseBid);
				return false ;
			}
		}
		String oldD="" ;
		for(Ticker oldData:historicData){
			oldD =oldD.concat(oldData.getBid()+"") ;
		}
		return true ;
	}

	private void checkIfProfitByASK(Ticker ticker){
		BigDecimal mBitCoins = tradeClient.getBitCoinBalance() ;
		if(mBitCoins.compareTo(BigDecimal.ZERO)==1&&shouldAsk(ticker)){
			BigDecimal amountToSell=mBitCoins.divide(BigDecimal.valueOf(opAmount)) ;
			placeOrder(OrderType.ASK, amountToSell, ticker.getAsk()) ;
			historicData.clear();
		}
	}
	private boolean shouldAsk(Ticker ticker){
		if(historicData.isEmpty()) return false ;
		for(Ticker historicTicker:historicData){
			BigDecimal previousValue= historicTicker.getAsk() ;
			if(ticker.getAsk().doubleValue()>previousValue.doubleValue()){
				logger.debug("没");
				return false ;
			}
		}
		String oldD="" ;
		for(Ticker oldData:historicData){
			oldD=oldD.concat(oldData.getAsk()+",") ;
		}
		logger.info(""+ticker.getAsk()+"高于"+oldD);
		return true ;
		
	}
	private void checkIfDeadLimitOrders() {
		if (limitOrderCount < CHECK_DEADORDERS_FREQ) {
			limitOrderCount++;
			return;
		}
		limitOrderCount = 0;
		OpenOrders openOrders = tradeClient.getOpenOrders();
		if (openOrders == null || openOrders.getOpenOrders() == null
				|| openOrders.getOpenOrders().size() == 0)
			return;
		Date time = new Date() ;
		for(LimitOrder limitOrder:openOrders.getOpenOrders()){
			int timeSincePlaced =(int)(time.getTime()-limitOrder.getTimestamp().getTime());
			int minutesSincePlacedLime= timeSincePlaced/60/1000 ;
			if(minutesSincePlacedLime>MAC_MINUTES_TOO_PROCESSED){
				tradeClient.cancelLimitOrder(limitOrder) ;
				logger.info("LImit placed "+minutesSincePlacedLime+"mins ago,取消") ;
			}else{
				logger.info("Limit placed");
			}
		}

	}

	public int getTurtleSpeed() {
		return turtleSpeed;
	}

	public int getOpAmount() {
		return opAmount;
	}
	
}
