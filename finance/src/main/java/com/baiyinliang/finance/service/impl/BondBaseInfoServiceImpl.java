package com.baiyinliang.finance.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baiyinliang.finance.entity.*;
import com.baiyinliang.finance.enums.BusinessEnums;
import com.baiyinliang.finance.mapper.*;
import com.baiyinliang.finance.request.BondInfoListReq;
import com.baiyinliang.finance.service.BondBaseInfoService;
import com.baiyinliang.finance.tools.NumberUtil;
import com.baiyinliang.finance.tools.ThreadPoolUtils;
import com.baiyinliang.finance.vo.BondInfoVO;
import com.baiyinliang.finance.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.baiyinliang.finance.common.Constants.DECIMAL_100;
import static com.baiyinliang.finance.common.Constants.DECIMAL_1000;

/**
 * <p>
 * 可转债基本信息表，除了状态，其它数据不变 服务实现类
 * </p>
 *
 * @author root
 * @since 2023-11-25
 */
@Service
@Slf4j
public class BondBaseInfoServiceImpl extends ServiceImpl<BondBaseInfoDao, BondBaseInfo> implements BondBaseInfoService {


    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private BondBaseInfoDao baseInfoDao;
    @Autowired
    private BondRateInfoDao bondRateInfoDao;
    @Autowired
    private BondRatingCdDao bondRatingCdDao;
    @Autowired
    private BondPriceInfoDao bondPriceInfoDao;
    @Autowired
    private BondAmtInfoDao bondAmtInfoDao;
    @Autowired
    private CommonServiceImpl commonServiceImpl;


    // 方法拆分，这里只保存基本数据
    @Override
    public void saveBondBaseInfoList() {
        // 收集基本数据
        Map<String, Integer> bondCodePrefixMap = new HashMap<>();
        Map<String, Integer> bondFlagMap = new HashMap<>();
//        查询数据库中的所有bond数据
        getBondBaseInfoMapData(bondCodePrefixMap, bondFlagMap);

        List<BondBaseInfo> addBondBaseInfoList = new ArrayList<>();
        List<String> delist = new ArrayList<>();
        // 循环页面数据
        collectWebBondBaseInfo(bondCodePrefixMap, bondFlagMap, addBondBaseInfoList, delist);

        // 调用事务访求
        ((BondBaseInfoServiceImpl) AopContext.currentProxy()).afterProcess(addBondBaseInfoList, delist);
    }

    @Transactional
    public void afterProcess(List<BondBaseInfo> addBondBaseInfoList, List<String> delist) {
        // 保存 及退市
        if (!CollectionUtils.isEmpty(delist)) {
            LambdaUpdateWrapper<BondBaseInfo> bondBaseInfoUpdateWrapper = new LambdaUpdateWrapper<>();
            bondBaseInfoUpdateWrapper.set(BondBaseInfo::getFlag, BusinessEnums.BondCodeFlag.退市.getFlag())
                    .set(BondBaseInfo::getUpdateTime, new Date()).set(BondBaseInfo::getCreateTime, new Date())
                    .in(BondBaseInfo::getBondCode, delist);
            int updateCount = baseInfoDao.update(null, bondBaseInfoUpdateWrapper);
            log.info("本次退市条数：{}", updateCount);
        } else {
            log.info("本次无退市");
        }

        if (!CollectionUtils.isEmpty(addBondBaseInfoList)) {
            int insertCount = baseInfoDao.batchInsert(addBondBaseInfoList);
            log.info("本次新增条数：{}", insertCount);

        } else {
            log.info("本次无新增");
        }
    }


    /**
     * 查询数据库中的所有bond数据
     *
     * @param bondCodePrefixMap 110key 123value 倒序取最大的值
     * @param bondFlagMap       每个bond的状态
     */
    private void getBondBaseInfoMapData(Map<String, Integer> bondCodePrefixMap, Map<String, Integer> bondFlagMap) {
        // 查询表中不同前缀最大的code
        LambdaQueryWrapper<BondBaseInfo> bondBaseInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondBaseInfoQueryWrapper.select(BondBaseInfo::getBondCode, BondBaseInfo::getFlag).orderByDesc(BondBaseInfo::getBondCode);
        List<BondBaseInfo> bondBaseInfoList = baseInfoDao.selectList(bondBaseInfoQueryWrapper);
        if (CollectionUtils.isEmpty(bondBaseInfoList)) {
            log.warn("可转债基本数据表为空");
            return;
        }

        for (BondBaseInfo bondBaseInfo : bondBaseInfoList) {
            String bondCode = bondBaseInfo.getBondCode();
            String bondCodePrefix = bondCode.substring(0, 3);
            bondCodePrefixMap.putIfAbsent(bondCodePrefix, Integer.parseInt(bondCode.substring(3)));
            bondFlagMap.put(bondCode, bondBaseInfo.getFlag());
        }
//        log.info("bondCodeMap={}", JSON.toJSONString(bondCodePrefixMap));
    }

    @Override
    public void test() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        CountDownLatch latch = new CountDownLatch(5);
//        ExecutorService service = Executors.newFixedThreadPool(5);

