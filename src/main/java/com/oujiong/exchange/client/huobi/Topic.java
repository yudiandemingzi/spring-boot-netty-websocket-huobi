package com.oujiong.exchange.client.huobi;

/**
 * @Description: 订阅主题, 火币网
 *   一般需要订阅以下五种数据类型
 *
 * @author xub
 * @date 2019/7/29 下午8:53
 */
public final class Topic {

    /**
     * K线订阅
     */
    public static String KLINE_SUB = "market.%s.kline.%s";

    /**
     * 交易深度
     */
    public static String MARKET_DEPTH_SUB = "market.%s.depth.step0";

    /**
     * 交易行情
     */
    public static String MARKET_TRADE_SUB = "market.%s.trade.detail";

    /**
     * 行情
     */
    public static String MARKET_DETAIL_SUB = "market.%s.detail";

    /**
     * K线交易周期
     */
    public static String[] PERIOD = {"1min" /*, "5min", "15min", "30min", "60min", "1day", "1mon", "1week", "1year"*/ };
}
