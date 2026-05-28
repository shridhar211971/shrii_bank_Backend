//package com.shrii.bank;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.scheduling.annotation.EnableAsync;
//
//@SpringBootApplication
//@EnableAsync
//public class ShriiBankBackendApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(ShriiBankBackendApplication.class, args);
//	}
//
//}

package com.shrii.bank;

import com.shrii.bank.auth_users.entity.User;
import com.shrii.bank.enums.NotificationType;
import com.shrii.bank.notification.dtos.NotificationDTO;
import com.shrii.bank.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableAsync
//@RequiredArgsConstructor
public class ShriiBankBackendApplication {

//    private final NotificationService notificationService;

    public static void main(String[] args) {
        SpringApplication.run(ShriiBankBackendApplication.class, args);
    }

//    @Bean
//    CommandLineRunner runner() {
//
//        return args -> {
//
//            Map<String, Object> vars = new HashMap<>();
//            vars.put("name", "Dennis");
//
//            NotificationDTO dto = NotificationDTO.builder()
//                    .recipient("officialshridhar211971@gmail.com")
//                    .subject("Welcome to Shrii Bank")
//                    .templateName("welcome-email")
//                    .templateVariables(vars)
//                    .type(NotificationType.EMAIL)
//                    .build();
//
//            notificationService.sendEmail(dto, new User());
//
//        };
//    }
}
