package pl.konradboniecki.budget.mvc.model.frontendforms;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ExpenseCreationForm {

    @Size(max = 50, message = "{expenseCreationForm.expenseCommentMaxSize}")
    private String comment;
    private Long amount;
}
