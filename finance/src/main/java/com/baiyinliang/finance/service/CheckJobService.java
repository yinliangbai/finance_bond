package com.baiyinliang.finance.service;

public interface CheckJobService {

    // 检查每天的跑批是否正常跑了，没跑要发微信提醒
    void checkJob();
}
