package pl.konradboniecki.budget.mvc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Invitation;
import pl.konradboniecki.budget.mvc.service.SecurityHelper;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;
import pl.konradboniecki.budget.mvc.service.client.AccountManagementClient;
import pl.konradboniecki.budget.mvc.service.client.FamilyManagementClient;
import pl.konradboniecki.budget.mvc.service.client.MailServiceClient;

import java.util.Optional;
import java.util.UUID;

import static pl.konradboniecki.budget.mvc.service.ErrorType.*;
import static pl.konradboniecki.budget.mvc.service.client.FamilyIdType.FAMILY_ID;

@Slf4j
@Controller
@RequestMapping(value = "/budget/family/invitations")
public class InvitationController {

    private AccountManagementClient accMgtClient;
    private MailServiceClient mailServiceClient;
    private FamilyManagementClient familyManagementClient;
    private SecurityHelper securityHelper;
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @Autowired
    public InvitationController(AccountManagementClient accMgtClient, MailServiceClient mailServiceClient,
                                FamilyManagementClient familyManagementClient, SecurityHelper securityHelper) {
        this.accMgtClient = accMgtClient;
        this.mailServiceClient = mailServiceClient;
        this.familyManagementClient = familyManagementClient;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/invite-to-family")
    public ModelAndView handleInvitationToFamilyFromApp(@RequestParam("newMemberEmail") String newMemberEmail,
                                                        @ModelAttribute("familyObject") Family family) {
        String invitationCode = UUID.randomUUID().toString();
        boolean isNewUser = false;
        Optional<Account> newMember = accMgtClient.findAccountByEmail(newMemberEmail);
        if (newMember.isPresent()) {
            Account account = accMgtClient.findAccountByEmail(newMemberEmail).get();
            Optional<Account> owner = accMgtClient.findAccountById(family.getOwnerId());
            mailServiceClient.sendFamilyInvitationToExistingUser(family, account, owner.get(), invitationCode);
        } else {
            // Invitation Code is not necessary
            isNewUser = true;
            String inviterEmail = SecurityContextHolder.getContext().getAuthentication().getName();//TODO: securityHelper.getEmailOfLoggedUser();
            Optional<Account> inviter = accMgtClient.findAccountByEmail(inviterEmail);
            if (inviter.isPresent()) {
                mailServiceClient.sendFamilyInvitationToNewUser(inviter.get(), family, newMemberEmail);
            } else {
                log.error("Inviter has not been found: returning 500.");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong.");
            }
        }

        Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(newMemberEmail, family.getId());
        if (invitation.isPresent()) {
            familyManagementClient.deleteInvitationById(invitation.get().getId());
        }
        familyManagementClient.saveInvitation(new Invitation(newMemberEmail, family.getId(), invitationCode, isNewUser));

        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.FAMILY_HOME_PAGE, "familyObject", family);
    }

    @PostMapping("/invite-to-family/resend-invitation")
    public ModelAndView resendInvitationMail(@RequestParam("invitationId") Long invitationId) {
        Optional<Invitation> invitation = familyManagementClient.findInvitationById(invitationId);
        if (invitation.isPresent()) {
            String emailDest = invitation.get().getEmail();
            Optional<Account> account = accMgtClient.findAccountByEmail(emailDest);
            Family family = familyManagementClient.findFamilyByIdWithType(invitation.get().getFamilyId(), FAMILY_ID).get();
            if (account.isPresent()) {
                Optional<Account> owner = accMgtClient.findAccountById(family.getOwnerId());
                mailServiceClient.sendFamilyInvitationToExistingUser(family, account.get(), owner.get(), invitation.get().getInvitationCode());
            } else {
                String inviterEmail = SecurityContextHolder.getContext().getAuthentication().getName();//TODO: securityHelper.getEmailOfLoggedUser();
                Optional<Account> inviter = accMgtClient.findAccountByEmail(inviterEmail);
                if (inviter.isPresent()) {
                    mailServiceClient.sendFamilyInvitationToNewUser(inviter.get(), family, emailDest);
                } else {
                    log.error("Inviter has not been found: returning 500.");
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong.");
                }
            }
        }
        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.FAMILY_HOME_PAGE);
    }

    @GetMapping("/{familyId}/addMember/{id}/{invitationCode}")
    public ModelAndView addAccountToFamily(@PathVariable("invitationCode") String code,
                                           @PathVariable("id") Long accountId,
                                           @PathVariable("familyId") Long familyId) {

        Optional<Account> accountOpt = accMgtClient.findAccountById(accountId);
        if (familyManagementClient.findFamilyByIdWithType(familyId, FAMILY_ID).isPresent() &&
                accountOpt.isPresent()) {

            Account account = accountOpt.get();
            if (account.hasFamily()) {
                return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", ALREADY_IN_FAMILY);
            } else {
                Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(account.getEmail(), familyId);
                if (invitation.isPresent()) {
                    if (!invitation.get().getInvitationCode().equals(code)) {
                        log.error("Wrong invitation code: " + invitation.get().toString()
                                + "and given invitation code: " + code);
                        return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", INVALID_INVITATION_LINK);
                    } else {
                        if (familyManagementClient.countFreeSlotsInFamilyWithId(familyId) > 0) {
                            accMgtClient.setFamilyIdInAccountWithId(familyId, accountId);
                            familyManagementClient.deleteInvitationById(invitation.get().getId());
                        } else {
                            log.error("Not enough space in family for new user " + account.toString() +
                                    "in family with id: " + familyId);
                            return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", NOT_ENOUGH_SPACE_IN_FAMILY);
                        }
                    }
                } else {
                    log.error("No such family invitation with  " + account.getEmail() + " and familyId:" + familyId);
                    return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", INVALID_INVITATION_LINK);
                }
            }
        }
        return new ModelAndView("redirect:" + BASE_URL + "/login");
    }

    @PostMapping("/accept-invitation-in-family-creation-form")
    public ModelAndView acceptInvitationInFamilyCreationForm(
            @RequestParam(value = "familyOwnerId") Long ownerId) {

        String inviteeEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Account> invitee = accMgtClient.findAccountByEmail(inviteeEmail);
        Optional<Account> ownerOpt = accMgtClient.findAccountById(ownerId);
        Long familyId = ownerOpt.get().getFamilyId();

        Optional<Invitation> invitationToDelete = familyManagementClient.findInvitationByEmailAndFamilyId(inviteeEmail, familyId);
        if (invitationToDelete.isPresent()) {
            familyManagementClient.deleteInvitationById(invitationToDelete.get().getId());
        }

        accMgtClient.setFamilyIdInAccountWithId(ownerOpt.get().getFamilyId(), invitee.get().getId());
        return new ModelAndView("redirect:" + BASE_URL + "/budget/family");
    }

    @PostMapping("/remove")
    public ModelAndView removeInvitation(@RequestParam("invitationId") Long invitationId) {
        Optional<Invitation> familyInvitation = familyManagementClient.findInvitationById(invitationId);
        if (familyInvitation.isPresent()) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();// TODO: use security helper
            familyManagementClient.deleteInvitationById(invitationId);
            log.info("Invitation with id: " + invitationId + " to " + familyInvitation.get().getEmail()
                    + " has been deleted by: " + email);
        }
        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.FAMILY_HOME_PAGE);
    }
}
