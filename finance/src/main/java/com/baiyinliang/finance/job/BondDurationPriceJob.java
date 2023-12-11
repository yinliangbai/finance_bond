package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.BondDurationPriceService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BondDurationPriceJob {

    @Autowired
    private BondDurationPriceService bondDurationPriceService;


    @XxlJob("bondDurationPriceJobHandler")
    public void bondDurationPriceJobHandler() {
        log.info("最高最低价跑批开始----------------------------------");
        bondDurationPriceService.setBondDurationPrice();
        log.info("最高最低价跑批结束----------------------------------");
    }

    @XxlJob("bondDurationPriceWechatMsgHandler")
    public void bondDurationPriceWechatMsgHandler(){
        log.info("最高最低价微信消息跑批开始----------------------------------");
        bondDurationPriceService.comparePriceInTime();
        log.info("最高最低价微信消息跑批结束----------------------------------");
    }
}
