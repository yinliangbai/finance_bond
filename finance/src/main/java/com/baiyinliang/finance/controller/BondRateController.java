package com.baiyinliang.finance.controller;


import com.baiyinliang.finance.entity.BondRate;
import com.baiyinliang.finance.response.ResponseData;
import com.baiyinliang.finance.service.BondRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author root
 * @since 2023-03-14
 */
@RestController
@RequestMapping("/bond-rate")
public class BondRateController {

    @Autowired
    private BondRateService bondRateService;

    @RequestMapping("/rate")
    public ResponseData setBondRates() {
        bondRateService.setBondRate();
        return ResponseData.success("记录新债利息成功");

    }

    @RequestMapping("/rateList")
    public List<BondRate> rateList() {
        return bondRateService.selectBondRateList();

    }

    @RequestMapping("/bondRate")
    public void aa() {
        bondRateService.getBondRate2(123054);
    }
}
