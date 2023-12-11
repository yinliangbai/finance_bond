package com.baiyinliang.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.baiyinliang.finance.entity.BondInfo;
import com.baiyinliang.finance.entity.BondPrice;
import com.baiyinliang.finance.mapper.BondDurationPriceDao;
import com.baiyinliang.finance.mapper.BondInfoDao;
import com.baiyinliang.finance.mapper.BondPriceDao;
import com.baiyinliang.finance.service.BondPriceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author root
 * @since 2023-03-06
 */
@Service
@Slf4j
public class BondPriceServiceImpl extends ServiceImpl<BondPriceDao, BondPrice> implements BondPriceService {

    @Autowired
    private BondPriceDao bondPriceDao;

    @Autowired
    private BondInfoDao bondInfoDao;

    @Autowired
    private BondDurationPriceDao bondDurationPriceDao;

    @Override
    public List<SingleBondPrice> getBondDiffPrice() {
//    public Map<Integer, SingleBondPrice> getBondDiffPrice() {
        Map<Integer, SingleBondPrice> singleBondPriceMap = new HashMap<>();
        List<SingleBondPrice> singleBondPrices = new ArrayList<>();

        LambdaQueryWrapper<BondPrice> bondPriceQueryWrapper = new LambdaQueryWrapper<>();
        List<BondPrice> bondPrices = bondPriceDao.selectList(bondPriceQueryWrapper);
        if (CollectionUtils.isEmpty(bondPrices)) {
            return singleBondPrices;
        }


        // 每只债 几天可以 差值 排序，一天的差值，两天的差值

        MultiValueMap<Integer, BondPrice> bondPriceMultiValueMap = new LinkedMultiValueMap<>();
        for (BondPrice bondPrice : bondPrices) {
            Integer bondId = bondPrice.getBondId();
            bondPriceMultiValueMap.add(bondId, bondPrice);
        }

        Map<Integer, BondInfo> bondInfoMap = new HashMap<>();
        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.eq(BondInfo::getFlag, 1);
        bondInfoQueryWrapper.in(BondInfo::getBondId, bondPriceMultiValueMap.keySet());
        List<BondInfo> bondInfos = bondInfoDao.selectList(bondInfoQueryWrapper);
        for (BondInfo bondInfo : bondInfos) {
            bondInfoMap.put(bondInfo.getBondId(), bondInfo);
        }

        for (Map.Entry<Integer, List<BondPrice>> entry : bondPriceMultiValueMap.entrySet()) {
            Integer bondId = entry.getKey();
            List<BondPrice> bondPriceList = entry.getValue();
            if (CollectionUtils.isEmpty(bondPriceList)) {
                log.warn("id={}的价格记录不存在", bondId);
                continue;
            }


            BigDecimal minPrice = BigDecimal.ZERO;
            BigDecimal maxPrice = BigDecimal.ZERO;
            Date beginDate = new Date();
            Date endDate = new Date();
            for (int i = 0; i < bondPriceList.size(); i++) {
                BondPrice bondPrice = bondPriceList.get(i);
//                bondNm = bondPrice.getBondNm();
                BigDecimal currentMinPrice = bondPrice.getMinPrice();
                BigDecimal currentMaxPrice = bondPrice.getMaxPrice();
                if (i == 0) {
                    minPrice = currentMinPrice;
                    maxPrice = currentMaxPrice;
                    beginDate = bondPrice.getDate();
                } else {
                    if (minPrice.compareTo(currentMinPrice) > 0) {
                        minPrice = currentMinPrice;
                    }
                    if (maxPrice.compareTo(currentMaxPrice) < 0) {
                        maxPrice = currentMaxPrice;
                    }
                }

                if (i == bondPriceList.size() - 1) {
                    endDate = bondPrice.getDate();
                }
            }

            // 区间：最高价 - 最低价
            BigDecimal balance = maxPrice.subtract(minPrice);
            // 区间：时长
            long daysDiff = twoDatesDiffBeforeJava8(beginDate, endDate);
            log.info("id={} 两个时间之间的天数差={}", bondId, daysDiff);

            BondInfo bondInfo = bondInfoMap.get(bondId);
            SingleBondPrice singleBondPrice = new SingleBondPrice();
            singleBondPrice.setBondId(bondId);
            singleBondPrice.setBondNm(bondInfo.getBondNm());
            singleBondPrice.setBalance(balance);
            singleBondPrice.setDaysDiff(daysDiff);
            singleBondPrice.setPrice(bondInfo.getPrice());
            singleBondPrice.setEarningsPrice(bondInfo.getEarningsPrice());
            singleBondPriceMap.put(bondId, singleBondPrice);
            singleBondPrices.add(singleBondPrice);
        }

        // 排序
        ArrayList<SingleBondPrice> values = new ArrayList<>();
        for (SingleBondPrice value : singleBondPriceMap.values()) {
            if (value.getDaysDiff() != 0) {
                values.add(value);
            }
        }
//        ArrayList<SingleBondPrice> values = new ArrayList<>(singleBondPriceMap.values());
        values.sort(Comparator.comparing(SingleBondPrice::getBalance).reversed());
        singleBondPrices.sort(Comparator.comparing(SingleBondPrice::getBalance).reversed());
//        log.info("values={}", values);
        log.info("singleBondPrices={}", singleBondPrices);
        log.info("singleBondPriceMap={}", JSON.toJSONString(singleBondPriceMap));
        return singleBondPrices;
    }

