package com.baiyinliang.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.baiyinliang.finance.entity.BondDurationPrice;
import com.baiyinliang.finance.entity.BondInfo;
import com.baiyinliang.finance.entity.BondPrice;
import com.baiyinliang.finance.mapper.BondDurationPriceDao;
import com.baiyinliang.finance.mapper.BondInfoDao;
import com.baiyinliang.finance.mapper.BondPriceDao;
import com.baiyinliang.finance.service.BondDurationPriceService;
import com.baiyinliang.finance.tools.DriverUtil;
import com.baiyinliang.finance.tools.RedisUtil;
import com.baiyinliang.finance.tools.WeChatTemplateMsgUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author root
 * @since 2023-11-09
 */
@Service
@Slf4j
public class BondDurationPriceServiceImpl extends ServiceImpl<BondDurationPriceDao, BondDurationPrice> implements BondDurationPriceService {

    @Autowired
    private BondInfoDao bondInfoDao;
    @Autowired
    private BondPriceDao bondPriceDao;
    @Autowired
    private BondDurationPriceDao bondDurationPriceDao;
    @Autowired
    private WeChatTemplateMsgUtils weChatTemplateMsgUtils;
    @Autowired
    private RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setBondDurationPrice() {
        // 适时查询价格，低于几天提醒

        // 每天跑批，每只债的一天最低价、二天最低价，等
        List<BondPrice> bondPrices = bondPriceDao.selectList(null);
        MultiValueMap<Integer, BondPrice> bondPriceMultiValueMap = new LinkedMultiValueMap<>();
        for (BondPrice bondPrice : bondPrices) {
            bondPriceMultiValueMap.add(bondPrice.getBondId(), bondPrice);
        }

        for (Map.Entry<Integer, List<BondPrice>> entry : bondPriceMultiValueMap.entrySet()) {
            Integer bondId = entry.getKey();
            List<BondPrice> bondPriceList = entry.getValue();
            Collections.sort(bondPriceList, Comparator.comparing(BondPrice::getCreateTime).reversed());
            BigDecimal minPrice = BigDecimal.ZERO;
            BigDecimal maxPrice = BigDecimal.ZERO;

            for (int i = 0; i < bondPriceList.size(); i++) {
                BondPrice bondPrice = bondPriceList.get(i);
                BigDecimal currentMinPrice = bondPrice.getMinPrice() == null ? BigDecimal.ZERO : bondPrice.getMinPrice();
                BigDecimal currentMaxPrice = bondPrice.getMaxPrice() == null ? BigDecimal.ZERO : bondPrice.getMaxPrice();
                if (minPrice.compareTo(BigDecimal.ZERO) == 0) {
                    minPrice = currentMinPrice;
                } else if (minPrice.compareTo(currentMinPrice) > 0) {
                    minPrice = currentMinPrice;
                }

                if (maxPrice.compareTo(BigDecimal.ZERO) == 0) {
                    maxPrice = currentMaxPrice;
                } else if (maxPrice.compareTo(currentMaxPrice) < 0) {
                    maxPrice = currentMaxPrice;
                }

                BondDurationPrice minBondDurationPrice = new BondDurationPrice();
                minBondDurationPrice.setBondId(bondPrice.getBondId());
                minBondDurationPrice.setCreateTime(new Date());
                minBondDurationPrice.setMinPrice(minPrice);
                minBondDurationPrice.setMaxPrice(maxPrice);
                minBondDurationPrice.setDuration(i + 1);
                bondDurationPriceDao.insert(minBondDurationPrice);
            }
        }
    }

