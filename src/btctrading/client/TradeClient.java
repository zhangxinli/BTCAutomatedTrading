package org.btctrading.client;

import java.math.BigDecimal;
import java.util.List;

import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.dto.trade.Wallet;

public interface TradeClient {
	/**
	 * 得到最近交易
	 * 
	 * @return
	 */
	Ticker getTicker() ;
	/**
	 * 尝试下单
	 * 
	 * @param orderType
	 * @param amount
	 * @param price
	 * @return
	 */
	String placeOrder(OrderType orderType, BigDecimal amount,
			BigDecimal price) ;
	/**
	 * 取消订单
	 * 
	 * @param limitOrder
	 * @return
	 */
	boolean cancelLimitOrder(LimitOrder limitOrder);

	/**
	 * 得到钱包
	 * 
	 * @return
	 */
	List<Wallet> getWallets();
	/**
	 * 得到钱包的资产
	 * 
	 * @return
	 */
	BigDecimal getBitCoinBalance() ;
	/**
	 * 得到当前的账户
	 * 
	 * @return
	 */
	 BigDecimal getCurrencyBalance();
	 OpenOrders getOpenOrders();
	 OrderBook getOrderBook() ;
	 Trades getTradeHistory(int numberOfTrades);
	 Trades getTrades();
}