        long beginTime = System.currentTimeMillis();
        log.info("收集基本数据开始---------");
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            ThreadPoolUtils.execute(() -> {
                try {
                    System.out.println("finalI = " + finalI);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }, 5);
            /*Runnable runnable = new Runnable(){
                @Override
                public void run() {
                    try {
                        System.out.println("finalI = " + finalI);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            };
            service.submit(runnable);*/
        }
        try {
            latch.await();
            log.info("结束了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 遍历深市和沪市的甩有bond，根据code遍历，并获取要退市的数据 和新增的数据
     *
     * @param bondCodeMap
     * @param bondFlagMap
     * @param addBondBaseInfoList 待新增的
     * @param delist              待退市的
     */
    private void collectWebBondBaseInfo
    (Map<String, Integer> bondCodeMap, Map<String, Integer> bondFlagMap, List<BondBaseInfo> addBondBaseInfoList, List<String> delist) {
        String bondInfoUrl = StringUtils.EMPTY;
        String bondInfoUrlPart = "https://www.jisilu.cn/data/convert_bond_detail/";


//        SnowFlake snowFlake = new SnowFlake(1, 1);
        List<String> szCodeList = BusinessEnums.BondMarketEnum.SZ_MARKET.getCodeList();
        List<String> shCodeList = BusinessEnums.BondMarketEnum.SH_MARKET.getCodeList();
        ArrayList<String> allList = new ArrayList<>();
        allList.addAll(shCodeList);
        allList.addAll(szCodeList);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int size = allList.size();
        CountDownLatch latch = new CountDownLatch(size);
        long beginTime = System.currentTimeMillis();
        log.info("收集基本数据开始---------");
        for (String prefix : allList) {
            ThreadPoolUtils.execute(() -> {
                // 可转债编码，110开头的话 最小是多少 先给个固定值110100，判断如果是
                // 使用集思路判断 是否已退市，退市的如果在表中存在，则更新状态，

                // 3种状态判断，1：已退市 2：不存在（集思录 该转债不存在） 3：未上市（集思录 上市日为空）
                // 先查询表中最大的编码是多少+1,然后遍历,如果表中没有则插入表，如果有则通过集思录判断状态，更新状态
                // 初始值为
//            int defaultCode = BusinessEnums.DefaultBondCode.getCodeByPrefix(prefix) + 1;
                try {
                    Integer bondCodeMaxVal = bondCodeMap.getOrDefault(prefix, BusinessEnums.DefaultBondCode.getCodeByPrefix(prefix) + 1);
                    for (int i = 0; i <= bondCodeMaxVal; i++) {
                        String bondCode = prefix.concat(String.format("%03d", i));
                        String concatUrl = bondInfoUrlPart.concat(bondCode);
                        log.info("集思录详情地址：{}", concatUrl);
                        //                访问数据请求
                        ResponseEntity<String> result = getBondBaseInfoWeb(concatUrl);
                        if (HttpStatus.OK.equals(result.getStatusCode())) {
//                            log.info("详情页访问成功");
                        } else {
                            log.error("详情页访问失败：{}", result);
                            continue;
                        }

                        String body = result.getBody();
                        assert body != null;

                        // 该转债不存在！
                        String notExistsStr = "该转债不存在！";
                        if (body.contains(notExistsStr)) {
                            log.warn(notExistsStr);
                            continue;
                        }

                        // 判断是否已退市  // todo 放for循环外面
                        String delistStr = "已退市";
                        if (body.contains(delistStr)) {
                            if (bondFlagMap.get(bondCode) != null && BusinessEnums.BondCodeFlag.退市.getFlag() != bondFlagMap.get(bondCode)) {
                                // 放进集合，批量更新表数据为已退市
                                delist.add(bondCode);
                            }
                            log.warn(delistStr);
                            continue;
                        }


                        BondBaseInfo bondBaseInfo = new BondBaseInfo();
                        if (!bondFlagMap.containsKey(bondCode)) {
//                            bondBaseInfo.setId(snowFlake.nextId());
                            bondBaseInfo.setBondCode(bondCode);
                            // 待新增的需要下面方法
                            boolean exists = getBondBaseInfoFromJSL(body, bondBaseInfo);
                            if (exists) {
                                // 放进待添加集合
                                addBondBaseInfoList.add(bondBaseInfo);
                                bondBaseInfo.setPrice(DECIMAL_100.intValue());
                                //访问雪球页面获取到期赎回价
                                getRedeemPrice(bondCode, bondBaseInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                    log.info("latch.getCount():" + latch.getCount());
                }

            }, size);
        }
        try {
            latch.await();
            log.info("结束了");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("收集基本数据结束---------共耗时：{}", System.currentTimeMillis() - beginTime);

    }

    /**
     * 获取集思录页面数据，即bond详情
     *
     * @param body
     * @param bondBaseInfo
     */
    private boolean getBondBaseInfoFromJSL(String body, BondBaseInfo bondBaseInfo) {
//        String bondInfoUrl = "https://www.jisilu.cn/data/convert_bond_detail/" + bondBaseInfo.getBondCode();

        String bondCode = bondBaseInfo.getBondCode();

        // 上市日
        String listDtStr = "id=\"list_dt\">";
        int listDtIndex = body.indexOf(listDtStr);
        String listDtSubstr = body.substring(listDtIndex + listDtStr.length());
        String listDtValStr = listDtSubstr.substring(0, listDtSubstr.indexOf("</td>"));
        if (StringUtils.isNotBlank(listDtValStr) && !listDtValStr.equals("-")) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date listDt = null;
            try {
                listDt = simpleDateFormat.parse(listDtValStr.trim());
                // 判断上市日期是否晚于今天
                if (DateUtil.compare(listDt, new Date(), "yyyy-MM-dd") > 0) {
                    log.warn("{}未上市，暂不处理", bondCode);
                    return false;
                }
                bondBaseInfo.setListDt(listDt);
            } catch (ParseException e) {
                log.error("上市日获取失败 listDtValStr={}", listDtValStr);
                e.printStackTrace();
            }
        } else {
            log.warn("{}未上市，暂不处理", bondCode);
            return false;
        }

        String title = "<title>";
        int titleIndex = body.indexOf(title);
        String titleSubStr = body.substring(titleIndex + title.length());
        String titleValStr = titleSubStr.substring(0, titleSubStr.indexOf(" "));
//        String bondNm = body.substring(titleIndex + title.length(), titleIndex + title.length() + 4);
//        log.info("bondNm=" + bondNm);
        bondBaseInfo.setBondNm(titleValStr.trim());


//                到期日
        String maturityDtStr = "id=\"maturity_dt\" nowrap>";
        int maturityDtIndex = body.indexOf(maturityDtStr);
        String maturityDtSubstr = body.substring(maturityDtIndex + maturityDtStr.length());
        String maturityDtValStr = maturityDtSubstr.substring(0, maturityDtSubstr.indexOf("</td>"));
        if (StringUtils.isNotBlank(maturityDtValStr)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date maturityDt = null;
            try {
                maturityDt = simpleDateFormat.parse(maturityDtValStr.trim());
                bondBaseInfo.setMaturityDt(maturityDt);
            } catch (ParseException e) {
                log.error("到期日获取失败 maturityDtValStr={}", maturityDtValStr);
                e.printStackTrace();
            }
        }

//                发行规模(亿)
        String origIssAmt = "id=\"orig_iss_amt\" title=\"按转债面值计算，亿元\">";
        int origIssAmtIndex = body.indexOf(origIssAmt);
        String origIssAmtSubstr = body.substring(origIssAmtIndex + origIssAmt.length());
        String origIssAmtValStr = origIssAmtSubstr.substring(0, origIssAmtSubstr.indexOf("</td>"));
        BigDecimal origIssAmtVal = null;
        try {
            origIssAmtVal = new BigDecimal(origIssAmtValStr).multiply(DECIMAL_1000);
            bondBaseInfo.setOrigIssAmt(origIssAmtVal.longValue());
        } catch (Exception e) {
            log.error("发行规模(亿)获取失败 origIssAmtValStr={}", origIssAmtValStr);
            e.printStackTrace();
        }

        log.info("bondBaseInfo={}", JSON.toJSONString(bondBaseInfo));
//        return bondBaseInfo;

        return true;
    }


    /**
     * 访问数据请求
     *
     * @param bondInfoUrl
     * @return
     */
    private ResponseEntity<String> getBondBaseInfoWeb(String bondInfoUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(HttpHeaders.COOKIE, "compare_dish=show; kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=1699508886; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpqplqWppqCrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis());
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43");
        headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8");

        HashMap<Object, Object> params = new HashMap<>();
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = restTemplate.exchange(bondInfoUrl, HttpMethod.GET, entity, String.class);
        return result;
    }


    /**
     * 访问雪球页面获取到期赎回价
     *
     * @param bondCode
     * @param bondBaseInfo
     */
    private void getRedeemPrice(String bondCode, BondBaseInfo bondBaseInfo) {

        String xueqiuBondDetailUrl = null;
        if (bondCode.startsWith("12")) {
            xueqiuBondDetailUrl = "https://xueqiu.com/S/sz" + bondCode;
        } else if (bondCode.startsWith("11")) {
            xueqiuBondDetailUrl = "https://xueqiu.com/S/sh" + bondCode;
//            xueqiuBondDetailUrl = "https://xueqiu.com/S/sh" + (bondCode.startsWith("11")? "sh":"sz") + bondCode;
        }

        log.info("访问雪球页面获取到期赎回价，url={}", xueqiuBondDetailUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//        headers.add(HttpHeaders.COOKIE, "xq_a_token=cf755d099237875c767cae1769959cee5a1fb37c; xqat=cf755d099237875c767cae1769959cee5a1fb37c; xq_r_token=e073320f4256c0234a620b59c446e458455626d9; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTcwMTk5NTg4MCwiY3RtIjoxNjk5Njk3MDExMzA3LCJjaWQiOiJkOWQwbjRBWnVwIn0.bgjWeA3aKpL3t8UatYIgxEjhTdAwb9PGpG3p4V0udPqni0kRHqq6n90Fx-LovLcrMWJyIhRmkqJFrhkzqSKESgw3Y6zUyx5vnvSrw8Ajz6QKlUHau9LDN9jYMF97qBesMbjcpDu6ygy6x3eaoITjLCXE0cmaC7RJHQgxTDR8zvAJ18C8sTenaEUV6VE11_dRL-8OsvXMhM1OkRuoYJWCLIwlAoewIg8p0RfrjCt254YmiB-M3vk9Rfx9u5sDbEmnfJWNUT3PUX6rfwGqgENi0E_QupfVE89psAm-XRhAHIDJiASj04s3utYUPtzN0fGRzfJyypFVm22ziVVheafxMA; cookiesu=921699697061450; u=921699697061450; Hm_lvt_1db88642e346389874251b5a1eded6e3=1699697060; device_id=1a375e35f186bff0e577cc75d781a0af; s=af12g4bgex; __utma=1.1194915567.1699697069.1699697069.1699697069.1; __utmc=1; __utmz=1.1699697069.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lpvt_1db88642e346389874251b5a1eded6e3=1699697081; acw_tc=2760827d16997558965437479e75b191de6c29df1bab2460f5097d7f2fa460");
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
                return; //可能是已退市，但集思录页面慢未抓取
            }

            body = result.getBody();
            assert body != null;

            String redeemPriceStr = "到期赎回价：<span>";
            int redeemPriceIndex = body.indexOf(redeemPriceStr);
            String redeemPriceStrSubstring = body.substring(redeemPriceIndex + redeemPriceStr.length());
            String redeemPriceValueStr = redeemPriceStrSubstring.substring(0, redeemPriceStrSubstring.indexOf("</span>"));
            bondBaseInfo.setRedeemPrice(new BigDecimal(redeemPriceValueStr).multiply(DECIMAL_1000).intValue());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("雪球详情页访问失败：{}, body={}", e.getCause(), body);
        }
    }


    @Override
    public PageVO<BondInfoVO> getInterestMarginList(BondInfoListReq req) {
        List<BondInfoVO> result = new ArrayList<>();
        PageVO<BondInfoVO> pageVO = new PageVO<>();
        pageVO.setPageNum(req.getPageNum());
        pageVO.setPageSize(req.getPageSize());
        pageVO.setTotal(0);
        pageVO.setHasNext(false);

        List<BondInfoVO> bondInfoVOList = new ArrayList<>();

        // 参数查询
        String[] market = req.getMarket();
        String param = req.getParam();
        boolean shMarket = false,szMarket = false;
        if (market.length > 0) {
            for (String m : market) {
                if (StringUtils.isBlank(m)) {
                    continue;
                }

                if ("sh".equals(m.toLowerCase())) {
                    shMarket = true;
                } else if ("sz".equals(m.toLowerCase())) {
                    szMarket = true;
                }
            }
        }

        List<BondBaseInfo> listBondBaseInfo = new ArrayList<>();
        if((shMarket==szMarket)  && StringUtils.isBlank(param)){
            listBondBaseInfo = commonServiceImpl.getListBondBaseInfo();
        }else {
            listBondBaseInfo =  baseInfoDao.selectBaseInfoListByParams(param, shMarket,szMarket);
        }
        if (CollectionUtils.isEmpty(listBondBaseInfo)) {
            log.info("bond数据为空");
            return pageVO;
        }


        // 编码集合
        Set<String> bondCodeSet = listBondBaseInfo.stream().map(BondBaseInfo::getBondCode).collect(Collectors.toSet());
        // 现价 最新一天的收盘价
        Map<String, Integer> currPriceMap = new HashMap<>();
        // 当前税后收益率
        Map<String, Integer> incomeMap = new HashMap<>();
        // 获取价格数据
        Map<String, List<BondInfoVO.BondPriceRange>> bondPriceRangeMap = getPriceRangeMap(bondCodeSet, currPriceMap, incomeMap);
        // 剩余规模
        Map<String, Integer> bondAmtInfoMap = getAmtInfoMap(bondCodeSet);
        // 付息数据
        Map<String, BondRateInfo> bondRateInfoMap = getBondRateInfoMap(bondCodeSet);

        Date today = formatCurrDate();
        listBondBaseInfo.forEach((info) -> {
            String bondCode = info.getBondCode();
            BondInfoVO bondInfoVO = new BondInfoVO();
            bondInfoVO.setBondCode(bondCode);
            bondInfoVO.setBondNm(info.getBondNm());
            bondInfoVO.setPrice(NumberUtil.subRemainder(currPriceMap.getOrDefault(bondCode, 0), 3));
            bondInfoVO.setRedeemPrice(NumberUtil.subRemainder(info.getRedeemPrice()));
            bondInfoVO.setListDt(info.getListDt());
            bondInfoVO.setMaturityDt(info.getMaturityDt());
            bondInfoVO.setCurrIssAmt(NumberUtil.subRemainder(bondAmtInfoMap.getOrDefault(bondCode, 0), 3)); // 剩余规模
            bondInfoVO.setEarningsPrice(NumberUtil.subRemainder(info.getRedeemPrice() - currPriceMap.getOrDefault(bondCode, 0), 3));
            bondInfoVO.setIncomeRt(NumberUtil.subRemainder(incomeMap.getOrDefault(bondCode, 0)));
            // 前n日价格 暂不使用
//            processBondPrice(bondPriceRangeMap, bondCode, bondInfoVO);
            // 临近付息日
            BondRateInfo bondRate = bondRateInfoMap.getOrDefault(bondCode, new BondRateInfo());
//            log.info("{}", bondCode);
            Date paymentDate = bondRate.getPaymentDate();
            bondInfoVO.setCurrentPeriodPayInterestDate(paymentDate);
            bondInfoVO.setCurrentPeriodRate(NumberUtil.subRemainder(bondRate.getRate()));
            if (paymentDate.before(today)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.PAID.getCode());
            } else if (paymentDate.after(today)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.NON_PAYMENT.getCode());
            } else {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.CURRENT.getCode());
            }


            bondInfoVOList.add(bondInfoVO);
//            log.info("bondInfoVO={}", JSON.toJSONString(bondInfoVO));
        });

        String prop = req.getProp();
        String sort = req.getSort();
        if (StringUtils.isBlank(sort)) {
            sort = "asc";
        }
        if (StringUtils.isBlank(prop)) {
            // 默认按付息日期排序
            if ("desc".equals(sort.trim())) {
                bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getCurrentPeriodPayInterestDate).reversed());
            } else {
                bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getCurrentPeriodPayInterestDate));
            }
        } else {
            String propStr = prop.trim();
            switch (propStr) {
                case "price":
                    // 按当前价格排序
                    if ("desc".equals(sort.trim())) {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getPrice).reversed());
                    } else {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getPrice));
                    }
                    break;
                case "earningsPrice":
                    if ("desc".equals(sort.trim())) {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getEarningsPrice).reversed());
                    } else {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getEarningsPrice));
                    }
                    break;
                case "PayInterestDate":
                    if ("desc".equals(sort.trim())) {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getCurrentPeriodPayInterestDate).thenComparing(BondInfoVO::getCurrentPeriodRate).reversed());
                    } else {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getCurrentPeriodPayInterestDate).thenComparing(BondInfoVO::getCurrentPeriodRate));
                    }
                    break;
                case "redeemPrice":
                    if ("desc".equals(sort.trim())) {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getRedeemPrice).thenComparing(BondInfoVO::getEarningsPrice).reversed());
                    } else {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getRedeemPrice).thenComparing(BondInfoVO::getEarningsPrice));
                    }
                    break;
                case "currIssAmt":
                    if ("desc".equals(sort.trim())) {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getCurrIssAmt).reversed());
                    } else {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getCurrIssAmt));
                    }
                    break;
                case "maturityDt":
                    if ("desc".equals(sort.trim())) {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getMaturityDt).thenComparing(BondInfoVO::getEarningsPrice).reversed());
                    } else {
                        bondInfoVOList.sort(Comparator.comparing(BondInfoVO::getMaturityDt).thenComparing(BondInfoVO::getEarningsPrice));
                    }
                    break;
            }
        }

        result = bondInfoVOList.stream().skip((req.getPageNum() - 1) * req.getPageSize()).limit(req.getPageSize()).collect(Collectors.toList());
        pageVO.setHasNext(req.getPageNum() * req.getPageSize() < bondInfoVOList.size());
        pageVO.setTotal(bondInfoVOList.size());
        pageVO.setData(result);
        return pageVO;
    }

    private Map<String, BondRateInfo> getBondRateInfoMap(Set<String> bondCodeSet) {
        Map<String, BondRateInfo> bondRateInfoMap = new HashMap<>();
        List<BondRateInfo> bondRateInfos = bondRateInfoDao.selectRecentlyBondRateList(bondCodeSet);

        if (CollectionUtils.isEmpty(bondRateInfos)) {
            log.warn("付息数据为空");
        } else {
            bondRateInfoMap = bondRateInfos.stream().collect(Collectors.toMap(BondRateInfo::getBondCode, Function.identity(), (x1, x2) -> x2));
        }
        return bondRateInfoMap;
    }

    private void processBondPrice(Map<String, List<BondInfoVO.BondPriceRange>> bondPriceRangeMap, String bondCode, BondInfoVO bondInfoVO) {
        List<BondInfoVO.BondPriceRange> bondPriceRangeList = bondPriceRangeMap.getOrDefault(bondCode, new ArrayList<>());
        if (CollectionUtils.isEmpty(bondPriceRangeList)) {
            log.info("{} 没有每日价格", bondCode);
        } else {
            BigDecimal minBondPrice = BigDecimal.ZERO;
            BigDecimal maxBondPrice = BigDecimal.ZERO;
            for (BondInfoVO.BondPriceRange priceRange : bondPriceRangeList) {
                BigDecimal minPrice = priceRange.getMinPrice();
                BigDecimal maxPrice = priceRange.getMaxPrice();
                if (minBondPrice.equals(BigDecimal.ZERO)) {
                    minBondPrice = minPrice;
                } else if (minBondPrice.compareTo(minPrice) > 0) {
                    minBondPrice = minPrice;
                }
                if (maxBondPrice.equals(BigDecimal.ZERO)) {
                    maxBondPrice = maxPrice;
                } else if (maxBondPrice.compareTo(maxPrice) < 0) {
                    maxBondPrice = maxPrice;
                }
            }

            for (BondInfoVO.BondPriceRange priceRange : bondPriceRangeList) {
                if (minBondPrice.compareTo(priceRange.getMinPrice()) == 0) {
                    priceRange.setMinFlag(true);
                }
                if (maxBondPrice.compareTo(priceRange.getMaxPrice()) == 0) {
                    priceRange.setMaxFlag(true);
                }
            }

            setPriceRange(bondPriceRangeList, bondInfoVO);
        }
    }

    private Map<String, Integer> getAmtInfoMap(Set<String> bondCodeSet) {
        Map<String, Integer> bondAmtInfoMap = new HashMap<>();
        List<BondAmtInfo> bondAmtInfoList = bondAmtInfoDao.selectLatestAmtList(bondCodeSet);
        if (CollectionUtils.isEmpty(bondAmtInfoList)) {
            log.warn("剩余规模数据为空");
        } else {
            bondAmtInfoMap = bondAmtInfoList.stream().collect(Collectors.toMap(BondAmtInfo::getBondCode, BondAmtInfo::getCurrIssAmt, (x1, x2) -> x2));
        }

        return bondAmtInfoMap;
    }

    private Map<String, List<BondInfoVO.BondPriceRange>> getPriceRangeMap(Set<String> bondCodeSet, Map<String, Integer> currPriceMap, Map<String, Integer> incomeMap) {
        Map<String, List<BondInfoVO.BondPriceRange>> bondPriceRangeMap = new HashMap<>();
        List<BondPriceInfo> bondPriceInfos = bondPriceInfoDao.selectBondPriceInfoPeriod(5, bondCodeSet);
        if (CollectionUtils.isEmpty(bondPriceInfos)) {
            log.warn("最近5天价格数据为空");
        } else {
            bondPriceInfos.forEach((item) -> {
                String bondCode = item.getBondCode();

                // 若有则不放
                currPriceMap.putIfAbsent(bondCode, item.getCurrentPrice());
                // 税后收益率
                incomeMap.putIfAbsent(bondCode, item.getIncome());

                List<BondInfoVO.BondPriceRange> priceRangeList = bondPriceRangeMap.getOrDefault(bondCode, new ArrayList<>());
                BigDecimal currMinPrice = NumberUtil.subRemainder(item.getMinPrice(), 3);
                BigDecimal currMaxPrice = NumberUtil.subRemainder(item.getMaxPrice(), 3);
                BondInfoVO.BondPriceRange bondPriceRange = new BondInfoVO.BondPriceRange();
                bondPriceRange.setMinPrice(currMinPrice);
                bondPriceRange.setMaxPrice(currMaxPrice);
                priceRangeList.add(bondPriceRange);
                bondPriceRangeMap.put(bondCode, priceRangeList);
            });
        }
        return bondPriceRangeMap;
    }

    private Date formatCurrDate() {
        Date parseDate = null;
        try {
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            parseDate = sdf.parse(sdf.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return parseDate;
    }

    private void setPriceRange(List<BondInfoVO.BondPriceRange> bondPriceRangeList, Object obj) {
        try {
//            Collections.reverse(bondPriceRangeList);
            for (int i = 0; ; i++) {
                Field declaredField = obj.getClass().getDeclaredField("priceRange" + (i + 1));
                if (bondPriceRangeList.size() == i) {
                    break;
                }
                declaredField.setAccessible(true);
                declaredField.set(obj, bondPriceRangeList.get(i));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
//            log.info("obj={}", JSON.toJSONString(obj));
//            e.printStackTrace();
        }
    }


    @Override
    public BondInfoVO getBondInfo(String bondCode) {
        BondInfoVO bondInfoVO = new BondInfoVO();
        if (StringUtils.isBlank(bondCode)) {
            log.error("查询转债详情失败，参数编码为空");
            return bondInfoVO;
        }

        // 判断 flag 状态
        LambdaQueryWrapper<BondBaseInfo> baseInfoQueryWrapper = new LambdaQueryWrapper<>();
        baseInfoQueryWrapper.select(BondBaseInfo::getBondCode, BondBaseInfo::getBondNm, BondBaseInfo::getListDt, BondBaseInfo::getMaturityDt
                , BondBaseInfo::getOrigIssAmt, BondBaseInfo::getPrice, BondBaseInfo::getRedeemPrice, BondBaseInfo::getFlag).eq(BondBaseInfo::getBondCode, bondCode);
        List<BondBaseInfo> bondBaseInfos = baseInfoDao.selectList(baseInfoQueryWrapper);
        if (CollectionUtils.isEmpty(bondBaseInfos)) {
            log.error("{}的bond不存在", bondCode);
            return bondInfoVO;
        }
        BondBaseInfo bondInfo = bondBaseInfos.get(0);
        if (bondInfo == null) {
            log.error("查询转债详情失败，参数编码={}的转债数据不存在", bondCode);
            return bondInfoVO;
        }

        bondInfoVO.setBondCode(bondCode);
        bondInfoVO.setBondNm(bondInfo.getBondNm());
//        bondInfoVO.setStockNm(bondInfo.getStockNm());
        // 转债评级
        bondInfoVO.setRatingCd(getBondRatingCd(bondCode));
        // 剩余规模
        bondInfoVO.setCurrIssAmt(getCurrIssAmt(bondCode));
//        上市日期
        bondInfoVO.setListDt(bondInfo.getListDt());
//        到期日期
        bondInfoVO.setMaturityDt(bondInfo.getMaturityDt());
//        现价
        BondPriceInfo latestBondPriceInfo = getSingletonLatestBondPriceInfo(bondCode);
        if (latestBondPriceInfo.getCurrentPrice() != null) {
            bondInfoVO.setPrice(NumberUtil.subRemainder(latestBondPriceInfo.getCurrentPrice(), 3));
        } else {
            bondInfoVO.setCurrentPeriodRate(NumberUtil.subRemainder(0));
        }
//        到期赎回价
        bondInfoVO.setRedeemPrice(NumberUtil.subRemainder(bondInfo.getRedeemPrice()));
        BondRateInfo latestBondRateInfo = getSingletonLatestBondRateInfo(bondCode);
        // 付息日
        bondInfoVO.setCurrentPeriodPayInterestDate(latestBondRateInfo.getPaymentDate());
        if (latestBondRateInfo.getRate() != null) {
            // 当前利率
            bondInfoVO.setCurrentPeriodRate(NumberUtil.subRemainder(latestBondRateInfo.getRate()));
        } else {
            bondInfoVO.setCurrentPeriodRate(NumberUtil.subRemainder(0));
        }

        // todo 小程序 画折线图
//        log.info("bondInfoVO详情={}", JSON.toJSONString(bondInfoVO));
        return bondInfoVO;
    }


    /**
     * 获取付息数据
     *
     * @param bondCode
     * @return
     */
    private BondRateInfo getSingletonLatestBondRateInfo(String bondCode) {
        BondRateInfo bondRateInfo = bondRateInfoDao.selectLatestRateInfo(bondCode);
        if (bondRateInfo == null) {
            log.warn("{}的付息数据不存在", bondCode);
            return new BondRateInfo();
        }
        return bondRateInfo;
    }


    /**
     * 获取价格数据
     *
     * @param bondCode
     * @return
     */
    private BondPriceInfo getSingletonLatestBondPriceInfo(String bondCode) {
        BondPriceInfo bondPriceInfo = bondPriceInfoDao.selectLatestPrice(bondCode);
        if (bondPriceInfo == null) {
            log.warn("{}的价格数据不存在", bondCode);
            return new BondPriceInfo();
        }
        return bondPriceInfo;
    }

    /**
     * 获取剩余规模
     *
     * @param bondCode
     * @return
     */
    private BigDecimal getCurrIssAmt(String bondCode) {
        BondAmtInfo bondAmtInfo = bondAmtInfoDao.selectLatestAmt(bondCode);
        if (bondAmtInfo == null || bondAmtInfo.getCurrIssAmt() == null) {
            log.warn("{}的剩余规模数据为空", bondCode);
            return NumberUtil.subRemainder(0, 3);
        }
        return NumberUtil.subRemainder(bondAmtInfo.getCurrIssAmt(), 3);
    }

    /**
     * 查询评级数据
     *
     * @param bondCode
     * @return
     */
    private String getBondRatingCd(String bondCode) {
        BondRatingCd bondRatingCd = bondRatingCdDao.selectLatestBondRatingCd(bondCode);
        if (bondRatingCd == null) {
            log.warn("{}的评级数据不存在", bondCode);
        } else {
            return bondRatingCd.getRatingCd();
        }

        return "--";
    }


//    ---------------------------------------------------------------------------------------------

    public void logic() {
        // 查询基本表的数据
        List<BondBaseInfo> bondBaseInfoList = getBondBaseInfoList();
        if (CollectionUtils.isEmpty(bondBaseInfoList)) {
            log.warn("可转债基本数据表为空");
            return;
        }
        // 处理查询到的数据
        Map<String, Integer> bondFlagMap = new HashMap<>();
        Map<String, Integer> bondCodeMap = new HashMap<>();
        processBondBaseInfoList(bondBaseInfoList, bondFlagMap, bondCodeMap);
        // 表中没有的转债数据需要插入

        // 表中有的需要更新状态、价格
    }

    private List<BondBaseInfo> getBondBaseInfoList() {
        LambdaQueryWrapper<BondBaseInfo> bondBaseInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondBaseInfoQueryWrapper.select(BondBaseInfo::getBondCode, BondBaseInfo::getFlag).orderByDesc(BondBaseInfo::getBondCode);
        return baseInfoDao.selectList(bondBaseInfoQueryWrapper);
    }

    private void processBondBaseInfoList
            (List<BondBaseInfo> bondBaseInfoList, Map<String, Integer> bondFlagMap, Map<String, Integer> bondCodeMap) {
//        Map<String, Integer> bondFlagMap = new HashMap<>();
//        Map<String, Integer> bondCodeMap = new HashMap<>();
        for (BondBaseInfo bondBaseInfo : bondBaseInfoList) {
            String bondCode = bondBaseInfo.getBondCode();
            String bondCodePrefix = bondCode.substring(0, 3);
            bondCodeMap.putIfAbsent(bondCodePrefix, Integer.parseInt(bondCode.substring(3)));

            bondFlagMap.put(bondCode, bondBaseInfo.getFlag());
        }
        log.info("bondCodeMap={}", JSON.toJSONString(bondCodeMap));
    }

    private ResponseEntity<String> iterateWebBond(String bondInfoUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(HttpHeaders.COOKIE, "compare_dish=show; kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=1699508886; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpqplqWppqCrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis());
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43");
        headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8");

        HashMap<Object, Object> params = new HashMap<>();
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        return restTemplate.exchange(bondInfoUrl, HttpMethod.GET, entity, String.class);
    }

//    private void webBondIterator(Map<String, Integer> bondCodeMap, Map<String, Integer> bondFlagMap) {
//        String bondInfoUrl = StringUtils.EMPTY;
//        String bondInfoUrlPart = "https://www.jisilu.cn/data/convert_bond_detail/";
//
//        List<String> delist = new ArrayList<>();
//        List<BondBaseInfo> addBondBaseInfoList = new ArrayList<>();
//        List<BondAmtInfo> addBondAmtInfoList = new ArrayList<>();
//        List<BondRatingCd> addBondRatingCdList = new ArrayList<>();
//        List<BondRateInfo> addBondRateInfoList = new ArrayList<>();
//        List<BondPriceInfo> addBondPriceInfoList = new ArrayList<>();
//
//        List<String> szCodeList = BusinessEnums.BondMarketEnum.SZ_MARKET.getCodeList();
//        for (String prefix : szCodeList) {
//            Integer bondCodeMaxVal = bondCodeMap.getOrDefault(prefix, BusinessEnums.DefaultBondCode.getCodeByPrefix(prefix) + 1);
//            for (int i = 0; i <= bondCodeMaxVal; i++) {
//                String bondCode = prefix.concat(String.format("%03d", i));
//                bondInfoUrl = bondInfoUrlPart.concat(bondCode);
//                ResponseEntity<String> result = iterateWebBond(bondInfoUrl);
//                if (HttpStatus.OK.equals(result.getStatusCode())) {
//                    log.info("详情页访问成功");
//                    String body = result.getBody();
//                } else {
//                    log.error("详情页访问失败：{}", result);
//                    continue;
//                }
//
//                // 这里判断 基本表是否存在
//                BondBaseInfo bondBaseInfo = new BondBaseInfo();
//                if (!bondFlagMap.containsKey(bondCode)) {
//                    // 放进待添加集合
//                    addBondBaseInfoList.add(bondBaseInfo);
//                }
//                bondBaseInfo.setBondCode(bondCode);
//
//                collectWebBondBaseInfo(Objects.requireNonNull(result.getBody()), delist, addBondRateInfoList,
//                        addBondAmtInfoList, addBondRatingCdList,
//                        addBondBaseInfoList, addBondPriceInfoList,
//                        bondFlagMap, bondCode);
//            }
//        }
//
//        // 处理各个集合 数据  todo
//    }

/*    private List<String> sss(String body, Map<String, Integer> bondFlagMap, String bondCode){
        List<String> delist = new ArrayList<>();
        // 判断是否已退市  // todo 放for循环外面
        String delistStr = "已退市";
        if (body.contains(delistStr) && BusinessEnums.BondCodeFlag.退市.getFlag() != bondFlagMap.get(bondCode)) {
            // 放进集合，批量更新表数据为已退市
            delist.add(bondCode);
        }

        return delist;
    }*/

//    private void collectWebBondBaseInfo(String body, List<String> delist, List<BondRateInfo> addBondRateInfoList,
//                                        List<BondAmtInfo> addBondAmtInfoList, List<BondRatingCd> addBondRatingCdList,
//                                        List<BondBaseInfo> addBondBaseInfoList, List<BondPriceInfo> addBondPriceInfoList,
//                                        Map<String, Integer> bondFlagMap, String bondCode) {
////        List<String> delist = new ArrayList<>();
//        // 判断是否已退市  // todo 放for循环外面
//        String delistStr = "已退市";
//        if (body.contains(delistStr) && BusinessEnums.BondCodeFlag.退市.getFlag() != bondFlagMap.get(bondCode)) {
//            // 放进集合，批量更新表数据为已退市
//            delist.add(bondCode);
//            return;
//        }
//
//        BondBaseInfo bondBaseInfo = new BondBaseInfo();
//        bondBaseInfo.setBondCode(bondCode);
//        // 剩余规模
//        BondAmtInfo currIssAmt = getCurrIssAmtFromJSL(body, bondCode);
//        addBondAmtInfoList.add(currIssAmt);
////                债券评级
//        BondRatingCd ratingCd = getRatingCdFromJSL(body, bondCode);
//        addBondRatingCdList.add(ratingCd);
//
//        // 价格 从雪球
////        https://xueqiu.com/S/
//        BondPriceInfo bondPriceInfo = getBondPriceInfo(bondCode);
//        addBondPriceInfoList.add(bondPriceInfo);
//        if (!bondFlagMap.containsKey(bondCode)) {
//            // 放进待添加集合
//            addBondBaseInfoList.add(bondBaseInfo);
//            // 待新增的需要下面方法
//            getBondBaseInfoFromJSL(body, bondBaseInfo);
//            // 利率
//            Map<Date, BigDecimal> bondYearRateMap = getBondRate(bondCode);
//            for (Map.Entry<Date, BigDecimal> bondYearRateEntry : bondYearRateMap.entrySet()) {
//                BondRateInfo bondRateInfo = new BondRateInfo();
//                bondRateInfo.setPaymentDate(bondYearRateEntry.getKey());
//                bondRateInfo.setBondCode(bondCode);
//                bondRateInfo.setRate(bondYearRateEntry.getValue().multiply(DECIMAL_1000).intValue());
//                addBondRateInfoList.add(bondRateInfo);
//            }
//        }
//
//
//    }


    @Override
    public void saveBonds() {
        // 跑批job
        // 访问东方财富网站，查询所有可转债
        // 表中没有的就插入
        // 访问深圳转债
        String szBondUrl = "https://13.push2.eastmoney.com/api/qt/clist/get?cb=jQuery1124005634438358755611_1700902094387&pn=16&pz=20&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&wbp2u=|0|0|0|web&fid=f3&fs=m:0+b:MK0354&fields=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f26,f22,f33,f11,f62,f128,f136,f115,f152&_=" + System.currentTimeMillis();
//"https://www.jisilu.cn/data/convert_bond_detail"
        // sina

        // 128 144

        // 基本资料
        String bondInfoUrl = StringUtils.EMPTY;
        String bondInfoUrlPart = "https://www.jisilu.cn/data/convert_bond_detail/";
        // 查询表中不同前缀最大的code
        LambdaQueryWrapper<BondBaseInfo> bondBaseInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondBaseInfoQueryWrapper.select(BondBaseInfo::getBondCode, BondBaseInfo::getFlag).orderByDesc(BondBaseInfo::getBondCode);
        List<BondBaseInfo> bondBaseInfoList = baseInfoDao.selectList(bondBaseInfoQueryWrapper);
        if (CollectionUtils.isEmpty(bondBaseInfoList)) {
            log.warn("可转债基本数据表为空");
            return;
        }

        Map<String, Integer> bondFlagMap = new HashMap<>();
        Map<String, Integer> bondCodeMap = new HashMap<>();
        for (BondBaseInfo bondBaseInfo : bondBaseInfoList) {
            String bondCode = bondBaseInfo.getBondCode();
            String bondCodePrefix = bondCode.substring(0, 3);
            bondCodeMap.putIfAbsent(bondCodePrefix, Integer.parseInt(bondCode.substring(3)));

            bondFlagMap.put(bondCode, bondBaseInfo.getFlag());
        }
//        log.info("bondCodeMap={}", JSON.toJSONString(bondCodeMap));

        List<BondBaseInfo> addBondBaseInfoList = new ArrayList<>();
        List<BondAmtInfo> addBondAmtInfoList = new ArrayList<>();
        List<BondRatingCd> addBondRatingCdList = new ArrayList<>();
        List<BondRateInfo> addBondRateInfoList = new ArrayList<>();
        List<String> delist = new ArrayList<>();
        List<String> unlist = new ArrayList<>();
        List<String> szCodeList = BusinessEnums.BondMarketEnum.SZ_MARKET.getCodeList();
        for (String prefix : szCodeList) {
            // 可转债编码，110开头的话 最小是多少 先给个固定值110100，判断如果是
            // 使用集思路判断 是否已退市，退市的如果在表中存在，则更新状态，

            // 3种状态判断，1：已退市 2：不存在（集思录 该转债不存在） 3：未上市（集思录 上市日为空）
            // 先查询表中最大的编码是多少+1,然后遍历,如果表中没有则插入表，如果有则通过集思录判断状态，更新状态
            // 初始值为
//            int defaultCode = BusinessEnums.DefaultBondCode.getCodeByPrefix(prefix) + 1;
            Integer bondCodeMaxVal = bondCodeMap.getOrDefault(prefix, BusinessEnums.DefaultBondCode.getCodeByPrefix(prefix) + 1);
            for (int i = 0; i <= bondCodeMaxVal; i++) {
                String bondCode = prefix.concat(String.format("%03d", i));
                bondInfoUrl = bondInfoUrlPart.concat(bondCode);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                headers.add(HttpHeaders.COOKIE, "compare_dish=show; kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=1699508886; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpqplqWppqCrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis());
                headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43");
                headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8");

                HashMap<Object, Object> params = new HashMap<>();
                HttpEntity<Object> entity = new HttpEntity<>(params, headers);
                ResponseEntity<String> result = null;
                result = restTemplate.exchange(bondInfoUrl, HttpMethod.GET, entity, String.class);
                if (HttpStatus.OK.equals(result.getStatusCode())) {
                    log.info("详情页访问成功");
                } else {
                    log.error("详情页访问失败：{}", result);
                    continue;
                }

                String body = result.getBody();
                assert body != null;
                // 判断是否已退市  // todo 放for循环外面
                String delistStr = "已退市";
                if (body.contains(delistStr) && BusinessEnums.BondCodeFlag.退市.getFlag() != bondFlagMap.get(bondCode)) {
                    // 放进集合，批量更新表数据为已退市
                    delist.add(bondCode);
                }


                BondBaseInfo bondBaseInfo = new BondBaseInfo();
                if (!bondFlagMap.containsKey(bondCode)) {
                    // 放进待添加集合
                    addBondBaseInfoList.add(bondBaseInfo);
                }
                bondBaseInfo.setBondCode(bondCode);
                // 待新增的需要下面方法
                getBondBaseInfoFromJSL(body, bondBaseInfo);
                // 平时只需要更新价格数据
                // 剩余规模
//                BondAmtInfo currIssAmt = getCurrIssAmtFromJSL(body, bondCode);
//                addBondAmtInfoList.add(currIssAmt);
////                债券评级
//                BondRatingCd ratingCd = getRatingCdFromJSL(body, bondCode);
//                addBondRatingCdList.add(ratingCd);
//                // 利率
//                Map<Date, BigDecimal> bondYearRateMap = getBondRate(bondCode);
//                for (Map.Entry<Date, BigDecimal> bondYearRateEntry : bondYearRateMap.entrySet()) {
//                    BondRateInfo bondRateInfo = new BondRateInfo();
//                    bondRateInfo.setPaymentDate(bondYearRateEntry.getKey());
//                    bondRateInfo.setBondCode(bondCode);
//                    bondRateInfo.setRate(bondYearRateEntry.getValue().multiply(DECIMAL_1000).intValue());
//                    addBondRateInfoList.add(bondRateInfo);
//                }
                // 判断未上市
//                String unlistDtStr = "id=\"list_dt\">-</td>";
//                if (body.contains(unlistDtStr)) {
//                    // 放进集合，批量更新表数据为未上市
////                    unlist.add(bondCode);
//                    // 未上市 的不处理不记录，与策略无关
//                }


            }
        }

    }


}
