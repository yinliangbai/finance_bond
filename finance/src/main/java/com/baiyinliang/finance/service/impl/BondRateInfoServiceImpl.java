package com.baiyinliang.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baiyinliang.finance.common.Constants;
import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baiyinliang.finance.entity.BondRateInfo;
import com.baiyinliang.finance.enums.BusinessEnums;
import com.baiyinliang.finance.mapper.BondBaseInfoDao;
import com.baiyinliang.finance.mapper.BondRateInfoDao;
import com.baiyinliang.finance.service.BondRateInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 可转债付息表 服务实现类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Slf4j
@Service
public class BondRateInfoServiceImpl extends ServiceImpl<BondRateInfoDao, BondRateInfo> implements BondRateInfoService {


    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private BondRateInfoDao bondRateInfoDao;
    @Autowired
    private BondBaseInfoDao baseInfoDao;


    @Override
    public void addBondRateInfoList() {
        Set<String> notExistsCodeFromRateInfo = getNotExistsCodeFromRateInfo();
        if (CollectionUtils.isEmpty(notExistsCodeFromRateInfo)) {
            log.info("传入编码集合为空");
            return;
        }

        List<BondRateInfo> allBondRateInfoList = new ArrayList<>();
        for (String bonCode : notExistsCodeFromRateInfo) {
            List<BondRateInfo> bondRateInfoList = doGetBondRateInfoList(bonCode);
            if (CollectionUtils.isEmpty(bondRateInfoList)) {
                log.error("{}的付息数据为空", bonCode);
            } else {
                allBondRateInfoList.addAll(bondRateInfoList);
            }
        }

        if (CollectionUtils.isEmpty(allBondRateInfoList)) {
            log.info("本次待新增的付息数据为空，入参为={}", JSON.toJSONString(notExistsCodeFromRateInfo));
            return;
        }

        // 调用事务访求
        ((BondRateInfoServiceImpl) AopContext.currentProxy()).batchInsert(allBondRateInfoList);

    }

    @Transactional
    public void batchInsert(List<BondRateInfo> allBondRateInfoList) {
        int insertCount = bondRateInfoDao.batchInsert(allBondRateInfoList);
        log.info("本次新增付息条数：{}", insertCount);
    }

    // 查询每
    @Override
    public Map<String, BondRateInfo> getBondRateInfoList(Collection<String> bondCodeList) {
        // controller
        // 先根据查询条件：剩余规模区间、持有人、从bond表查询出，然后再传入rete表查付息日期
        if (CollectionUtils.isEmpty(bondCodeList)) {
            log.warn("查询当年付息数据失败，传入的编码为空");
            return null;
        }

        Calendar instance = Calendar.getInstance();
        int year = instance.get(Calendar.YEAR);
        instance.set(Calendar.MONTH,Calendar.JANUARY);
        instance.set(Calendar.DAY_OF_MONTH,1);
        Date minTime = instance.getTime();

        instance.set(Calendar.YEAR, year+1);
        instance.set(Calendar.MONTH,Calendar.JANUARY);
        instance.set(Calendar.DAY_OF_MONTH,1);
        Date maxTime = instance.getTime();

        LambdaQueryWrapper<BondRateInfo> rateInfoQueryWrapper = new LambdaQueryWrapper<>();
        rateInfoQueryWrapper.select(BondRateInfo::getBondCode, BondRateInfo::getPaymentDate, BondRateInfo::getRate).in(BondRateInfo::getBondCode, bondCodeList)
        .between(BondRateInfo::getPaymentDate, minTime, maxTime);
        List<BondRateInfo> bondRateInfos = bondRateInfoDao.selectList(rateInfoQueryWrapper);

        if(CollectionUtils.isEmpty(bondRateInfos)){
            log.warn("查询付息数据为空");
            return null;
        }

        Map<String, BondRateInfo> bondRateInfoMap = bondRateInfos.stream().collect(Collectors.toMap(BondRateInfo::getBondCode, Function.identity(), (x1, x2) -> x2));
        return bondRateInfoMap;
    }

