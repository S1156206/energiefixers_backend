package com.energiefixers.backend.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.energiefixers.backend.user.models.Role;
import com.energiefixers.backend.user.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Long countByPropertyRegionIdAndRole(Long regionId, Role role);
}
