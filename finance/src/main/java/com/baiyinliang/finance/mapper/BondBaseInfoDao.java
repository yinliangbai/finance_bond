package com.baiyinliang.finance.mapper;

import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 可转债基本信息表，除了状态，其它数据不变 Mapper 接口
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Mapper
@Repository
public interface BondBaseInfoDao extends BaseMapper<BondBaseInfo> {


    int batchInsert(List<BondBaseInfo> bonds);

    List<BondBaseInfo> selectBaseInfoListByParams(@Param("param") String param, @Param("shMarket") boolean shMarket, @Param("szMarket") boolean szMarket);
}
