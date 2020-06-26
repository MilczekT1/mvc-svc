package pl.konradboniecki.budget.mvc.model.frontendforms;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class FamilyCreationForm {

    @NotEmpty(message = "{familyCreationForm.titleRequired}")
    @Pattern(regexp = "[a-zA-Z][a-zA-Z ]{4,50}", message = "{familyCreationForm.titleRegex}")
    private String title;
}
