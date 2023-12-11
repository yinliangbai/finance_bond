package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondDurationPrice;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author root
 * @since 2023-11-09
 */
public interface BondDurationPriceService extends IService<BondDurationPrice> {

    void setBondDurationPrice();

    void comparePriceInTime();
}
