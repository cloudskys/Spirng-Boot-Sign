package com.show.sign;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@MapperScan("com.show.sign.dao")
public class SignServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SignServerApplication.class, args);
    }
}
