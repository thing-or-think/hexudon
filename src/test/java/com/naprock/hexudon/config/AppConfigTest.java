//package com.naprock.hexudon.config;
//
//import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
//import com.naprock.hexudon.application.service.MatchApplicationService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.ApplicationContext;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class AppConfigTest {
//
//    @Autowired
//    private ApplicationContext context;
//
//
//    @Test
//    void testBeansRegistrationAndWiring() {
//
//        MatchConfigLoaderPort matchConfigLoader =
//                context.getBean(MatchConfigLoaderPort.class);
//
//        MatchApplicationService matchApplicationService =
//                context.getBean(MatchApplicationService.class);
//
//
//        assertAll(
//                () -> assertNotNull(
//                        matchConfigLoader,
//                        "MatchConfigLoaderPort bean should be registered"
//                ),
//
//                () -> assertNotNull(
//                        matchApplicationService,
//                        "MatchApplicationService bean should be registered"
//                )
//        );
//    }
//}