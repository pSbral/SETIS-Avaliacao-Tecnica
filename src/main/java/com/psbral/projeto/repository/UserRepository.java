package com.psbral.projeto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.psbral.projeto.models.User;

public interface UserRepository extends JpaRepository<User, Long>{
	boolean existsByEmail(String email);
}
