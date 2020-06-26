package pl.konradboniecki.budget.mvc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Budget;
import pl.konradboniecki.budget.mvc.model.Jar;
import pl.konradboniecki.budget.mvc.model.frontendforms.JarCreationForm;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;
import pl.konradboniecki.budget.mvc.service.client.AccountManagementClient;
import pl.konradboniecki.budget.mvc.service.client.budgetmanagement.BudgetMgtServiceFacade;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping(value = "/budget/manage")
public class JarController {

    private BudgetMgtServiceFacade budgetMgtServiceFacade;
    private AccountManagementClient accMgtClient;
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @Autowired
    public JarController(BudgetMgtServiceFacade budgetMgtServiceFacade, AccountManagementClient accMgtClient) {
        this.budgetMgtServiceFacade = budgetMgtServiceFacade;
        this.accMgtClient = accMgtClient;
    }

    @GetMapping("/create-jar")
    public ModelAndView createJar(ModelMap modelMap) {
        modelMap.put("newJarCreationForm", new JarCreationForm());
        return new ModelAndView(ViewTemplate.JAR_CREATION_PAGE, modelMap);
    }

    @PostMapping("/create-jar")
    public ModelAndView createJar(
            @ModelAttribute("newJarCreationForm") @Valid JarCreationForm jarCreationForm, BindingResult bindingResult, ModelMap modelMap) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView(ViewTemplate.JAR_CREATION_PAGE);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Account> accOpt = accMgtClient.findAccountByEmail(email);
        if (!accOpt.isPresent()) {
            throw new RuntimeException("Account doesn't exist");
        }
        Account acc = accOpt.get();

        Optional<Budget> budgetOpt = budgetMgtServiceFacade.findBudgetByFamilyId(acc.getFamilyId());
        if (!accOpt.isPresent()) {
            throw new RuntimeException("Budget doesn't exist");
        }
        Budget budget = budgetOpt.get();

        List<Jar> jarList = budgetMgtServiceFacade.getAllJarsFromBudgetWithId(budget.getId());
        //TODO: replace with redirect? this way refresh of page in browser will not create
        if (jarList.size() < budget.getMaxJars()) {
            Jar jar = new Jar(jarCreationForm);
            jar.setBudgetId(budget.getId());
            budgetMgtServiceFacade.saveJar(jar, budget.getId());
            jarList = budgetMgtServiceFacade.getAllJarsFromBudgetWithId(budget.getId());
            modelMap.addAttribute("jarList", jarList);
            return new ModelAndView(ViewTemplate.BUDGET, modelMap);
        } else {
            modelMap.put("maxJarsAmountExceeded", true);
            modelMap.put("jarList", jarList);
            return new ModelAndView(ViewTemplate.BUDGET, modelMap);
        }
    }

    @PostMapping("/remove-jar")
    public ModelAndView removeJarFromBudget(@RequestParam("jarId") Long jarId,
                                            @RequestParam("budgetId") Long budgetId,
                                            ModelMap modelMap) {
        budgetMgtServiceFacade.removeJarFromBudget(jarId, budgetId);
        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.BUDGET, modelMap);
    }

    @PostMapping("/change-current-amount")
    public ModelAndView changeCurrentAmountInJarWithId(
            @RequestParam("jarId") Long jarId,
            @RequestParam("amount") Long amount,
            @RequestParam("budgetId") Long budgetId,
            ModelMap modelMap) {

        Optional<Jar> jarOpt = budgetMgtServiceFacade.findJarByIdInBudget(budgetId, jarId);
        if (jarOpt.isPresent()) {
            Jar jar = jarOpt.get();
            jar.setCurrentAmount(jar.getCurrentAmount() + amount);
            budgetMgtServiceFacade.updateJar(jar, budgetId);
            return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.BUDGET, modelMap);
        } else {
            return new ModelAndView(ViewTemplate.ERROR_PAGE, modelMap);
        }

    }
}
