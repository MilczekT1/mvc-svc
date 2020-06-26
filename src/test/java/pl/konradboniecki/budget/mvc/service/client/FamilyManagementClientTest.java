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
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Invitation;
import pl.konradboniecki.chassis.exceptions.ResourceConflictException;
import pl.konradboniecki.chassis.exceptions.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.FamilyManagementClientTest.*;

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
class FamilyManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "family-management";
    public static final String STUB_VERSION = "0.4.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;
    @Autowired
    private FamilyManagementClient familyManagementClient;

    @BeforeEach
    void setUp() {
        familyManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void givenOwnerId_whenFamilyFound_thenReturnFamily() {
        // Given:
        Long ownerId = 1L;
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyByIdWithType(ownerId, FamilyIdType.OWNER_ID);
        // Then:
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(familyResponse.isPresent()).isTrue(),
                () -> assertThat(familyResponse.get().getId()).isNotNull(),
                () -> assertThat(familyResponse.get().getOwnerId()).isNotNull(),
                () -> assertThat(familyResponse.get().getOwnerId()).isEqualTo(ownerId),
                () -> assertThat(familyResponse.get().getBudgetId()).isNotNull(),
                () -> assertThat(familyResponse.get().getTitle()).isNotNull(),
                () -> assertThat(familyResponse.get().getMaxMembers()).isNotNull()
        );
    }

    @Test
    void givenOwnerId_whenFamilyNotFound_thenReturnEmpty() {
        // Given:
        Long ownerId = 5L;
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyByIdWithType(ownerId, FamilyIdType.OWNER_ID);
        // Then:
        assertThat(familyResponse.isPresent()).isFalse();
    }

    @Test
    void givenFamilyId_whenFamilyNotFound_thenReturnEmpty() {
        // Given:
        Long familyId = 5L;
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyByIdWithType(familyId, FamilyIdType.FAMILY_ID);
        // Then:
        assertThat(familyResponse.isPresent()).isFalse();
    }

    @Test
    void givenFamilyId_whenFamilyFound_thenReturnFamily() {
        // Given:
        Long familyId = 1L;
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyByIdWithType(familyId, FamilyIdType.FAMILY_ID);
        // Then:
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(familyResponse.isPresent()).isTrue(),
                () -> assertThat(familyResponse.get().getId()).isNotNull(),
                () -> assertThat(familyResponse.get().getId()).isEqualTo(familyId),
                () -> assertThat(familyResponse.get().getOwnerId()).isNotNull(),
                () -> assertThat(familyResponse.get().getBudgetId()).isNotNull(),
                () -> assertThat(familyResponse.get().getTitle()).isNotNull(),
                () -> assertThat(familyResponse.get().getMaxMembers()).isNotNull()
        );
    }

    @Test
    void givenExistingFamily_whenCountSlots_thenReturnFreeSlots() {
        // Given:
        Long familyId = 1L;
        // When:
        Long freeSlots = familyManagementClient.countFreeSlotsInFamilyWithId(familyId);
        // Then:
        assertThat(freeSlots).isNotNull();
        assertThat(freeSlots).isEqualTo(4);
    }

    @Test
    void givenAbsentFamily_whenCountSlots_thenThrow() {
        // Given:
        Long familyId = 5L;
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.countFreeSlotsInFamilyWithId(familyId));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenExistingFamily_whenDelete_thenReturnTrue() {
        // Given:
        Long familyId = 100L;
        // When:
        boolean result = familyManagementClient.deleteFamilyById(familyId);
        // Then:
        assertThat(result).isTrue();
    }

    @Test
    void givenAbsentFamily_whenDelete_thenThrow() {
        // Given:
        Long familyId = 5L;
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.deleteFamilyById(familyId));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenNewFamily_whenSave_thenReturnFamily() {
        // Given:
        Family family = new Family()
                .setOwnerId(100L)
                .setMaxMembers(5)
                .setBudgetId(4L)
                .setTitle("title");
        // When:
        Family createdFamily = familyManagementClient.saveFamily(family);
        // Then:
        Assertions.assertAll(
                () -> assertThat(createdFamily).isNotNull(),
                () -> assertThat(createdFamily.getOwnerId()).isEqualTo(family.getOwnerId()),
                () -> assertThat(createdFamily.getMaxMembers()).isEqualTo(family.getMaxMembers()),
                () -> assertThat(createdFamily.getBudgetId()).isEqualTo(family.getBudgetId()),
                () -> assertThat(createdFamily.getTitle()).isEqualTo(family.getTitle()),
                () -> assertThat(createdFamily.getId()).isNotNull(),
                () -> assertThat(createdFamily.getId()).isGreaterThan(0L)
        );
    }

    @Test
    void givenConflict_whenSave_thenThrow() {
        // Given:
        Long idOfOwnerWhoAlreadyHasAFamily = 101L;
        Family familyToCreate = new Family()
                .setOwnerId(idOfOwnerWhoAlreadyHasAFamily)
                .setBudgetId(6L)
                .setTitle("testTitle")
                .setMaxMembers(5);
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.saveFamily(familyToCreate));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void givenAbsentFamily_whenUpdate_thenThrow() {
        // Given:
        Long invalidId = 5L;
        Family familyWithInvalidId = new Family()
                .setId(invalidId)
                .setOwnerId(5L)
                .setBudgetId(6L)
                .setTitle("testTitle")
                .setMaxMembers(5);
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.updateFamily(familyWithInvalidId));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenPresentFamily_whenUpdate_thenReturnEditedFamily() {
        // Given:
        Long familyId = 1L;
        Optional<Family> familyResponse = familyManagementClient.findFamilyByIdWithType(familyId, FamilyIdType.FAMILY_ID);
        Family familyBeforeEdit = familyResponse.get();
        Family familyDuringEdit = new Family()
                .setId(familyBeforeEdit.getId())
                .setBudgetId(familyBeforeEdit.getBudgetId())
                .setTitle("newTitle")
                .setMaxMembers(6)
                .setOwnerId(8L);
        // When:
        Family familyAfterEdit = familyManagementClient.updateFamily(familyDuringEdit);
        // Then:
        Assertions.assertAll(
                () -> assertThat(familyAfterEdit).isNotNull(),
                () -> assertThat(familyAfterEdit.getId()).isEqualTo(familyBeforeEdit.getId()),
                () -> assertThat(familyAfterEdit.getBudgetId()).isEqualTo(familyBeforeEdit.getBudgetId()),
                () -> assertThat(familyAfterEdit.getTitle()).isEqualTo(familyDuringEdit.getTitle()),
                () -> assertThat(familyAfterEdit.getMaxMembers()).isEqualTo(familyDuringEdit.getMaxMembers()),
                () -> assertThat(familyAfterEdit.getOwnerId()).isEqualTo(familyDuringEdit.getOwnerId())
        );
    }

    @Test
    void givenPresentInvitation_whenDelete_thenReturnTrue() {
        // Given:
        Long presentId = 1L;
        // When:
        boolean fiDeleted = familyManagementClient.deleteInvitationById(presentId);
        // Then:
        assertThat(fiDeleted).isTrue();
    }

    @Test
    void givenAbsentInvitation_whenDelete_thenThrow() {
        // Given:
        Long absentId = 5L;
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.deleteInvitationById(absentId));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenInvitation_whenSave_thenReturnInvitation() {
        // Given:
        Invitation invitationToSave = new Invitation()
                .setEmail("test@email.com")
                .setFamilyId(3L)
                .setInvitationCode(UUID.randomUUID().toString())
                .setRegisteredStatus(false);
        // When:
        Invitation invitationAfterSave = familyManagementClient.saveInvitation(invitationToSave);
        // Then:
        Assertions.assertAll(
                () -> assertThat(invitationAfterSave).isNotNull(),
                () -> assertThat(invitationAfterSave.getId()).isNotNull(),
                () -> assertThat(invitationAfterSave.getId()).isGreaterThan(0L),
                () -> assertThat(invitationAfterSave.getEmail()).isEqualTo(invitationToSave.getEmail()),
                () -> assertThat(invitationAfterSave.getFamilyId()).isEqualTo(invitationToSave.getFamilyId()),
                () -> assertThat(invitationAfterSave.getInvitationCode()).isEqualTo(invitationToSave.getInvitationCode()),
                () -> assertThat(invitationAfterSave.getRegisteredStatus()).isEqualTo(invitationToSave.getRegisteredStatus())
        );
    }

    @Test
    void givenEmail_whenNoInvitations_thenReturnEmptyList() {
        // Given:
        String emailWithoutInvitations = "email@without-invitations.com";
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByEmail(emailWithoutInvitations);
        // Then:
        assertThat(invitations.size()).isEqualTo(0);
    }

    @Test
    void givenEmail_whenHasInvitations_thenReturnList() {
        // Given:
        String emailWithInvitations = "email@with-invitations.com";
        Invitation firstInvitation = new Invitation()
                .setId(6L)
                .setFamilyId(6L)
                .setEmail(emailWithInvitations)
                .setInvitationCode("34b7a194-b0d3-47f7-8aef-1d64caefcdf4")
                .setApplyTime(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegisteredStatus(true);
        Invitation secondInvitation = new Invitation()
                .setId(7L)
                .setFamilyId(null)
                .setEmail(emailWithInvitations)
                .setInvitationCode("c04a8005-cb67-46de-a4dc-e4f84d26faf3")
                .setApplyTime(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegisteredStatus(true);
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByEmail(emailWithInvitations);
        // Then:
        assertThat(invitations.size()).isEqualTo(2);
        assertThat(invitations.contains(firstInvitation)).isTrue();
        assertThat(invitations.contains(secondInvitation)).isTrue();
    }

    @Test
    void givenFamilyId_whenNoInvitations_thenReturnEmptyList() {
        // Given:
        Long familyId = 5L;
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByFamilyId(familyId);
        // Then:
        assertThat(invitations.size()).isEqualTo(0);
    }

    @Test
    void givenFamilyId_whenHasInvitations_thenReturnList() {
        // Given:
        Long familyId = 1L;
        Invitation firstInvitation = new Invitation()
                .setId(6L)
                .setFamilyId(familyId)
                .setEmail("mail_1@mail.com")
                .setInvitationCode("34b7a194-b0d3-47f7-8aef-1d64caefcdf4")
                .setApplyTime(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegisteredStatus(true);
        Invitation secondInvitation = new Invitation()
                .setId(7L)
                .setFamilyId(familyId)
                .setEmail("mail_2@mail.com")
                .setInvitationCode("c04a8005-cb67-46de-a4dc-e4f84d26faf3")
                .setApplyTime(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegisteredStatus(false);
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByFamilyId(familyId);
        // Then:
        assertThat(invitations.size()).isEqualTo(2);
        assertThat(invitations.contains(firstInvitation)).isTrue();
        assertThat(invitations.contains(secondInvitation)).isTrue();
    }

    @Test
    public void givenEmailAndFamilyId_whenInvitationNotFound_thenReturnEmpty() {
        // Given:
        Long familyId = 5L;
        String email = "email@without-invitations.com";
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(email, familyId);
        // Then:
        assertThat(invitation.isPresent()).isFalse();
    }

    @Test
    public void givenEmailAndFamilyId_whenInvitationFound_thenReturnInvitation() {
        // Given:
        Long familyId = 1L;
        String email = "email@with-invitations.com";
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(email, familyId);
        // Then:
        assertThat(invitation.isPresent()).isTrue();
        Assertions.assertAll(
                () -> assertThat(invitation.get().getId()).isNotNull(),
                () -> assertThat(invitation.get().getEmail()).isEqualTo(email),
                () -> assertThat(invitation.get().getFamilyId()).isEqualTo(familyId),
                () -> assertThat(invitation.get().getInvitationCode()).isNotNull(),
                () -> assertThat(invitation.get().getRegisteredStatus()).isNotNull(),
                () -> assertThat(invitation.get().getApplyTime()).isNotNull()
        );
    }

    @Test
    public void givenInvitationId_whenNotFound_thenReturnEmpty() {
        // Given:
        Long id = 5L;
        String email = "email@without-invitations.com";
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(email, id);
        // Then:
        assertThat(invitation.isPresent()).isFalse();
    }

    @Test
    public void givenInvitationId_whenFound_thenReturnInvitation() {
        // Given:
        Long id = 1L;
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationById(id);
        // Then:
        assertThat(invitation.isPresent()).isTrue();
        Assertions.assertAll(
                () -> assertThat(invitation.get().getId()).isNotNull(),
                () -> assertThat(invitation.get().getId()).isEqualTo(1L),
                () -> assertThat(invitation.get().getEmail()).isNotNull(),
                () -> assertThat(invitation.get().getFamilyId()).isNotNull(),
                () -> assertThat(invitation.get().getInvitationCode()).isNotNull(),
                () -> assertThat(invitation.get().getRegisteredStatus()).isNotNull(),
                () -> assertThat(invitation.get().getApplyTime()).isNotNull()
        );
    }
}
