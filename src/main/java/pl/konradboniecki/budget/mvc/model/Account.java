package pl.konradboniecki.budget.mvc.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.frontendforms.AccountForm;
import pl.konradboniecki.chassis.tools.HashGenerator;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static pl.konradboniecki.budget.mvc.service.UserType.USER;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Account implements Serializable {

    private Long id;
    private Long familyId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private ZonedDateTime registerDate;
    private String role;
    private boolean enabled;
    private boolean budgetGranted;
    private boolean horseeGranted;

    public Account(AccountForm accountForm) {
        setFirstName(accountForm.getFirstName());
        setLastName(accountForm.getLastName());
        setEmail(accountForm.getEmail().toLowerCase());
        setPassword(accountForm.getPassword());
        setRole(USER.getRoleName());
        registerDate = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"));
        setEnabled(false);
        setBudgetGranted(false);
        setHorseeGranted(false);
    }

    public Account(JsonNode jsonNode) {
        if (jsonNode.has("id")) {
            Long id = jsonNode.path("id").asLong();
            if (id != 0L) setId(id);
        }
        if (jsonNode.has("familyId")) {
            Long familyId = jsonNode.path("familyId").asLong();
            if (familyId != 0L) setFamilyId(familyId);
        }
        if (jsonNode.has("firstName")) setFirstName(jsonNode.path("firstName").asText());
        if (jsonNode.has("lastName")) setLastName(jsonNode.path("lastName").asText());
        if (jsonNode.has("email")) setEmail(jsonNode.path("email").asText());
        if (jsonNode.has("role")) setRole(jsonNode.path("role").asText());
        if (jsonNode.has("enabled")) setEnabled(jsonNode.path("enabled").asBoolean());
        if (jsonNode.has("budgetGranted")) setBudgetGranted(jsonNode.path("budgetGranted").asBoolean());
        if (jsonNode.has("horseeGranted")) setHorseeGranted(jsonNode.path("horseeGranted").asBoolean());
    }

    public Account setPassword(String password) {
        this.password = new HashGenerator().hashPassword(password);
        return this;
    }

    public Account setEmail(String email) {
        this.email = email.toLowerCase();
        return this;
    }

    public boolean hasFamily() {
        return familyId != null;
    }
}
