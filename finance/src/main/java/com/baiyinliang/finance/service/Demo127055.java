package com.baiyinliang.finance.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baiyinliang.finance.model.Bond;
import com.baiyinliang.finance.model.PageModel;
import com.baiyinliang.finance.tools.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 官方API
 * https://www.selenium.dev/documentation/webdriver/bidirectional/
 */
@Slf4j
public class Demo127055 {

    /**
     * 是否开启无头浏览器模式(不打开浏览器界面看不到界面 速度更快)
     * linux服务器 需要用true
     * 声明一个ChromeOptions变量，并配置headless属性为true，表示不在前台打开chrome。
     */
    public static boolean headless = false;

    /**
     * 是否禁用图片
     */
    public static boolean imagesEnabled = false;

    /**
     * chromedirver存放位置
     */
    public static String chromedirverPath = "D:\\Projects\\chromedriver.exe";


    public static void main(String[] args) {
        System.out.println(System.getProperty("file.encoding"));

        // 设置 chromedirver的存放位置
        System.getProperties().setProperty("webdriver.chrome.driver", chromedirverPath);

        // 设置浏览器参数
        ChromeOptions chromeOptions = new ChromeOptions();

        //是否开启无头浏览模式
        chromeOptions.setHeadless(headless);
        chromeOptions.getCapabilityNames();

        //设置为 headless 模式避免报错用的参数
        chromeOptions.addArguments("--disable-gpu");

        //禁用沙箱
        chromeOptions.addArguments("--no-sandbox");

        //禁用开发者shm
        chromeOptions.addArguments("--disable-dev-shm-usage");
/*
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        //文档地址:https://www.selenium.dev/zh-cn/documentation/webdriver/capabilities/shared/#unhandledpromptbehavior
        chromeOptions.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);

        //传下面参数来禁止掉谷歌受自动化控制的信息栏
        chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        if (!headless) {//有界面时给予跳过弹窗的处理
            Object prefs = SeleniumUtil.getAllowProtocolPrefs();
            chromeOptions.setExperimentalOption("prefs", prefs);
        }

        // 禁用保存密码提示框
        Map<String, Object> prefs = new HashMap();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        chromeOptions.setExperimentalOption("prefs", prefs);

        if (imagesEnabled) {
            //禁用图片
            chromeOptions.addArguments("blink-settings=imagesEnabled=false");
        }*/

        /**
         * 反反爬虫 Start
         */
        chromeOptions.addArguments("--disable-blink-features");
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
        /**
         * 反反爬虫 End
         */


        //初始化
        ChromeDriver driver = new ChromeDriver(chromeOptions);   //初始化一个chrome驱动实例，保存到driver中


        //清理cooki
//        driver.manage().deleteAllCookies();

        // 与浏览器同步非常重要，必须等待浏览器加载完毕
        //隐式等待10秒
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //最大化窗口
        driver.manage().window().maximize();  //最大化窗口
        //设置隐性等待时间

//        driver.get("https://www.jisilu.cn/data/convert_bond_detail/127055");
        driver.get("https://www.jisilu.cn/data/convert_bond_detail/113595");

        //等待页面加载完成，后续写代码注意，如https://www.taobao.com/果页面加载未完成，可能导致页面元素找不到
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("当前打开页面的标题是： " + driver.getTitle());

        WebElement body = driver.findElement(By.tagName("body"));
        System.out.println("JSON.toJSONString(body) = " + body);
//        WebElement grid_data_content = body.findElement(By.className("grid data_content"));
//        System.out.println("grid_data_content = " + grid_data_content);
        WebElement tc_data = body.findElement(By.id("tc_data"));
        System.out.println("tc_data = " + tc_data);
        WebElement info_box = tc_data.findElement(By.className("info_box"));
        System.out.println("info_box = " + info_box);
        WebElement info_data = info_box.findElement(By.className("info_data"));
        System.out.println("info_data = " + info_data);
        WebElement jisilu_tcdata = info_data.findElement(By.className("jisilu_tcdata"));
        System.out.println("jisilu_tcdata = " + jisilu_tcdata);
        WebElement bond_nm = jisilu_tcdata.findElement(By.className("bond_nm"));
        System.out.println("bond_nm = " + bond_nm);
        WebElement bondNameElement = driver.findElement(By.tagName("body")).findElement(By.id("tc_data")).findElement(By.className("info_box"))
                .findElement(By.className("info_data")).findElement(By.className("jisilu_tcdata")).findElement(By.className("bond_nm"));
        if (bondNameElement == null) {
            System.err.println("为空了");
        } else {
            System.out.println("JSON.toJSONString(bondNameElement) = " + bondNameElement);
            String text = bondNameElement.getText();
            System.out.println("text = " + text);
        }

        String font_18 = bondNameElement.findElement(By.className("font_18")).getText();

        System.out.println("font_18 = " + font_18);


        // 转股起始日
        /*List<WebElement> tbodyList = driver.findElements(By.tagName("tbody"));
        WebElement tbody = tbodyList.get(0);
        System.out.println("tbody = " + tbody);
        List<WebElement> trList = tbody.findElements(By.tagName("tr"));
        WebElement tr = trList.get(0);
        System.out.println("tr = " + tr);*/
        WebElement convert_dt = driver.findElement(By.id("convert_dt"));
        System.out.println("convert_dt = " + convert_dt);
        System.out.println("convert_dt.getText() = " + convert_dt.getText());
        WebElement maturity_dt = driver.findElement(By.id("maturity_dt"));
        System.out.println("maturity_dt.getText() = " + maturity_dt.getText());
        String redeem_price = driver.findElement(By.id("redeem_price")).getText();
        System.out.println("redeem_price = " + redeem_price);

        ////////////////////////////////////////////////////////////////////////////////////////
//        driver.get("https://www.jisilu.cn/data/cbnew/cb_list_new/?___jsl=LST___");
//        driver.get("fprice=&tprice=124&curr_iss_amt=6&volume=&svolume=&premium_rt=&ytm_rt=&rating_cd=&is_search=Y&market_cd[]=shmb&market_cd[]=shkc&market_cd[]=szmb&market_cd[]=szcy&btype=C&listed=Y&qflag=N&sw_cd=&bond_ids=&rp=50");
//

        String loginUrl = "https://www.jisilu.cn/webapi/account/login_process/";
        Map<String, String> loginParamMap = new HashMap<>();
        loginParamMap.put("return_url", "https://www.jisilu.cn/");
        loginParamMap.put("user_name", "5afd3be470bb961aeb6646a2040848f8");
        loginParamMap.put("password", "8676cf8c7adfcffe1400a3abdfdcfc89");
        loginParamMap.put("auto_login", "1");
        loginParamMap.put("aes", "1");
        try {
            String loginResult = HttpClientUtil.simplePost(loginUrl, loginParamMap, null);
            log.info("loginResult--------->" + loginResult);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("login error e = " + e);
        }
        System.out.println("登录成功");


        String url = "https://www.jisilu.cn/data/cbnew/cb_list_new/?___jsl=LST___&fprice=&tprice=124&curr_iss_amt=6&volume=&svolume=&premium_rt=&ytm_rt=&rating_cd=&is_search=Y&market_cd[]=shmb&market_cd[]=shkc&market_cd[]=szmb&market_cd[]=szcy&btype=C&listed=Y&qflag=N&sw_cd=&bond_ids=&rp=50";
        Map<String, String> paramMap = new HashMap<>();
        try {
            paramMap.put("tprice", "124");
            paramMap.put("curr_iss_amt", "6");
            paramMap.put("is_search", "Y");
            paramMap.put("btype", "C");
            paramMap.put("listed", "Y");
            paramMap.put("qflag", "N");
            String getResult = HttpClientUtil.simplePost(url, paramMap, null);
            log.info("getResult--------->" + getResult);

            PageModel pageModel = JSONObject.parseObject(getResult, PageModel.class);
            log.info("pageModel={}", JSON.toJSONString(pageModel));

            List<PageModel.Row> rows = pageModel.getRows();
//            Collections.sort(rows, Comparator.comparing(get))
            List<Bond> bonds = new ArrayList<>();
            Map<Integer, Bond> bondMap = new HashMap<>();
            Set<Integer> bondIds = new HashSet<>();
            for (PageModel.Row row : rows) {
                PageModel.Cell cell = row.getCell();
                if(cell.getYear_left() == null){
                    log.warn("{}的剩余年限为空",cell.getBond_nm());
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
                BeanUtils.copyProperties(cell, bond);
                Integer bond_id = bond.getBond_id();
                if(bond_id == null){
                    log.warn("{}的bond_id为空",cell.getBond_nm());
                    continue;
                }
                bondIds.add(bond_id);
                bondMap.put(bond_id, bond);
                bonds.add(bond);
            }

            // 遍历 获取每一个的 强赎价
            Map<Integer, BigDecimal> redeemPriceMap = batchGetRedeemPrice(driver, bondIds);
            for (Map.Entry<Integer, Bond> bondEntry : bondMap.entrySet()) {
                Integer bondId = bondEntry.getKey();
                Bond bond = bondEntry.getValue();
                bond.setRedeem_price(redeemPriceMap.get(bondId));
//                bond.setEarnings_price(bond.getRedeem_price().subtract(bond.getPrice()));
            }

            // 按换手率排序 ，然后 价格小于 强赎价的
            // 换手率排序
//            List<Bond> topBondList = new ArrayList<>();
//            Collections.sort(bonds, Comparator.comparing(Bond::getConvert_value));
//            for (int i = bonds.size() - 1; i >= 0; i--) {
//                // 只取前5个
//                Bond bond = bonds.get(i);
//                topBondList.add(bond);
//            }

            // 稳定率 多天的价格波动

            // 更稳定的策略 ，筛选得到价格低于强赎价的，然后按价格从小到大， 然后排换手率
            // 按  强赎价-价格 的差排序， 差值越大，排最靠前
                        List<Bond> topBondList = new ArrayList<>();
            List<Bond> list = new ArrayList<Bond>(bondMap.values());
//            Collections.sort(list, Comparator.comparing(Bond::getEarnings_price));
            for (int i = list.size() - 1; i >= 0; i--) {
                //                // 只取前10个
                if(topBondList.size() == 10){
                    break;
                }
                Bond bond = bonds.get(i);
                topBondList.add(bond);
            }

            // 再按换手率排序
            Collections.sort(topBondList, Comparator.comparing(Bond::getTurnover_rt));
            log.info("最终的 topBondList={}", JSON.toJSONString(topBondList));

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }


        //关闭并退出浏览器
        driver.quit();


    }


    private static Map<Integer, BigDecimal> batchGetRedeemPrice(ChromeDriver driver, Collection<Integer> bondIds) {
        Map<Integer, BigDecimal> redeemPriceMap = new HashMap<>();
        for (Integer bondId : bondIds) {
            BigDecimal redeemPrice = getRedeemPrice(driver, bondId);
            redeemPriceMap.put(bondId, redeemPrice);
        }

        return redeemPriceMap;
    }


    private static BigDecimal getRedeemPrice(ChromeDriver driver, Integer bondId) {
        driver.get("https://www.jisilu.cn/data/convert_bond_detail/" + bondId);

        //等待页面加载完成，后续写代码注意，如https://www.taobao.com/果页面加载未完成，可能导致页面元素找不到
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("当前打开页面的标题是： " + driver.getTitle() + " bondId=" + bondId);
        String redeem_price = driver.findElement(By.id("redeem_price")).getText();

//        到期税后收益

        try {
            BigDecimal bigDecimal = new BigDecimal(redeem_price);
            return bigDecimal;
        } catch (Exception e) {
//            e.printStackTrace();
            String flagStr = "\\+";
            if(redeem_price.contains("+")){
                System.out.println("redeem_price = " + redeem_price);
                String[] split = redeem_price.split(flagStr);
                BigDecimal bigDecimal = new BigDecimal(0);
                for (String s : split) {
                    bigDecimal = bigDecimal.add(new BigDecimal(s));
                }
                System.out.println("bigDecimal = " + bigDecimal);
                return bigDecimal;
            }else {
                log.info("异常 redeem_price={}", redeem_price);
            }
        }


        return new BigDecimal(redeem_price);
//        redeem_price
        // todo 下修天计数
    }

}