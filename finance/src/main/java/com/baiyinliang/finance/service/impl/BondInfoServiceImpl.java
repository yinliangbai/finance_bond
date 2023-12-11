package com.baiyinliang.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baiyinliang.finance.entity.BondInfo;
import com.baiyinliang.finance.entity.BondPrice;
import com.baiyinliang.finance.entity.BondRate;
import com.baiyinliang.finance.enums.BusinessEnums;
import com.baiyinliang.finance.mapper.BondInfoDao;
import com.baiyinliang.finance.mapper.BondPriceDao;
import com.baiyinliang.finance.mapper.BondRateDao;
import com.baiyinliang.finance.model.Bond;
import com.baiyinliang.finance.model.PageModel;
import com.baiyinliang.finance.request.BondInfoListReq;
import com.baiyinliang.finance.request.BondInfoPageReq;
import com.baiyinliang.finance.request.Sorter;
import com.baiyinliang.finance.service.BondInfoService;
import com.baiyinliang.finance.tools.HttpClientUtil;
import com.baiyinliang.finance.vo.BondInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
public class BondInfoServiceImpl extends ServiceImpl<BondInfoDao, BondInfo> implements BondInfoService {

    // 写一个跑批，每天把新上市 的可转债放进去

    @Autowired
    private BondInfoDao bondInfoDao;
    @Autowired
    private BondPriceDao bondPriceDao;
    @Autowired
    private BondRateDao bondRateDao;
    @Resource
    private RestTemplate restTemplate;


    @Override
    public void processBond() {
        // 查询库中的所有id, 原则上只增不删
        // 1. 先根据总的条件，价格、余额、到期时间筛选出所有符合条件的数据（todo 这里要去掉st的） 对状态变的要加状态，比如st/赎回/不满足条件的
        // 2. 查询库中的所有id，然后更新数据，没有批量新增
        // 3. 每天的价格记录到price表中（todo 在另一个方法）


        PageModel jslPage = getJSLPage();
        if (jslPage == null) {
            log.error("jslPage 为null");
            return;
        }

        Map<Integer, Bond> bondMap = filterBondModel(jslPage);
        if (CollectionUtils.isEmpty(bondMap)) {
            log.error("bondMap 为null");
            return;
        }

//        查询表中现有的所有可转债数据
        Map<Integer, BondInfo> bondInfoMap = getBondInfoMap();
//        新的需要设置强赎价的可转债，批量insert到表中
        Set<Integer> newBondIdSet = new HashSet<>();
//        已经存在表中不需要设置强赎价的可转债，一条一条更新，不用查详情
        Set<Integer> bondIdSet = new HashSet<>();
//        给新的符合条件的可转债设置强赎价、到期日期、上市日期
        setBondDetail(bondMap, bondInfoMap, newBondIdSet, bondIdSet);

        // newBondList 用于批量插入
        List<BondInfo> newBondList = new ArrayList<>();
        for (Integer bondId : newBondIdSet) {
            BondInfo bondInfo = new BondInfo();
            if (bondMap.containsKey(bondId)) {

//                BeanUtils.copyProperties(bond, bondInfo);
                Bond bond = bondMap.get(bondId);
                bondInfo.setBondId(bondId);
                bondInfo.setYtmRt(bond.getYtm_rt());
                bondInfo.setCurrIssAmt(bond.getCurr_iss_amt());
                bondInfo.setPremiumRt(bond.getPremium_rt());
                bondInfo.setConvertValue(bond.getConvert_value());
                bondInfo.setPrice(bond.getPrice());
                bondInfo.setBondNm(bond.getBond_nm());
                bondInfo.setListDt(bond.getList_dt());
                bondInfo.setMaturityDt(bond.getMaturity_dt());
                bondInfo.setRatingCd(bond.getRating_cd());
                bondInfo.setRedeemPrice(bond.getRedeem_price());
                bondInfo.setEarningsPrice(bond.getRedeem_price().subtract(bond.getPrice()));
                bondInfo.setStockId(bond.getStock_id());
                bondInfo.setStockNm(bond.getStock_nm());
                bondInfo.setCreateTime(new Date());
                newBondList.add(bondInfo);
            }
        }

        if (!CollectionUtils.isEmpty(newBondList)) {
            for (BondInfo bondInfo : newBondList) {
                log.info("本次插入bondInfo={}", JSON.toJSONString(bondInfo));
                int insert = bondInfoDao.insert(bondInfo);
                log.info("插入成功");
            }
            // 批量插入
//            bondInfoDao.insert(newBondLi
//            st.get(0));
//            int insertCount = bondInfoDao.batchInsert(newBondList);
//            log.info("共插入{}条可转债数据", insertCount);
        }

        // 已经存在的，要做更新
        for (Integer bondId : bondIdSet) {
            Bond bond = bondMap.get(bondId);
            // 只更新部分字段
            BondInfo bondInfo = bondInfoMap.get(bondId);
            bondInfo.setPrice(bond.getPrice());
//            log.info("bondInfo null异常={}", JSON.toJSONString(bondInfo));
            bondInfo.setEarningsPrice(bondInfo.getRedeemPrice().subtract(bond.getPrice()));
            bondInfo.setConvertValue(bond.getConvert_value());
            bondInfo.setPremiumRt(bond.getPremium_rt());
            bondInfo.setCurrIssAmt(bond.getCurr_iss_amt());
            bondInfo.setYtmRt(bond.getYtm_rt());
            bondInfo.setUpdateTime(new Date());
            bondInfoDao.updateById(bondInfo);
            log.info("可转债更新成功，数据={}", JSON.toJSONString(bondInfo));
        }

        log.info("处理完成");

    }

