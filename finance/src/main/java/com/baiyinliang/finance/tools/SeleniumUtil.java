package com.baiyinliang.finance.tools;


import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class SeleniumUtil {


    public static void handlerAlert(ChromeDriver driver) {
        //        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
//                .withTimeout(Duration.ofSeconds(30))
//                .pollingEvery(Duration.ofSeconds(5));
        Wait<WebDriver> wait = new WebDriverWait(driver, 1l);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.dismiss();
        } catch (Exception e) {
        }
    }

    public static Object getCustomProtocolPrefs() {
        Map<String, Object> ignoredProtocolHandlers = new HashMap<>();
        ignoredProtocolHandlers.put("protocol", "macappstores");
        Map<String, Object> customHandlers = new HashMap<>();
        customHandlers.put("ignored_protocol_handlers", Arrays.asList(ignoredProtocolHandlers));
        customHandlers.put("enabled", true);
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("custom_handlers", customHandlers);
        return prefs;
    }

    public static Object getExcludedProtocolPrefs() {
        Map<String, Object> excludedSchemes = new HashMap<>();
        excludedSchemes.put("macappstores", true);
        Map<String, Object> protocolHandler = new HashMap<>();
        protocolHandler.put("excluded_schemes", excludedSchemes);
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("protocol_handler", protocolHandler);
        return prefs;
    }


    public static Object getAllowProtocolPrefs() {
        Map<String, Object> appleUrl = new HashMap<>();
        appleUrl.put("macappstores", true);
        Map<String, Object> allowedOriginProtocolPairs = new HashMap<>();
        allowedOriginProtocolPairs.put("https://apps.apple.com", appleUrl);
        Map<String, Object> protocolHandler = new HashMap<>();
        protocolHandler.put("allowed_origin_protocol_pairs", allowedOriginProtocolPairs);
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("protocol_handler", protocolHandler);
        return prefs;
    }


    /**
     * 点击按钮 如果不存在则等待
     * @param driver
     * @param elementId
     * @param name
     */
    protected void clickConfirmWindow(WebDriver driver, By by, String name) {
        // Check if warning window is displayed using button ID
        System.out.println("Searching " + name + " using " + by);
        if (isClickable(driver, by, 1)) {
            System.out.println("Found " + name + " using " + by);
            driver.findElement(by).click();
        }
    }


    /**
     * 检查元素是否存在 等待
     * @param driver
     * @param by
     * @param timeOut
     * @return
     */
    private Boolean isClickable(WebDriver driver, By by, int timeOut) {
        try {
            new WebDriverWait(driver, timeOut).until(ExpectedConditions.visibilityOfElementLocated(by));
            return true;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return false;
        }
    }
}
