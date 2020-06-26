package pl.konradboniecki.budget.mvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Invitation {

    private Long id;
    private Long familyId;
    private String email;
    private String invitationCode;
    private Instant applyTime;
    private Boolean registeredStatus;

    public Invitation(String email, Long familyId) {
        setApplyTime(Instant.now());
        setEmail(email);
        setFamilyId(familyId);
    }

    public Invitation(String email, Long familyId, String invitationCode, Boolean registeredStatus) {
        this(email, familyId);
        setInvitationCode(invitationCode);
        setRegisteredStatus(registeredStatus);
    }
}
