package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondInfo;
import com.baiyinliang.finance.entity.BondPrice;
import com.baiyinliang.finance.request.BondInfoListReq;
import com.baiyinliang.finance.request.BondInfoPageReq;
import com.baiyinliang.finance.vo.BondInfoVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public interface BondInfoService extends IService<BondInfo> {

    void processBond();

    void setBondPrice();

    List<BondInfoVO> getInterestMarginList();

    Page<BondInfoVO> getBondInfoVOPage(BondInfoPageReq req);

    BondPrice getBondPrice2(Integer bondId);

    List<BondInfoVO> getBondInfoList(BondInfoListReq req);

    BondInfoVO getBondInfo(Integer bondId);

}
