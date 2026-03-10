package com.chronicle.config;

import com.chronicle.entity.Catalog;
import com.chronicle.entity.User;
import com.chronicle.repository.CatalogRepository;
import com.chronicle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CatalogRepository catalogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("管理员");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
        }

        if (catalogRepository.count() == 0) {
            List<String> titles = List.of(
                "第一篇 综述", "第二篇 行政区划", "第三篇 人口", "第四篇 经济",
                "第五篇 工业", "第六篇 农业", "第七篇 交通", "第八篇 教育",
                "第九篇 科技", "第十篇 文化"
            );
            for (int i = 0; i < titles.size(); i++) {
                Catalog c = new Catalog();
                c.setTitle(titles.get(i));
                c.setLevel(1);
                c.setOrderNum(i + 1);
                catalogRepository.save(c);
            }
        }
    }
}
