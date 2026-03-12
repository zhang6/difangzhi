package com.chronicle.config;

import com.chronicle.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final AuthService authService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            authService.initDefaultUsers();
            log.info("默认用户初始化完成 (admin/admin123, editor1/123456, editor2/123456)");
        } catch (Exception e) {
            log.warn("初始化默认用户失败: {}", e.getMessage());
        }
    }
}
