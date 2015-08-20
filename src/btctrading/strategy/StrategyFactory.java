package org.btctrading.strategy;

import org.btctrading.client.TradeClient;
import org.btctrading.strategy.impl.EMAStrategy;
import org.btctrading.strategy.impl.TurtleStrategy;

public class StrategyFactory {
	    private final TradeClient tradeClient;

	    private Strategy strategy;

	    public StrategyFactory(TradeClient tradeClient) {
	        this.tradeClient = tradeClient;
	        initDefaultTradingStrategy();
	    }


	    public Strategy buildTurtleStrategy(TradeClient tradeClient) {
	        return  buildTurtleStrategy(tradeClient, 4, 2) ;
	    }
	    public Strategy buildTurtleStrategy(TradeClient tradeClient,int newOpAmount ,int newTurtleSpeed) {
	        return new  TurtleStrategy(this, tradeClient, 4, 2) ;
	    } 

	    public  Strategy buildEMAStrategy() {
	        return new EMAStrategy(tradeClient, this) ;
	    }

	    public void switchStrategy(Strategy tradingStrategy) {
	        this.strategy = tradingStrategy;
	    }

	    public Strategy getTradingStrategy() {
	        return this.strategy;
	    }

	    private void initDefaultTradingStrategy() {
	        this.strategy = buildEMAStrategy();
	    }
}
