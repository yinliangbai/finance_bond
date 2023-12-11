package com.baiyinliang.finance.service;

import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baiyinliang.finance.request.BondInfoListReq;
import com.baiyinliang.finance.vo.BondInfoVO;
import com.baiyinliang.finance.vo.PageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 可转债基本信息表，除了状态，其它数据不变 服务类
 * </p>
 *
 * @author root
 * @since 2023-11-25
 */
public interface BondBaseInfoService extends IService<BondBaseInfo> {

    // 跑批job
    // 访问东方财富网站，查询所有可转债
    // 表中没有的就插入
    void saveBonds();

    void saveBondBaseInfoList();

    void test();

    PageVO<BondInfoVO> getInterestMarginList(BondInfoListReq req);

    BondInfoVO getBondInfo(String bondCode);
}
