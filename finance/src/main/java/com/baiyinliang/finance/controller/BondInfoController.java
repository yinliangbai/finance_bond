package com.baiyinliang.finance.controller;


import com.baiyinliang.finance.request.BondInfoListReq;
import com.baiyinliang.finance.request.BondInfoPageReq;
import com.baiyinliang.finance.response.ResponseData;
import com.baiyinliang.finance.service.BondInfoService;
import com.baiyinliang.finance.vo.BondInfoVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author root
 * @since 2023-03-06
 */
@RestController
@RequestMapping("/bond-info")
public class BondInfoController {

    @Autowired
    private BondInfoService bondInfoService;

    @RequestMapping("/process")
    public ResponseData processBond() {
        bondInfoService.processBond();
        return ResponseData.success("可转债基本信息更新成功");
    }

    @RequestMapping(value = "/test")
    public ResponseData test(@RequestParam("num") Integer num) {
        if (num > 100) {
            return ResponseData.error("失败了");
        } else {
            return ResponseData.success(200, "成功了", null);
        }
    }


    @RequestMapping("/price")
    public ResponseData setBondPrice() {
        bondInfoService.setBondPrice();
        return ResponseData.success("可转债当天价格入库成功");
    }

    @RequestMapping("/getInterestMarginList")
    public List<BondInfoVO> getInterestMarginList() {
        return bondInfoService.getInterestMarginList();
    }

    @RequestMapping("/getBondListPage")
    public Page<BondInfoVO> getBondListPage(@RequestBody BondInfoPageReq req) {
        return bondInfoService.getBondInfoVOPage(req);
    }

    @RequestMapping("/getBondInfoList")
    public List<BondInfoVO> getBondInfoList(@RequestBody BondInfoListReq req) {
        return bondInfoService.getBondInfoList(req);
    }

    @RequestMapping("/getBondInfo")
    public ResponseData<BondInfoVO> getBondInfo(@RequestParam("bondId") Integer bondId) {
        return ResponseData.success(bondInfoService.getBondInfo(bondId));
    }

    @RequestMapping("/detail")
    public void detail() {
//        bondInfoService.getRedeemPrice2(128062);
    }

    @RequestMapping("/getBondPrice")
    public void getBondPrice() {
        bondInfoService.getBondPrice2(123193);
    }

    // 临近付息日 五天吧
//    public void get


}
