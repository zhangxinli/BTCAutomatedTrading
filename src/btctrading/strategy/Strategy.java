package org.btctrading.strategy;

import com.xeiam.xchange.dto.marketdata.Ticker;

public interface Strategy {
	/**
	 * 交易策略
	 * @param ticker
	 */
	void receiveTicker(Ticker ticker);
}
