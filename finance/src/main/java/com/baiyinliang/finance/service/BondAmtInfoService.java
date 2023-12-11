package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondAmtInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 可转债剩余规模表 服务类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
public interface BondAmtInfoService extends IService<BondAmtInfo> {

    void addCurrIssAmtList();

}