    @Override
    public void setBondPrice() {
        // 从 bond表查询 id, 然后每个去访问详情页，再将当天最高和最低价放入表中
        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.select(BondInfo::getBondId, BondInfo::getBondNm, BondInfo::getRedeemPrice, BondInfo::getPrice);
        bondInfoQueryWrapper.eq(BondInfo::getFlag, 1);
        List<BondInfo> bondInfos = bondInfoDao.selectList(bondInfoQueryWrapper);
        if (CollectionUtils.isEmpty(bondInfos)) {
            log.warn("查询可转债信息表为空");
            return;
        }

        Set<Integer> bondIdSet = new HashSet<>();
        Map<Integer, BondInfo> bondInfoMap = new HashMap<>();
        for (BondInfo bondInfo : bondInfos) {
            bondIdSet.add(bondInfo.getBondId());
            bondInfoMap.put(bondInfo.getBondId(), bondInfo);
        }

        if (CollectionUtils.isEmpty(bondIdSet)) {
            log.warn("查询可转债信息表,bondId为空");
            return;
        }

//        ChromeDriver chromeDriver = DriverUtil.getChromeDriver();
        for (Integer bondId : bondIdSet) {
            BondInfo bondInfo = bondInfoMap.get(bondId);
            BondPrice bondPrice = getBondPrice2(bondId);
            BondInfo bondInfo1 = bondInfoDao.selectById(bondId);
            if (bondInfo1 != null && bondInfo1.getFlag() != 1) {
                log.info("可转债 {} 已退市或停牌，不处理", bondInfo1.getBondNm());
                continue;
            }
            bondPrice.setBondId(bondId);
            bondPrice.setBondNm(bondInfo.getBondNm());
            bondPrice.setPrice(bondInfo.getPrice());
            bondPrice.setRedeemPrice(bondInfo.getRedeemPrice());
            bondPrice.setCreateTime(new Date());
            bondPrice.setDate(new Date());
            bondPrice.setWeek(getWeekOfDate(new Date()));
            bondPriceDao.insert(bondPrice);
            log.info("插入数据，{}", JSON.toJSONString(bondPrice));
        }
//        chromeDriver.quit();
        log.info("插入结束");
    }


    @Override
    public List<BondInfoVO> getInterestMarginList() {
        // todo
        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.eq(BondInfo::getFlag, 1);
        bondInfoQueryWrapper.select(BondInfo::getBondId, BondInfo::getBondNm, BondInfo::getPrice,
                BondInfo::getRatingCd, BondInfo::getMaturityDt, BondInfo::getCurrIssAmt,
                BondInfo::getYtmRt, BondInfo::getRedeemPrice, BondInfo::getEarningsPrice,
                BondInfo::getUpdateTime).orderByDesc(BondInfo::getEarningsPrice);
//                .last("limit 10");
        List<BondInfo> bondInfoList = bondInfoDao.selectList(bondInfoQueryWrapper);
        // 五天
        Set<Integer> bondIdSet = new HashSet<>(bondInfoList.size());
        for (BondInfo bondInfo : bondInfoList) {
            bondIdSet.add(bondInfo.getBondId());
        }

        // 临近付息日
        // 在本年已过的 今天以前的是灰色，今天的是红色，今天以后的是黄色
        // 付息日字段 标志是否已过的状态
        // 1. 查询利率表,付息日本年的，bondId集合入参
        List<BondRate> bondRates = bondRateDao.selectRecentlyBondRateList(bondIdSet);
        Date parseDate = null;
        try {
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            parseDate = sdf.parse(sdf.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<Integer, Integer> paidFlagMap = new HashMap<>(bondRates.size());
        Map<Integer, BondRate> bondRateMap = new HashMap<>(bondRates.size());
        for (BondRate bondRate : bondRates) {
            bondRateMap.put(bondRate.getBondId(), bondRate);
        }


        List<BondPrice> bondPrices = bondPriceDao.selectBondPricePeriod(5, bondIdSet);
        Map<Integer, List<BondInfoVO.BondPriceRange>> bondPriceRangeMap = new HashMap<>();
        for (BondPrice bondPrice : bondPrices) {
            Integer bondId = bondPrice.getBondId();
            List<BondInfoVO.BondPriceRange> priceRangeList = bondPriceRangeMap.getOrDefault(bondId, new ArrayList<>());
            BondInfoVO.BondPriceRange bondPriceRange = new BondInfoVO.BondPriceRange();
            bondPriceRange.setMinPrice(bondPrice.getMinPrice());
            bondPriceRange.setMaxPrice(bondPrice.getMaxPrice());
            priceRangeList.add(bondPriceRange);
//            priceRangeList.add(bondPrice.getMinPrice()+":"+bondPrice.getMaxPrice());
            bondPriceRangeMap.put(bondId, priceRangeList);
        }

        List<BondInfoVO> result = new ArrayList<>(bondInfoList.size());
        for (BondInfo bondInfo : bondInfoList) {
            Integer bondId = bondInfo.getBondId();
            BondInfoVO bondInfoVO = new BondInfoVO();
            BeanUtils.copyProperties(bondInfo, bondInfoVO);

            // 前n日价格
            List<BondInfoVO.BondPriceRange> bondPriceRangeList = bondPriceRangeMap.getOrDefault(bondId, new ArrayList<>());
            if (CollectionUtils.isEmpty(bondPriceRangeList)) {
                log.info("id={}的没有每日价格", bondId);
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

            // 临近付息日
            BondRate bondRate = bondRateMap.getOrDefault(bondId, new BondRate());
            Date paymentDate = bondRate.getPaymentDate();
            bondInfoVO.setCurrentPeriodPayInterestDate(paymentDate);
            bondInfoVO.setCurrentPeriodRate(bondRate.getRate());
            if (paymentDate.before(parseDate)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.PAID.getCode());
            } else if (paymentDate.after(parseDate)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.NON_PAYMENT.getCode());
            } else {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.CURRENT.getCode());
            }

/*            BondInfoVO.BondPayInterestInfo bondPayInterestInfo = new BondInfoVO.BondPayInterestInfo();
            bondPayInterestInfo.setCurrentPeriodPayInterestDate(paymentDate);
            bondPayInterestInfo.setCurrentPeriodRate(bondRate.getRate());
            if(paymentDate.before(parseDate)){
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.PAID.getCode());
            }else if(paymentDate.after(parseDate)){
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.NON_PAYMENT.getCode());
            }else {
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.CURRENT.getCode());
            }
            bondInfoVO.setBondPayInterestInfo(bondPayInterestInfo);*/


            result.add(bondInfoVO);
            log.info("bondInfoVO={}", JSON.toJSONString(bondInfoVO));
        }

        // 五天最低价 临近付息日

        return result;
    }

