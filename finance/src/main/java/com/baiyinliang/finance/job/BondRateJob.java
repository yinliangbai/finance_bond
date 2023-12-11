package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.BondDurationPriceService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BondRateJob {

    @Autowired
    private BondJobService bondJobService;

    @Autowired
    private BondDurationPriceService bondDurationPriceService;

    @XxlJob("bondJobHandler")
    public void bondJobHandler() {
        log.info("跑批开始----------------------------------");
        bondJobService.runBondJobService();
        log.info("跑批结束----------------------------------");
    }


}
