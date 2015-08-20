package org.btctrading.client;

import org.btctrading.client.impl.OKCoinClient;

public class ClientFactory {
	 public static TradeClient buildTraderAgent() {
	       return new OKCoinClient();
	  }
}
