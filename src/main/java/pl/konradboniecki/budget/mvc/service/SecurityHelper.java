package pl.konradboniecki.budget.mvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.service.client.AccountManagementClient;

//TODO: test this class
@Service
public class SecurityHelper {

    private AccountManagementClient accountManagementClient;

    @Autowired
    public SecurityHelper(AccountManagementClient accountManagementClient) {
        this.accountManagementClient = accountManagementClient;
    }

    public String getEmailOfLoggedUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Account getLoggedAccountByEmail(String email) {
        return accountManagementClient.findAccountByEmail(email)
                .orElseThrow(RuntimeException::new);
    }
}
