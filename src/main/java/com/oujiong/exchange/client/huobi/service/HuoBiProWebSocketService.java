package com.oujiong.exchange.client.huobi.service;

import java.util.List;

/**
 * @Description: 其它服务拉取交易对消息 和调其它服务保存消息 相关接口
 *
 * @author xub
 * @date 2019/7/29 下午8:55
 */
public interface HuoBiProWebSocketService {

    /**
     * 获取订阅的消息进行消费
     *
     * @param msg 消息内容
     */
    void onReceive(String msg);

    /**
     * 获取交易所， 交易对缓存
     *
     * @return 返回 已缓存的交易对
     */
    List<String> getChannelCache();
}
