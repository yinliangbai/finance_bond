package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondPrice;
import com.baiyinliang.finance.service.impl.BondPriceServiceImpl;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author root
 * @since 2023-03-06
 */
public interface BondPriceService extends IService<BondPrice> {

    List<BondPriceServiceImpl.SingleBondPrice> getBondDiffPrice();

    List<BondPrice> getBondPriceList();

    BondPriceServiceImpl.BondPriceRange getBondPriceRangeById(Integer bondId);
}
