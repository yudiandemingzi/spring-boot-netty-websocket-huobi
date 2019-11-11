package com.oujiong.exchange.client.huobi.service.impl;

import com.google.common.collect.Lists;
import com.oujiong.exchange.client.huobi.service.HuoBiProWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author xub
 * @Description: 其它服务拉取交易对消息 和调其它服务保存消息
 * @date 2019/7/30 下午6:58
 */
@Service
@Slf4j
public class HuoBiProWebSocketServiceImpl implements HuoBiProWebSocketService {

    @Override
    public void onReceive(String msg) {
        // 直接发送消息给中转服务， 中转服务来处理信息
        if (StringUtils.isBlank(msg)) {
            log.error("====onReceive-huobi==msg is null");
            return;
        }
        log.info("火币网数据:{}", msg);
    }

    @Override
    public synchronized List<String> getChannelCache() {
        // 假设这里是从远处拉取数据
        List<String> list = Lists.newArrayList("btcusdt");
        return list;
    }

}
