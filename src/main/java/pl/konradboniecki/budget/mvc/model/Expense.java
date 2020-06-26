package pl.konradboniecki.budget.mvc.model;

import lombok.Data;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.frontendforms.ExpenseCreationForm;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class Expense {

    private Long id;
    private Long budgetId;
    private Long amount;
    private String comment;
    private Instant created;

    public Expense() {
        setCreated(Instant.now());
    }

    public Expense(ExpenseCreationForm expenseCreationForm) {
        this();
        setAmount(expenseCreationForm.getAmount());
        setComment(expenseCreationForm.getComment());
    }
}
