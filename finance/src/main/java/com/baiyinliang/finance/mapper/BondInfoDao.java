package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondInfo;
import com.baiyinliang.finance.model.Bond;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-03-06
 */
@Repository
public interface BondInfoDao extends BaseMapper<BondInfo> {

//    int batchInsert(@Param("bonds") Collection<BondInfo> bonds);
}
