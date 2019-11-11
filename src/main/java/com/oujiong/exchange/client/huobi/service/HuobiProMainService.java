package com.oujiong.exchange.client.huobi.service;


/**
 * @Description: 订阅接口
 *
 * @author xub
 * @date 2019/7/28 下午7:06
 */
public interface HuobiProMainService {

    /**
     * 首次订阅数据
     */
    void start();

    /**
     * 刷新数据
     */
    void refreshSubData();
}
