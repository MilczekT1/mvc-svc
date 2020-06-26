package pl.konradboniecki.budget.mvc.controller;

import com.google.common.base.Throwables;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.frontendforms.AccountForm;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;
import pl.konradboniecki.budget.mvc.service.client.AccountManagementClient;
import pl.konradboniecki.budget.mvc.service.client.MailServiceClient;

import javax.validation.Valid;
import java.util.Optional;

import static pl.konradboniecki.budget.mvc.service.ErrorType.PROCESSING_EXCEPTION;

@Log
@Controller
public class RegisterController {

    private AccountManagementClient accMgtClient;
    private MailServiceClient mailServiceClient;

    @Autowired
    public RegisterController(AccountManagementClient accMgtClient, MailServiceClient mailServiceClient) {
        this.accMgtClient = accMgtClient;
        this.mailServiceClient = mailServiceClient;
    }

    @PostMapping("/register")
    public ModelAndView register(@ModelAttribute("accountFormObject") @Valid AccountForm newAccountForm,
                                 BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView(ViewTemplate.REGISTRATION_PAGE);
        } else if (!newAccountForm.checkRepeatedPassword()) {
            return new ModelAndView(ViewTemplate.REGISTRATION_PAGE, "repeatedPasswordFailure", true);
        }

        Optional<Account> accOpt = accMgtClient.findAccountByEmail(newAccountForm.getEmail());
        if (!accOpt.isPresent()) {
            Account accFromForm = new Account(newAccountForm);
            accMgtClient.saveAccount(accFromForm);
            accFromForm = accMgtClient.findAccountByEmail(accFromForm.getEmail()).get();

            String activationCode = accMgtClient.createActivationCodeForAccount(accFromForm.getId());
            return sendEmailWithActivationCode(activationCode, accFromForm);
        } else {
            return new ModelAndView(ViewTemplate.REGISTRATION_PAGE, "emailAlreadyExists", true);
        }
    }

    @GetMapping("/register")
    public ModelAndView showRegisterPane() {
        return new ModelAndView(ViewTemplate.REGISTRATION_PAGE, "accountFormObject", new AccountForm());
    }

    private ModelAndView sendEmailWithActivationCode(String activationCode, Account accFromForm) {
        try {
            mailServiceClient.sendSignUpConfirmation(accFromForm, activationCode);
            return new ModelAndView(ViewTemplate.REGISTRATION_SUCCESS_MSG);
        } catch (HttpClientErrorException | NullPointerException e) {
            log.severe(Throwables.getStackTraceAsString(e));
            return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType",
                    PROCESSING_EXCEPTION.getErrorTypeVarName());
        }
    }
}
