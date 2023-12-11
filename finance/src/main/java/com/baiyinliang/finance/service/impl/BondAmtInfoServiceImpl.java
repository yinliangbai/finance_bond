package com.baiyinliang.finance.service.impl;

import com.alibaba.fastjson.JSON;
import com.baiyinliang.finance.entity.BondAmtInfo;
import com.baiyinliang.finance.entity.BondBaseInfo;
import com.baiyinliang.finance.mapper.BondAmtInfoDao;
import com.baiyinliang.finance.service.BondAmtInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.baiyinliang.finance.common.Constants.DECIMAL_1000;

/**
 * <p>
 * 可转债剩余规模表 服务实现类
 * </p>
 *
 * @author root
 * @since 2023-11-26
 */
@Service
@Slf4j
public class BondAmtInfoServiceImpl extends ServiceImpl<BondAmtInfoDao, BondAmtInfo> implements BondAmtInfoService {
    @Resource
    private RestTemplate restTemplate;

    @Autowired
    private BondAmtInfoDao bondAmtInfoDao;

    @Autowired
    private CommonServiceImpl commonServiceImpl;


    @Override
    public void addCurrIssAmtList(){
        List<BondBaseInfo> listBondBaseInfo = commonServiceImpl.getListBondBaseInfo();
        if(CollectionUtils.isEmpty(listBondBaseInfo)){
            log.info("bond数据为空");
            return;
        }

        List<String> bondCodeList = listBondBaseInfo.stream().map(BondBaseInfo::getBondCode).collect(Collectors.toList());
        List<BondAmtInfo> bondAmtInfoList = getBondAmtInfoList(bondCodeList);
        if (CollectionUtils.isEmpty(bondAmtInfoList)) {
            log.info("本次待新增的剩余规模数据为空，入参为={}", JSON.toJSONString(bondCodeList));
            return;
        }

        // 调用事务访求
        ((BondAmtInfoServiceImpl) AopContext.currentProxy()).batchInsert(bondAmtInfoList);
    }

    @Transactional
    public void batchInsert(List<BondAmtInfo> addBondRatingCdList){
        int insertCount = bondAmtInfoDao.batchInsert(addBondRatingCdList);
        log.info("本次新增剩余规模条数：{}", insertCount);
    }

    private List<BondAmtInfo> getBondAmtInfoList(List<String> bondCodeList){
        List<BondAmtInfo> bondAmtInfoList = new ArrayList<>();
        for (String bondCode : bondCodeList) {
            BondAmtInfo currIssAmtFromJSL = getCurrIssAmtFromJSL(bondCode);
            if(currIssAmtFromJSL == null){
                log.error("{}的余额数据为空", bondCode);
                continue;
            }
            bondAmtInfoList.add(currIssAmtFromJSL);
        }

        return bondAmtInfoList;
    }




    // 剩余规模
    private BondAmtInfo getCurrIssAmtFromJSL(String bondCode) {
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

        BondAmtInfo bondAmtInfo = new BondAmtInfo();
        bondAmtInfo.setBondCode(bondCode);
        //                剩余规模(亿)
        String currIssAmtStr = "id=\"curr_iss_amt\" title=\"按转债面值计算，亿元\" class=\"data_val\">";
        int currIssAmtIndex = body.indexOf(currIssAmtStr);
        String currIssAmtSubstr = body.substring(currIssAmtIndex + currIssAmtStr.length());
        String currIssAmtValStr = currIssAmtSubstr.substring(0, currIssAmtSubstr.indexOf("</td>"));
        BigDecimal currIssAmtVal = null;
        try {
            currIssAmtVal = new BigDecimal(currIssAmtValStr).multiply(DECIMAL_1000);
            bondAmtInfo.setCurrIssAmt(currIssAmtVal.intValue());
        } catch (Exception e) {
            log.error("剩余规模(亿)获取失败 currIssAmtValStr={}", currIssAmtValStr);
            e.printStackTrace();
        }

        return bondAmtInfo;
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

}
