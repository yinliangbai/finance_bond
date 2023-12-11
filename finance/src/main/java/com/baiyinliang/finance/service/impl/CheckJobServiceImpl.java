package com.baiyinliang.finance.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baiyinliang.finance.entity.BondPriceInfo;
import com.baiyinliang.finance.mapper.BondBaseInfoDao;
import com.baiyinliang.finance.mapper.BondPriceInfoDao;
import com.baiyinliang.finance.service.CheckJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CheckJobServiceImpl implements CheckJobService {

    @Autowired
    private BondBaseInfoDao baseInfoDao;
    @Autowired
    private BondPriceInfoDao bondPriceInfoDao;

    @Autowired
    private CommonServiceImpl commonServiceImpl;


    @Override
    public void checkJob() {

    }

    // 检查价格是否跑批
    private void checkPrice(){
        // 公用方法：查询bond表上市数据
        List<BondBaseInfo> listBondBaseInfo = commonServiceImpl.getListBondBaseInfo();
        if(CollectionUtils.isEmpty(listBondBaseInfo)){
            log.info("bond数据为空");
            return;
        }
        Set<String> allBondCodeSet = listBondBaseInfo.stream().map(BondBaseInfo::getBondCode).collect(Collectors.toSet());
        // 价格表当天得有同样多的记录，如果没有：是多还是少，需要对比出来，发通知
        LambdaQueryWrapper<BondPriceInfo> priceInfoQueryWrapper = new LambdaQueryWrapper<>();
//        SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
        priceInfoQueryWrapper.select(BondPriceInfo::getBondCode).eq(BondPriceInfo::getDate,DateUtil.format(new Date(), "yyyy-MM-dd"));
        List<BondPriceInfo> bondPriceInfos = bondPriceInfoDao.selectList(priceInfoQueryWrapper);
        if(CollectionUtils.isEmpty(bondPriceInfos)){
            log.info("价格表为空");// 不会存在这种情况
        }

        Set<String> priceBondCodeSet = bondPriceInfos.stream().map(BondPriceInfo::getBondCode).collect(Collectors.toSet());
        for (String bondCode : allBondCodeSet) {
            if(!priceBondCodeSet.contains(bondCode)){
                log.error("");
            }
        }
        // 付息表只要检查 bond表的数据都有就行

        //
//        LambdaQueryWrapper<>
    }

}
