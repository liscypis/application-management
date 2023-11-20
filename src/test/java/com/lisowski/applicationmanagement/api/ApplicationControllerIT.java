package com.lisowski.applicationmanagement.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lisowski.applicationmanagement.mapper.dto.ApplicationAudDto;
import com.lisowski.applicationmanagement.mapper.dto.ApplicationDto;
import com.lisowski.applicationmanagement.mapper.dto.ReasonDto;
import com.lisowski.applicationmanagement.model.enums.Status;
import com.lisowski.applicationmanagement.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ApplicationControllerIT {
    @Autowired
    private ApplicationRepository repository;
    private final String URL = "http://localhost:9091/api/applications";
    @Autowired
    ObjectMapper objectMapper;

    private static final TestRestTemplate restTemplate = new TestRestTemplate();

    @BeforeAll
    public static void setup() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @BeforeEach
    public void clean() {
        repository.deleteAll();
    }

    @Test
    public void tryGetNotExistingApplication() {
        ResponseEntity<String> response = restTemplate.getForEntity(URL + "/43253465", String.class);

        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldCreateApplication() {
        ApplicationDto request = new ApplicationDto();
        request.setName("new app");
        request.setBody("Something");

        ResponseEntity<ApplicationDto> response = restTemplate.postForEntity(URL, request, ApplicationDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().getBody(), request.getBody());
        assertEquals(response.getBody().getName(), request.getName());
        assertEquals(response.getBody().getStatus(), Status.CREATED);
        assertNotNull(response.getBody().getId());
        assertNull(response.getBody().getReason());
        assertNull(response.getBody().getApplicationNumber());
    }

    @Test
    public void shouldReturnErrorWhenCreateApplicationWithoutName() {
        ApplicationDto request = new ApplicationDto();
        request.setBody("Something");

        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldReturnErrorWhenCreateApplicationWithoutBody() {
        ApplicationDto request = new ApplicationDto();
        request.setName("new name");

        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldEditCreatedApplication() {
        ApplicationDto application = createApplication();
        application.setName("newName");
        application.setBody("newBody");

        ResponseEntity<ApplicationDto> response = restTemplate.exchange(URL + "/" + application.getId(), HttpMethod.PUT, new HttpEntity<>(application), ApplicationDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().getBody(), application.getBody());
        assertEquals(response.getBody().getName(), application.getName());
        assertEquals(response.getBody().getStatus(), Status.CREATED);
        assertNull(response.getBody().getReason());
        assertNull(response.getBody().getApplicationNumber());
    }

    @Test
    public void shouldDeleteCreatedApplication() {
        ApplicationDto application = createApplication();
        ReasonDto reason = new ReasonDto();
        reason.setReason("some reason");

        ResponseEntity<String> response =
                restTemplate.exchange(URL + "/" + application.getId(), HttpMethod.DELETE, new HttpEntity<>(reason), String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void shouldThrowErrorWhenDeleteCreatedApplicationWithoutReason() {
        ApplicationDto application = createApplication();

        ResponseEntity<String> response =
                restTemplate.exchange(URL + "/" + application.getId(), HttpMethod.DELETE, new HttpEntity<>(new ReasonDto()), String.class);

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldVerifyCreatedApplication() {
        ApplicationDto application = createApplication();

        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);

        assertEquals(response.getBody(), application.getBody());
        assertEquals(response.getName(), application.getName());
        assertEquals(response.getStatus(), Status.VERIFIED);
        assertNull(response.getReason());
        assertNull(response.getApplicationNumber());
    }

    @Test
    public void shouldEditVerifiedApplication() {
        ApplicationDto application = createApplication();
        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.VERIFIED);
        application.setName("newName");
        application.setBody("newBody");

        ResponseEntity<ApplicationDto> updateResponse = restTemplate.exchange(URL + "/" + application.getId(), HttpMethod.PUT, new HttpEntity<>(application), ApplicationDto.class);

        assertEquals(updateResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(updateResponse.getBody().getBody(), application.getBody());
        assertEquals(updateResponse.getBody().getName(), application.getName());
        assertEquals(updateResponse.getBody().getStatus(), Status.VERIFIED);
        assertNull(updateResponse.getBody().getReason());
        assertNull(updateResponse.getBody().getApplicationNumber());
    }

    @Test
    public void shouldThrowExceptionWhenDeleteVerifiedApplication() {
        ApplicationDto application = createApplication();
        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.VERIFIED);
        ReasonDto reasonDto = new ReasonDto();
        reasonDto.setReason("someReason");

        ResponseEntity<String> updateResponse = restTemplate.exchange(URL + "/" + application.getId(), HttpMethod.DELETE, new HttpEntity<>(reasonDto), String.class);

        assertEquals(updateResponse.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldRejectVerifiedApplication() {
        ApplicationDto application = createApplication();
        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.VERIFIED);
        ReasonDto reason = new ReasonDto();
        reason.setReason("reason");

        ResponseEntity<ApplicationDto> rejectResponse =
                restTemplate.exchange(URL + "/" + application.getId() + "/reject", HttpMethod.PATCH, new HttpEntity<>(reason), ApplicationDto.class);

        assertEquals(rejectResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(rejectResponse.getBody().getBody(), application.getBody());
        assertEquals(rejectResponse.getBody().getName(), application.getName());
        assertEquals(rejectResponse.getBody().getStatus(), Status.REJECTED);
        assertEquals(rejectResponse.getBody().getReason(), reason.getReason());
        assertNull(rejectResponse.getBody().getApplicationNumber());
    }

    @Test
    public void shouldAcceptVerifiedApplication() {
        ApplicationDto application = createApplication();
        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.VERIFIED);

        ResponseEntity<ApplicationDto> acceptResponse =
                restTemplate.exchange(URL + "/" + application.getId() + "/accept", HttpMethod.PATCH, null, ApplicationDto.class);

        assertEquals(acceptResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(acceptResponse.getBody().getBody(), application.getBody());
        assertEquals(acceptResponse.getBody().getName(), application.getName());
        assertEquals(acceptResponse.getBody().getStatus(), Status.ACCEPTED);
        assertNull(acceptResponse.getBody().getReason());
        assertNull(acceptResponse.getBody().getApplicationNumber());
    }

    @Test
    public void shouldRejectAcceptedApplication() {
        ApplicationDto application = createApplication();
        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.VERIFIED);
        response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/accept", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.ACCEPTED);
        ReasonDto reason = new ReasonDto();
        reason.setReason("reason");

        ResponseEntity<ApplicationDto> rejectResponse =
                restTemplate.exchange(URL + "/" + application.getId() + "/reject", HttpMethod.PATCH, new HttpEntity<>(reason), ApplicationDto.class);

        assertEquals(rejectResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(rejectResponse.getBody().getBody(), application.getBody());
        assertEquals(rejectResponse.getBody().getName(), application.getName());
        assertEquals(rejectResponse.getBody().getStatus(), Status.REJECTED);
        assertEquals(rejectResponse.getBody().getReason(), reason.getReason());
        assertNull(rejectResponse.getBody().getApplicationNumber());
    }

    @Test
    public void shouldPublishAcceptedApplication() {
        ApplicationDto application = createApplication();
        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.VERIFIED);
        response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/accept", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.ACCEPTED);

        ResponseEntity<ApplicationDto> rejectResponse =
                restTemplate.exchange(URL + "/" + application.getId() + "/publish", HttpMethod.PATCH, null, ApplicationDto.class);

        assertEquals(rejectResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(rejectResponse.getBody().getBody(), application.getBody());
        assertEquals(rejectResponse.getBody().getName(), application.getName());
        assertEquals(rejectResponse.getBody().getStatus(), Status.PUBLISHED);
        assertNull(rejectResponse.getBody().getReason());
        assertNotNull(rejectResponse.getBody().getApplicationNumber());
    }

    @Test
    public void shouldGetPagedApplicationsFilteredByName() {
        for (int i = 0; i < 50; i++) {
            createApplication();
        }
        for (int i = 0; i < 50; i++) {
            createApplication("anotherName");
        }
        ResponseEntity<String> response = restTemplate.exchange(URL + "?name=anoth", HttpMethod.GET, null, String.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(response.getBody().contains("\"totalPages\":5"));
        assertTrue(response.getBody().contains("\"pageSize\":10"));
    }

    @Test
    public void shouldGetAuditLogPublishedApplication() throws JsonProcessingException {
        ApplicationDto application = createApplication();
        ApplicationDto response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/verify", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.VERIFIED);
        response =
                restTemplate.patchForObject(URL + "/" + application.getId() + "/accept", null, ApplicationDto.class);
        assertEquals(response.getStatus(), Status.ACCEPTED);

        ResponseEntity<ApplicationDto> rejectResponse =
                restTemplate.exchange(URL + "/" + application.getId() + "/publish", HttpMethod.PATCH, null, ApplicationDto.class);

        assertEquals(rejectResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(rejectResponse.getBody().getBody(), application.getBody());
        assertEquals(rejectResponse.getBody().getName(), application.getName());
        assertEquals(rejectResponse.getBody().getStatus(), Status.PUBLISHED);
        assertNull(rejectResponse.getBody().getReason());
        assertNotNull(rejectResponse.getBody().getApplicationNumber());

        ResponseEntity<String> auditResponse =
                restTemplate.getForEntity(URL + "/" + application.getId() + "/audit", String.class);
        List<ApplicationAudDto> list = objectMapper.readValue(auditResponse.getBody(), new TypeReference<>() {
        });

        assertEquals(auditResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(list.size(), 4);
        assertEquals(list.get(0).getStatus(), Status.CREATED);
        assertEquals(list.get(1).getStatus(), Status.VERIFIED);
        assertEquals(list.get(2).getStatus(), Status.ACCEPTED);
        assertEquals(list.get(3).getStatus(), Status.PUBLISHED);
    }

    private ApplicationDto createApplication() {
        return createApplication("new app");
    }

    private ApplicationDto createApplication(String name) {
        ApplicationDto request = new ApplicationDto();
        request.setName(name);
        request.setBody("Something");

        ResponseEntity<ApplicationDto> response = restTemplate.postForEntity(URL, request, ApplicationDto.class);

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        return response.getBody();
    }
}
