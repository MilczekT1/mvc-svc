package pl.konradboniecki.budget.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.Account;

@Data
@Accessors(chain = true)
public class SignUpConfirmation {
    @JsonProperty("Account")
    private Account account;
    @JsonProperty("ActivationCode")
    private String activationCode;
}