    @Override
    public void comparePriceInTime() {
        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.eq(BondInfo::getFlag, 1);
        List<BondInfo> bondInfos = bondInfoDao.selectList(bondInfoQueryWrapper);
        if (CollectionUtils.isEmpty(bondInfos)) {
            log.warn("查询可转债信息表为空");
            return;
        }

        Map<Integer, String> bondNameMap = new HashMap<>();
        Map<Integer, BondInfo> bondInfoMap = new HashMap<>();
        for (BondInfo bondInfo : bondInfos) {
            if (new BigDecimal("130").compareTo(bondInfo.getPrice()) > 0) {
                bondInfoMap.put(bondInfo.getBondId(), bondInfo);
            }
            bondNameMap.put(bondInfo.getBondId(), bondInfo.getBondNm());
        }

        MultiValueMap<Integer, BondDurationPrice> bondDurationPriceMultiValueMap = getBondDurationPriceList();

        Set<Integer> bondIdSet = bondInfoMap.keySet();
        // 循环 遍历所有详情页
        ChromeDriver chromeDriver = DriverUtil.getChromeDriver();
        for (Integer bondId : bondIdSet) {
            Map<Integer, String> minBondPriceMsgMap = new HashMap<>();
            Map<Integer, String> maxBndPriceMsgMap = new HashMap<>();
            BondInfo bondInfo = bondInfoMap.get(bondId);
            BigDecimal bondPrice = getBondPrice(chromeDriver, bondId);
            List<BondDurationPrice> bondDurationPrices = bondDurationPriceMultiValueMap.get(bondId);
            Collections.sort(bondDurationPrices, Comparator.comparing(BondDurationPrice::getDuration));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (BondDurationPrice bondDurationPrice : bondDurationPrices) {
                if (bondPrice.compareTo(bondDurationPrice.getMinPrice()) <= 0) {
                    // 这里发微信消息通知

                    log.info("id{}当前价{}为{}天内最低价", bondId, bondPrice, bondDurationPrice.getDuration());
                    minBondPriceMsgMap.put(bondId, sdf.format(new Date()) + " " + bondNameMap.get(bondId) + "[" + bondId + "] 的当前价" + bondPrice + "为" +
                            bondDurationPrice.getDuration() + "天内最低价");
                }

                if (bondPrice.compareTo(bondDurationPrice.getMaxPrice()) >= 0) {
                    // 这里发微信消息通知
                    log.info("id{}当前价{}为{}天内最高价", bondId, bondPrice, bondDurationPrice.getDuration());
                    maxBndPriceMsgMap.put(bondId, sdf.format(new Date()) + " " + bondNameMap.get(bondId) + "[" + bondId + "] 的当前价" + bondPrice + "为" +
                            bondDurationPrice.getDuration() + "天内最高价");
                }
            }

            if (!CollectionUtils.isEmpty(minBondPriceMsgMap)) {
                weChatTemplateMsgUtils.sendTemplateMsg(minBondPriceMsgMap.get(bondId));
                if (minBondPriceMsgMap.get(bondId) == null) {
                    log.error("bondId={}  minBondPriceMsgMap={}", bondId, JSON.toJSONString(minBondPriceMsgMap));
                }
                redisUtil.set("bond_min_price_" + bondId, minBondPriceMsgMap.get(bondId), 24 * 60 * 60 * 1000);
            }

            if (!CollectionUtils.isEmpty(maxBndPriceMsgMap)) {
                weChatTemplateMsgUtils.sendTemplateMsg(maxBndPriceMsgMap.get(bondId));
                if (maxBndPriceMsgMap.get(bondId) == null) {
                    log.error("bondId={}  maxBndPriceMsgMap={}", bondId, JSON.toJSONString(maxBndPriceMsgMap));
                }
                redisUtil.set("bond_max_price_" + bondId, maxBndPriceMsgMap.get(bondId), 24 * 60 * 60 * 1000);
            }
        }
        chromeDriver.quit();
        log.info("比价完成------------------------");
    }


    private MultiValueMap<Integer, BondDurationPrice> getBondDurationPriceList() {
        List<BondDurationPrice> bondDurationPrices = bondDurationPriceDao.selectList(null);
        MultiValueMap<Integer, BondDurationPrice> bondDurationPriceMultiValueMap = new LinkedMultiValueMap<>();
        for (BondDurationPrice bondDurationPrice : bondDurationPrices) {
            bondDurationPriceMultiValueMap.add(bondDurationPrice.getBondId(), bondDurationPrice);
        }

        return bondDurationPriceMultiValueMap;
    }


    private BigDecimal getBondPrice(ChromeDriver driver, Integer bondId) {
        if (bondId.toString().startsWith("12")) {
            driver.get("https://xueqiu.com/S/sz" + bondId);
        } else if (bondId.toString().startsWith("11")) {
            driver.get("https://xueqiu.com/S/sh" + bondId);
        }

        //等待页面加载完成
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("当前打开页面的标题是： " + driver.getTitle() + " bondId=" + bondId);

        BondPrice bondPrice = new BondPrice();

        // 当前价格
        List<WebElement> elements = driver.findElements(By.className("stock-current"));
        WebElement tableWebElement = elements.get(0);
        String text = tableWebElement.getText();
        log.info("bondId={} text={} ", bondId, text);
        return new BigDecimal(text.replace("¥", "").trim());
//        return bondPrice;
//        redeem_price
        // todo 下修天计数
    }

}
