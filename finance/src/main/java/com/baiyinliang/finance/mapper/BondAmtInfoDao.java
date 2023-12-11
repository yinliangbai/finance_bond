package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondAmtInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 可转债剩余规模表 Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Mapper
@Repository
public interface BondAmtInfoDao extends BaseMapper<BondAmtInfo> {


    int batchInsert(List<BondAmtInfo> bondAmtInfos);

    List<BondAmtInfo> selectLatestAmtList(Collection<String> bondCodeList);

    BondAmtInfo selectLatestAmt(String bondCode);
}
