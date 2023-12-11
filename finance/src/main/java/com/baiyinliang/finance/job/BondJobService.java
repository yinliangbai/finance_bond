package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.BondInfoService;
import com.baiyinliang.finance.service.BondPriceService;
import com.baiyinliang.finance.service.BondRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class BondJobService {
    @Autowired
    private BondInfoService bondInfoService;
    @Autowired
    private BondRateService bondRateService;
    @Autowired
    private BondPriceService bondPriceService;

    @Transactional(rollbackFor = Exception.class)
    public void runBondJobService(){
        bondInfoService.processBond();
        log.info("可转债基本信息更新成功");
        bondRateService.setBondRate();
        log.info("记录新债利息成功");
        bondInfoService.setBondPrice();
        log.info("可转债当天价格入库成功");
    }

}
