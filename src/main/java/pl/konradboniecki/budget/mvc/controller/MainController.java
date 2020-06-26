package pl.konradboniecki.budget.mvc.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.frontendforms.LoginForm;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;

@Controller
public class MainController {
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @GetMapping("/")
    public ModelAndView mainPage() {
        return new ModelAndView(ViewTemplate.INDEX);
    }

    @GetMapping(value = "/login")
    public ModelAndView showLoginPage() {
        return new ModelAndView(ViewTemplate.LOGIN_PAGE, "loginForm", new LoginForm());
    }

    @PostMapping("/")
    public ModelAndView showUserHomePageAfterCorrectLogin() {
        return new ModelAndView("redirect:" + BASE_URL + "/");
    }

    @GetMapping(value = "/error")
    public ModelAndView customError() {
        return new ModelAndView(ViewTemplate.ERROR_PAGE);
    }
}
