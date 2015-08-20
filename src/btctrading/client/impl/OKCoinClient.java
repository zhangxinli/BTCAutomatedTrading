package org.btctrading.client.impl;

import org.btctrading.client.AbstractClient;
import org.btctrading.config.Gobal;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.huobi.HuobiExchange;
import com.xeiam.xchange.okcoin.OkCoinExchange;
public class OKCoinClient extends AbstractClient {


	private static final CurrencyPair CURRENCY_PAIR = CurrencyPair.BTC_CNY;

	public OKCoinClient() {
		super(CURRENCY_PAIR);
	}

	protected Exchange buildExchange() {
		String msg="SecretKey是否为空:"+Gobal.getSecretKey()+
					"APIKey是否为空:"+Gobal.getAPIKey();
		if (Gobal.getSecretKey() == null
				|| Gobal.getAPIKey() == null) {
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		ExchangeSpecification exSpec = new ExchangeSpecification(OkCoinExchange.class);
		exSpec.setSecretKey(Gobal.getSecretKey());
		exSpec.setApiKey(Gobal.getAPIKey());
		return ExchangeFactory.INSTANCE.createExchange(exSpec);
	}

}