    @Override
    public BondInfoVO getBondInfo(Integer bondId) {
        BondInfoVO bondInfoVO = new BondInfoVO();
        if(bondId == null){
            log.error("查询转债详情失败，参数id为空");
            return bondInfoVO;
        }

        // 判断 flag 状态
        BondInfo bondInfo = bondInfoDao.selectById(bondId);
        if(bondInfo == null){
            log.error("查询转债详情失败，参数id={}的转债数据不存在", bondId);
            return bondInfoVO;
        }

        bondInfoVO.setBondId(bondId);
        bondInfoVO.setBondNm(bondInfo.getBondNm());
        bondInfoVO.setStockNm(bondInfo.getStockNm());
        // 转债评级
        bondInfoVO.setRatingCd(bondInfo.getRatingCd());
        // 剩余规模
        bondInfoVO.setCurrIssAmt(bondInfo.getCurrIssAmt());
//        上市日期
        bondInfoVO.setListDt(bondInfo.getListDt());
//        到期日期
        bondInfoVO.setMaturityDt(bondInfo.getMaturityDt());
//        现价
        bondInfoVO.setPrice(bondInfo.getPrice());
//        到期赎回价
        bondInfoVO.setRedeemPrice(bondInfo.getRedeemPrice());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String currentYearStr = sdf.format(new Date());

        LambdaQueryWrapper<BondRate> bondRateQueryWrapper = new LambdaQueryWrapper<>();
        bondRateQueryWrapper.select(BondRate::getBondId,BondRate::getPaymentDate,BondRate::getRate)
                .eq(BondRate::getBondId, bondId).likeRight(BondRate::getPaymentDate,currentYearStr);

        List<BondRate> bondRates = bondRateDao.selectList(bondRateQueryWrapper);
        if(CollectionUtils.isEmpty(bondRates)){
            log.warn("id={}的{}利率数据不存在",bondId, bondInfo.getBondNm());
        }else{
            if(bondRates.size() > 1){
                log.warn("id={}的{} 本年度存在重复利率数据",bondId, bondInfo.getBondNm());
            }
            BondRate bondRate = bondRates.get(0);
            // 当前利率
            bondInfoVO.setCurrentPeriodRate(bondRate.getRate());
            // 付息日
            bondInfoVO.setCurrentPeriodPayInterestDate(bondRate.getPaymentDate());
        }

        // todo 小程序 画折线图
        log.info("bondInfoVO详情={}", JSON.toJSONString(bondInfoVO));
        return bondInfoVO;
    }

    // 查询条件：深市、沪市 多选框，bondID/bondNM，
    // 排序：前端排序
    @Override
    public List<BondInfoVO> getBondInfoList(BondInfoListReq req) {
        log.info("req={}", JSON.toJSONString(req));
        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.select(BondInfo::getBondId, BondInfo::getBondNm, BondInfo::getPrice,
                BondInfo::getRatingCd, BondInfo::getMaturityDt, BondInfo::getCurrIssAmt,
                BondInfo::getYtmRt, BondInfo::getRedeemPrice, BondInfo::getEarningsPrice,
                BondInfo::getUpdateTime)
                .eq(BondInfo::getFlag, 1);


        String[] market = req.getMarket();
        boolean hasSH = false;
        boolean hasSZ = false;

        if (market.length > 0) {
            for (String s : market) {
                if (StringUtils.isNotBlank(s)) {
                    if (s.equals("sh")) {
                        hasSH = true;
                    } else if (s.equals("zh")) {
                        hasSZ = true;
                    } else {
                        log.warn("market传参异常={}", market);
                    }
                } else {
                    log.warn("market传参异常 s={}", s);
                }
            }
        }

        if (hasSH && hasSZ) {
            bondInfoQueryWrapper.and(wrapper -> wrapper.likeRight(BondInfo::getBondId, 11).or(wrapper1 -> wrapper1.likeRight(BondInfo::getBondId, 12)));
        } else if (hasSH) {
            bondInfoQueryWrapper.and(wrapper -> wrapper.likeRight(BondInfo::getBondId, 11));
        } else if (hasSZ) {
            bondInfoQueryWrapper.and(wrapper -> wrapper.likeRight(BondInfo::getBondId, 12));
        }

        String param = req.getParam();
        // 参数处理
        if (StringUtils.isNotBlank(param)) {
            String newParam = param.trim();
            // 判断是否纯数字
//            if(newParam.length() == 6 && (newParam.startsWith("11") || newParam.startsWith("12"))){
            // 判断数字是否11或12开头，且共6位
            String regex = "(([1][1])|([1][2]))[\\d]{4}";
            if (newParam.matches(regex)) {
                bondInfoQueryWrapper.like(BondInfo::getBondId, Long.parseLong(newParam));

            } else {
                // 按名称查询
                bondInfoQueryWrapper.like(BondInfo::getBondNm, newParam);
            }
        }

        List<BondInfo> bondInfoList = bondInfoDao.selectList(bondInfoQueryWrapper);
        // 五天
        Set<Integer> bondIdSet = new HashSet<>(bondInfoList.size());
        for (BondInfo bondInfo : bondInfoList) {
            bondIdSet.add(bondInfo.getBondId());
        }

        // 临近付息日
        // 在本年已过的 今天以前的是灰色，今天的是红色，今天以后的是黄色
        // 付息日字段 标志是否已过的状态
        // 1. 查询利率表,付息日本年的，bondId集合入参
        List<BondRate> bondRates = bondRateDao.selectRecentlyBondRateList(bondIdSet);
        Date parseDate = null;
        try {
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            parseDate = sdf.parse(sdf.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<Integer, Integer> paidFlagMap = new HashMap<>(bondRates.size());
        Map<Integer, BondRate> bondRateMap = new HashMap<>(bondRates.size());
        for (BondRate bondRate : bondRates) {
            bondRateMap.put(bondRate.getBondId(), bondRate);
        }


        List<BondPrice> bondPrices = bondPriceDao.selectBondPricePeriod(5, bondIdSet);
        Map<Integer, List<BondInfoVO.BondPriceRange>> bondPriceRangeMap = new HashMap<>();
        for (BondPrice bondPrice : bondPrices) {
            Integer bondId = bondPrice.getBondId();
            List<BondInfoVO.BondPriceRange> priceRangeList = bondPriceRangeMap.getOrDefault(bondId, new ArrayList<>());
            BondInfoVO.BondPriceRange bondPriceRange = new BondInfoVO.BondPriceRange();
            bondPriceRange.setMinPrice(bondPrice.getMinPrice());
            bondPriceRange.setMaxPrice(bondPrice.getMaxPrice());
            priceRangeList.add(bondPriceRange);
//            priceRangeList.add(bondPrice.getMinPrice()+":"+bondPrice.getMaxPrice());
            bondPriceRangeMap.put(bondId, priceRangeList);
        }

        List<BondInfoVO> result = new ArrayList<>(bondInfoList.size());
        for (BondInfo bondInfo : bondInfoList) {
            Integer bondId = bondInfo.getBondId();
            BondInfoVO bondInfoVO = new BondInfoVO();
            BeanUtils.copyProperties(bondInfo, bondInfoVO);

            // 前n日价格
            List<BondInfoVO.BondPriceRange> bondPriceRangeList = bondPriceRangeMap.getOrDefault(bondId, new ArrayList<>());
            if (CollectionUtils.isEmpty(bondPriceRangeList)) {
                log.info("id={}的没有每日价格", bondId);
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

            // 临近付息日
            BondRate bondRate = bondRateMap.getOrDefault(bondId, new BondRate());
            Date paymentDate = bondRate.getPaymentDate();
            bondInfoVO.setCurrentPeriodPayInterestDate(paymentDate);
            bondInfoVO.setCurrentPeriodRate(bondRate.getRate());
            if (paymentDate.before(parseDate)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.PAID.getCode());
            } else if (paymentDate.after(parseDate)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.NON_PAYMENT.getCode());
            } else {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.CURRENT.getCode());
            }

/*            BondInfoVO.BondPayInterestInfo bondPayInterestInfo = new BondInfoVO.BondPayInterestInfo();
            bondPayInterestInfo.setCurrentPeriodPayInterestDate(paymentDate);
            bondPayInterestInfo.setCurrentPeriodRate(bondRate.getRate());
            if(paymentDate.before(parseDate)){
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.PAID.getCode());
            }else if(paymentDate.after(parseDate)){
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.NON_PAYMENT.getCode());
            }else {
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.CURRENT.getCode());
            }
            bondInfoVO.setBondPayInterestInfo(bondPayInterestInfo);*/


            result.add(bondInfoVO);
            log.info("bondInfoVO={}", JSON.toJSONString(bondInfoVO));
        }

        // 五天最低价 临近付息日

        return result;
    }