    private List<BondRateInfo> doGetBondRateInfoList(String bondCode) {
        String eastmoneyDetailUrl = "https://datacenter-web.eastmoney.com/api/data/v1/get?"
                + "callback=jQuery112302665920256009826_" + System.currentTimeMillis() + "&reportName=RPT_BOND_CB_LIST&columns=ALL&quoteColumns=&quoteType=0&source=WEB&client=WEB&filter=(SECURITY_CODE=\"" + bondCode + "\")&_=" + System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8");

        HashMap<Object, Object> params = new HashMap<>();
        params.put("reportName", "RPT_BOND_CB_LIST");
        params.put("columns", "ALL");
        params.put("quoteType", 0);
        params.put("source", "WEB");
        params.put("client", "WEB");
        params.put("filter", "(SECURITY_CODE=\"" + bondCode + "\")");
        params.put("callback", "jQuery112302665920256009826_" + System.currentTimeMillis());

//        Map<Date, BigDecimal> yearRateMap = new HashMap<>();
        List<BondRateInfo> addBondRateInfoList = new ArrayList<>();

        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = null;
        try {
            result = restTemplate.exchange(eastmoneyDetailUrl, HttpMethod.GET, entity, String.class);
            if (HttpStatus.OK.equals(result.getStatusCode())) {
//                log.info("东方财富详情页访问成功");
                String jsonString = JSON.toJSONString(result.getBody());
                Object o = JSON.parseObject(StringEscapeUtils.unescapeJava(jsonString.substring(jsonString.indexOf("(") + 1, jsonString.length() - 3))).getJSONObject("result").getJSONArray("data").get(0);
                JSONObject jsonObject = JSON.parseObject(o.toString());
                // 利率 "第一年0.50%、第二年0.70%、第三年1.20%、第四年1.80%、第五年2.50%、第六年3.00%。"
                String interest_rate_explain = jsonObject.getString("INTEREST_RATE_EXPLAIN");
                String[] split = interest_rate_explain.split("%");

                // 到期日 "2026-06-10 00:00:00"
                String expire_date = jsonObject.getString("EXPIRE_DATE");
                // 付息日 "06-10"
                String pay_interest_day = jsonObject.getString("PAY_INTEREST_DAY");
                // 债券期限(年) 6
                int bond_expire = jsonObject.getIntValue("BOND_EXPIRE");

//                到期日的年份
                for (int i = 0; i < bond_expire; i++) {
                    Date date = DateUtils.parseDate(Integer.parseInt(expire_date.substring(0, 4)) - bond_expire + i + 1+ "-" + pay_interest_day, "yyyy-MM-dd");
//                    yearRateMap.put(date, new BigDecimal(split[i].substring(split[i].indexOf("年") + 2)));

                    BondRateInfo bondRateInfo = new BondRateInfo();
                    bondRateInfo.setBondCode(bondCode);
                    bondRateInfo.setPaymentDate(date);
                    if (split[i].contains("年为")) {
                        bondRateInfo.setRate(new BigDecimal(split[i].substring(split[i].indexOf("年") + 2)).multiply(Constants.DECIMAL_1000).intValue());
                    } else {
                        bondRateInfo.setRate(new BigDecimal(split[i].substring(split[i].indexOf("年") + 1)).multiply(Constants.DECIMAL_1000).intValue());
                    }
                    addBondRateInfoList.add(bondRateInfo);
                }
//                log.info("yearRateMap={}", JSON.toJSONString(yearRateMap));
            } else {
                log.error("东方财富详情页访问失败：{}", result);
            }
        } catch (Exception e) {
            log.error("catch东方财富详情页访问失败：{}", e.getMessage());
            e.printStackTrace();
        }

        return addBondRateInfoList;
    }

    // 联表查bond表中上市状态的 且在 BondRateInfo表中不存在
    private Set<String> getNotExistsCodeFromRateInfo() {
        Set<String> notExistsBondCodeSet = new HashSet<>();

        // 拆两个单表查
        QueryWrapper<BondRateInfo> bondRateInfoQueryWrapper = new QueryWrapper<>();
        bondRateInfoQueryWrapper.select("DISTINCT bond_code ");
        List<BondRateInfo> bondRateInfos = bondRateInfoDao.selectList(bondRateInfoQueryWrapper);

        LambdaQueryWrapper<BondBaseInfo> bondBaseInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondBaseInfoQueryWrapper.select(BondBaseInfo::getBondCode).eq(BondBaseInfo::getFlag, BusinessEnums.BondCodeFlag.上市.getFlag());
        List<BondBaseInfo> bondBaseInfoList = baseInfoDao.selectList(bondBaseInfoQueryWrapper);
        if (CollectionUtils.isEmpty(bondBaseInfoList)) {
            log.info("bond数据为空");
            return notExistsBondCodeSet;
        }

        if (CollectionUtils.isEmpty(bondRateInfos)) {
            log.info("bond付息数据为空");
            for (BondBaseInfo bondBaseInfo : bondBaseInfoList) {
                String bondCode = bondBaseInfo.getBondCode();
                notExistsBondCodeSet.add(bondCode);
            }
        } else {
            Set<String> bondCodeSetFromRateInfo = new HashSet<>();
            for (BondRateInfo bondRateInfo : bondRateInfos) {
                bondCodeSetFromRateInfo.add(bondRateInfo.getBondCode());
            }
            for (BondBaseInfo bondBaseInfo : bondBaseInfoList) {
                String bondCode = bondBaseInfo.getBondCode();
                if (!bondCodeSetFromRateInfo.contains(bondCode)) {
                    notExistsBondCodeSet.add(bondCode);
                }
            }
        }

        return notExistsBondCodeSet;
    }

}
