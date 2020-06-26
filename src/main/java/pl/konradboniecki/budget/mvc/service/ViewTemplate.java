package pl.konradboniecki.budget.mvc.service;

import lombok.extern.java.Log;

/**
 * ViewTemplate provides names for html templates.
 **/

@Log
public class ViewTemplate {
    public static final String LOGIN_PAGE = "auth/login";
    public static final String REGISTRATION_PAGE = "auth/registration";
    public static final String REGISTRATION_SUCCESS_MSG = "auth/registrationSuccessInfo";

    public static final String ERROR_PAGE = "error";
    public static final String INDEX = "index";

    public static final String HORSEE_HOME_PAGE = "horsee/home";
    public static final String BUDGET_HOME_PAGE = "budget/home";
    public static final String BUDGET = "budget/manage";
    public static final String JAR_CREATION_PAGE = "budget/manage/jarCreationForm";
    public static final String EXPENSE_CREATION_PAGE = "budget/manage/expenseCreationForm";
    public static final String FAMILY_HOME_PAGE = "budget/family";
    public static final String FAMILY_CREATION_PAGE = "budget/family/familyCreationForm";

    private ViewTemplate() {
        log.severe("this class shouldn't be instantiated!");
    }
}
