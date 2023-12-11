package com.baiyinliang.finance.service;





import cn.hutool.core.codec.Base64;
import cn.hutool.core.thread.ThreadUtil;
import com.baiyinliang.finance.tools.SeleniumUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;

import org.openqa.selenium.devtools.v108.network.Network;
import org.openqa.selenium.devtools.v108.network.model.Request;
import org.openqa.selenium.devtools.v108.network.model.RequestId;
import org.openqa.selenium.devtools.v108.network.model.ResourceType;
import org.openqa.selenium.devtools.v108.network.model.Response;
import org.openqa.selenium.interactions.Actions;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 官方API
 * https://www.selenium.dev/documentation/webdriver/bidirectional/
 */
@Slf4j
public class Demo {

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
    public static String chromedirverPath = "C:\\Users\\a\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";


    public static void main(String[] args) {


        // 设置 chromedirver的存放位置
        System.getProperties().setProperty("webdriver.chrome.driver", chromedirverPath);

//        ImmutableMap.of(
//                "", new CommandInfo("session/:sessionId/goog/cast/get_sinks", HttpMethod.GET),
//                "", new CommandInfo("session/:sessionId/goog/cast/set_sink_to_use", HttpMethod.POST),
//                "", new CommandInfo("session/:sessionId/goog/cast/start_desktop_mirroring", HttpMethod.POST),
//                "", new CommandInfo("session/:sessionId/goog/cast/start_tab_mirroring", HttpMethod.POST),
//                "", new CommandInfo("session/:sessionId/goog/cast/get_issue_message", HttpMethod.GET),
//                "", new CommandInfo("session/:sessionId/goog/cast/stop_casting", HttpMethod.POST));


        // 设置浏览器参数
//        ChromeOptions options=webdriver.ChromeOptions();

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

        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);

        //文档地址:https://www.selenium.dev/zh-cn/documentation/webdriver/capabilities/shared/#unhandledpromptbehavior
        chromeOptions.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);

        //传下面参数来禁止掉谷歌受自动化控制的信息栏
        chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        if (!headless) {//有界面时给予跳过弹窗的处理
            Object prefs = SeleniumUtil.getAllowProtocolPrefs();
            //Object prefs = getExcludedProtocolPrefs();
            //Object prefs = getCustomProtocolPrefs();
            chromeOptions.setExperimentalOption("prefs", prefs);
        }


        // 禁用阻止弹出窗口
//        chromeOptions.addArguments("--disable-popup-blocking")


        // 禁用保存密码提示框
        Map<String, Object> prefs = new HashMap();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        chromeOptions.setExperimentalOption("prefs", prefs);

        if (imagesEnabled) {

            //禁用图片
            chromeOptions.addArguments("blink-settings=imagesEnabled=false");
        }


        //更换代理ip
        /**
         * proxy_arr = [
         *     '--proxy-server=http://111.3.118.247:30001',
         *     '--proxy-server=http://183.247.211.50:30001',
         *     '--proxy-server=http://122.9.101.6:8888',
         * ]
         */
//        chromeOptions.addArguments("--proxy-server=http://111.3.118.247:30001");


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
        // WebDriver driver =  = new FirefoxDriver();


//        NetworkInterceptor interceptor = new NetworkInterceptor(
//                driver,
//                Route.matching(req -> true)
//                        .to(() -> req -> new HttpResponse()
//                                .setStatus(200)
//                                .addHeader("Content-Type", MediaType.HTML_UTF_8.toString())
//
//                        )
//
//
//        );


        //方法三：字节数组
//        InputStream inputStreamRoute = new ByteArrayInputStream(
//                new String("你好啊").getBytes());
//
//        NetworkInterceptor interceptor = new NetworkInterceptor(
//                driver,
//                new Route() {
//                    @Override
//                    protected HttpResponse handle(HttpRequest req) {
//                        return new HttpResponse().setStatus(200).setContent(new Supplier<InputStream>() {
//                            @Override
//                            public InputStream get() {
//                                return inputStreamRoute;
//                            }
//                        });
//                    }
//
//                    @Override
//                    public boolean matches(HttpRequest req) {
//                        return true;
//                    }
//                }
//        );


