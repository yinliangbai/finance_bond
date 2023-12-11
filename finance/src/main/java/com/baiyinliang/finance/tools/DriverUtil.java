package com.baiyinliang.finance.tools;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverUtil {

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

    public static ChromeDriver getChromeDriver() {
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
        return new ChromeDriver(chromeOptions);   //初始化一个chrome驱动实例，保存到driver中
    }
}
