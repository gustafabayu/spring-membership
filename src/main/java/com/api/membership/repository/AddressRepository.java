package com.api.membership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.membership.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address,String> {
    
}
