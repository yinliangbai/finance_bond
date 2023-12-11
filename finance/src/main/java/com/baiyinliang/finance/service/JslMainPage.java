package com.baiyinliang.finance.service;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;

public class JslMainPage {
    // https://www.jisilu.cn/web/data/cb/list
    public static void main(String[] args) {
        /*//下载的chromedriver位置
        System.setProperty("webdriver.chrome.driver","D:\\a\\Downloads\\chromedriver.exe");
        //实例化ChromeDriver对象
        WebDriver driver = new ChromeDriver();
        String url="https://www.jisilu.cn/web/data/cb/list";
        //打开指定网站
        driver.get(url);
        //解析页面
        String pageSource =driver.getPageSource();
        WebElement name = driver.findElement(By.id("aw-login-user-name"));
        WebElement password = driver.findElement(By.id("aw-login-user-password"));
        name.sendKeys("aaaaaaaaaa");
        System.out.println(name.getText());

        //定义选择器规则
        String rule="#resultList > div:nth-child(4) > p > span > a";
        //通过选择器拿到元素
        //模拟浏览器点击
        driver.findElement(By.cssSelector(rule)).click();*/

        WebDriver driver;   //声明WebDriver  "D:\Program Files\ModifiableWindowsApps\Mozilla Firefox\firefox.exe"
        System.setProperty("webdriver.firefox.marionette", "D:\\Program Files\\ModifiableWindowsApps\\Mozilla Firefox\\firefox.exe");
//指定Firefox浏览器的路径
        String Url = "https://www.baidu.com";   //百度的地址
        driver = new FirefoxDriver();        //new一个FirefoxDriver()
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);    //设置隐式等待10秒钟
        driver.get(Url);    //打开百度首页
        driver.manage().window().maximize();    //把浏览器窗口最大化
        try {
            Thread.sleep(3000);     //让线程等待3秒钟
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.quit();  //退出driver

    }
}
