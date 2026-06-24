package com.example.repository;

import com.example.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    // Spring sees "findByGroupId" and auto generates:
    // SELECT * FROM group_members WHERE group_id = ?
    List<GroupMember> findByGroupId(Long groupId);
}
