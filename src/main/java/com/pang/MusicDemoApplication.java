package com.pang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pang.mapper")
public class MusicDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicDemoApplication.class, args);
    }

}
