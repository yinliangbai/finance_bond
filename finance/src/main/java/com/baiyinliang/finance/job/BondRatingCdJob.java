package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.BondRatingCdService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BondRatingCdJob {
    @Autowired
    private BondRatingCdService bondRatingCdService;


    @XxlJob("addBondRatingCdList")
    public void addBondRatingCdList() {
        log.info("评级跑批开始");
        bondRatingCdService.addBondRatingCdList();
        log.info("评级跑批结束");
    }

}
