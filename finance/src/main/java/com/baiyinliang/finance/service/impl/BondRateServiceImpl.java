package com.baiyinliang.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baiyinliang.finance.entity.BondInfo;
import com.baiyinliang.finance.entity.BondRate;
import com.baiyinliang.finance.mapper.BondInfoDao;
import com.baiyinliang.finance.mapper.BondRateDao;
import com.baiyinliang.finance.service.BondRateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author root
 * @since 2023-03-14
 */
@Slf4j
@Service
public class BondRateServiceImpl extends ServiceImpl<BondRateDao, BondRate> implements BondRateService {

    @Autowired
    private BondInfoDao bondInfoDao;

    @Autowired
    private BondRateDao bondRateDao;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public List<BondRate> selectBondRateList() {
        return bondRateDao.selectBondRateList();
    }


    @Override
    public void setBondRate() {
        // 录入每只可转债的付息日及利息
//        ChromeDriver chromeDriver = DriverUtil.getChromeDriver();

        // 查询 rate表中没有的 才新增
        Set<Integer> bondIdSet = new HashSet<>();
        LambdaQueryWrapper<BondRate> bondRateQueryWrapper = new LambdaQueryWrapper<>();
        bondRateQueryWrapper.select(BondRate::getBondId).orderByAsc(BondRate::getBondId);
        List<BondRate> bondRates = bondRateDao.selectList(bondRateQueryWrapper);
        if (CollectionUtils.isEmpty(bondRates)) {

        } else {
            for (BondRate bondRate : bondRates) {
                bondIdSet.add(bondRate.getBondId());
            }
        }

        // 从 bondInfo 中 查询 在rate表中不存在的可转债
        Map<Integer, BondInfo> bondInfoMap = new HashMap<>();
        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.notIn(!CollectionUtils.isEmpty(bondIdSet), BondInfo::getBondId, bondIdSet);
        bondInfoQueryWrapper.eq(BondInfo::getFlag, 1);
        List<BondInfo> bondInfos = bondInfoDao.selectList(bondInfoQueryWrapper);
        for (BondInfo bondInfo : bondInfos) {
            bondInfoMap.put(bondInfo.getBondId(), bondInfo);
//            Map<Integer, Map<Integer, BigDecimal>> bondRateMap = getBondRate(chromeDriver, bondInfo.getBondId());
//            Map<Date, BigDecimal> bondYearRateMap = getBondRate(chromeDriver, bondInfo.getBondId());
            Map<Date, BigDecimal> bondYearRateMap = getBondRate2(bondInfo.getBondId());

            for (Map.Entry<Date, BigDecimal> entry : bondYearRateMap.entrySet()) {
                BondRate bondRate = new BondRate();
                bondRate.setBondId(bondInfo.getBondId());
                bondRate.setCreateTime(new Date());
                bondRate.setPaymentDate(entry.getKey());
                bondRate.setRate(entry.getValue());
                log.info("本次插入bondRate={}", JSON.toJSONString(bondRate));
                bondRateDao.insert(bondRate);
                log.info("插入成功");
            }
        }
//        chromeDriver.quit();
        log.info("全部插入完成");
    }


