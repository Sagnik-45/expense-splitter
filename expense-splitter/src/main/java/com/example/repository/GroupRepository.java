package com.example.repository;

import com.example.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    // JpaRepository gives you these for free:
    // save(), findById(), findAll(), deleteById()
}
