package pl.konradboniecki.budget.mvc.model.frontendforms;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class AccountForm {

    @NotEmpty(message = "{registration.firstNameRequired}")
    @Pattern(regexp = "[a-zA-Z]{3,}", message = "{registration.firstNameRegex}")
    private String firstName;

    @NotEmpty(message = "{registration.lastNameRequired}")
    @Pattern(regexp = "[a-zA-Z]{2,}", message = "{registration.lastNameRegex}")
    private String lastName;

    @NotEmpty(message = "{registration.emailRequired}")
    @Pattern(regexp = "(\\w||\\.)+@\\w+.[a-zA-Z]+", message = "{registration.emailRegex}")
    private String email;

    @NotEmpty(message = "{registration.passwordRequired}")
    @Size(min = 6, max = 200, message = "{registration.passwordSize}")
    private String password;

    @NotEmpty(message = "{registration.repeatedPasswordRequired}")
    @Size(min = 6, max = 200, message = "{registration.repeatedPasswordSize}")
    private String repeatedPassword;

    public boolean checkRepeatedPassword() {
        return password.equals(repeatedPassword);
    }
}
