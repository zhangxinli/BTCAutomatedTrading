package org.btctrading.client;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.dto.trade.Wallet;

public abstract class AbstractClient implements TradeClient {
	protected static final Logger logger = Logger
			.getLogger(AbstractClient.class);
	private Exchange exchange;
	private CurrencyPair currencyPair;

	public AbstractClient(CurrencyPair currencyPair) {
		this.exchange = buildExchange();
		this.currencyPair = currencyPair;
	}
	protected abstract Exchange buildExchange();
	public Ticker getTicker() {
		try {
			return exchange.getPollingMarketDataService().getTicker(
					currencyPair);
		} catch (Exception e) {
			logger.warn("Getting ticker " + e.getMessage());
			return getTicker();
		}
	}

	public String placeOrder(OrderType orderType, BigDecimal amount,
			BigDecimal price) {
		try {
			if (exchange.getPollingAccountService().getAccountInfo()
					.getTradingFee() == BigDecimal.ZERO) {
				throw new RuntimeException("有交易费用。"
						+ exchange.getPollingAccountService().getAccountInfo()
								.getTradingFee());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("正在下单 " + orderType + ";数量为：" + amount + "，价格为：" + price);
		return attemptPlaceOrder(orderType, amount, price, 0);

	}

	public boolean cancelLimitOrder(LimitOrder limitOrder) {
		return attemptCancelLimit(limitOrder, 0);
	}
	public List<Wallet> getWallets() {
		try {
			return exchange.getPollingAccountService().getAccountInfo()
					.getWallets();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return getWallets();
		}
	}

	public BigDecimal getBitCoinBalance() {
		try {
			Wallet myWallet = null;
			for (Wallet wallet : exchange.getPollingAccountService()
					.getAccountInfo().getWallets()) {
				if (currencyPair.baseSymbol.equals(wallet.getCurrency())) {
					myWallet = wallet;
				}
			}
			if (myWallet != null)
				return myWallet.getBalance();
			throw new RuntimeException("找不到");
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return getBitCoinBalance();
		}
	}

	public BigDecimal getCurrencyBalance() {
		try {
			Wallet myWallet = null;
			for (Wallet wallet : this.exchange.getPollingAccountService()
					.getAccountInfo().getWallets()) {
				if (currencyPair.counterSymbol.equals(wallet.getCurrency())) {
					myWallet = wallet;
				}
			}
			if (myWallet != null) {
				return myWallet.getBalance();
			}
			throw new RuntimeException("找不到  currency");
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return getBitCoinBalance();
		}
	}

	public OpenOrders getOpenOrders() {
		try {
			return this.exchange.getPollingTradeService().getOpenOrders();
		} catch (Exception e) {
			logger.warn( e.getMessage());
			return getOpenOrders();
		}
	}

	public OrderBook getOrderBook() {
		try {
			return this.exchange.getPollingMarketDataService().getOrderBook(
					currencyPair);
		} catch (Exception e) {
			logger.warn( e.getMessage());
			return getOrderBook();
		}
	}

	public Trades getTradeHistory(int numberOfTrades) {
		try {
			return this.exchange.getPollingMarketDataService().getTrades(
					currencyPair, numberOfTrades);
		} catch (Exception e) {
			logger.warn("oops " + e.getMessage());
			return getTradeHistory(numberOfTrades);
		}
	}

	public Trades getTrades() {
		try {
			return exchange.getPollingTradeService().getTradeHistory();
		} catch (Exception e) {
			logger.warn( e.getMessage());
			return getTrades();
		}
	}

	public Exchange getExchange() {
		return exchange;
	}

	public CurrencyPair getCurrencyPair() {
		return currencyPair;
	}

	private String attemptPlaceOrder(OrderType orderType, BigDecimal amount,
			BigDecimal price, int attempt) {
		if (attempt >= 5) {
			return "FAILED";
		}
		try {
			return exchange.getPollingTradeService().placeLimitOrder(
					new LimitOrder(orderType, amount, currencyPair, "0",
							new Date(), price));
		} catch (Exception e) {
			logger.warn("第" + (attempt + 1) + "下单失败，再次尝试下单" + e.getMessage());
			return attemptPlaceOrder(orderType, amount, price, attempt + 1);
		}
	}

	private boolean attemptCancelLimit(LimitOrder limitOrder, int attempt) {
		if (attempt >= 5) {
			return false;
		}
		try {
			return exchange.getPollingTradeService().cancelOrder(
					limitOrder.getId());
		} catch (Exception e) {
			logger.warn("第" + (attempt + 1) + "取消订单失败，再次尝试" + e.getMessage());
			return attemptCancelLimit(limitOrder, attempt + 1);
		}
	}

}
