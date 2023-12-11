package com.baiyinliang.finance.controller;


import com.baiyinliang.finance.entity.BondPrice;
import com.baiyinliang.finance.service.BondPriceService;
import com.baiyinliang.finance.service.impl.BondPriceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/bond-price")
public class BondPriceController {

    @Autowired
    private BondPriceService bondPriceService;

    @RequestMapping("/diffPrice")
    public List<BondPriceServiceImpl.SingleBondPrice> getBondDiffPrice() {
        return bondPriceService.getBondDiffPrice();
    }

    @RequestMapping("/diffPrice2")
    public List<BondPrice> getBondDiffPrice2() {
        return bondPriceService.getBondPriceList();
    }

    @RequestMapping("/getBondPriceRangeById")
    public BondPriceServiceImpl.BondPriceRange getBondPriceRangeById(@RequestParam("bondId") Integer bondId){
        return bondPriceService.getBondPriceRangeById(bondId);
    }
}
