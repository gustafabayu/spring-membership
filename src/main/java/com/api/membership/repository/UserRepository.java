package com.api.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.membership.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    
}
