package com.couriersync.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.couriersync.auth",
    "com.couriersync.common"
})
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@EnableScheduling
public class UserAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
