package pl.konradboniecki.budget.mvc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;

@Slf4j
@Controller
@RequestMapping
public class HomeController {

    @GetMapping("/budget/home")
    public ModelAndView showBudgetHomePage() {
        return new ModelAndView(ViewTemplate.BUDGET_HOME_PAGE);
    }

    @GetMapping("/horsee/home")
    public ModelAndView showHorseeHomePage() {
        return new ModelAndView(ViewTemplate.HORSEE_HOME_PAGE);
    }
}
