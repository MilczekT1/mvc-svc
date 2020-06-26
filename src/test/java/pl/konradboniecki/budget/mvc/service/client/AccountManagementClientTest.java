package pl.konradboniecki.budget.mvc.service.client;

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
import org.springframework.web.client.HttpClientErrorException;
import pl.konradboniecki.budget.mvc.model.Account;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.AccountManagementClientTest.*;

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
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class AccountManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "account-management";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private AccountManagementClient accountManagementClient;

    @BeforeEach
    void setUp() {
        accountManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void whenFoundAccountById_thenHandle200Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountById(1L);
        assertThat(accountResponse.isPresent());

        Account acc = accountResponse.get();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(acc.getId()).isNotNull(),
                () -> assertThat(acc.getFirstName()).isNotNull(),
                () -> assertThat(acc.getLastName()).isNotNull(),
                () -> assertThat(acc.getFamilyId()).isNotNull(),
                () -> assertThat(acc.getEmail()).isNotNull(),
                () -> assertThat(acc).isNotNull(),
                () -> assertThat(acc.isBudgetGranted()).isTrue(),
                () -> assertThat(acc.isHorseeGranted()).isTrue()
        );
    }

    @Test
    void whenNotFoundAccountById_thenHandle404Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountById(2L);
        assertThat(accountResponse.isPresent()).isFalse();
    }

    @Test
    void whenFoundAccountByEmail_thenHandle200Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountByEmail("existing_email@find_by_mail.com");
        assertThat(accountResponse.isPresent());

        Account acc = accountResponse.get();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(acc.getId()).isNotNull(),
                () -> assertThat(acc.getFirstName()).isNotNull(),
                () -> assertThat(acc.getLastName()).isNotNull(),
                () -> assertThat(acc.getFamilyId()).isNotNull(),
                () -> assertThat(acc.getEmail()).isNotNull(),
                () -> assertThat(acc).isNotNull(),
                () -> assertThat(acc.isBudgetGranted()).isTrue(),
                () -> assertThat(acc.isHorseeGranted()).isFalse()
        );
    }

    @Test
    void whenNotFoundAccountByEmail_thenHandle404Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountByEmail("notExistingEmail@mail.com");
        assertThat(accountResponse.isPresent()).isFalse();
    }

    @Test
    void whenAlreadyCreatedAccountDuringCreation_thenHandle409Response() {
        // Given:
        Account accountToSave = new Account()
                .setFirstName("mvcTestFirstName")
                .setLastName("mvcTestLastName")
                .setEmail("existing_email@mail.com")
                .setPassword("randomTestPasswd");
        // When:
        Throwable throwable = catchThrowableOfType(() ->
                        accountManagementClient.saveAccount(accountToSave),
                Throwable.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(HttpClientErrorException.Conflict.class);
    }

    @Test
    void whenNotCreatedAccount_thenHandle200Response() {
        // Given:
        Account accountToSave = new Account()
                .setFirstName("mvcTestFirstName")
                .setLastName("mvcTestLastName")
                .setEmail("not_existing_email@mail.com")
                .setPassword("randomTestPasswd");
        // When:
        Account retrievedAccount = accountManagementClient.saveAccount(accountToSave);
        // Then:
        Assertions.assertAll(
                () -> assertThat(retrievedAccount.getId()).isNotNull(),
                () -> assertThat(retrievedAccount.getFamilyId()).isNull(),
                () -> assertThat(retrievedAccount.getFirstName()).isEqualTo(accountToSave.getFirstName()),
                () -> assertThat(retrievedAccount.getLastName()).isEqualTo(accountToSave.getLastName()),
                () -> assertThat(retrievedAccount.getEmail()).isEqualTo(accountToSave.getEmail()),
                () -> assertThat(retrievedAccount.getPassword()).isNull(),
                () -> assertThat(retrievedAccount.getEmail()).isEqualTo(accountToSave.getEmail()),
                () -> assertThat(retrievedAccount.getRegisterDate()).isNull(),
                () -> assertThat(retrievedAccount.getRole()).isEqualTo("USER"),
                () -> assertThat(retrievedAccount.isEnabled()).isFalse(),
                () -> assertThat(retrievedAccount.isBudgetGranted()).isTrue(),
                () -> assertThat(retrievedAccount.isHorseeGranted()).isTrue()
        );

    }

    @Test
    void whenCreatedActivationCode_thenHandle201Response() {
        // Given:
        Long accIdForActivationCodeGeneration = 5L;
        // When:
        String activationCode = accountManagementClient.createActivationCodeForAccount(accIdForActivationCodeGeneration);
        // Then:
        assertThat(activationCode).isNotNull();
        assertThat(activationCode).isNotBlank();
    }

    @Test
    void whenAccountNotFoundDuringActivationCodeCreation_thenHandle404Response() {
        // Given:
        Long accIdForActivationCodeGeneration = 1000L;
        // When:
        Throwable throwable = catchThrowableOfType(() ->
                        accountManagementClient.createActivationCodeForAccount(accIdForActivationCodeGeneration),
                Throwable.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void givenCorrectPassword_whenCheckIfPasswordIsCorrect_thenReturnTrue() {
        // When:
        Boolean isPasswordValid = accountManagementClient.checkIfPasswordIsCorrect(5L, "correctHashValue");
        // Then:
        assertThat(isPasswordValid).isTrue();
    }

    @Test
    void givenIncorrectPassword_whenCheckIfPasswordIsCorrect_thenReturnFalse() {
        // When:
        Boolean isPasswordValid = accountManagementClient.checkIfPasswordIsCorrect(5L, "incorrectHashValue");
        // Then:
        assertThat(isPasswordValid).isFalse();
    }

    @Test
    void givenAbsentAccount_whenCheckIfPasswordIsCorrect_thenReturnFalse() {
        // When:
        Boolean isPasswordValid = accountManagementClient.checkIfPasswordIsCorrect(4L, "notImportantHashValue");
        // Then:
        assertThat(isPasswordValid).isFalse();
    }

    @Test
    void whenFamilyAssignedToAccount_thenHandle200Response() {
        // When:
        Boolean isFamilyAssigned = accountManagementClient.setFamilyIdInAccountWithId(1L, 1L);
        // Then:
        assertThat(isFamilyAssigned).isTrue();
    }

    @Test
    void givenAbsentFamily_whenAssignFamilyToAccount_thenHandle404Response() {
        // Given:
        // When:
        Boolean isFamilyAssigned = accountManagementClient.setFamilyIdInAccountWithId(5L, 1L);
        // Then:
        assertThat(isFamilyAssigned).isFalse();
    }

    @Test
    void givenAbsentAccount_whenAssignAccountToFAmily_thenHandle404Response() {
        // Given:
        // When:
        Boolean isFamilyAssigned = accountManagementClient.setFamilyIdInAccountWithId(1L, 2L);
        // Then:
        assertThat(isFamilyAssigned).isFalse();
    }
}
