package com.lms.auth.repository;

import com.lms.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository - Like Prisma's database client methods
 * JpaRepository provides CRUD operations out of the box:
 * - save(), findById(), findAll(), deleteById(), etc.
 * 
 * You can also define custom queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
