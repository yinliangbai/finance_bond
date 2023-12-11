package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondDurationPrice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-11-09
 */
@Mapper
@Repository
public interface BondDurationPriceDao extends BaseMapper<BondDurationPrice> {

}
