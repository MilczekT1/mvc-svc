package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Budget;
import pl.konradboniecki.chassis.exceptions.InternalServerErrorException;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Optional;

import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class BudgetManagementClient {

    @Setter
    @Value("${budget.baseUrl.budgetManagement}")
    private String BASE_URL;
    private RestTemplate restTemplate;

    @Autowired
    public BudgetManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Budget> findBudgetByFamilyId(Long familyId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);

        try {
            ResponseEntity<Budget> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + familyId + "?idType=family",
                    HttpMethod.GET,
                    httpEntity, Budget.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Budget with family_id: " + familyId + " not found.");
            return Optional.empty();
        }
    }

    public Budget saveBudget(Budget budget) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Budget> httpEntity = new HttpEntity<>(budget, headers);
        try {
            ResponseEntity<Budget> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets",
                    HttpMethod.POST,
                    httpEntity, Budget.class);
            return responseEntity.getBody();
        } catch (HttpServerErrorException e) {
            log.error("Failed to save budget.", e);
            throw new InternalServerErrorException("Failed to save budget.", e);
        }
    }
}
