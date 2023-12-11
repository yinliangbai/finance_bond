package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.BondRateInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BondRateInfoJob {
    @Autowired
    private BondRateInfoService bondRateInfoService;



    @XxlJob("addBondRateInfoList")
    public void addBondRateInfoList() {
        log.info("付息表跑批开始");
        bondRateInfoService.addBondRateInfoList();
        log.info("付息表跑批结束");
    }

}
