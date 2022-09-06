package com.wkelms.ebilling.edsscheduler

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
public class EdsSchedulerApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdsSchedulerApplication.class)

    public static void main(String[] args) {
        SpringApplication.run(EdsSchedulerApplication.class, args)
    }

}
