package com.baiyinliang.finance.controller;


import com.baiyinliang.finance.response.ResponseData;
import com.baiyinliang.finance.service.BondPriceInfoService;
import com.baiyinliang.finance.service.impl.BondPriceInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 可转债价格表 前端控制器
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@RestController
@RequestMapping("/bond-price-info")
public class BondPriceInfoController {

    @Autowired
    private BondPriceInfoService bondPriceInfoService;

    @RequestMapping("/saveBondPriceInfoList")
    public ResponseData saveBondPriceInfoList() {
        bondPriceInfoService.saveBondPriceInfoList();
        return ResponseData.success("可转债价格信息新增成功");
    }

    @RequestMapping("/getBondPriceRangeById")
    public ResponseData<BondPriceInfoServiceImpl.BondPriceRange> getBondPriceRangeById(@RequestParam("bondCode") String bondCode) {
        return ResponseData.success(bondPriceInfoService.getBondPriceRangeById(bondCode));
    }

}
