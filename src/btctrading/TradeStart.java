package org.btctrading;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.btctrading.client.ClientFactory;
import org.btctrading.client.TradeClient;
import org.btctrading.strategy.StrategyFactory;

import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;

public class TradeStart {
	private static final Logger logger = Logger.getLogger(TradeStart.class);
	public static final long SLEEP_TIME = 10000;
	private static final String LOG_PATH="./resources/log4j.properties" ;
	public static void main(String[] args) {
		PropertyConfigurator.configure(LOG_PATH);
		TradeClient client = ClientFactory.buildTraderAgent() ;
		StrategyFactory strategy = new StrategyFactory(client) ;
		Ticker preTicker =client.getTicker() ;
		while(true){
			Ticker ticker =client.getTicker() ;
			if(!tickerIsSame(ticker,preTicker)){
				strategy.getTradingStrategy().receiveTicker(ticker);
				preTicker =ticker ;
			}else{
				logger.info("和上次ticker一样");
			}
			sleep() ;
		}

	}

	private static boolean tickerIsSame(Ticker ticker, Ticker preTicker) {
		return ticker.getBid().compareTo(preTicker.getBid())==0 && ticker.getAsk().compareTo(preTicker.getAsk())==0;
	}
	private static void sleep(){
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (Exception e) {
				logger.info("休息"+e.getMessage());
		}
	}

}
