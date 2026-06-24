package com.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class AddExpenseRequest {
    private Long groupId;
    private String description;
    private Double amount;
    private Long paidByMemberId;
    private List<Long> splitAmongMemberIds;  // empty = split among all
}
