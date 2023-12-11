package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondPriceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 可转债价格表 Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Mapper
@Repository
public interface BondPriceInfoDao extends BaseMapper<BondPriceInfo> {


    int batchInsert(List<BondPriceInfo> bondPrices);

    /**
     * 批量查询最近几天的价格，包括每天的最低最高价，，，
     *
     * @param period
     * @return
     */
    List<BondPriceInfo> selectBondPriceInfoPeriod(@Param("period") Integer period, @Param("bondCodeList") Collection<String> bondCodeList);

    BondPriceInfo selectLatestPrice(String bondCode);

    List<BondPriceInfo> selectSingletonPrice(Integer period, String bondCode);
}
