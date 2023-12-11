package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.BondBaseInfoService;
import com.baiyinliang.finance.service.BondPriceInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BondInfoAndPriceJob {
    @Autowired
    private BondBaseInfoService bondBaseInfoService;
    @Autowired
    private BondPriceInfoService bondPriceInfoService;



    @XxlJob("bondInfoAndPriceHandler")
    public void bondInfoAndPriceHandler() {
        bondBaseInfoService.saveBondBaseInfoList();
        log.info("新增bond基本信息更新成功");
        bondPriceInfoService.saveBondPriceInfoList();
        log.info("当天价格记录成功");
    }

}
