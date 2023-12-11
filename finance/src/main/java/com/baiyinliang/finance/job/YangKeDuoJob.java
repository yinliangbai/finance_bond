package com.baiyinliang.finance.job;

import com.baiyinliang.finance.service.YangKeDuoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class YangKeDuoJob {

    @Autowired
    private YangKeDuoService yangKeDuoService;


    @XxlJob("miaoJobHandler")
    public void miaoJobHandler() {
        log.info("miaoJobHandler 跑批开始----------------------------------");
//        yangKeDuoService.visitYangKeDuo();
        boolean flag = true;
        int pageNo = 0;
        int offset = 20;
        String access_token = "66PDBA6LUPQMIDDQNXFMQ3JIPJSVOFY3ET3GJKWMYTWRFZ35TLFA122589f";
        for (int i = 0; i < 20; i++) {
            yangKeDuoService.visitYangKeDuo(1 + i * offset, 20, access_token);
        }
//        do {
//            flag = yangKeDuoService.visitYangKeDuo(1 + pageNo * offset, 20, access_token);
//            pageNo++;
//            try {
//                int v = (int) (Math.random() * (10)) + 1;
////                long l = v + 1 * 100L;
//                log.info("sleep {} 秒", v);
////                TimeUnit.SECONDS.sleep(v);
//            } catch (Exception e) {
//                log.error("sleep 异常了");
//            }
//        } while (flag);

        log.info("miaoJobHandler 跑批结束----------------------------------");
    }


}
