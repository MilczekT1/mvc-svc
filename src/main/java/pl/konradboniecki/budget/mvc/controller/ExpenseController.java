package pl.konradboniecki.budget.mvc.controller;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.budget.mvc.model.frontendforms.ExpenseCreationForm;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;
import pl.konradboniecki.budget.mvc.service.client.budgetmanagement.BudgetMgtServiceFacade;

import javax.validation.Valid;

@Controller
@RequestMapping(value = "/budget/manage/expense")
public class ExpenseController {

    private BudgetMgtServiceFacade budgetMgtServiceFacade;
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @Autowired
    public ExpenseController(BudgetMgtServiceFacade budgetMgtServiceFacade) {
        this.budgetMgtServiceFacade = budgetMgtServiceFacade;
    }

    @GetMapping("/show-form")
    public ModelAndView showExpenseForm(@ModelAttribute("budgetId") Long budgetId, ModelMap modelMap) {
        modelMap.addAttribute("newExpenseCreationForm", new ExpenseCreationForm());
        modelMap.addAttribute("budgetId", budgetId);
        return new ModelAndView(ViewTemplate.EXPENSE_CREATION_PAGE, modelMap);
    }

    @PostMapping("/remove")
    public ModelAndView removeExpenseFromBudget(@ModelAttribute("expenseId") Long expenseId,
                                                @ModelAttribute("budgetId") Long budgetId) {
        budgetMgtServiceFacade.deleteExpenseInBudget(expenseId, budgetId);
        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.BUDGET);
    }

    @PostMapping("/add")
    public ModelAndView addExpense(
            @ModelAttribute("newExpenseCreationForm") @Valid ExpenseCreationForm form,
            @ModelAttribute("budgetId") @NonNull Long budgetId, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            //TODO: check if budget id is lost or not
            return new ModelAndView(ViewTemplate.EXPENSE_CREATION_PAGE);
        }
        Expense expense = new Expense(form);
        expense.setBudgetId(budgetId);
        budgetMgtServiceFacade.saveExpense(expense, expense.getBudgetId());

        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.BUDGET);
    }
}