//        AddSeleniumUserAgent addSeleniumUserAgent = new AddSeleniumUserAgent();
//
//        addSeleniumUserAgent.
//
//        NetworkInterceptor interceptor = new NetworkInterceptor(
//                driver,
//                new
//        );


        /**
         * 监听网络
         * https://blog.51cto.com/u_15406013/4331846
         * 操作网络请求
         * https://www.selenium.dev/documentation/webdriver/bidirectional/bidi_api_remotewebdriver/
         *
         *
         *
         */
        DevTools devTools = driver.getDevTools();

        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.requestIntercepted(), responseReceived -> {

            Request request = responseReceived.getRequest();
            String url = request.getUrl();


        });

        devTools.addListener(Network.requestServedFromCache(), responseReceived -> {
            String s = responseReceived.toJson();

            System.out.println(s);

        });

        /**
         * 获取Request信息
         * 监听将被发送的数据
         */
        devTools.addListener(Network.requestWillBeSent(), responseReceived -> {

            Request request = responseReceived.getRequest();

            String method = request.getMethod();

            Optional<ResourceType> resourceType = responseReceived.getType();

            String type = resourceType.get().toJson();
            String url = request.getUrl();

            switch (type) {
                case "Document":
                case "Image":
                case "Font":
                case "Ping":
                case "Script":
                case "Stylesheet":
                case "Fetch":
                    return;

                case "Other":

                    if (url.endsWith(".ico")
                            || url.endsWith(".png")
                            || url.endsWith(".jpg")
                            || url.endsWith(".gif")
                    ) {
                        return;
                    }


                case "XHR":

                default:
                    break;
            }

            if (url.contains(".js")
                    || url.contains(".css")
                    || url.contains(".jpg")
                    || url.contains(".png")
                    || url.contains(".gif")
            ) {
                return;
            }

            System.out.println(url);

            if (url.equalsIgnoreCase("https://login.taobao.com/newlogin/login.do?appName=taobao&fromSite=0&_bx-v=2.2.3")) {
                request.getPostData().map(new Function<String, Object>() {
                    @Override
                    public Object apply(String s) {
                        return null;
                    }
                });
            }

            System.out.println(request);

        });


        /**
         * 获取Response信息
         * 监听网络请求返回值  这里可以抓包
         *
         * devTools是通过事件监听来获取网络数据的，具体监听事件有很多，比如responseReceived，requestWillBeSent，dataReceived等等。
         *
         * 需要注意的有几点：
         *
         * 获取response的时候，记得try catch，以防止有的请求并没有body导致的异常。
         * responseReceived事件触发时，这个时候获取response未必能取到，因为只是响应返回了，但是body可能比较大，数据可能还没有接收完。
         * dataReceived事件触发时，大概率是可以获取到返回的body的，但是保险起见，可以sleep500毫秒。
         * 如果有一些请求，请求的URL都一样，只是参数不同，而我们只关心特定参数的request返回的response，则可以订阅requestWillBeSent事件，确认该请求是需要的，则把RequestId扔到队列里，在dataReceived的时候从队列里取出RequestId来获取返回数据。
         * requestWillBeSent的RequestId和dataReceived的RequestId内容是一样的。
         * 除了通过devTools监听数据外，还可以做很多其它的事情，比如修改请求HEAD，修改Cookie，具体API可以去​ ​官网​​查询。
         *
         * 当然有了driver一样可以像以前一样，访问URL，获取页面元素，交互。比如如下代码
         * -----------------------------------
         * ©著作权归作者所有：来自51CTO博客作者武码公社的原创作品，请联系作者获取转载授权，否则将追究法律责任
         * 使用selenium4监听网络请求
         * https://blog.51cto.com/u_15406013/4331846
         */
        devTools.addListener(Network.responseReceived(), responseReceived -> {
            RequestId requestId = responseReceived.getRequestId();
            Response receivedResponse = responseReceived.getResponse();
            String url = receivedResponse.getUrl();
            String type = responseReceived.getType().toString();

            switch (type) {
                case "Document":
                case "Image":
                case "Font":
                case "Ping":
                case "Script":
                case "Stylesheet":
                case "Fetch":
                    return;

                case "Other":

                    if (url.endsWith(".ico")
                            || url.endsWith(".png")
                            || url.endsWith(".jpg")
                            || url.endsWith(".gif")
                    ) {
                        return;
                    }
                case "XHR":

                default:
                    break;
            }

            System.out.println("type:" + type + ", url:" + url);

            //进行请求
            try {

                Command<String> requestPostData = Network.getRequestPostData(requestId);

                //请求参数
                Command<Network.GetResponseBodyResponse> responseBody = Network.getResponseBody(requestId);

                //类型
                String method = responseBody.getMethod();
                //参数
                Map<String, Object> params = responseBody.getParams();


                Command<Network.GetResponseBodyResponse> responseBody1 = responseBody;
                Map<String, Object> params1 = responseBody1.getParams();

                //执行请求
                Network.GetResponseBodyResponse response = devTools.send(responseBody1);

                //是否base64编码
                Boolean base64Encoded = response.getBase64Encoded();

                //获取body
                String body = response.getBody();

                if (base64Encoded) {
                    //进行解码
                    body = Base64.decodeStr(body);
                }


                log.debug(body);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }


        });

        //清理cooki
//        driver.manage().deleteAllCookies();

        // 与浏览器同步非常重要，必须等待浏览器加载完毕
        //隐式等待10秒
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //最大化窗口
        driver.manage().window().maximize();  //最大化窗口
        //设置隐性等待时间

        driver.get("https://www.taobao.com/");

        //等待页面加载完成，后续写代码注意，如果页面加载未完成，可能导致页面元素找不到
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("当前打开页面的标题是： " + driver.getTitle());

        //判断是否登陆
        // 选择元素并进行点击
        driver.findElement(By.linkText("亲，请登录")).click();

          //等待页面加载完成，后续写代码注意，如果页面加载未完成，可能导致页面元素找不到
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("当前打开页面的标题是： " + driver.getTitle());

        //输入账号密码
        driver.findElement(By.id("fm-login-id")).sendKeys("淘宝账号");
        driver.findElement(By.id("fm-login-password")).sendKeys("淘宝密码");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //滑块  nc_1_n1z
        ;

        //拖拽
        Actions action = new Actions(driver);
        // 鼠标拖拽动作，将 source 元素拖放到 target 元素的位置。
//        action.dragAndDrop(driver.findElement(By.id("nc_1_n1z")),driver.findElement(By.id("nc_1__scale_text")));
        // 鼠标拖拽动作，将 source 元素拖放到 (xOffset, yOffset) 位置，其中 xOffset 为横坐标，yOffset 为纵坐标。


//        action.dragAndDrop(driver.findElement(By.id("nc_1_n1z")),xOffset,yOffset);

        //移动到对应的偏移量
        action.dragAndDropBy(driver.findElement(By.id("nc_1_n1z")), -260, 0);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        action.dragAndDropBy()

        //点击登陆按钮
        driver.findElement(By.cssSelector(".fm-btn button")).click();

        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //关闭并退出浏览器
        driver.quit();


    }
}