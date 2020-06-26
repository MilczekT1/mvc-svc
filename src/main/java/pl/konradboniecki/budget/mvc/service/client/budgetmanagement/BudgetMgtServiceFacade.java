package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.mvc.model.Budget;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.budget.mvc.model.Jar;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BudgetMgtServiceFacade {

    private BudgetManagementClient budgetManagementClient;
    private ExpenseManagementClient expenseManagementClient;
    private JarManagementClient jarManagementClient;

    @Autowired
    public BudgetMgtServiceFacade(BudgetManagementClient budgetManagementClient,
                                  ExpenseManagementClient expenseManagementClient,
                                  JarManagementClient jarManagementClient) {
        this.budgetManagementClient = budgetManagementClient;
        this.expenseManagementClient = expenseManagementClient;
        this.jarManagementClient = jarManagementClient;
    }

    public Optional<Budget> findBudgetByFamilyId(Long familyId) {
        return budgetManagementClient.findBudgetByFamilyId(familyId);
    }

    public Budget saveBudget(Budget budget) {
        return budgetManagementClient.saveBudget(budget);
    }

    public Optional<Jar> findJarByIdInBudget(Long budgetId, Long id) {
        return jarManagementClient.findInBudgetById(budgetId, id);
    }

    public List<Jar> getAllJarsFromBudgetWithId(Long budgetId) {
        return jarManagementClient.getAllJarsFromBudgetWithId(budgetId);
    }

    public boolean removeJarFromBudget(Long jarId, Long budgetId) {
        return jarManagementClient.removeJarFromBudget(jarId, budgetId);
    }

    public Jar saveJar(Jar jar, Long budgetId) {
        return jarManagementClient.saveJar(jar, budgetId);
    }

    public Optional<Jar> updateJar(Jar jar, Long budgetId) {
        return jarManagementClient.updateJar(jar, budgetId);
    }

    public List<Expense> getAllExpensesFromBudgetWithId(Long budgetId) {
        return expenseManagementClient.getAllExpensesFromBudgetWithId(budgetId);
    }

    public Expense saveExpense(Expense ex, Long budgetId) {
        return expenseManagementClient.saveExpense(ex, budgetId);
    }

    public boolean deleteExpenseInBudget(Long id, Long budgetId) {
        return expenseManagementClient.deleteExpenseInBudget(id, budgetId);
    }
}
