package com.example.service;

import com.example.dto.*;
import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupExpenseService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository splitRepository;

    // ── CREATE GROUP WITH MEMBERS ──────────────────────────────────
    public Group createGroup(CreateGroupRequest request) {
        Group group = new Group();
        group.setName(request.getGroupName());
        group = groupRepository.save(group);

        for (String name : request.getMemberNames()) {
            GroupMember member = new GroupMember(null, name, group);
            memberRepository.save(member);
        }
        return group;
    }

    // ── ADD EXPENSE ────────────────────────────────────────────────
    public Expense addExpense(AddExpenseRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupMember paidBy = memberRepository.findById(request.getPaidByMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setPaidBy(paidBy);
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense = expenseRepository.save(expense);

        // Decide who splits this expense
        List<Long> splitIds = request.getSplitAmongMemberIds();
        List<GroupMember> splitMembers;

        if (splitIds == null || splitIds.isEmpty()) {
            // Empty = split among ALL members in the group
            splitMembers = memberRepository.findByGroupId(request.getGroupId());
        } else {
            splitMembers = memberRepository.findAllById(splitIds);
        }

        double share = expense.getAmount() / splitMembers.size();
        // Round to 2 decimal places
        share = Math.round(share * 100.0) / 100.0;

        for (GroupMember member : splitMembers) {
            ExpenseSplit split = new ExpenseSplit(null, expense, member, share);
            splitRepository.save(split);
        }

        return expense;
    }

    // ── GET GROUP SUMMARY ──────────────────────────────────────────
    public GroupSummaryResponse getGroupSummary(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<ExpenseSplit> allSplits = splitRepository.findByExpenseGroupId(groupId);

        // Total of all expenses
        double totalExpenses = expenses.stream()
                .mapToDouble(Expense::getAmount).sum();

        // Track how much each member PAID
        Map<Long, Double> paidMap = new HashMap<>();
        members.forEach(m -> paidMap.put(m.getId(), 0.0));
        for (Expense expense : expenses) {
            Long payerId = expense.getPaidBy().getId();
            paidMap.merge(payerId, expense.getAmount(), Double::sum);
        }

        // Track how much each member was CHARGED (their share)
        Map<Long, Double> chargedMap = new HashMap<>();
        members.forEach(m -> chargedMap.put(m.getId(), 0.0));
        for (ExpenseSplit split : allSplits) {
            chargedMap.merge(split.getMember().getId(), split.getShare(), Double::sum);
        }

        // Balance = paid - charged
        // Positive = others owe them
        // Negative = they owe others
        Map<Long, Double> balanceMap = new HashMap<>();
        members.forEach(m -> {
            double balance = paidMap.get(m.getId()) - chargedMap.get(m.getId());
            // Round to 2 decimal places
            balanceMap.put(m.getId(), Math.round(balance * 100.0) / 100.0);
        });

        // Build member summaries
        List<MemberSummary> summaries = members.stream()
                .map(m -> new MemberSummary(
                        m.getId(),
                        m.getName(),
                        chargedMap.get(m.getId()),
                        paidMap.get(m.getId()),
                        balanceMap.get(m.getId())
                ))
                .collect(Collectors.toList());

        // Calculate settlements
        List<Settlement> settlements = calculateSettlements(members, balanceMap);

        return new GroupSummaryResponse(
                group.getId(),
                group.getName(),
                group.getCreatedDate(),
                totalExpenses,
                summaries,
                settlements
        );
    }

    // ── SETTLEMENT ALGORITHM ───────────────────────────────────────
    private List<Settlement> calculateSettlements(
            List<GroupMember> members, Map<Long, Double> balanceMap) {

        List<Settlement> settlements = new ArrayList<>();

        // Make a mutable copy of balances
        Map<Long, Double> balances = new HashMap<>(balanceMap);

        // Separate into debtors (owe money) and creditors (are owed money)
        LinkedList<GroupMember> debtors = new LinkedList<>();
        LinkedList<GroupMember> creditors = new LinkedList<>();

        for (GroupMember m : members) {
            double balance = balances.get(m.getId());
            if (balance < -0.01) debtors.add(m);
            else if (balance > 0.01) creditors.add(m);
        }

        // Sort: largest debtor first, largest creditor first
        debtors.sort((a, b) -> Double.compare(
                balances.get(a.getId()), balances.get(b.getId())));
        creditors.sort((a, b) -> Double.compare(
                balances.get(b.getId()), balances.get(a.getId())));

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            GroupMember debtor = debtors.getFirst();
            GroupMember creditor = creditors.getFirst();

            double owes = -balances.get(debtor.getId());
            double owed = balances.get(creditor.getId());

            double payment = Math.min(owes, owed);
            payment = Math.round(payment * 100.0) / 100.0;

            settlements.add(new Settlement(
                    debtor.getName(),
                    creditor.getName(),
                    payment
            ));

            // Update balances after payment
            balances.merge(debtor.getId(), payment, Double::sum);
            balances.merge(creditor.getId(), -payment, Double::sum);

            // Remove fully settled members
            if (Math.abs(balances.get(debtor.getId())) < 0.01) debtors.removeFirst();
            if (Math.abs(balances.get(creditor.getId())) < 0.01) creditors.removeFirst();
        }

        return settlements;
    }
}