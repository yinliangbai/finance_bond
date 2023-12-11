package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondRate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-03-14
 */
@Repository
public interface BondRateDao extends BaseMapper<BondRate> {

    List<BondRate> selectBondRateList();

    List<BondRate> selectRecentlyBondRateList(@Param("bondIds") Collection<Integer> bondIds);
}
