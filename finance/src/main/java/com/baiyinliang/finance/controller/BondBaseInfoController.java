package com.baiyinliang.finance.controller;


import com.baiyinliang.finance.request.BondInfoListReq;
import com.baiyinliang.finance.response.ResponseData;
import com.baiyinliang.finance.service.BondBaseInfoService;
import com.baiyinliang.finance.vo.BondInfoVO;
import com.baiyinliang.finance.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 可转债基本信息表，除了状态，其它数据不变 前端控制器
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@RestController
@RequestMapping("/bond-base-info")
public class BondBaseInfoController {

    @Autowired
    private BondBaseInfoService bondBaseInfoService;

    @RequestMapping("/saveBaseInfoList")
    public ResponseData saveBondBaseInfoList() {
        bondBaseInfoService.saveBondBaseInfoList();
        return ResponseData.success("可转债基本信息更新成功");
    }

    @RequestMapping("/test")
    public ResponseData test() {
        bondBaseInfoService.test();
        return ResponseData.success("可转债基本信息更新成功");
    }

    @RequestMapping(value = "/getInterestMarginList", method = RequestMethod.POST)
    public ResponseData<PageVO<BondInfoVO>> getInterestMarginList(@RequestBody BondInfoListReq req) {
        return ResponseData.success(bondBaseInfoService.getInterestMarginList(req));
    }

    @RequestMapping("/getBondInfo")
    public ResponseData<BondInfoVO> getBondInfo(@RequestParam("bondCode") String bondCode) {
        return ResponseData.success(bondBaseInfoService.getBondInfo(bondCode));
    }
}
