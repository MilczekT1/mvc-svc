package pl.konradboniecki.budget.mvc.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Budget {

    private Long id;
    private Long familyId;
    private Long maxJars;

    public Budget() {
        setMaxJars(6L);
    }
}

