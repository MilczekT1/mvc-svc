package pl.konradboniecki.budget.mvc.model;

import lombok.Data;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.frontendforms.FamilyCreationForm;

@Data
@Accessors(chain = true)
public class Family {

    private Long id;
    private Long ownerId;
    private Long budgetId;
    private String title;
    private Integer maxMembers;

    public Family() {
        setMaxMembers(5);
    }

    public Family(FamilyCreationForm familyCreationForm) {
        this();
        setTitle(familyCreationForm.getTitle());
    }

    public Family(FamilyCreationForm familyCreationForm, Long ownerId) {
        this(familyCreationForm);
        setOwnerId(ownerId);
    }
}
