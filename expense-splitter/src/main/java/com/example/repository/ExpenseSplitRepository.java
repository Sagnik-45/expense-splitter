package com.example.repository;

import com.example.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    // This navigates: ExpenseSplit → expense → group → id
    // SELECT * FROM expense_splits WHERE expense_id IN
    //   (SELECT id FROM expenses WHERE group_id = ?)
    List<ExpenseSplit> findByExpenseGroupId(Long groupId);
}
