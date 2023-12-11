package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondRatingCd;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 可转债评级表 服务类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
public interface BondRatingCdService extends IService<BondRatingCd> {

    void addBondRatingCdList();
}
