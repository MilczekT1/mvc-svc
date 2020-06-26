package pl.konradboniecki.budget.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Family;

@Data
@Accessors(chain = true)
public class FamilyInvitationToNewUser {
    @JsonProperty("Inviter")
    private Account inviter;
    @JsonProperty("Family")
    private Family family;
    @JsonProperty("NewMemberEmail")
    private String newMemberEmail;
}
