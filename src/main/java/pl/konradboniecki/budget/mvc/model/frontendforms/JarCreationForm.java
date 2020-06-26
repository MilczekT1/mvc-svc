package pl.konradboniecki.budget.mvc.model.frontendforms;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class JarCreationForm {

    @Min(value = 1, message = "{jarCreationForm.capacitySize}")
    private Long capacity;

    @Size(min = 1, max = 50)
    @NotEmpty(message = "{jarCreationForm.jarNameRequired}")
    @Pattern(regexp = "\\w{1,50}", message = "{jarCreationForm.jarNameRegex}")
    private String jarName;

}
