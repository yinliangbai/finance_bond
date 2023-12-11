package com.baiyinliang.finance.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baiyinliang.finance.service.YangKeDuoService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/pdd")
public class YangDuoDuo {

    @Resource
    private RestTemplate restTemplate;

    @Autowired
    private YangKeDuoService yangKeDuoService;

    // 一、登录
    // 1.访问登录页面
    @RequestMapping("/loginPage")
    public void visitLoginPage() {
        String loginUrl = "https://mobile.yangkeduo.com/login.html?from=https%3A%2F%2Fmobile.yangkeduo.com%2Fspike.html%3F__rp_name%3Dspike_v3%26_pdd_fs%3D1%26_pdd_tc%3Dffffff%26_pdd_sbs%3D1%26_pdd_nc%3Dd4291d%26refer_spike_source%3D110%26refer_page_el_sn%3D99956%26refer_page_name%3Dindex%26refer_page_id%3D10002_1699065260255_zkhrs7h682%26refer_page_sn%3D10002&refer_page_name=spike_v3&refer_page_id=47200_1699065322609_ng0ggqp4nk&refer_page_sn=47200";
        loginUrl = "https://mobile.yangkeduo.com/login.html";
//        loginUrl = "https://www.pinduoduo.com/home/seckill/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        HashMap<Object, Object> params = new HashMap<>();

        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> result = null;

        try {
            result = restTemplate.exchange(loginUrl, HttpMethod.GET, entity, String.class);
            if (HttpStatus.OK.equals(result.getStatusCode())) {
                log.info("登录页访问成功");
            } else {
                log.error("登录页访问失败");
            }
//            JSONObject jsonObject = JSONObject.parseObject(result.getBody());
//            log.info("jsonObject={}", JSON.toJSONString(jsonObject));
//            JSONObject data = jsonObject.getJSONObject("data");
//            log.info("data={}", JSON.toJSONString(data));
//            String id = data.getString("id");
//            log.info("id={}", id);
        } catch (Exception e) {
            log.error("登录页访问失败：" + e.getMessage());
        }
    }

    // 2.获取本机手机号
    // 3.发短信验证码
    @RequestMapping("/sendCode")
    public void sendCode() {
        String loginUrl = "https://mobile.yangkeduo.com/login.html?from=https%3A%2F%2Fmobile.yangkeduo.com%2Fspike.html%3F__rp_name%3Dspike_v3%26_pdd_fs%3D1%26_pdd_tc%3Dffffff%26_pdd_sbs%3D1%26_pdd_nc%3Dd4291d%26refer_spike_source%3D110%26refer_page_el_sn%3D99956%26refer_page_name%3Dindex%26refer_page_id%3D10002_1699065260255_zkhrs7h682%26refer_page_sn%3D10002&refer_page_name=spike_v3&refer_page_id=47200_1699065322609_ng0ggqp4nk&refer_page_sn=47200";
        loginUrl = "https://mobile.yangkeduo.com/proxy/api/api/sigerus/mobile/code/request?pdduid=7604495727192";
//        loginUrl = "https://www.pinduoduo.com/home/seckill/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        HashMap<Object, Object> params = new HashMap<>();
        params.put("mobile", "17621591871");
        params.put("platform", "4");
        params.put("touchevent", "{}");
        params.put("fingerprint", "{\"innerHeight\":200,\"innerWidth\":1446,\"devicePixelRatio\":1.5,\"availHeight\":894,\"availWidth\":1494,\"height\":934,\"width\":1494,\"colorDepth\":24,\"locationHerf\":\"https://mobile.yangkeduo.com/login.html\",\"timezoneOffset\":-480,\"navigator\":{\"appCodeName\":\"Mozilla\",\"appName\":\"Netscape\",\"hardwareConcurrency\":16,\"language\":\"zh-CN\",\"cookieEnabled\":true,\"platform\":\"Win32\",\"doNotTrack\":null,\"ua\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.43\",\"vendor\":\"Google Inc.\",\"product\":\"Gecko\",\"productSub\":\"20030107\"}}");
        params.put("screen_token", "0arAfqnydiXyj9maQ6iUBC7aOBDZu6dA29_LuIhWLV99OGmUPyKhH9-6UYDuvcuc3ExUfgEwj03bqwgZk9TfDhCMo_CBQdU2YxbPnB9wqX6ofCEXTq5TuN0QD4pAmFrMJrrJUhk95CmArpioKWFbuoQ6fw1vFZodMo_k_o33o4SOJiVQTAlzNrGrK2L8YU9IxVpYUsEJNgvBQqR5khaOEnHOGZx5_sE8uCRnTfHB2Qld_VC50coas92EmT301XEzNTgYBsNfkeC6-lvh9hGBXog0txCG4BOEDxstB9OgQuCk2QVs7gWf5BAPOxVQ4e-E73YiamiTB8xIlvwzqFIyLZcsDvfj91sr_eISYi8c86dNUvcgGdMw_jG3lsjdph4KyGVjR3bHoqpKR2yLem7leChJdGYplMEqxxd6bhFyJDJ4emHEk-BIvXefw-nLRgAZitOVXhySUk_ZIdp1hQw1AxEK7GejFrWPEkwJnzZ3BebZeuSIESwfP-heFMyf9Hc87H3iQhfzZv5eGIQh5rJ0d09L5uXdJ6xsgjzVPxU8GHEpDEB_Ek-L2S_2EBHPWOgH1fOOBjp0RFIZ3s8NzLwmCcUwEOoBQ9Wfyy5H1nA7EcDFzw5GBdFu2W0Z2_n2JFzZ_C1biNwpY5LixjGmYYshDXvOhjwpvhLUG_5ZOYGIZqusYHgw40dqmVXFlXi-Vvff0G8N6KoM4S72eH5fs3UwdeZeaNyxCyYa6kA_18f4IBBU8xQQtPywloI6OPYh6L6ieEiHydJRooDdOUhpxAkCN8iVIivL8ZqdhFBHiEzYBVR7JQgJROnIDDv1tDd-nKM5p7Gr_gFjGDUR99W_yHR94gvlQqdlVfVK");


//        HttpEntity<String> formEntity = new HttpEntity<String>(JSON.toJSONString(param), headers);
//        ResponseEntity<String> exchange = restTemplate.exchange(url_login,
//                HttpMethod.POST, formEntity, String.class);
        HttpEntity<String> entity = new HttpEntity<>(JSONObject.toJSONString(params), headers);
        ResponseEntity<String> result;

        try {
            result = restTemplate.exchange(loginUrl, HttpMethod.POST, entity, String.class);
            if (HttpStatus.OK.equals(result.getStatusCode())) {
                log.info("发送登录验证码成功");
            } else {
                log.error("发送登录验证码失败");
            }
//            JSONObject jsonObject = JSONObject.parseObject(result.getBody());
//            log.info("jsonObject={}", JSON.toJSONString(jsonObject));
//            JSONObject data = jsonObject.getJSONObject("data");
//            log.info("data={}", JSON.toJSONString(data));
//            String id = data.getString("id");
//            log.info("id={}", id);
        } catch (Exception e) {
            log.error("发送登录验证码：" + e.getMessage());
        }
    }

