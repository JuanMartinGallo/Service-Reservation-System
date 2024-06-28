package com.srs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan()
public class ServiceReservationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceReservationSystemApplication.class, args);
    }

}
