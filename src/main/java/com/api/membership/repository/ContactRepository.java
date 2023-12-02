package com.api.membership.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.membership.entity.Contact;
import com.api.membership.entity.User;

@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {

    Optional<Contact> findFirstByUserAndId(User user, String id);
}