    // 4.读取短信验证码
    // 5.登录提交
    @RequestMapping("/newSubmit")
    public void newSubmit(@RequestParam String code, @RequestParam int offset, @RequestParam int limit) {
        // 登录
        String access_token = yangKeDuoService.submitLogin(code);
        // 分页查列表
        yangKeDuoService.visitYangKeDuo(offset, limit, access_token);
    }


    @RequestMapping(value = "/submitLogin/{code}", method = {RequestMethod.GET, RequestMethod.POST})
    public void submitLogin(@PathVariable String code) {
        String submitUrl = "https://mobile.yangkeduo.com/proxy/api/api/sigerus/login_mobile?pdduid=0";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        HashMap<Object, Object> params = new HashMap<>();
        params.put("mobile", "17621591871");
        params.put("login_app_id", 155);
        params.put("code", code);
        params.put("add_cookie", true);
        params.put("anti_content", "0arWfqnYgcPs6gm2A4uCroA2QsT1q4tLew4YhrXYOnf2d8J77nijWPhuP5euwApYhqXLLq3TH4t1Zs1MzakTVvunVTC34ocfddQOUqPw4CYgS7wlZTdz_QdeCbWbViCASqRoiNQaqazkJZQQ3a9D3RX3l_jfgvsyVos1tLHNwm6n8jD8sqJ8DNkhcsUj1RBuozknTjAENlPuOPyBglyqi07l8V6RGp0Zdra4igCc53arkfeQi1LT2DwdvOBQgpyfm59G3Mw6XotHUrTCujmYNQQEBDtIXCZB");
        log.info("登录提交传参={}", JSON.toJSONString(params));

        HttpEntity<String> entity = new HttpEntity<>(JSONObject.toJSONString(params), headers);
        ResponseEntity<String> result;

        try {
            result = restTemplate.exchange(submitUrl, HttpMethod.POST, entity, String.class);
            log.info("result={}", JSONObject.toJSONString(result));
            if (HttpStatus.OK.equals(result.getStatusCode())) {
                log.info("登录成功");
            } else {
                log.error("登录失败");
            }
            JSONObject jsonObject = JSONObject.parseObject(result.getBody());
            log.info("jsonObject={}", JSONObject.toJSONString(jsonObject));
            String access_token = jsonObject.getString("access_token");
//            JSONObject data = jsonObject.getJSONObject("data");
//            log.info("data={}", JSON.toJSONString(data));
//            String id = data.getString("id");
//            log.info("id={}", id);
        } catch (Exception e) {
            log.error("登录：" + e.getMessage());
        }
    }

    @Data
    class CookieData {
//            private String api_uid = "";

    }

    // 二、秒杀
    // 1.打开秒杀页面
    @RequestMapping("/spike")
    public void visitYangKeDuo(@RequestParam int offset, @RequestParam int limit, @RequestParam String access_token) {
//    public void visitYangKeDuo( @RequestParam String access_token) {
        yangKeDuoService.visitYangKeDuo(offset,limit, access_token);
    }
    // 2.循环产品列表
    // 2.1.打开每个产品详情页面
    // 2.2.读取在拼人数
    // 2.3.超过300的，查询差评数据
    // 2.4.发通知带链接 或者 自动收藏
}
