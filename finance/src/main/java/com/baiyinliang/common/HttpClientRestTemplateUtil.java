package com.baiyinliang.common;

import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

public class HttpClientRestTemplateUtil {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Authentication authentication;

    public Object doGet(String uri, Map<String, String> params, HttpHeaders headers){
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
        ResponseEntity<Object> exchange = restTemplate.exchange(uri, HttpMethod.GET, entity, Object.class);
        return exchange;
    }

    public String doPost(String uri, String token, Map<String, Object> params){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//        headers.add(Authentication., "Bearer " + token);
        HttpEntity<Object> entity = new HttpEntity<>(params, headers);
//        return restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        JSONObject data = Objects.requireNonNull(JSONObject.parseObject(result.getBody())).getJSONObject("data");
        return data.getString("id");
    }




}
