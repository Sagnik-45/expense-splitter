package com.example.controller;

import com.example.dto.*;
import com.example.entity.*;
import com.example.repository.GroupMemberRepository;
import com.example.service.GroupExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GroupController {

    private final GroupExpenseService service;
    private final GroupMemberRepository memberRepository;

    // ── CREATE GROUP WITH MEMBERS ──────────────────────────────────
    @PostMapping("/groups")
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(service.createGroup(request));
    }

    // ── ADD EXPENSE ────────────────────────────────────────────────
    @PostMapping("/expenses")
    public ResponseEntity<Expense> addExpense(@RequestBody AddExpenseRequest request) {
        return ResponseEntity.ok(service.addExpense(request));
    }

    // ── GET GROUP SUMMARY (balances + settlements) ─────────────────
    @GetMapping("/groups/{groupId}/summary")
    public ResponseEntity<GroupSummaryResponse> getSummary(@PathVariable Long groupId) {
        return ResponseEntity.ok(service.getGroupSummary(groupId));
    }

    // ── GET ALL MEMBERS OF A GROUP ─────────────────────────────────
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(memberRepository.findByGroupId(groupId));
    }
}