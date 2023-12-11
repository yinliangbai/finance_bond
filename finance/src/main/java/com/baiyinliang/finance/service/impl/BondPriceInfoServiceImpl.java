package com.baiyinliang.finance.service.impl;

import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baiyinliang.finance.entity.BondPriceInfo;
import com.baiyinliang.finance.enums.BusinessEnums;
import com.baiyinliang.finance.mapper.BondBaseInfoDao;
import com.baiyinliang.finance.mapper.BondPriceInfoDao;
import com.baiyinliang.finance.service.BondPriceInfoService;
import com.baiyinliang.finance.tools.NumberUtil;
import com.baiyinliang.finance.tools.ThreadPoolUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.baiyinliang.finance.common.Constants.DECIMAL_1000;

/**
 * <p>
 * 可转债价格表 服务实现类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Service
@Slf4j
public class BondPriceInfoServiceImpl extends ServiceImpl<BondPriceInfoDao, BondPriceInfo> implements BondPriceInfoService {

    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private BondBaseInfoDao baseInfoDao;
    @Autowired
    private BondPriceInfoDao bondPriceInfoDao;


    @Override
    public void saveBondPriceInfoList() {
//        查询bond表中上市状态的所有数据
        List<BondBaseInfo> bondBaseInfoList = getBondBaseInfos();
        if (CollectionUtils.isEmpty(bondBaseInfoList)) {
            log.warn("可转债基本数据表为空");
            return;
        }

        final int argNum = 6;
//        计算分页并获取所有bond的价格数据
        List<BondPriceInfo> addBondPriceInfoList = getBondPriceInfoList(bondBaseInfoList, argNum);
        // 最后的入库事务
        ((BondPriceInfoServiceImpl) AopContext.currentProxy()).afterProcess(addBondPriceInfoList);
    }

    /**
     * 数据分成6部分，使用多线程查询
     *
     * @param size bond总数
     * @return 要开启的线程数
     */
    private int calc(int size, int argNum) {
        int count = 1;
        // size<100,则不用分组
        if (size > 100) {
//            int i1 = size / 6;
            int i2 = size % argNum;
            count = argNum;
            if (i2 > 0) {
                count += 1;
            }
        }

        return count;
    }

    /**
     * 计算分页并获取所有bond的价格数据
     *
     * @param bondBaseInfoList bond数据集合
     * @param argNum           分页数，默认为6
     * @return 待新增的bond价格数据
     */
    private List<BondPriceInfo> getBondPriceInfoList(List<BondBaseInfo> bondBaseInfoList, int argNum) {
        int size = bondBaseInfoList.size();
//        数据分成6部分，使用多线程查询
        int count = calc(size, argNum);
        int i1 = size / argNum;
        int i2 = size % argNum;
        // 待新增的bond集合
        List<BondPriceInfo> addBondPriceInfoList = new ArrayList<>(size);
        List<BondBaseInfo> bondBaseInfos = new ArrayList<>();
//        SnowFlake snowFlake = new SnowFlake(1, 1);
        CountDownLatch latch = new CountDownLatch(count);
        log.info("收集价格数据开始---------");
        long beginTime = System.currentTimeMillis();
        // 开6个线程
        for (int i = 0; i < count; i++) {
            if (i == count - 1 && (i2 > 0)) {
                bondBaseInfos = bondBaseInfoList.subList(i * i1, size);
            } else {
                bondBaseInfos = bondBaseInfoList.subList(i * i1, i * i1 + i1);
            }
            log.info("i={}, size={}", i, bondBaseInfos.size());

            List<BondBaseInfo> finalBondBaseInfos = bondBaseInfos;
            ThreadPoolUtils.execute(() -> {
                try {
//                    循环获取每个bond的价格数据
                    loop(finalBondBaseInfos, addBondPriceInfoList);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }, count);
        }


        try {
            latch.await();
            log.info("结束了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("收集价格数据结束---------共耗时：{}", System.currentTimeMillis() - beginTime);

        log.info("addBondPriceInfoList={}", addBondPriceInfoList.size());
        return addBondPriceInfoList;
    }


    /**
     * 查询bond表中上市状态的所有数据
     *
     * @return
     */
    private List<BondBaseInfo> getBondBaseInfos() {
        LambdaQueryWrapper<BondBaseInfo> bondBaseInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondBaseInfoQueryWrapper.select(BondBaseInfo::getBondCode).eq(BondBaseInfo::getFlag, BusinessEnums.BondCodeFlag.上市.getFlag()).orderByAsc(BondBaseInfo::getBondCode);
        List<BondBaseInfo> bondBaseInfoList = baseInfoDao.selectList(bondBaseInfoQueryWrapper);
        return bondBaseInfoList;
    }

    /**
     * 循环获取每个bond的价格数据
     *
     * @param bondBaseInfos        bond集合
     * @param addBondPriceInfoList 待新增的bond集合
     */
    private void loop(List<BondBaseInfo> bondBaseInfos, List<BondPriceInfo> addBondPriceInfoList) {
        for (BondBaseInfo bondBaseInfo : bondBaseInfos) {
            String bondCode = bondBaseInfo.getBondCode();
//            从雪球网站获取价格数据, 返回对应的价格数据，若有异常则为null
            BondPriceInfo bondPriceInfo = doGetBondPriceInfo(bondCode);
            /*int i =0;
            while (bondPriceInfo== null){
                log.info("{}重试第{}次",bondCode, i+1);
                bondPriceInfo = getBondPriceInfo(bondCode);
                i++;
            }*/
            if (bondPriceInfo != null) {
                addBondPriceInfoList.add(bondPriceInfo);
            } else {
                log.error("{}获取价格失败", bondCode);
            }
        }
    }

    /**
     * 从雪球网站获取价格数据
     *
     * @param bondCode bond编码
     * @return 对应的价格数据，若有异常则为null
     */
    private BondPriceInfo doGetBondPriceInfo(String bondCode) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        BondPriceInfo bondPriceInfo = new BondPriceInfo();
//        bondPriceInfo.setId(snowFlake.nextId());
        bondPriceInfo.setBondCode(bondCode);
        try {
            bondPriceInfo.setDate(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
        } catch (ParseException e) {
            log.warn("日期格式化异常");
//            e.printStackTrace();
        }
        String xueqiuBondDetailUrl = null;
        if (bondCode.startsWith("12")) {
            xueqiuBondDetailUrl = "https://xueqiu.com/S/sz" + bondCode;
        } else if (bondCode.startsWith("11")) {
            xueqiuBondDetailUrl = "https://xueqiu.com/S/sh" + bondCode;
        }
        log.info("雪球详情地址：{}", xueqiuBondDetailUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(HttpHeaders.COOKIE, "xq_a_token=cf755d099237875c767cae1769959cee5a1fb37c; xqat=cf755d099237875c767cae1769959cee5a1fb37c; xq_r_token=e073320f4256c0234a620b59c446e458455626d9; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTcwMTk5NTg4MCwiY3RtIjoxNjk5Njk3MDExMzA3LCJjaWQiOiJkOWQwbjRBWnVwIn0.bgjWeA3aKpL3t8UatYIgxEjhTdAwb9PGpG3p4V0udPqni0kRHqq6n90Fx-LovLcrMWJyIhRmkqJFrhkzqSKESgw3Y6zUyx5vnvSrw8Ajz6QKlUHau9LDN9jYMF97qBesMbjcpDu6ygy6x3eaoITjLCXE0cmaC7RJHQgxTDR8zvAJ18C8sTenaEUV6VE11_dRL-8OsvXMhM1OkRuoYJWCLIwlAoewIg8p0RfrjCt254YmiB-M3vk9Rfx9u5sDbEmnfJWNUT3PUX6rfwGqgENi0E_QupfVE89psAm-XRhAHIDJiASj04s3utYUPtzN0fGRzfJyypFVm22ziVVheafxMA; cookiesu=921699697061450; u=921699697061450; Hm_lvt_1db88642e346389874251b5a1eded6e3=1699697060; device_id=1a375e35f186bff0e577cc75d781a0af; s=af12g4bgex; __utma=1.1194915567.1699697069.1699697069.1699697069.1; __utmc=1; __utmz=1.1699697069.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lpvt_1db88642e346389874251b5a1eded6e3=" + System.currentTimeMillis() + "; acw_tc=2760827d16997558965437479e75b191de6c29df1bab2460f5097d7f2fa460");
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43");
        headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8");

        HashMap<Object, Object> params = new HashMap<>();
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = null;
        String body = null;
        try {
            assert xueqiuBondDetailUrl != null;
            result = restTemplate.exchange(xueqiuBondDetailUrl, HttpMethod.GET, entity, String.class);
            if (HttpStatus.OK.equals(result.getStatusCode())) {
//                log.info("雪球详情页访问成功");
            } else {
                log.error("雪球详情页访问失败：{}", result);
                return null;
            }

            // 当天最高价
            body = result.getBody();
            assert body != null;

            String maxPriceStr = "最高：<span class=\"stock-rise\">";
            String maxPriceStr2 = "最高：<span class=\"stock-fall\">";
            String maxPriceStr3 = "最高：<span>";
            int maxPriceIndex = body.indexOf(maxPriceStr);
            if (maxPriceIndex == -1) {
                maxPriceIndex = body.indexOf(maxPriceStr2);
                maxPriceStr = maxPriceStr2;
            }
            if (maxPriceIndex == -1) {
                maxPriceIndex = body.indexOf(maxPriceStr3);
                maxPriceStr = maxPriceStr3;

            }
            if (maxPriceIndex == -1) {
                log.error("雪球网详情页面，获取最高价格失败，body={}", body);
                return null;
//                throw new Exception("雪球网详情页面，获取最高价格失败");
            }
            String maxPriceStrSubstring = body.substring(maxPriceIndex + maxPriceStr.length());
            try {
                String maxPriceValueStr = maxPriceStrSubstring.substring(0, maxPriceStrSubstring.indexOf("</span>"));
                bondPriceInfo.setMaxPrice(new BigDecimal(maxPriceValueStr).multiply(DECIMAL_1000).intValue());
            } catch (Exception e) {
                log.warn("maxPriceStrSubstring={} ", maxPriceStrSubstring);
                log.error("最高价格转换异常", e.getCause());
//                retryBondPriceInfoList.add(bondCode);
                return null;
//                e.printStackTrace();
            }

            String minPriceStr = "最低：<span class=\"stock-fall\">";
            String minPriceStr2 = "最低：<span class=\"stock-rise\">";
            String minPriceStr3 = "最低：<span>";
            int minPriceIndex = body.indexOf(minPriceStr);
            if (minPriceIndex == -1) {
                minPriceIndex = body.indexOf(minPriceStr2);
                minPriceStr = minPriceStr2;
            }
            if (minPriceIndex == -1) {
                minPriceIndex = body.indexOf(minPriceStr3);
                minPriceStr = minPriceStr3;

            }
            if (minPriceIndex == -1) {
                log.error("雪球网详情页面，获取最低价格失败，body={}", body);
//                retryBondPriceInfoList.add(bondCode);
                return null;
//                throw new Exception("雪球网详情页面，获取最低价格失败");
            }
            String minPriceStrSubstring = body.substring(minPriceIndex + minPriceStr.length());
            try {
                String minPriceValueStr = minPriceStrSubstring.substring(0, minPriceStrSubstring.indexOf("</span>"));
                bondPriceInfo.setMinPrice(new BigDecimal(minPriceValueStr).multiply(DECIMAL_1000).intValue());
            } catch (Exception e) {
//                log.error("雪球网详情页面，获取最高价格失败，22body={}", body);

                log.error("minPriceStrSubstring={}", minPriceStrSubstring);
                log.error("最低价格转换异常", e.getCause());
                return null;
//                e.printStackTrace();
            }


            String currentPriceStr = "<strong>¥";
            int currentPriceIndex = body.indexOf(currentPriceStr);
            String currentPriceStrSubstring = body.substring(currentPriceIndex + currentPriceStr.length());
            try {
                String currentPriceValueStr = currentPriceStrSubstring.substring(0, currentPriceStrSubstring.indexOf("</strong>"));
                bondPriceInfo.setCurrentPrice(new BigDecimal(currentPriceValueStr).multiply(DECIMAL_1000).intValue());
            } catch (Exception e) {
                log.error("currentPriceStrSubstring={}", currentPriceStrSubstring);
                log.error("当前价 即当天收盘价转换异常", e.getCause());
                return null;
//                e.printStackTrace();
            }


//            今开：<span class="stock-rise">
            String openPriceStr = "\"open\":";
            int openPriceIndex = body.indexOf(openPriceStr);
            String openPriceStrSubstring = body.substring(openPriceIndex + openPriceStr.length());
            try {
                String openPriceValueStr = openPriceStrSubstring.substring(0, openPriceStrSubstring.indexOf(","));
                bondPriceInfo.setOpeningPrice(new BigDecimal(openPriceValueStr).multiply(DECIMAL_1000).intValue());
            } catch (Exception e) {
                log.error("openPriceStrSubstring={}", openPriceStrSubstring);
                log.error("开盘价格转换异常", e.getCause());
                return null;
//                e.printStackTrace();
            }

            // 税后收益
            String afterRateInComeStr = "税后收益：<span>";
            int afterRateInComeIndex = body.indexOf(afterRateInComeStr);
            String afterRateInComeSubstring = body.substring(afterRateInComeIndex + afterRateInComeStr.length());
            try {
                String afterRateInComeValueStr = afterRateInComeSubstring.substring(0, afterRateInComeSubstring.indexOf("%"));
                bondPriceInfo.setIncome(new BigDecimal(afterRateInComeValueStr).multiply(DECIMAL_1000).intValue());
            } catch (Exception e) {
                log.error("afterRateInComeSubstring={}", afterRateInComeSubstring);
                log.error("税后收益转换异常", e.getCause());
                return null;
//                e.printStackTrace();
            }


        } catch (Exception e) {
            log.error("雪球详情页访问失败：{}, body={}", e.getCause(), body);
            return null;
//            e.printStackTrace();
        }

        return bondPriceInfo;
    }


    @Transactional
    public void afterProcess(List<BondPriceInfo> addBondPriceInfoList) {
        // 保存
        if (!CollectionUtils.isEmpty(addBondPriceInfoList)) {
            int insertCount = bondPriceInfoDao.batchInsert(addBondPriceInfoList);
            log.info("本次新增条数：{}", insertCount);

        } else {
            log.info("本次无新增");
        }
    }

    @Override
    public BondPriceRange getBondPriceRangeById(String bondCode) {
        BondPriceRange bondPriceRange = new BondPriceRange();
        bondPriceRange.setBondCode(bondCode);
        List<BondPriceInfo> bondPrices = bondPriceInfoDao.selectSingletonPrice(5, bondCode);
        if (CollectionUtils.isEmpty(bondPrices)) {
            log.warn("{}的区间价格数据不存在", bondCode);
            return bondPriceRange;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

        List<PriceRange> priceRangeList = new ArrayList<>(bondPrices.size());
        for (int i = bondPrices.size() - 1, j = 0; i >= 0; i--, j++) {
            BondPriceInfo bondPrice = bondPrices.get(i);
            PriceRange priceRange = new PriceRange();
            priceRange.setDate(sdf.format(bondPrice.getDate()));
            priceRange.setMinPrice(NumberUtil.subRemainder(bondPrice.getMinPrice(),3));
            priceRange.setMaxPrice(NumberUtil.subRemainder(bondPrice.getMaxPrice(),3));
            priceRangeList.add(j, priceRange);
        }

        bondPriceRange.setPriceRangeList(priceRangeList);
//        log.info("bondPriceRange={}", JSON.toJSONString(bondPriceRange));

        return bondPriceRange;
    }

    @Data
    public class BondPriceRange {
        // id
        private String bondCode;
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
