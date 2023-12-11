package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondRate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author root
 * @since 2023-03-14
 */
public interface BondRateService extends IService<BondRate> {

    void setBondRate();

    List<BondRate> selectBondRateList();

    Map<Date, BigDecimal> getBondRate2(Integer bondId);
}
