package com.baiyinliang.finance.service.impl;

import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baiyinliang.finance.enums.BusinessEnums;
import com.baiyinliang.finance.mapper.BondBaseInfoDao;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公共实现类
 */
@Service
@Slf4j
public class CommonServiceImpl {

    @Autowired
    private BondBaseInfoDao baseInfoDao;

    // 所有上市状态的bond
    public List<BondBaseInfo> getListBondBaseInfo() {
        LambdaQueryWrapper<BondBaseInfo> bondBaseInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondBaseInfoQueryWrapper.select(BondBaseInfo::getBondCode, BondBaseInfo::getBondNm, BondBaseInfo::getPrice,
                BondBaseInfo::getRedeemPrice, BondBaseInfo::getMaturityDt, BondBaseInfo::getOrigIssAmt, BondBaseInfo::getListDt).eq(BondBaseInfo::getFlag, BusinessEnums.BondCodeFlag.上市.getFlag());
        List<BondBaseInfo> bondBaseInfoList = baseInfoDao.selectList(bondBaseInfoQueryWrapper);

        return bondBaseInfoList;
    }
}
