package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondPriceInfo;
import com.baiyinliang.finance.service.impl.BondPriceInfoServiceImpl;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 可转债价格表 服务类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
public interface BondPriceInfoService extends IService<BondPriceInfo> {

    void saveBondPriceInfoList();

    BondPriceInfoServiceImpl.BondPriceRange getBondPriceRangeById(String bondCode);
}
