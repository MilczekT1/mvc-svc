package pl.konradboniecki.budget.mvc.service.client;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Family;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.MailServiceClientTest.*;

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
public class MailServiceClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "mail";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private MailServiceClient mailServiceClient;

    @BeforeEach
    void setUp() {
        mailServiceClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }


    @Test
    void sendSignUpConfirmation() {
        // Given:
        String activationCode = "29431ce1-8282-4489-8dd9-50f91e4c5653";
        Account acc = new Account()
                .setFirstName("testFirstName")
                .setLastName("testLastName")
                .setEmail("test@mail.com")
                .setId(2L);
        // When:
        boolean isSent = mailServiceClient.sendSignUpConfirmation(acc, activationCode);
        // Then:
        Assertions.assertThat(isSent).isTrue();
    }

    @Test
    void inviteExistingUser() {
        // Given:
        Account account = new Account()
                .setId(2L)
                .setFirstName("testFirstName1")
                .setLastName("testLastName1")
                .setEmail("email@email1.com");
        Account inviter = new Account()
                .setId(1L)
                .setFirstName("testFirstName2")
                .setLastName("testLastName2")
                .setEmail("email@email2.com");
        Family family = new Family()
                .setId(3L)
                .setTitle("testFamilyTitle");
        String invitationCode = "8f37ab38-971a-471e-9fac-d8e63e47ce34";
        // When:
        boolean isSent = mailServiceClient.sendFamilyInvitationToExistingUser(family, account, inviter, invitationCode);
        // Then:
        Assertions.assertThat(isSent).isTrue();
    }

    @Test
    void inviteNewUser() {
        // Given:
        Account inviter = new Account()
                .setId(1L)
                .setFirstName("testFirstName2")
                .setLastName("testLastName2")
                .setEmail("email@email2.com");
        Family family = new Family()
                .setId(3L)
                .setTitle("testFamilyTitle");
        String newMemberEmail = "test@mail.com";
        // When:
        boolean isSent = mailServiceClient.sendFamilyInvitationToNewUser(inviter, family, newMemberEmail);
        // Then:
        Assertions.assertThat(isSent).isTrue();
    }
}
