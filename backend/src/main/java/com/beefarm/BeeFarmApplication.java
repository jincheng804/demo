package com.beefarm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.beefarm.**.mapper")
public class BeeFarmApplication {
    public static void main(String[] args) {
        SpringApplication.run(BeeFarmApplication.class, args);

    }
}
