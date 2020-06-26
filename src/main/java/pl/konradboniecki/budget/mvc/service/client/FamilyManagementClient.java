package pl.konradboniecki.budget.mvc.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Invitation;
import pl.konradboniecki.chassis.exceptions.BadRequestException;
import pl.konradboniecki.chassis.exceptions.ResourceConflictException;
import pl.konradboniecki.chassis.exceptions.ResourceNotFoundException;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class FamilyManagementClient {

    private RestTemplate restTemplate;
    @Setter
    @Value("${budget.baseUrl.familyManagement}")
    private String BASE_URL;

    @Autowired
    public FamilyManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Family> findFamilyByIdWithType(Long id, FamilyIdType idType) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Family> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family/" + id + "?idType=" + idType.getQueryParamName(),
                    HttpMethod.GET,
                    httpEntity, Family.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            log.error("Family with id: " + id + " not found.");
            return Optional.empty();
        }
    }

    public boolean deleteFamilyById(Long familyId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family/" + familyId,
                    HttpMethod.DELETE,
                    httpEntity, Void.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            //TODO: improve handling as is should be only not found
            throw new ResourceNotFoundException("Failed to delete family with id: " + familyId, e);
        }
    }

    public Long countFreeSlotsInFamilyWithId(Long familyId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family/" + familyId + "/slots",
                    HttpMethod.GET,
                    httpEntity, JsonNode.class);
            return responseEntity.getBody().path("freeSlots").asLong();
        } catch (HttpClientErrorException e) {
            throw new ResourceNotFoundException("Failed to fetch free slots in family with id: " + familyId, e);
        }
    }

    public Family saveFamily(Family family) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Family> httpEntity = new HttpEntity<>(family, headers);
        try {
            ResponseEntity<Family> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family",
                    HttpMethod.POST,
                    httpEntity, Family.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException.Conflict e) {
            log.error("Failed to save app with id: {}, already exists.", family.getId());
            throw new ResourceConflictException("Conflict during family creation. Conflict.", e);
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to create family.", e);
        }
    }

    public Family updateFamily(Family family) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(family, headers);
        try {
            ResponseEntity<Family> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family",
                    HttpMethod.PUT,
                    httpEntity, Family.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Family not found with id: " + family.getId(), e);
        }
    }

    public boolean deleteInvitationById(Long invitationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family-invitations/" + invitationId,
                    HttpMethod.DELETE,
                    httpEntity, Void.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Family with id: " + invitationId + " not found.", e);
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to delete family invitation", e);
        }
    }

    public Invitation saveInvitation(Invitation invitation) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Invitation> httpEntity = new HttpEntity<>(invitation, headers);
        ResponseEntity<Invitation> responseEntity = restTemplate.exchange(
                BASE_URL + "/api/family-invitations",
                HttpMethod.POST,
                httpEntity, Invitation.class);
        return responseEntity.getBody();
    }

    public List<Invitation> findAllInvitationsByEmail(String email) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);

        try {
            ResponseEntity<List<Invitation>> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family-invitations/find-all?email=" + email,
                    HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<List<Invitation>>() {
                    });
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch all invitations for email: " + email);
            return Collections.emptyList();
        }
    }

    public List<Invitation> findAllInvitationsByFamilyId(Long id) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);

        try {
            ResponseEntity<List<Invitation>> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family-invitations/find-all?familyId=" + id,
                    HttpMethod.GET,
                    httpEntity, new ParameterizedTypeReference<List<Invitation>>() {
                    });
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch all invitations to family with id: " + id, e);
            return Collections.emptyList();
        }
    }

    public Optional<Invitation> findInvitationByEmailAndFamilyId(String email, Long familyId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);

        try {
            ResponseEntity<Invitation> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family-invitations/find-one?email=" + email + "&familyId=" + familyId + "&strict=true",
                    HttpMethod.GET,
                    httpEntity, Invitation.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("FamilyInvitation with email:" + email + " and id: " + familyId + " not found.", e);
            return Optional.empty();
        }
    }

    public Optional<Invitation> findInvitationById(Long id) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);

        try {
            ResponseEntity<Invitation> responseEntity = restTemplate.exchange(
                    BASE_URL + "/api/family-invitations/find-one?id=" + id,
                    HttpMethod.GET,
                    httpEntity, Invitation.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("FamilyInvitation with id:" + id + " not found.", e);
            return Optional.empty();
        }
    }
}
