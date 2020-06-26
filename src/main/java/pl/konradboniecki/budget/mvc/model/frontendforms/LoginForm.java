package pl.konradboniecki.budget.mvc.model.frontendforms;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class LoginForm {

    @NotEmpty(message = "{login.emailRequired}")
    @Pattern(regexp = "\\w+@\\w+.[a-zA-Z]+", message = "{login.emailRegex}")
    private String email;

    @NotEmpty(message = "{login.passwordRequired}")
    @Size(min = 6, max = 200, message = "{login.passwordSize}")
    private String password;
}
