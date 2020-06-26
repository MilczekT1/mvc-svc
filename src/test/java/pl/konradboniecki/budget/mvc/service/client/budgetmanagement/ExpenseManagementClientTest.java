package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.chassis.exceptions.BadRequestException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.budgetmanagement.ExpenseManagementClientTest.*;

@ExtendWith(SpringExtension.class)
@TestInstance(PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "stubrunner.cloud.loadbalancer.enabled=false"
        }
)
@AutoConfigureStubRunner(
        repositoryRoot = "http://77.55.214.60:5001/repository/maven-public/",
        ids = {STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID + ":" + STUB_VERSION + ":stubs"},
        stubsMode = REMOTE
)
public class ExpenseManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "budget-management";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;
    @Autowired
    private ExpenseManagementClient expenseManagementClient;

    @BeforeEach
    void setUp() {
        expenseManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void givenTwoExpensesInBudget_whenFetchExpenses_thenReturnList() {
        // Given:
        Long budgetId = 1L;
        // When:
        List<Expense> expenseList = expenseManagementClient.getAllExpensesFromBudgetWithId(budgetId);
        // Then:
        assertThat(expenseList).isNotNull();
        assertThat(expenseList.size()).isEqualTo(2);
        Assertions.assertAll(
                () -> assertThat(expenseList.get(0).getId()).isEqualTo(1L),
                () -> assertThat(expenseList.get(0).getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(expenseList.get(0).getAmount()).isEqualTo(3L),
                () -> assertThat(expenseList.get(0).getComment()).isEqualTo("test_comments_1"),
                () -> assertThat(expenseList.get(0).getCreated()).isEqualTo(Instant.parse("2019-06-16T10:22:54.246625Z")),
                () -> assertThat(expenseList.get(1).getId()).isEqualTo(2L),
                () -> assertThat(expenseList.get(1).getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(expenseList.get(1).getAmount()).isEqualTo(4L),
                () -> assertThat(expenseList.get(1).getComment()).isEqualTo("test_comments_2"),
                () -> assertThat(expenseList.get(1).getCreated()).isEqualTo(Instant.parse("2019-06-16T10:28:23.053553Z"))
        );
    }

    @Test
    void givenNoExpensesInBudget_whenFetchExpenses_thenReturnEmptyList() {
        // Given:
        Long budgetId = 10L;
        // When:
        List<Expense> expenseList = expenseManagementClient.getAllExpensesFromBudgetWithId(budgetId);
        // Then:
        assertThat(expenseList).isNotNull();
        assertThat(expenseList.size()).isEqualTo(0);
    }

    @Test
    void givenNoExpenseInBudget_whenRemoveExpense_thenReturnFalse() {
        // Given:
        Long budgetId = 1L;
        Long absentExpenseId = 2L;
        // When:
        boolean isDeleted = expenseManagementClient.deleteExpenseInBudget(absentExpenseId, budgetId);
        // Then:
        assertThat(isDeleted).isFalse();
    }

    @Test
    void givenExpenseInBudget_whenRemoveExpense_thenReturnTrue() {
        // Given:
        Long budgetId = 1L;
        Long absentExpenseId = 1L;
        // When:
        boolean isDeleted = expenseManagementClient.deleteExpenseInBudget(absentExpenseId, budgetId);
        // Then:
        assertThat(isDeleted).isTrue();
    }

    @Test
    void givenValidExpense_whenSaveExpense_thenReturnExpense() {
        // Given:
        Long budgetId = 1L;
        Expense expense = new Expense()
                .setBudgetId(budgetId)
                .setComment("testComment")
                .setAmount(3L);
        // When:
        Expense savedExpense = expenseManagementClient.saveExpense(expense, budgetId);
        // Then:
        assertThat(savedExpense).isNotNull();
        Assertions.assertAll(
                () -> assertThat(savedExpense.getId()).isGreaterThan(0L),
                () -> assertThat(savedExpense.getComment()).isEqualTo("testComment"),
                () -> assertThat(savedExpense.getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(savedExpense.getAmount()).isEqualTo(3L),
                () -> assertThat(savedExpense.getCreated()).isInstanceOf(Instant.class)
        );
    }

    @Test
    void givenInvalidBudgetInPathAndBody_whenSave_thenThrow() {
        // Given:
        Long budgetIdInPath = 65L;
        Long budgetIdInBody = 2L;
        Expense expense = new Expense()
                .setBudgetId(budgetIdInBody)
                .setAmount(3L);
        // When:
        Throwable throwable = catchThrowable(() -> expenseManagementClient.saveExpense(expense, budgetIdInPath));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(BadRequestException.class);
    }
}
