package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.mvc.model.Budget;
import pl.konradboniecki.chassis.exceptions.InternalServerErrorException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.budgetmanagement.BudgetManagementClientTest.*;

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
public class BudgetManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "budget-management";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private BudgetManagementClient budgetManagementClient;

    @BeforeEach
    void setUp() {
        budgetManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }


    @Test
    void givenFailure_whenSaveBudget_thenRethrow500() {
        // Given:
        Budget budgetToSave = new Budget()
                .setMaxJars(8L)
                .setFamilyId(8L);
        // When:
        Throwable throwable = catchThrowable(
                () -> budgetManagementClient.saveBudget(budgetToSave));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(InternalServerErrorException.class);
    }

    @Test
    void givenSuccess_whenSaveBudget_thenReturnBudget() {
        // Given:
        Budget budgetToSave = new Budget()
                .setMaxJars(6L)
                .setFamilyId(6L);
        // When:
        Budget budget = budgetManagementClient.saveBudget(budgetToSave);
        // Then:
        assertThat(budget).isNotNull();
        assertThat(budget.getId()).isGreaterThan(0L);
        assertThat(budget.getFamilyId()).isEqualTo(6L);
        assertThat(budget.getMaxJars()).isEqualTo(6L);
    }

    @Test
    void givenPresentFamily_whenFind_thenReturnFamily() {
        // Given:
        Long presentFamilyId = 1L;
        // When:
        Optional<Budget> budgetOptional = budgetManagementClient.findBudgetByFamilyId(presentFamilyId);
        // Then:
        assertThat(budgetOptional.isPresent()).isTrue();
        assertThat(budgetOptional.get().getId()).isGreaterThan(0L);
        assertThat(budgetOptional.get().getFamilyId()).isEqualTo(presentFamilyId);
        assertThat(budgetOptional.get().getMaxJars()).isEqualTo(6L);
    }

    @Test
    void givenAbsentFamily_whenFind_thenReturnEmpty() {
        // Given:
        Long absentFamilyId = 100L;
        // When:
        Optional<Budget> budgetOptional = budgetManagementClient.findBudgetByFamilyId(absentFamilyId);
        // Then:
        assertThat(budgetOptional.isPresent()).isFalse();
    }
}
