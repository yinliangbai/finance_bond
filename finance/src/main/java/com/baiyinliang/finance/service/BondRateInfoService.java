package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondRateInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.Map;

/**
 * <p>
 * 可转债付息表 服务类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */

public interface BondRateInfoService extends IService<BondRateInfo> {


    void addBondRateInfoList();

    Map<String, BondRateInfo> getBondRateInfoList(Collection<String> bondCodeList);
}
