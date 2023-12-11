package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondRatingCd;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 可转债评级表 Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Mapper
@Repository
public interface BondRatingCdDao extends BaseMapper<BondRatingCd> {

    int batchInsert(List<BondRatingCd> bondRatingCdList);

    BondRatingCd selectLatestBondRatingCd(String bondCode);

    List<BondRatingCd> selectBondRatingCdPeriod(@Param("period") Integer period, @Param("bondCodeList") Collection<String> bondCodeList);
}