    @Override
    public Page<BondInfoVO> getBondInfoVOPage(BondInfoPageReq req) {
        Page<BondInfoVO> result = new Page<>();
        Page<BondInfo> page = new Page<>(req.getPageNo(), req.getSize());
        String param = req.getParam();

        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.eq(BondInfo::getFlag, 1);
        bondInfoQueryWrapper.select(BondInfo::getBondId, BondInfo::getBondNm, BondInfo::getPrice,
                BondInfo::getRatingCd, BondInfo::getMaturityDt, BondInfo::getCurrIssAmt,
                BondInfo::getYtmRt, BondInfo::getRedeemPrice, BondInfo::getEarningsPrice,
                BondInfo::getUpdateTime).last(Sorter.getOrderByStatement(req));
//                .orderByDesc(BondInfo::getEarningsPrice);
        // 参数处理
        if (StringUtils.isNotBlank(param)) {
            String newParam = param.trim();
            // 判断是否纯数字
//            if(newParam.length() == 6 && (newParam.startsWith("11") || newParam.startsWith("12"))){
            // 判断数字是否11或12开头，且共6位
            String regex = "(([1][1])|([1][2]))[\\d]{4}";
            if (newParam.matches(regex)) {
                bondInfoQueryWrapper.eq(BondInfo::getBondId, Long.parseLong(newParam));

            } else {
                // 按名称查询
                bondInfoQueryWrapper.like(BondInfo::getBondNm, newParam);
            }
        }

//                .last("limit 10");
        IPage<BondInfo> bondInfoIPage = bondInfoDao.selectPage(page, bondInfoQueryWrapper);
        long total = bondInfoIPage.getTotal();
        List<BondInfo> bondInfoList = bondInfoIPage.getRecords();
        if (CollectionUtils.isEmpty(bondInfoList)) {
            log.warn("");
            return null;
        }
        // 五天
        Set<Integer> bondIdSet = new HashSet<>(bondInfoList.size());
        for (BondInfo bondInfo : bondInfoList) {
            bondIdSet.add(bondInfo.getBondId());
        }

        // 临近付息日
        // 在本年已过的 今天以前的是灰色，今天的是红色，今天以后的是黄色
        // 付息日字段 标志是否已过的状态
        // 1. 查询利率表,付息日本年的，bondId集合入参
        List<BondRate> bondRates = bondRateDao.selectRecentlyBondRateList(bondIdSet);
        Date parseDate = null;
        try {
            Date currentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            parseDate = sdf.parse(sdf.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<Integer, Integer> paidFlagMap = new HashMap<>(bondRates.size());
        Map<Integer, BondRate> bondRateMap = new HashMap<>(bondRates.size());
        for (BondRate bondRate : bondRates) {
            bondRateMap.put(bondRate.getBondId(), bondRate);
        }


        List<BondPrice> bondPrices = bondPriceDao.selectBondPricePeriod(5, bondIdSet);
        Map<Integer, List<BondInfoVO.BondPriceRange>> bondPriceRangeMap = new HashMap<>();
        for (BondPrice bondPrice : bondPrices) {
            Integer bondId = bondPrice.getBondId();
            List<BondInfoVO.BondPriceRange> priceRangeList = bondPriceRangeMap.getOrDefault(bondId, new ArrayList<>());
            BondInfoVO.BondPriceRange bondPriceRange = new BondInfoVO.BondPriceRange();
            bondPriceRange.setMinPrice(bondPrice.getMinPrice());
            bondPriceRange.setMaxPrice(bondPrice.getMaxPrice());
            priceRangeList.add(bondPriceRange);
//            priceRangeList.add(bondPrice.getMinPrice()+":"+bondPrice.getMaxPrice());
            bondPriceRangeMap.put(bondId, priceRangeList);
        }

        List<BondInfoVO> list = new ArrayList<>(bondInfoList.size());
        for (BondInfo bondInfo : bondInfoList) {
            Integer bondId = bondInfo.getBondId();
            BondInfoVO bondInfoVO = new BondInfoVO();
            BeanUtils.copyProperties(bondInfo, bondInfoVO);

            // 前n日价格
            List<BondInfoVO.BondPriceRange> bondPriceRangeList = bondPriceRangeMap.getOrDefault(bondId, new ArrayList<>());
            if (CollectionUtils.isEmpty(bondPriceRangeList)) {
                log.info("id={}的没有每日价格", bondId);
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

            // 临近付息日
            BondRate bondRate = bondRateMap.getOrDefault(bondId, new BondRate());
            Date paymentDate = bondRate.getPaymentDate();
            bondInfoVO.setCurrentPeriodPayInterestDate(paymentDate);
            bondInfoVO.setCurrentPeriodRate(bondRate.getRate());
            if (paymentDate.before(parseDate)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.PAID.getCode());
            } else if (paymentDate.after(parseDate)) {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.NON_PAYMENT.getCode());
            } else {
                bondInfoVO.setPaidFlag(BusinessEnums.PaidFlagEnum.CURRENT.getCode());
            }

/*            BondInfoVO.BondPayInterestInfo bondPayInterestInfo = new BondInfoVO.BondPayInterestInfo();
            bondPayInterestInfo.setCurrentPeriodPayInterestDate(paymentDate);
            bondPayInterestInfo.setCurrentPeriodRate(bondRate.getRate());
            if(paymentDate.before(parseDate)){
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.PAID.getCode());
            }else if(paymentDate.after(parseDate)){
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.NON_PAYMENT.getCode());
            }else {
                bondPayInterestInfo.setPaidFlag(BusinessEnums.PaidFlagEnum.CURRENT.getCode());
            }
            bondInfoVO.setBondPayInterestInfo(bondPayInterestInfo);*/


            list.add(bondInfoVO);

            log.info("bondInfoVO={}", JSON.toJSONString(bondInfoVO));
        }

        // 五天最低价 临近付息日
        result.setRecords(list);
        result.setSize(req.getSize());
        result.setCurrent(req.getPageNo());
        result.setTotal(bondInfoIPage.getTotal());
        return result;
    }

    private void setPriceRange(List<BondInfoVO.BondPriceRange> bondPriceRangeList, Object obj) {
        try {
//            Collections.reverse(bondPriceRangeList);
            for (int i = 0; ; i++) {
                Field declaredField = obj.getClass().getDeclaredField("priceRange" + (i + 1));
                if (declaredField == null || bondPriceRangeList.size() == i) {
                    break;
                }
                declaredField.setAccessible(true);
                declaredField.set(obj, bondPriceRangeList.get(i));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.info("obj={}", JSON.toJSONString(obj));
//            e.printStackTrace();
        }
    }

    // 画图表
    // 每只转债的 最高、最低价格 折线，以天、周、月为维度 的最高、最低价
    // 以天
    public void draw() {
//        CategoryDataset dataset=createDataset();
//        JFreeChart chart=createChart(dataset);
//        ChartPanel chartPanel=new ChartPanel(chart);
//        chartPanel.setPreferredSize(new Dimension(800,500));
//        setContentPane(chartPanel);
    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(212, "Letter", "A");
        dataset.addValue(504, "Letter", "B");
        dataset.addValue(1520, "Letter", "C");
        dataset.addValue(1842, "Letter", "D");
        dataset.addValue(2991, "Letter", "E");
        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createLineChart(
                "Line Chart Demo",
                "Category Axis",
                "Value Axis",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        return chart;
    }


    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    private Integer getWeekOfDate(Date dt) {
//        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Integer[] weekDays = {7, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }


    /**
     * 查询表中现有的所有可转债数据
     *
     * @return
     */
    private Map<Integer, BondInfo> getBondInfoMap() {
        Map<Integer, BondInfo> bondInfoMap = new HashMap<>();

        // 查询表中的数据，已经存在的是不需要再去查询强赎价的
        LambdaQueryWrapper<BondInfo> bondInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondInfoQueryWrapper.eq(BondInfo::getFlag, 1);
        List<BondInfo> bondInfos = bondInfoDao.selectList(bondInfoQueryWrapper);
        if (CollectionUtils.isEmpty(bondInfos)) {
            log.warn("可转债表为空");
            return bondInfoMap;
        }

        for (BondInfo bondInfo : bondInfos) {
            bondInfoMap.put(bondInfo.getBondId(), bondInfo);
        }

        return bondInfoMap;
    }


    /**
     * 给新的符合条件的可转债设置强赎价
     *
     * @param bondMap
     * @param bondInfoMap
     */
    private void setBondDetail(Map<Integer, Bond> bondMap, Map<Integer, BondInfo> bondInfoMap, Set<Integer> newBondIdSet, Set<Integer> bondIdSet) {

        // 移除表中的id，剩下是需要新增的
        /*bondMap.keySet().removeAll(bondInfoMap.keySet());
        log.info("bondMap.keySet={}", JSON.toJSONString(bondMap.keySet()));*/
        for (Integer bondId : bondMap.keySet()) {
            if (!bondInfoMap.containsKey(bondId)) {
                newBondIdSet.add(bondId);
            } else {
                bondIdSet.add(bondId);
            }
        }

        if (CollectionUtils.isEmpty(newBondIdSet)) {
            log.warn("没有需要查询 强赎价的可转债");
            return;
        }

        // 遍历 获取每一个的 强赎价
        Map<Integer, BondDetailInfo> redeemPriceMap = batchGetRedeemPrice(newBondIdSet);
        for (Map.Entry<Integer, BondDetailInfo> entry : redeemPriceMap.entrySet()) {
            Integer bondId = entry.getKey();
            Bond bond = bondMap.get(bondId);
            BondDetailInfo bondDetailInfo = redeemPriceMap.get(bondId);
            if (bondDetailInfo == null) {
                log.warn("{}的详情数据为空", bondId);
            } else {
                bond.setRedeem_price(bondDetailInfo.getRedeemPrice());
                bond.setEarnings_price(bond.getRedeem_price().subtract(bond.getPrice()));
                bond.setMaturity_dt(bondDetailInfo.getMaturityDt());
                bond.setList_dt(bondDetailInfo.getListDt());
            }
        }
    }


    private Map<Integer, Bond> filterBondModel(PageModel pageModel) {
        List<PageModel.Row> rows = pageModel.getRows();
        List<Bond> bonds = new ArrayList<>();
        Map<Integer, Bond> bondMap = new HashMap<>();
        Set<Integer> bondIds = new HashSet<>();
        DecimalFormat df = new DecimalFormat( "#,##0.00 ");
        for (PageModel.Row row : rows) {
            PageModel.Cell cell = row.getCell();
            if (cell.getYear_left() == null) {
                log.warn("{}的剩余年限为空", cell.getBond_nm());
                continue;
            }
            if (StringUtils.isNotBlank(cell.getStock_nm()) && cell.getStock_nm().startsWith("ST")) {
                log.warn("{}的正股st", cell.getBond_nm());
                continue;
            }
            // 添加各个限制条件
            // 到期时间大于一年
            if (cell.getYear_left().compareTo(BigDecimal.ONE) <= 0) {
                continue;
            }

            // 到期税前收益率
            // 现价
            // 行业

            Bond bond = new Bond();
            log.info("cell={}", JSON.toJSONString(cell));
            BeanUtils.copyProperties(cell, bond);
            BigDecimal roundedNumber = bond.getYtm_rt().setScale(3, BigDecimal.ROUND_HALF_UP);
            bond.setYtm_rt(roundedNumber);
//            BigDecimal currIssAmt = bond.getCurr_iss_amt().setScale(3, BigDecimal.ROUND_HALF_UP);
//            bond.setCurrIssAmt(currIssAmt);
            bond.setPremium_rt(bond.getPremium_rt().setScale(3, BigDecimal.ROUND_HALF_UP));
//            bond.setConvertValue(bond.getConvert_value().setScale(3, BigDecimal.ROUND_HALF_UP));
            bond.setPrice(bond.getPrice().setScale(3, BigDecimal.ROUND_HALF_UP));
//            bond.setRedeem_price(bond.getRedeem_price().setScale(3, BigDecimal.ROUND_HALF_UP));
//            bond.setEarnings_price((bond.getRedeem_price().subtract(bond.getPrice())).setScale(3, BigDecimal.ROUND_HALF_UP));
            bond.setPremium_rt(bond.getPremium_rt().setScale(3, BigDecimal.ROUND_HALF_UP));
//            bondInfo.setPremiumRt(bond.getPremium_rt());
//            bondInfo.setConvertValue(bond.getConvert_value());
//            bondInfo.setPrice(bond.getPrice());
//            bondInfo.setBondNm(bond.getBond_nm());
//            bondInfo.setListDt(bond.getList_dt());
//            bondInfo.setMaturityDt(bond.getMaturity_dt());
//            bondInfo.setRatingCd(bond.getRating_cd());
//            bondInfo.setRedeemPrice(bond.getRedeem_price());
//            bondInfo.setEarningsPrice(bond.getRedeem_price().subtract(bond.getPrice()));

            log.info("bond={}", JSON.toJSONString(bond));
            Integer bond_id = bond.getBond_id();
            if (bond_id == null) {
                log.warn("{}的bond_id为空", cell.getBond_nm());
                continue;
            }
            bondIds.add(bond_id);
            bondMap.put(bond_id, bond);
            bonds.add(bond);
        }

        log.info("bondMap={}", JSON.toJSONString(bondMap));
        return bondMap;
    }


    private PageModel getJSLPage() {
        String url = "https://www.jisilu.cn/data/cbnew/cb_list_new/?___jsl=LST___&fprice=&tprice=124&curr_iss_amt=6&volume=&svolume=&premium_rt=&ytm_rt=&rating_cd=&is_search=Y&market_cd[]=shmb&market_cd[]=shkc&market_cd[]=szmb&market_cd[]=szcy&btype=C&listed=Y&qflag=N&sw_cd=&bond_ids=&rp=50";
        Map<String, String> paramMap = new HashMap<>();
//        paramMap.put("tprice", "124");
        paramMap.put("curr_iss_amt", "6");
        paramMap.put("is_search", "Y");
        paramMap.put("btype", "C");
        paramMap.put("listed", "Y");
        paramMap.put("qflag", "N");
        String getResult = null;
        try {
            getResult = HttpClientUtil.simplePost(url, paramMap, null);
        } catch (IOException e) {
            log.error("根据条件查询jsl列表数据异常，入参={}，异常={}", JSON.toJSONString(paramMap), e.getMessage());
            e.printStackTrace();
            return null;
        }

        log.info("getResult--------->" + getResult);
        if (StringUtils.isBlank(getResult) || getResult.contains("游客仅显示前 30 条转债记录，请登录查看完整列表数据")) {
            log.error("根据条件查询jsl列表数据，结果为空，入参={}", JSON.toJSONString(paramMap));
            return null;
        }

        PageModel pageModel = JSONObject.parseObject(getResult, PageModel.class);
        log.info("pageModel={}", JSON.toJSONString(pageModel));

        return pageModel;
    }


    private Map<Integer, BondDetailInfo> batchGetRedeemPrice(Collection<Integer> bondIds) {
        Map<Integer, BondDetailInfo> redeemPriceMap = new HashMap<>();
        for (Integer bondId : bondIds) {
            BondDetailInfo bondDetailInfo = getRedeemPrice2(bondId);
            redeemPriceMap.put(bondId, bondDetailInfo);
        }

        return redeemPriceMap;
    }


    @Data
    static class BondDetailInfo {
        private BigDecimal redeemPrice;
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date listDt;
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date maturityDt;
    }


    @Data
    static class BondPriceInfo {

        private Date last_chg_dt;
        private BigDecimal price;

    }


    @Override
    public BondPrice getBondPrice2(Integer bondId) {
        BondPrice bondPrice = new BondPrice();

        String xueqiuBondDetailUrl = null;
        if (bondId.toString().startsWith("12")) {
            xueqiuBondDetailUrl = "https://xueqiu.com/S/sz" + bondId;
        } else if (bondId.toString().startsWith("11")) {
            xueqiuBondDetailUrl = "https://xueqiu.com/S/sh" + bondId;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(HttpHeaders.COOKIE, "xq_a_token=cf755d099237875c767cae1769959cee5a1fb37c; xqat=cf755d099237875c767cae1769959cee5a1fb37c; xq_r_token=e073320f4256c0234a620b59c446e458455626d9; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTcwMTk5NTg4MCwiY3RtIjoxNjk5Njk3MDExMzA3LCJjaWQiOiJkOWQwbjRBWnVwIn0.bgjWeA3aKpL3t8UatYIgxEjhTdAwb9PGpG3p4V0udPqni0kRHqq6n90Fx-LovLcrMWJyIhRmkqJFrhkzqSKESgw3Y6zUyx5vnvSrw8Ajz6QKlUHau9LDN9jYMF97qBesMbjcpDu6ygy6x3eaoITjLCXE0cmaC7RJHQgxTDR8zvAJ18C8sTenaEUV6VE11_dRL-8OsvXMhM1OkRuoYJWCLIwlAoewIg8p0RfrjCt254YmiB-M3vk9Rfx9u5sDbEmnfJWNUT3PUX6rfwGqgENi0E_QupfVE89psAm-XRhAHIDJiASj04s3utYUPtzN0fGRzfJyypFVm22ziVVheafxMA; cookiesu=921699697061450; u=921699697061450; Hm_lvt_1db88642e346389874251b5a1eded6e3=1699697060; device_id=1a375e35f186bff0e577cc75d781a0af; s=af12g4bgex; __utma=1.1194915567.1699697069.1699697069.1699697069.1; __utmc=1; __utmz=1.1699697069.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lpvt_1db88642e346389874251b5a1eded6e3=1699697081; acw_tc=2760827d16997558965437479e75b191de6c29df1bab2460f5097d7f2fa460");
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
                return bondPrice;
            }


            // 当天最高价
            body = result.getBody();
            assert body != null;
            if (body.contains("\"flagStr\":\"退市\"") || body.contains("\"flagStr\":\"停牌\"")) {
                // 收集已退市的可转债，更新表中的状态
                BondInfo bondInfo = bondInfoDao.selectById(bondId);
                if (bondInfo != null) {
                    bondInfo.setFlag(2);
                    bondInfo.setUpdateTime(new Date());
                    bondInfoDao.updateById(bondInfo);
                    log.info("可转债{}已退市 或 停牌", bondInfo.getBondNm());
                    return bondPrice;
                }
            }

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
                throw new Exception("雪球网详情页面，获取最高价格失败");
            }
            String maxPriceStrSubstring = body.substring(maxPriceIndex + maxPriceStr.length());
            String maxPriceValueStr = maxPriceStrSubstring.substring(0, maxPriceStrSubstring.indexOf("</span>"));
            try {
                BigDecimal maxPriceBigDecimal = new BigDecimal(maxPriceValueStr);
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("maxPriceStrSubstring={}  maxPriceValueStr={} ", maxPriceStrSubstring, maxPriceValueStr);
                log.error("最高价格转换异常", e.getCause());
            }
            bondPrice.setMaxPrice(new BigDecimal(maxPriceValueStr));

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
                throw new Exception("雪球网详情页面，获取最低价格失败");
            }
            String minPriceStrSubstring = body.substring(minPriceIndex + minPriceStr.length());
            String minPriceValueStr = minPriceStrSubstring.substring(0, minPriceStrSubstring.indexOf("</span>"));
            try {
                BigDecimal minPriceBigDecimal = new BigDecimal(minPriceValueStr);
            } catch (Exception e) {
                log.error("xueqiuBondDetailUrl={}  minPriceStrSubstring={}", xueqiuBondDetailUrl, minPriceStrSubstring);
                log.error("最低价格转换异常", e.getCause());
                e.printStackTrace();
            }
            bondPrice.setMinPrice(new BigDecimal(minPriceValueStr));

            String currentPriceStr = "<strong>¥";
            int currentPriceIndex = body.indexOf(currentPriceStr);
            String currentPriceStrSubstring = body.substring(currentPriceIndex + currentPriceStr.length());
            String currentPriceValueStr = currentPriceStrSubstring.substring(0, currentPriceStrSubstring.indexOf("</strong>"));
            bondPrice.setPrice(new BigDecimal(currentPriceValueStr));

            String redeemPriceStr = "到期赎回价：<span>";
            int redeemPriceIndex = body.indexOf(redeemPriceStr);
            String redeemPriceStrSubstring = body.substring(redeemPriceIndex + redeemPriceStr.length());
            String redeemPriceValueStr = redeemPriceStrSubstring.substring(0, redeemPriceStrSubstring.indexOf("</span>"));
            bondPrice.setRedeemPrice(new BigDecimal(redeemPriceValueStr));

            // 成交额
            String volumeStr = "成交额：<span>";
            int volumeIndex = body.indexOf(volumeStr);
            String volumeStrSubstring = body.substring(volumeIndex + volumeStr.length());
            String volumeValueStr = volumeStrSubstring.substring(0, volumeStrSubstring.indexOf("</span>"));
            if (volumeValueStr.endsWith("万")) {
                bondPrice.setVolume(new BigDecimal(volumeValueStr.replace("万", "")));
            } else if (volumeValueStr.endsWith("亿")) {
                bondPrice.setVolume(new BigDecimal(volumeValueStr.replace("亿", "")).multiply(new BigDecimal(10000)));
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("雪球详情页访问失败：{}, body={}", e.getCause(), body);

        }

        return bondPrice;
    }

    @Deprecated
    private BondPrice getBondPrice(ChromeDriver driver, Integer bondId) {
        BondDetailInfo bondDetailInfo = new BondDetailInfo();
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

        List<WebElement> elements = driver.findElements(By.className("quote-info"));
        WebElement tableWebElement = elements.get(0);
        List<WebElement> trList = tableWebElement.findElements(By.tagName("tr"));
        for (int j = 0; j < trList.size(); j++) {
            WebElement tr = trList.get(j);
            if (j == 0) {
                List<WebElement> tdList = tr.findElements(By.tagName("td"));
                for (int i = 0; i < tdList.size(); i++) {
                    WebElement td = tdList.get(i);
                    WebElement span = td.findElement(By.tagName("span"));
                    String text = span.getText();
                    if (i == 0) {
                        bondPrice.setMaxPrice(new BigDecimal(text));
                    }
                }
            } else if (j == 1) {
                List<WebElement> tdList = tr.findElements(By.tagName("td"));
                for (int i = 0; i < tdList.size(); i++) {
                    WebElement td = tdList.get(i);
                    WebElement span = td.findElement(By.tagName("span"));
                    String text = span.getText();
                    if (i == 0) {
                        bondPrice.setMinPrice(new BigDecimal(text));
                    } else if (i == 2) {
                        if (text.endsWith("万")) {
                            bondPrice.setVolume(new BigDecimal(text.replace("万", "")));
                        } else if (text.endsWith("亿")) {
                            bondPrice.setVolume(new BigDecimal(text.replace("亿", "")).multiply(new BigDecimal(10000)));
                        }
                    }
                }
            }
        }


//        到期税后收益


        return bondPrice;
//        redeem_price
        // todo 下修天计数
    }


    private BondDetailInfo getRedeemPrice2(Integer bondId) {
        BondDetailInfo bondDetailInfo = new BondDetailInfo();

        String bondDetailUrl = "https://www.jisilu.cn/data/convert_bond_detail/" + bondId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(HttpHeaders.COOKIE, "compare_dish=show; kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=1699508886; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpqplqWppqCrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1699685351");
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43");
        headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8");

        HashMap<Object, Object> params = new HashMap<>();
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = null;

        try {
            result = restTemplate.exchange(bondDetailUrl, HttpMethod.GET, entity, String.class);
            if (HttpStatus.OK.equals(result.getStatusCode())) {
                log.info("详情页访问成功");
            } else {
                log.error("详情页访问失败：{}", result);
                return bondDetailInfo;
            }

            // 到期赎回价
            String body = result.getBody();
            assert body != null;
            String redeemPriceStr = "id=\"redeem_price\" class=\"data_val\">";
            int redeemPriceIndex = body.indexOf(redeemPriceStr);
            String redeemPriceSubstring = body.substring(redeemPriceIndex + redeemPriceStr.length());
            String redeemPriceValueStr = redeemPriceSubstring.substring(0, redeemPriceSubstring.indexOf("</td>"));
            if (StringUtils.isNotBlank(redeemPriceValueStr)) {
//                Integer redeemPriceValue = Integer.valueOf(redeemPriceValueStr.trim());
                log.info("redeemPriceValueStr={}", redeemPriceValueStr);
                try {
                    BigDecimal bigDecimal = new BigDecimal(redeemPriceValueStr.trim());
                    bondDetailInfo.setRedeemPrice(bigDecimal);
                } catch (Exception e) {
                    e.printStackTrace();
                    String flagStr = "\\+";
                    String totalStr = "合计到期赎回价";
                    if(redeemPriceValueStr.contains(totalStr)){
                        String substring = redeemPriceValueStr.substring(redeemPriceValueStr.indexOf(totalStr) + totalStr.length());
                        String redeem_price_str = substring.substring(0, substring.indexOf("元"));
                        log.info("redeem_price_str={}", redeem_price_str);
                        BigDecimal redeem_price_bigDecimal = new BigDecimal(redeem_price_str.trim());
                        bondDetailInfo.setRedeemPrice(redeem_price_bigDecimal);
                    }
//                    if (redeemPriceValueStr.contains("+")) {
//                        System.out.println("redeem_price = " + redeemPriceValueStr);
//                        String[] split = redeemPriceValueStr.split(flagStr);
//                        BigDecimal bigDecimal = new BigDecimal(0);
//                        for (String s : split) {
//                            bigDecimal = bigDecimal.add(new BigDecimal(s));
//                        }
//                        System.out.println("bigDecimal = " + bigDecimal);
//                    } else {
//                        log.info("异常 redeem_price={}", redeemPriceValueStr);
//                    }
                }

            }

            // 上市日
            String listDtStr = "id=\"list_dt\">";
            int listDtIndex = body.indexOf(listDtStr);
            String listDtSubstring = body.substring(listDtIndex + listDtStr.length());
            String listDtValueStr = listDtSubstring.substring(0, listDtSubstring.indexOf("</td>"));
            if (StringUtils.isNotBlank(listDtValueStr) && !listDtValueStr.equals("-")) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date listDt = null;
                try {
                    listDt = simpleDateFormat.parse(listDtValueStr.trim());
                } catch (ParseException e) {
                    e.printStackTrace();
                    log.error("bondDetailUrl={}  listDtValueStr={}", bondDetailUrl, listDtValueStr);
                }
                bondDetailInfo.setListDt(listDt);
            }

            // 到期日
            String maturityDtStr = "id=\"maturity_dt\" nowrap>";
            int maturityDtIndex = body.indexOf(maturityDtStr);
            String maturityDtSubstring = body.substring(maturityDtIndex + maturityDtStr.length());
            String maturityDtValueStr = maturityDtSubstring.substring(0, maturityDtSubstring.indexOf("</td>"));
            if (StringUtils.isNotBlank(maturityDtValueStr)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date maturityDt = null;
                try {
                    maturityDt = simpleDateFormat.parse(maturityDtValueStr.trim());
                } catch (ParseException e) {
                    log.error("maturityDtValueStr={}", maturityDtValueStr);
                    e.printStackTrace();
                }
                bondDetailInfo.setMaturityDt(maturityDt);
            }

//            到期税后收益率  暂不使用
            String ytmRtTaxStr = "colspan=\"2\">到期税后收益";
            int ytmRtTaxIndex = body.indexOf(ytmRtTaxStr);
            String ytmRtTaxSubstring = body.substring(ytmRtTaxIndex + ytmRtTaxStr.length());
            String ytmRtTaxValueStr = ytmRtTaxSubstring.substring(0, ytmRtTaxSubstring.indexOf("</td>"));
        } catch (Exception e) {
            log.error("详情页访问失败：" + e.getMessage() + "  bondDetailUrl=" + bondDetailUrl);
        }

        return bondDetailInfo;
    }

    @Deprecated
    private BondDetailInfo getRedeemPrice(ChromeDriver driver, Integer bondId) {
        BondDetailInfo bondDetailInfo = new BondDetailInfo();
        driver.get("https://www.jisilu.cn/data/convert_bond_detail/" + bondId);

        //等待页面加载完成，后续写代码注意，如https://www.taobao.com/果页面加载未完成，可能导致页面元素找不到
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("当前打开页面的标题是： " + driver.getTitle() + " bondId=" + bondId);
//        到期赎回价
        String redeem_price = driver.findElement(By.id("redeem_price")).getText();
//        上市日
        String list_dt = driver.findElement(By.id("list_dt")).getText();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (list_dt.equals("-")) {
                log.error("list_dt={} bondId={}", list_dt, bondId);
            }
            Date listDt = simpleDateFormat.parse(list_dt);
            bondDetailInfo.setListDt(listDt);
        } catch (java.text.ParseException e) {
            log.error("list_dt={}", list_dt);
            e.printStackTrace();
        }
//        到期日
        String maturity_dt = driver.findElement(By.id("maturity_dt")).getText();
        try {
            Date maturityDt = simpleDateFormat.parse(maturity_dt);
            bondDetailInfo.setMaturityDt(maturityDt);
        } catch (java.text.ParseException e) {
            log.error("maturityDt={}", maturity_dt);
            e.printStackTrace();
        }
//        到期税后收益

        try {
            BigDecimal bigDecimal = new BigDecimal(redeem_price);
            bondDetailInfo.setRedeemPrice(bigDecimal);
        } catch (Exception e) {
//            e.printStackTrace();
            String flagStr = "\\+";
            if (redeem_price.contains("+")) {
                System.out.println("redeem_price = " + redeem_price);
                String[] split = redeem_price.split(flagStr);
                BigDecimal bigDecimal = new BigDecimal(0);
                for (String s : split) {
                    bigDecimal = bigDecimal.add(new BigDecimal(s));
                }
                System.out.println("bigDecimal = " + bigDecimal);
            } else {
                log.info("异常 redeem_price={}", redeem_price);
            }
        }


        return bondDetailInfo;
//        redeem_price
        // todo 下修天计数
    }


}
