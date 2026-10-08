package com.umg.quejasbancario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.umg.quejasbancario.entity")
public class QuejasBancarioApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuejasBancarioApplication.class, args);
    }
}
