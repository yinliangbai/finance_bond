package com.baiyinliang.finance.tools;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author riemann
 * @date 2019/05/25 0:58
 */
@Slf4j
public class HttpClientUtil {
//    private static Log log = LogFactory.getLog(HttpClientUtil.class);

    private final static String DEFAULT_ENCODING = "UTF-8";
    private final static int DEFAULT_CONNECT_TIMEOUT = 5000; // 设置连接超时时间，单位毫秒
    private final static int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 1000;// 设置从connect Manager获取Connection 超时时间，单位毫秒
    private final static int DEFAULT_SOCKET_TIMEOUT = 5000;// 请求获取数据的超时时间，单位毫秒 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用

    /**
     * 简单http post请求
     *
     * @param url      地址
     * @param paramMap 参数
     * @param encoding 编码
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static String simplePost(String url, Map<String, String> paramMap, String encoding) throws ParseException, IOException {
        String body = "";
        encoding = StringUtils.isBlank(encoding) ? DEFAULT_ENCODING : encoding;
        //1、创建CloseableHttpClient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //2、创建请求方法的实例，并指定请求URL。如果需要发送GET请求，创建HttpGet对象；如果需要发送POST请求，创建HttpPost对象。
        HttpPost httpPost = postForm(paramMap, url, DEFAULT_ENCODING);
        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = null;
        try {
            //5、调用CloseableHttpClient对象的execute(HttpUriRequest request)发送请求，该方法返回一个CloseableHttpResponse。
            response = client.execute(httpPost);
            //6、调用HttpResponse的getEntity()方法可获取HttpEntity对象，该对象包装了服务器的响应内容。程序可通过该对象获取服务器的响应内容；调用CloseableHttpResponse的getAllHeaders()、getHeaders(String name)等方法可获取服务器的响应头。
            StatusLine status = response.getStatusLine();
            log.info("请求回调状态               ：" + status);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, encoding);
                log.info("请求回调数据               ：" + body);
            }
            //7、释放连接。无论执行方法是否成功，都必须释放连接
            EntityUtils.consume(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("简单post请求遇到UnSpEcode异常", e);
            throw new IOException(e);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("简单post请求遇到IO异常", e);
            throw new IOException(e);
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("简单post请求遇到PE异常", e);
            throw new ParseException();
        } finally {
            //释放链接
            response.close();
        }
        return body;
    }

    /**
     * post请求url与请求参数组装
     *
     * @param paramMap
     * @param url
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    private static HttpPost postForm(Map<String, String> paramMap, String url, String encoding) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        //装填参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (paramMap != null) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        //3、如果需要发送请求参数，可可调用setEntity(HttpEntity entity)方法来设置请求参数。setParams方法已过时（4.4.1版本）
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
        //4、调用HttpGet、HttpPost对象的setHeader(String name, String value)方法设置header信息，或者调用setHeaders(Header[] headers)设置一组header信息。
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=1h9modhm5lent408sl98t0ut73; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1677996300"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; kbzw__Session=ss5fobnc25pr393sejpc6f77p6; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1678103535"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; kbzw__Session=840e8jocki31h7ppm39jikmvr1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476,1678325906; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1678326061"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; kbzw__Session=840e8jocki31h7ppm39jikmvr1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476,1678325906; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1678431790"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=840e8jocki31h7ppm39jikmvr1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476,1678325906; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1678702231"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=840e8jocki31h7ppm39jikmvr1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476,1678325906; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1678788272"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=840e8jocki31h7ppm39jikmvr1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476,1678325906; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1678872820"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=840e8jocki31h7ppm39jikmvr1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476,1678325906; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1678872885"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=840e8jocki31h7ppm39jikmvr1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677379591,1677989557,1678071476,1678325906; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqekt7e_1KLA59vZzeDapJ6nnJeKzcrl2-Lv1JCvyJmmmaecsoLNyuWtp7OB6JeoopWq6dzjx83G2cLc7JCllKunqJ-ZlMKqyq7Do5PkytvGlMDazOTboIK5yenm4N2QoZKkp6WXqZqngsnC3djl4ZCllKunqJ-ZsdvJpZKop6Goj6GWrLCjn6w.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1679307243"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpipmaSsq5qyjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWqo5mnnKWSlrTY3dTqyp-Wp7CjnK-MvMbdkKiopKaRnpKqr6aXrp0.; kbzw__Session=ki7q73n2s1rjb1i2unt3fhnqb1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677989557,1678071476,1678325906,1679815759; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1679909024"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpipmaSsq5qyjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWqo5mnnKWSlrTY3dTqyp-Wp7CjnK-MvMbdkKiopKaRnpKqr6aXrp0.; kbzw__Session=ki7q73n2s1rjb1i2unt3fhnqb1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1677989557,1678071476,1678325906,1679815759; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1679909385"));
//        httpPost.addHeader(new BasicHeader("cookie","kbzw__Session=ki7q73n2s1rjb1i2unt3fhnqb1; Hm_lvt_164fe01b1433a19b507595a43bf58262=1679815759; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpmhk6yyrZyrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWqo5mnnKWSlrTY3dTqyp-Wp7CjnK-MvMbdkKiopKaRnpKqr6aXrp0.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1681289884"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=9a0okkqr053arcg627vq177547; Hm_lvt_164fe01b1433a19b507595a43bf58262=1679815759,1681459022; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpmhlamyppyxjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWro5mnnKWSlrTY3dTqyp-Wp7CjnK-MvMbdkKiopaaRnpKrq6eXrKU.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1681459135"));
//        httpPost.addHeader(new BasicHeader("cookie","kbz_newcookie=1; kbzw__Session=9a0okkqr053arcg627vq177547; Hm_lvt_164fe01b1433a19b507595a43bf58262=1681459022; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1682471599; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18mikqeWqK3UsZPSmdmqpsqu0dmX2MTbr6uulqeT2bHby6mNso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpmilauqq5msjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWro5mnnKWSlrTY3dTqyp-Wp7CjnK-MvMbdkKiopaaRnpKrq6eXrKU."));
//        httpPost.addHeader(new BasicHeader("cookie","kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=1699508886; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpqplqWppqCrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=1699536101"));
//        httpPost.addHeader(new BasicHeader("cookie", "kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis() + "; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkrpqplqWppqCrjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis()));
//        httpPost.addHeader(new BasicHeader("cookie", "kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis() + "; kbz_newcookie=1; Hm_lpvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis()));
        httpPost.addHeader(new BasicHeader("cookie", "kbzw__Session=4f9oofi0v812j0m0ct72chai64; Hm_lvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis() + "; kbz_newcookie=1; kbzw__user_login=7Obd08_P1ebax9aX5dXazJiyoOjC49Tr6OfN18milquVsKur2sKllqTbq5yuoNia3Jfb3KOtmKHEqduroq6Nso_rytvV0KOSpZWtqaWbmqeko8q_1KKkr5Ggl6WvqpmyjbKPy6LV1J7F0OrK4OXWmK6ap4KeuODl1-fY44HCzZWaqZqnnZa44OWprJyQ2aqtnom63OfO27jc2b7h1Z-Wp7CjnK-Mn62-tcTDn5jN2czZmbzO3Nfmi5ak3-ni5cafkqWyo5mnnKWSlrTY3dTqyp-Wp7CjnK8.; Hm_lpvt_164fe01b1433a19b507595a43bf58262=" + System.currentTimeMillis()));
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        return httpPost;
    }

    /**
     * 简单get请求传输
     *
     * @param url
     * @param paramMap
     * @param encoding
     * @return
     */
    public static String simpleGet(String url, Map<String, String> paramMap, String encoding) {
        String body = "";
        encoding = StringUtils.isBlank(encoding) ? DEFAULT_ENCODING : encoding;
        //1、创建CloseableHttpClient对象
        CloseableHttpClient client = HttpClients.createDefault();

        //2、创建get请求
        HttpGet httpGet = new HttpGet(url);
        //装填参数
        List<NameValuePair> lists = new ArrayList<NameValuePair>();
        if (paramMap != null) {
            //每个key-value构成一个entrySet对象
            Set<Map.Entry<String, String>> setMap = paramMap.entrySet();
            //遍历对象  将值保存list集合种
            for (Map.Entry<String, String> entry : setMap) {
                lists.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        //将传递参数  编码化
        String param = URLEncodedUtils.format(lists, encoding);
        log.info("simpleGet------->" + param);
        //设置数据
        httpGet.setURI(URI.create(url + "?" + param));
        log.info("simpleGet --- url------->" + httpGet.getURI().toString());
        //配置连接参数信息
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
        httpGet.setConfig(requestConfig);

        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = null;
        try {
            //5、调用CloseableHttpClient对象的execute(HttpUriRequest request)发送请求，该方法返回一个CloseableHttpResponse。
            response = client.execute(httpGet);
            //6、调用HttpResponse的getEntity()方法可获取HttpEntity对象，该对象包装了服务器的响应内容。程序可通过该对象获取服务器的响应内容；调用CloseableHttpResponse的getAllHeaders()、getHeaders(String name)等方法可获取服务器的响应头。
            StatusLine status = response.getStatusLine();
            log.info("get请求回调状态               ：" + status);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, encoding);
                log.info("get请求回调数据               ：" + body);
            }
            //7、释放连接。无论执行方法是否成功，都必须释放连接
            EntityUtils.consume(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("简单get请求遇到UnSpEcode异常", e);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("简单get请求遇到IO异常", e);
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("简单get请求遇到PE异常", e);
            throw new ParseException();
        } finally {
            //释放链接
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return body;
    }
}