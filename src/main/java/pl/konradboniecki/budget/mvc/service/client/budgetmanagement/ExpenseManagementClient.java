package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.chassis.exceptions.BadRequestException;
import pl.konradboniecki.chassis.exceptions.InternalServerErrorException;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;
import pl.konradboniecki.chassis.tools.PaginatedList;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class ExpenseManagementClient {

    @Setter
    @Value("${budget.baseUrl.budgetManagement}")
    private String BASE_URL;
    private RestTemplate restTemplate;

    @Autowired
    public ExpenseManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Expense> getAllExpensesFromBudgetWithId(Long budgetId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<PaginatedList<Expense>> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/expenses",
                    HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<PaginatedList<Expense>>() {
                    });
            return responseEntity.getBody().getItems();
        } catch (HttpClientErrorException | NullPointerException e) {
            log.error("Error occurred during fetch of all expenses from budget with id: " + budgetId, e);
            return Collections.EMPTY_LIST;
        }
    }

    public Expense saveExpense(Expense ex, Long budgetId) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(ex, headers);
        try {
            ResponseEntity<Expense> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/expenses",
                    HttpMethod.POST,
                    httpEntity, Expense.class);
            return responseEntity.getBody();
        } catch (HttpServerErrorException e) {
            log.error("Failed to save expense.", e);
            throw new InternalServerErrorException("Failed to save expense.", e);
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Failed to save expense.", e);
            throw new BadRequestException("Failed to save expense.");
        }
    }

    public boolean deleteExpenseInBudget(Long id, Long budgetId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/budgets/" + budgetId + "/expenses/" + id,
                    HttpMethod.DELETE,
                    httpEntity, String.class);
            return responseEntity.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Expense with id: " + id + " not found in budget with id: " + budgetId);
            return false;
        } catch (HttpClientErrorException e) {
            log.error("Failed to remove expense With id: " + id + "from budget with id: " + budgetId, e);
            return false;
        }
    }

//    public Optional<Expense> findExpenseByIdInBudget(Long expenseId, Long budgetId) {
//        HttpHeaders headers = defaultGetHTTPHeaders();
//        HttpEntity httpEntity = new HttpEntity(headers);
//
//        try {
//            ResponseEntity<Expense> responseEntity = restTemplate.exchange(
//                    BUDGET_URI + "/api/budgets/" + budgetId + "/expenses/" + expenseId,
//                    HttpMethod.GET,
//                    httpEntity, Expense.class);
//            return Optional.ofNullable(responseEntity.getBody());
//        } catch (HttpClientErrorException e) {
//            log.error("Expense with id: " + expenseId + " not found in budget with id: " + budgetId);
//            return Optional.empty();
//        }
//    }
//
//    public Expense updateExpense(Expense expense, Long budgetId) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity httpEntity = new HttpEntity(expense, headers);
//        ResponseEntity<Expense> responseEntity = restTemplate.exchange(
//                BUDGET_URI + "/api/budgets/" + budgetId + "/expenses/" + expense.getId(),
//                HttpMethod.PUT,
//                httpEntity, Expense.class);
//        return responseEntity.getBody();
//    }
}
