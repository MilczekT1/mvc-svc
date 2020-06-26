package pl.konradboniecki.budget.mvc.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.chassis.exceptions.ResourceConflictException;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class AccountManagementClient {

    private RestTemplate restTemplate;
    @Setter
    @Value("${budget.baseUrl.accountManagement}")
    private String BASE_URL;

    @Autowired
    public AccountManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Account> findAccountById(Long id) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Account> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/account/" + id + "?findBy=id",
                    HttpMethod.GET,
                    httpEntity, Account.class);
            return Optional.of(responseEntity.getBody());
        } catch (HttpClientErrorException | NullPointerException e) {
            log.error("Account with id: " + id + " not found.", e);
            return Optional.empty();
        }
    }

    public Optional<Account> findAccountByEmail(String email) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Account> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/account/" + email + "?findBy=email",
                    HttpMethod.GET,
                    httpEntity, Account.class);
            return Optional.of(responseEntity.getBody());
        } catch (HttpClientErrorException | NullPointerException e) {
            log.error("Account with email: " + email + " not found.", e);
            return Optional.empty();
        }
    }

    public Account saveAccount(Account accountToSave) throws ResourceConflictException {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(accountToSave, headers);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/account",
                    HttpMethod.POST,
                    httpEntity, JsonNode.class);
            return new Account(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Failed to create account with email: " + accountToSave.getEmail());
            throw e;
        }
    }

    public String createActivationCodeForAccount(Long accountId) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.set("id", accountId.toString());
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/account/activationCode",
                    HttpMethod.POST,
                    httpEntity, JsonNode.class);
            return responseEntity.getBody().path("activationCode").asText();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Error during activation code creation. Account with id {} not found.", accountId);
            throw e;
        }
    }

    public Boolean checkIfPasswordIsCorrect(Long accountId, String hashedPassword) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.set("accountId", accountId.toString());
        headers.set("password", hashedPassword);
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/account/credentials",
                    HttpMethod.GET,
                    httpEntity, Void.class);
            return responseEntity.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            if (e instanceof HttpClientErrorException.BadRequest) {
                log.info("Password not matched for account with id: " + accountId, e);
            } else if (e instanceof HttpClientErrorException.NotFound) {
                log.error("Account with id: " + accountId + " not found during password check", e);
            } else {
                log.error("Failed to validate password, check failed.", e);
            }
            return false;
        }
    }

    public Boolean setFamilyIdInAccountWithId(Long familyId, Long accountId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/account/" + accountId + "/family/" + familyId,
                    HttpMethod.PUT,
                    httpEntity, String.class);
            return responseEntity.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            if (e instanceof HttpClientErrorException.NotFound) {
                log.info("Account with id: {} or family with id: {} not found.", accountId, familyId);
            }
            return false;
        }
    }
}
