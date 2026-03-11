package com.chronicle.repository;

import com.chronicle.entity.YbUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface YbUserRepository extends JpaRepository<YbUser, UUID> {
    Optional<YbUser> findByUsername(String username);
}