    @Data
    public static class SingleBondPrice {
        private Integer bondId;
        private String bondNm;
        // 差额
        private BigDecimal balance;
        // 现价
        private BigDecimal price;
        // 强赎价和现价的差值，越大越适合买
        private BigDecimal earningsPrice;
        // 日期区间
        private long daysDiff;
    }


    @Override
    public List<BondPrice> getBondPriceList() {
//        List<BondPrice> bondPrices2 = bondPriceDao.selectBondPricePeriod2();
        List<BondPrice> bondPrices2 = bondPriceDao.aaa();
        return bondPrices2;
    }

    @Override
    public BondPriceRange getBondPriceRangeById(Integer bondId) {
        BondPriceRange bondPriceRange = new BondPriceRange();
        bondPriceRange.setBondId(bondId);
        LambdaQueryWrapper<BondPrice> bondPriceQueryWrapper = new LambdaQueryWrapper<>();
        bondPriceQueryWrapper.select(BondPrice::getBondId, BondPrice::getBondNm, BondPrice::getDate, BondPrice::getMinPrice, BondPrice::getMaxPrice)
                .eq(BondPrice::getBondId, bondId).orderByDesc(BondPrice::getId).last(" limit 5");
        List<BondPrice> bondPrices = bondPriceDao.selectList(bondPriceQueryWrapper);
        if (CollectionUtils.isEmpty(bondPrices)) {
            log.warn("bondId={}的区间价格数据不存在", bondId);
            return bondPriceRange;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

        List<PriceRange> priceRangeList = new ArrayList<>(bondPrices.size());
        for (int i = bondPrices.size() - 1, j = 0; i >= 0; i--, j++) {
            BondPrice bondPrice = bondPrices.get(i);
            bondPriceRange.setBondNm(bondPrice.getBondNm());
            PriceRange priceRange = new PriceRange();
            priceRange.setDate(sdf.format(bondPrice.getDate()));
            priceRange.setMinPrice(bondPrice.getMinPrice());
            priceRange.setMaxPrice(bondPrice.getMaxPrice());
            priceRangeList.add(j, priceRange);
        }

        bondPriceRange.setPriceRangeList(priceRangeList);
        log.info("bondPriceRange={}", JSON.toJSONString(bondPriceRange));

        return bondPriceRange;
    }

    @Data
    public class BondPriceRange {
        // id
        private Integer bondId;
        private String bondNm;
        private List<PriceRange> priceRangeList;
    }

    @Data
    static
    class PriceRange {
//        @JsonFormat(pattern = "MM-dd", timezone = "GMT+8")
        private String date;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
    }

    private long twoDatesDiffBeforeJava8(Date beginDate, Date endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date formatBeginDate = sdf.parse(sdf.format(beginDate));
            Date formatEndDate = sdf.parse(sdf.format(endDate));
            long diffInMillis = Math.abs(formatEndDate.getTime() - formatBeginDate.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            return diff;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
