package pl.konradboniecki.budget.mvc.service.client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.dto.FamilyInvitationToNewUser;
import pl.konradboniecki.budget.mvc.model.dto.FamilyInvitationToOldUser;
import pl.konradboniecki.budget.mvc.model.dto.SignUpConfirmation;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class MailServiceClient {

    private RestTemplate restTemplate;
    @Setter
    @Value("${budget.baseUrl.mail}")
    private String BASE_URL;

    @Autowired
    public MailServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean sendSignUpConfirmation(Account account, String activationCode) throws HttpStatusCodeException {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        SignUpConfirmation signUpConfirmation = new SignUpConfirmation()
                .setAccount(account)
                .setActivationCode(activationCode);
        HttpEntity<SignUpConfirmation> httpEntity = new HttpEntity<>(signUpConfirmation, headers);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                BASE_URL + "/api/mail/activate-account",
                HttpMethod.POST,
                httpEntity, Void.class);
        return responseEntity.getStatusCode() == HttpStatus.OK;
    }

    public boolean sendFamilyInvitationToNewUser(Account owner, Family family, String destEmail) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        FamilyInvitationToNewUser requestBody = new FamilyInvitationToNewUser()
                .setInviter(owner)
                .setNewMemberEmail(destEmail)
                .setFamily(family);
        HttpEntity<FamilyInvitationToNewUser> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                BASE_URL + "/api/mail/invite-user/new",
                HttpMethod.POST,
                httpEntity, Void.class);
        return responseEntity.getStatusCode() == HttpStatus.OK;
    }

    public boolean sendFamilyInvitationToExistingUser(Family family, Account accountToInvite, Account owner, String invitationCode) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        FamilyInvitationToOldUser requestBody = new FamilyInvitationToOldUser()
                .setAccount(accountToInvite)
                .setInviter(owner)
                .setFamily(family)
                .setInvitationCode(invitationCode);

        HttpEntity<FamilyInvitationToOldUser> httpEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                BASE_URL + "/api/mail/invite-user/existing",
                HttpMethod.POST,
                httpEntity, Void.class);
        return responseEntity.getStatusCode() == HttpStatus.OK;
    }
}
