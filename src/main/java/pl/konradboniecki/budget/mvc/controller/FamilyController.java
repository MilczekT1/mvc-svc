package pl.konradboniecki.budget.mvc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Budget;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Invitation;
import pl.konradboniecki.budget.mvc.model.frontendforms.FamilyCreationForm;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;
import pl.konradboniecki.budget.mvc.service.client.AccountManagementClient;
import pl.konradboniecki.budget.mvc.service.client.FamilyManagementClient;
import pl.konradboniecki.budget.mvc.service.client.budgetmanagement.BudgetMgtServiceFacade;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static pl.konradboniecki.budget.mvc.service.ErrorType.PROCESSING_EXCEPTION;
import static pl.konradboniecki.budget.mvc.service.client.FamilyIdType.FAMILY_ID;

@Slf4j
@Controller
@RequestMapping(value = "/budget/family")
public class FamilyController {

    private BudgetMgtServiceFacade budgetMgtServiceFacade;
    private AccountManagementClient accMgtClient;
    private FamilyManagementClient familyManagementClient;
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @Autowired
    public FamilyController(BudgetMgtServiceFacade budgetMgtServiceFacade,
                            AccountManagementClient accMgtClient,
                            FamilyManagementClient familyManagementClient) {
        this.budgetMgtServiceFacade = budgetMgtServiceFacade;
        this.accMgtClient = accMgtClient;
        this.familyManagementClient = familyManagementClient;
    }

    @GetMapping
    public ModelAndView showFamily(ModelMap modelMap) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Account> acc = accMgtClient.findAccountByEmail(email);
        if (!acc.isPresent()) {
            return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", PROCESSING_EXCEPTION);
        }

        HashMap<String, Object> modelAttributes = new HashMap<>();
        if (acc.get().hasFamily()) {
            // when user has family then get his family, get all invitations to this family
            Family family = familyManagementClient.findFamilyByIdWithType(acc.get().getFamilyId(), FAMILY_ID).get();
            modelAttributes.put("familyObject", family);
            List<Invitation> invitations = familyManagementClient.findAllInvitationsByFamilyId(acc.get().getFamilyId());
            modelMap.putIfAbsent("invitationsList", invitations);
            modelMap.addAttribute("familyObject", family);
            return new ModelAndView(ViewTemplate.FAMILY_HOME_PAGE, modelMap);
        } else {
            modelAttributes.put("newFamilyCreationForm", new FamilyCreationForm());
            List<Invitation> invitations = familyManagementClient.findAllInvitationsByEmail(email);
            List<Long> familyIds = new LinkedList<>();
            for (Invitation invitation : invitations) {
                familyIds.add(invitation.getFamilyId());
            }
            invitations.clear();

            if (!familyIds.isEmpty()) {
                Optional<Family> family;
                List<Account> familyOwners = new LinkedList<>();
                for (Long familyId : familyIds) {
                    family = familyManagementClient.findFamilyByIdWithType(familyId, FAMILY_ID);
                    Optional<Account> account = accMgtClient.findAccountById(family.get().getOwnerId());
                    familyOwners.add(account.get());
                }
                modelAttributes.put("familyOwnersList", familyOwners);
            }
            return new ModelAndView(ViewTemplate.FAMILY_CREATION_PAGE, modelAttributes);
        }
    }

    @PostMapping("/create")
    public ModelAndView createFamilyFromForm(@ModelAttribute("newFamilyCreationForm")
                                             @Valid FamilyCreationForm familyCreationForm,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Error during validation of family form: {}", familyCreationForm);
            return new ModelAndView(ViewTemplate.FAMILY_CREATION_PAGE);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Account> acc = accMgtClient.findAccountByEmail(email);

        Family family = new Family(familyCreationForm, acc.get().getId());
        family = familyManagementClient.saveFamily(family);

        accMgtClient.setFamilyIdInAccountWithId(family.getId(), acc.get().getId());

        Budget budget = budgetMgtServiceFacade.saveBudget(new Budget().setFamilyId(family.getId()));
        family.setBudgetId(budget.getId());
        familyManagementClient.updateFamily(family);

        return new ModelAndView("redirect:" + BASE_URL + "/budget/family");
    }

    @PostMapping("/remove-family")
    public ModelAndView removeFamily(@RequestParam("familyId") Long id) {
        if (familyManagementClient.findFamilyByIdWithType(id, FAMILY_ID).isPresent()) {
            familyManagementClient.deleteFamilyById(id);
            return new ModelAndView("redirect:" + BASE_URL + "/budget/family");
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "family with id:" + id + " not found.");
        }
    }
}
