package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Jar;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class JarManagementClient {

    @Setter
    @Value("${budget.baseUrl.budgetManagement}")
    private String BASE_URL;
    private RestTemplate restTemplate;

    @Autowired
    public JarManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Jar> findInBudgetById(Long budgetId, Long jarId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Jar> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/jars/" + jarId,
                    HttpMethod.GET,
                    httpEntity, Jar.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("jar with id: " + jarId + " not found in budget with id: " + budgetId);
            return Optional.empty();
        }
    }

    public List<Jar> getAllJarsFromBudgetWithId(Long budgetId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<List<Jar>> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/jars",
                    HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<List<Jar>>() {
                    });
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("error occured during fetch of all jars from budget with id: " + budgetId);
            return Collections.EMPTY_LIST;
        }
    }

    public boolean removeJarFromBudget(Long jarId, Long budgetId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/jars/" + jarId,
                    HttpMethod.DELETE,
                    httpEntity, String.class);
            return responseEntity.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Jar with id: " + jarId + " not found for budget with id: " + budgetId);
            return false;
        } catch (HttpClientErrorException e) {
            log.error("Failed to delete jar with id: " + jarId + ", from budget with id: " + budgetId + ".", e);
            return false;
        }
    }

    public Jar saveJar(Jar jar, Long budgetId) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Jar> httpEntity = new HttpEntity<>(jar, headers);

        try {
            ResponseEntity<Jar> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/jars",
                    HttpMethod.POST,
                    httpEntity, Jar.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to create jar.", e);
            throw e;
        }
    }

    public Optional<Jar> updateJar(Jar jar, Long budgetId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(jar, headers);

        try {
            ResponseEntity<Jar> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/jars/" + jar.getId(),
                    HttpMethod.PUT,
                    httpEntity, Jar.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Failed to update jar in budget with id: " + budgetId, e);
            return Optional.empty();
        }
    }
}
