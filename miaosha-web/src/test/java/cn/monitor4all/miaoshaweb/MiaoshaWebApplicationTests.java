package cn.monitor4all.miaoshaweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;

@SpringBootTest
class MiaoshaWebApplicationTests {

    private static Logger logger = LoggerFactory.getLogger(MiaoshaWebApplicationTests.class);
    private static ExecutorService executorServicePool;
    private static String url = "http://127.0.0.1:8080/createOptimisticLimitOrder/1" ;

}
