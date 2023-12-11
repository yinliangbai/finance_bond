package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.BondAmtInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BondAmtInfoJob {
    @Autowired
    private BondAmtInfoService bondAmtInfoService;



    @XxlJob("addCurrIssAmtList")
    public void addCurrIssAmtList() {
        log.info("剩余规模跑批开始");
        bondAmtInfoService.addCurrIssAmtList();
        log.info("剩余规模跑批结束");
    }

}
