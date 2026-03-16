package com.devplus.repository;

import com.devplus.model.Role;
import com.devplus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByGithubUsername(String githubUsername);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(Role role);
}