    @Override
    public Map<Date, BigDecimal> getBondRate2(Integer bondId) {
        String eastmoneyDetailUrl = "https://datacenter-web.eastmoney.com/api/data/v1/get?"
//                +"callback=jQuery112302665920256009826_"+System.currentTimeMillis()+"&reportName=RPT_BOND_CB_LIST&columns=ALL&quoteColumns=f2~01~CONVERT_STOCK_CODE~CONVERT_STOCK_PRICE%2Cf235~10~SECURITY_CODE~TRANSFER_PRICE%2Cf236~10~SECURITY_CODE~TRANSFER_VALUE%2Cf2~10~SECURITY_CODE~CURRENT_BOND_PRICE%2Cf237~10~SECURITY_CODE~TRANSFER_PREMIUM_RATIO%2Cf239~10~SECURITY_CODE~RESALE_TRIG_PRICE%2Cf240~10~SECURITY_CODE~REDEEM_TRIG_PRICE%2Cf23~01~CONVERT_STOCK_CODE~PBV_RATIO&quoteType=0&source=WEB&client=WEB&filter=(SECURITY_CODE=\"123054\")&_="+System.currentTimeMillis();
                + "callback=jQuery112302665920256009826_" + System.currentTimeMillis() + "&reportName=RPT_BOND_CB_LIST&columns=ALL&quoteColumns=&quoteType=0&source=WEB&client=WEB&filter=(SECURITY_CODE=\"" + bondId + "\")&_=" + System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//        headers.add(HttpHeaders.COOKIE, "compare_dish=show; kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=1699508886; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpqplqWppqCrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1699685351");
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43");
//        headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8");

        HashMap<Object, Object> params = new HashMap<>();
        params.put("reportName", "RPT_BOND_CB_LIST");
        params.put("columns", "ALL");
        params.put("quoteType", 0);
        params.put("source", "WEB");
        params.put("client", "WEB");
        params.put("filter", "(SECURITY_CODE=\"" + bondId + "\")");
//        params.put("quoteColumns", "f2~01~CONVERT_STOCK_CODE~CONVERT_STOCK_PRICE,f235~10~SECURITY_CODE~TRANSFER_PRICE,f236~10~SECURITY_CODE~TRANSFER_VALUE,f2~10~SECURITY_CODE~CURRENT_BOND_PRICE,f237~10~SECURITY_CODE~TRANSFER_PREMIUM_RATIO,f239~10~SECURITY_CODE~RESALE_TRIG_PRICE,f240~10~SECURITY_CODE~REDEEM_TRIG_PRICE,f23~01~CONVERT_STOCK_CODE~PBV_RATIO");
        params.put("callback", "jQuery112302665920256009826_" + System.currentTimeMillis());

        Map<Date, BigDecimal> yearRateMap = new HashMap<>();


        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = null;


        try {
            result = restTemplate.exchange(eastmoneyDetailUrl, HttpMethod.GET, entity, String.class);
            if (HttpStatus.OK.equals(result.getStatusCode())) {
                log.info("东方财富详情页访问成功");
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
                    Date date = DateUtils.parseDate(Integer.parseInt(expire_date.substring(0, 4)) - bond_expire + i + "-" + pay_interest_day, "yyyy-MM-dd");
                    yearRateMap.put(date, new BigDecimal(split[i].substring(split[i].indexOf("年") + 2)));
                }

                log.info("yearRateMap={}", JSON.toJSONString(yearRateMap));
            } else {
                log.error("东方财富详情页访问失败：{}", result);
            }
        } catch (Exception e) {
            log.error("东方财富详情页访问失败：{}", e.getMessage());
        }

        return yearRateMap;
    }

    @Deprecated
    private Map<Date, BigDecimal> getBondRate(ChromeDriver driver, Integer bondId) {
        driver.get("https://data.eastmoney.com/kzz/detail/" + bondId + ".html");
        //等待页面加载完成
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("当前打开页面的标题是： " + driver.getTitle() + " bondId=" + bondId);

        Date qxrDate = null;
        Date payDate = null;
        // 起息日
        WebElement qxrElement = driver.findElement(By.className("qxr"));
        // 每年付息日
        WebElement paydayElement = driver.findElement(By.className("payday"));
        // 利率说明
        WebElement ratedesElement = driver.findElement(By.className("ratedes"));
        try {
            qxrDate = DateUtils.parseDate(qxrElement.getText(), "yyyy-MM-dd");
            log.info("qxrElement.getText()={} qxrDate={}", qxrElement.getText(), qxrDate);
            payDate = DateUtils.parseDate(paydayElement.getText(), "MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String ratedesElementText = ratedesElement.getText();
        if (StringUtils.isBlank(ratedesElementText)) {
            log.warn("id={}的可转债的利率说明为空", bondId);
        } else {
            String newRatedesElementText = null;
//            本次发行的可转债票面利率设定为:第一年0.30%、第二年0.50%、第三年1.00%、第四年1.50%、第五年1.80%、第六年2.00%。
            String[] split = new String[6];
            if (ratedesElementText.contains(",到期赎回价为")) {
                newRatedesElementText = ratedesElementText.substring(0, ratedesElementText.indexOf(",到期赎回价为"));
            } else {
                newRatedesElementText = ratedesElementText;
            }
            log.info("newRatedesElementText={}", newRatedesElementText);
            if (newRatedesElementText.contains(",")) {
                split = newRatedesElementText.split(",");
            } else if (newRatedesElementText.contains("、")) {
                split = newRatedesElementText.split("、");
            }

            log.warn("id={}的可转债的利率说明特殊情况={}", bondId, newRatedesElementText);
            Map<Date, BigDecimal> yearRateMap = new HashMap<>();
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (i >= 6) {
                    break;
                }
                log.info("s={}", s);
                if (s.endsWith("%") || s.endsWith("。")) {
                    String rateStr = "";
                    if (s.contains(".")) {
                        rateStr = s.substring(s.indexOf(".") - 1, s.indexOf("%"));
                    } else {
                        rateStr = s.substring(s.indexOf("%") - 1, s.indexOf("%"));

                    }
                    log.info("rateStr={}", rateStr);
                    yearRateMap.put(DateUtils.addYears(qxrDate, i + 1), new BigDecimal(rateStr));
                }
            }
//                Map<Integer, Map<Integer, BigDecimal>> bondRateMap = new HashMap<>();
//                bondRateMap.put(bondId, yearRateMap);
            log.info("yearRateMap={}", JSON.toJSONString(yearRateMap));
            return yearRateMap;
        }

        return null;
    }


}
