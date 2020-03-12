package cn.monitor4all.miaoshaservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.monitor4all.miaoshadao.mapper")
public class MiaoshaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiaoshaServiceApplication.class, args);
    }

}
