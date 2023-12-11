package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondPrice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-03-06
 */
@Repository
public interface BondPriceDao extends BaseMapper<BondPrice> {


    /**
     * 批量查询最近几天的价格，包括每天的最低最高价，，，
     *
     * @param period
     * @return
     */
    List<BondPrice> selectBondPricePeriod(@Param("period") Integer period, @Param("bondIds") Collection<Integer> bondIds);

    List<BondPrice> aaa();
}
