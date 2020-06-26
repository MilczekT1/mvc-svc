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
import org.springframework.web.client.HttpClientErrorException;
import pl.konradboniecki.budget.mvc.model.Jar;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.budgetmanagement.JarManagementClientTest.*;

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
public class JarManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "budget-management";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private JarManagementClient jarManagementClient;

    @BeforeEach
    void setUp() {
        jarManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void givenAbsentJarId_whenNotFoundInBudget_thenReturnEmpty() {
        // Given:
        Long budgetId = 1L;
        Long absentJarId = 5L;
        // When:
        Optional<Jar> jar = jarManagementClient.findInBudgetById(budgetId, absentJarId);
        // Then:
        assertThat(jar.isPresent()).isFalse();
    }

    @Test
    void givenPresentJarId_whenFoundInBudget_thenReturnJar() {
        // Given:
        Long budgetId = 1L;
        Long presentJarId = 1L;
        // When:
        Optional<Jar> jarO = jarManagementClient.findInBudgetById(budgetId, presentJarId);
        // Then:
        assertThat(jarO.isPresent()).isTrue();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(jarO.get().getId()).isEqualTo(presentJarId),
                () -> assertThat(jarO.get().getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(jarO.get().getJarName().isEmpty()).isFalse(),
                () -> assertThat(jarO.get().getCurrentAmount()).isGreaterThan(0L),
                () -> assertThat(jarO.get().getCapacity()).isGreaterThan(0L),
                () -> assertThat(jarO.get().getStatus()).isEqualTo("IN PROGRESS")
        );
    }

    @Test
    void givenBudgetWithoutJars_whenJarsNotFound_thenReturnEmptyList() {
        // Given:
        Long budgetWithoutJarsId = 2L;
        // When:
        List<Jar> listOfJars = jarManagementClient.getAllJarsFromBudgetWithId(budgetWithoutJarsId);
        // Then:
        assertThat(listOfJars.isEmpty()).isTrue();
    }

    @Test
    void givenBudgetWithJars_whenJarsFound_thenReturnList() {
        // Given:
        Long budgetWithJarsId = 1L;
        // When:
        List<Jar> listOfJars = jarManagementClient.getAllJarsFromBudgetWithId(budgetWithJarsId);
        // Then:
        assertThat(listOfJars.isEmpty()).isFalse();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(listOfJars.size()).isEqualTo(2),
                () -> assertThat(listOfJars.get(0).getId()).isEqualTo(1L),
                () -> assertThat(listOfJars.get(0).getBudgetId()).isEqualTo(1L),
                () -> assertThat(listOfJars.get(0).getJarName()).isEqualTo("name1"),
                () -> assertThat(listOfJars.get(0).getCurrentAmount()).isEqualTo(0L),
                () -> assertThat(listOfJars.get(0).getCapacity()).isEqualTo(3L),
                () -> assertThat(listOfJars.get(0).getStatus()).isEqualTo("IN PROGRESS"),
                () -> assertThat(listOfJars.get(1).getId()).isEqualTo(2L),
                () -> assertThat(listOfJars.get(1).getBudgetId()).isEqualTo(1L),
                () -> assertThat(listOfJars.get(1).getJarName()).isEqualTo("name2"),
                () -> assertThat(listOfJars.get(1).getCurrentAmount()).isEqualTo(0L),
                () -> assertThat(listOfJars.get(1).getCapacity()).isEqualTo(3L),
                () -> assertThat(listOfJars.get(1).getStatus()).isEqualTo("IN PROGRESS")
        );
    }

    @Test
    void givenPresentJar_whenDelete_thenReturnTrue() {
        // Given:
        Long budgetId = 1L;
        Long jarId = 1L;
        // When:
        Boolean result = jarManagementClient.removeJarFromBudget(jarId, budgetId);
        // Then:
        assertThat(result).isTrue();
    }

    @Test
    void givenAbsentJar_whenDelete_thenReturnFalse() {
        // Given:
        Long budgetId = 1L;
        Long absentJarId = 5L;
        // When:
        Boolean result = jarManagementClient.removeJarFromBudget(absentJarId, budgetId);
        // Then:
        assertThat(result).isFalse();
    }

    @Test
    void givenInvalidBudgetIdInPathAndBody_whenSave_thenThrow() {
        // Given:
        Long pathBudgetId = 1L;
        Long payloadBudgetId = 3L;
        Jar jarToSave = new Jar()
                .setBudgetId(payloadBudgetId)
                .setJarName("testJarName")
                .setCapacity(6L);
        // When:
        Throwable throwable = catchThrowable(
                () -> jarManagementClient.saveJar(jarToSave, pathBudgetId));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void givenValidJar_whenSave_thenReturnJar() {
        // Given:
        Long budgetId = 1L;
        Jar jarToSave = new Jar()
                .setBudgetId(budgetId)
                .setJarName("testJarName")
                .setCapacity(6L);
        // When:
        Jar jar = jarManagementClient.saveJar(jarToSave, budgetId);
        // Then:
        assertThat(jar).isNotNull();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(jar.getId()).isGreaterThan(0L),
                () -> assertThat(jar.getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(jar.getJarName()).isEqualTo("testJarName"),
                () -> assertThat(jar.getCapacity()).isEqualTo(6L),
                () -> assertThat(jar.getCurrentAmount()).isEqualTo(0L),
                () -> assertThat(jar.getStatus()).isEqualTo("IN PROGRESS")
        );
    }

    @Test
    void givenAbsentJar_whenUpdate_thenReturnEmpty() {
        // Given:
        Long budgetId = 1L;
        Long absentJarId = 5L;
        Jar jarToUpdate = new Jar()
                .setId(absentJarId)
                .setBudgetId(budgetId)
                .setJarName("testJarName")
                .setCapacity(6L)
                .setCurrentAmount(5L);
        // When:
        Optional<Jar> jarO = jarManagementClient.updateJar(jarToUpdate, budgetId);
        // Then:
        assertThat(jarO.isPresent()).isFalse();
    }

    @Test
    void givenPresentJar_whenUpdate_thenReturnJar() {
        // Given:
        Long budgetId = 4L;
        Long presentJarId = 4L;
        Jar jarToUpdate = new Jar()
                .setId(presentJarId)
                .setBudgetId(budgetId)
                .setJarName("testJarName")
                .setCapacity(6L)
                .setCurrentAmount(5L);
        // When:
        Optional<Jar> jarO = jarManagementClient.updateJar(jarToUpdate, budgetId);
        // Then:
        assertThat(jarO.isPresent()).isTrue();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(jarO.get().getId()).isEqualTo(presentJarId),
                () -> assertThat(jarO.get().getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(jarO.get().getJarName()).isEqualTo(jarToUpdate.getJarName()),
                () -> assertThat(jarO.get().getCapacity()).isEqualTo(jarToUpdate.getCapacity()),
                () -> assertThat(jarO.get().getCurrentAmount()).isEqualTo(jarToUpdate.getCurrentAmount()),
                () -> assertThat(jarO.get().getStatus()).isEqualTo("IN PROGRESS")
        );
    }
}
