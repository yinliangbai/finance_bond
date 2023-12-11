package com.baiyinliang.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baiyinliang.finance.entity.BondRatingCd;
import com.baiyinliang.finance.enums.BusinessEnums;
import com.baiyinliang.finance.mapper.BondBaseInfoDao;
import com.baiyinliang.finance.mapper.BondRatingCdDao;
import com.baiyinliang.finance.service.BondRatingCdService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 可转债评级表 服务实现类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Service
@Slf4j
public class BondRatingCdServiceImpl extends ServiceImpl<BondRatingCdDao, BondRatingCd> implements BondRatingCdService {

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    private BondRatingCdDao bondRatingCdDao;

    @Autowired
    private BondBaseInfoDao baseInfoDao;

    @Autowired
    private CommonServiceImpl commonServiceImpl;

    /**
     * 评级数据新增
     */
    @Override
    public void addBondRatingCdList(){
//        Set<String> notExistsCodeFromRatingCd = getNotExistsCodeFromRatingCd();
//        if (CollectionUtils.isEmpty(notExistsCodeFromRatingCd)) {
//            log.info("传入编码集合为空");
//            return;
//        }
        List<BondBaseInfo> listBondBaseInfo = commonServiceImpl.getListBondBaseInfo();
        if(CollectionUtils.isEmpty(listBondBaseInfo)){
            log.info("bond数据为空");
            return;
        };
        List<String> bondCodeList = listBondBaseInfo.stream().map(BondBaseInfo::getBondCode).collect(Collectors.toList());
        List<BondRatingCd> addBondRatingCdList = getAddBondRatingCdList(bondCodeList);
        if (CollectionUtils.isEmpty(addBondRatingCdList)) {
            log.info("本次待新增的评级数据为空，入参为={}", JSON.toJSONString(bondCodeList));
            return;
        }

        // 调用事务访求
        ((BondRatingCdServiceImpl) AopContext.currentProxy()).batchInsert(addBondRatingCdList);
    }

    @Transactional
    public void batchInsert(List<BondRatingCd> addBondRatingCdList){
        int insertCount = bondRatingCdDao.batchInsert(addBondRatingCdList);
        log.info("本次新增评级条数：{}", insertCount);
    }

    // 联表查bond表中上市状态的 且在 RatingCd表中不存在
    private Set<String> getNotExistsCodeFromRatingCd(){
        Set<String> notExistsBondCodeSet = new HashSet<>();

        // 拆两个单表查
        QueryWrapper<BondRatingCd> bondRatingCdQueryWrapper = new QueryWrapper<>();
        bondRatingCdQueryWrapper.select("DISTINCT bond_code ");
        List<BondRatingCd> bondRatingCds = bondRatingCdDao.selectList(bondRatingCdQueryWrapper);

        LambdaQueryWrapper<BondBaseInfo> bondBaseInfoQueryWrapper = new LambdaQueryWrapper<>();
        bondBaseInfoQueryWrapper.select(BondBaseInfo::getBondCode).eq(BondBaseInfo::getFlag, BusinessEnums.BondCodeFlag.上市.getFlag());
        List<BondBaseInfo> bondBaseInfoList = baseInfoDao.selectList(bondBaseInfoQueryWrapper);
        if(CollectionUtils.isEmpty(bondBaseInfoList)){
            log.info("bond数据为空");
            return notExistsBondCodeSet;
        }

        if(CollectionUtils.isEmpty(bondRatingCds)){
            log.info("bond评级数据为空");
            for (BondBaseInfo bondBaseInfo : bondBaseInfoList) {
                String bondCode = bondBaseInfo.getBondCode();
                notExistsBondCodeSet.add(bondCode);
            }
        }else {
            Set<String> bondCodeSetFromRatingCd = new HashSet<>();
            for (BondRatingCd bondRatingCd : bondRatingCds) {
                bondCodeSetFromRatingCd.add(bondRatingCd.getBondCode());
            }
            for (BondBaseInfo bondBaseInfo : bondBaseInfoList) {
                String bondCode = bondBaseInfo.getBondCode();
                if(!bondCodeSetFromRatingCd.contains(bondCode)){
                    notExistsBondCodeSet.add(bondCode);
                }
            }
        }

        return notExistsBondCodeSet;
    }


    private List<BondRatingCd> getAddBondRatingCdList(Collection<String> bondCodeList) {

        List<BondRatingCd> addBondRatingCdList = new ArrayList<>();
        for (String bonCode : bondCodeList) {
            BondRatingCd bondRatingCd = doGetRatingCdFromJSL(bonCode);
            if (bondRatingCd == null) {
                log.error("{}的评级数据为空", bonCode);
            } else {
                addBondRatingCdList.add(bondRatingCd);
            }
        }

        return addBondRatingCdList;
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

    // 债券评级
    private BondRatingCd doGetRatingCdFromJSL(String bondCode) {
        String bondInfoUrl = "https://www.jisilu.cn/data/convert_bond_detail/" + bondCode;

        //                访问数据请求
        ResponseEntity<String> result = getBondBaseInfoWeb(bondInfoUrl);
        if (HttpStatus.OK.equals(result.getStatusCode())) {
//                            log.info("详情页访问成功");
        } else {
            log.error("详情页{}访问失败：{}", bondInfoUrl, result);
            return null;
        }

        String body = result.getBody();
        assert body != null;

        // 该转债不存在！
        String notExistsStr = "该转债不存在！";
        if (body.contains(notExistsStr)) {
            log.warn(notExistsStr);
            log.error("详情页{},{}：{}", bondInfoUrl,notExistsStr, result);
            return null;
        }

        BondRatingCd bondRatingCd = new BondRatingCd();
        bondRatingCd.setBondCode(bondCode);
        String ratingCdStr = "id=\"rating_cd\">";
        String ratingCdValStr = StringUtils.EMPTY;
        try {
            int ratingCdIndex = body.indexOf(ratingCdStr);
            String ratingCdSubstr = body.substring(ratingCdIndex + ratingCdStr.length());
            ratingCdValStr = ratingCdSubstr.substring(0, ratingCdSubstr.indexOf("</td>"));
            bondRatingCd.setRatingCd(ratingCdValStr);
        } catch (Exception e) {
            log.error("详情页{} 债券评级获取失败body={} ratingCdValStr={}", bondInfoUrl, body, ratingCdValStr);
            e.printStackTrace();
            return null;
        }

        return bondRatingCd;
    }

}
