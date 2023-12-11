package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondRateInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 可转债付息表 Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Mapper
@Repository
public interface BondRateInfoDao extends BaseMapper<BondRateInfo> {


    int batchInsert(List<BondRateInfo> bondRateInfoList);

    List<BondRateInfo> selectRecentlyBondRateList(@Param("bondCodeList") Collection<String> bondCodeList);

    BondRateInfo selectLatestRateInfo(String bondCode);
}
