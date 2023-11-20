package com.lisowski.applicationmanagement.service;

import com.lisowski.applicationmanagement.model.Application;
import com.lisowski.applicationmanagement.model.enums.Status;
import com.lisowski.applicationmanagement.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revisions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public Application getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application with id: " + id + " not found"));
    }

    public Page<Application> getApplications(Pageable pageable, String name, Status status) {
        return applicationRepository.findByNameOrStatus(name, status, pageable);
    }


    public Application saveApplication(Application application, Long applicationId) {
        if (applicationId != null) {
            application.setId(applicationId);
        }
        if (application.getStatus() != Status.PUBLISHED) {
            application.setApplicationNumber(null);
        }
        if (application.getStatus() != Status.DELETED && application.getStatus() != Status.REJECTED) {
            application.setReason(null);
        }
        return applicationRepository.save(application);
    }

    public Application updateApplication(Application application, Long applicationId) {
        Application foundApplication = getApplication(applicationId);

        if (foundApplication.getStatus().equals(Status.CREATED) || foundApplication.getStatus().equals(Status.VERIFIED)) {
            application.setStatus(foundApplication.getStatus());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot edit this application");
        }
        return saveApplication(application, applicationId);
    }

    public Application updateApplicationStatus(Status status, Long applicationId) {
        return updateApplicationStatus(status, applicationId, null);
    }

    public Application updateApplicationStatus(Status status, Long applicationId, String reason) {
        Application application = getApplication(applicationId);
        switch (application.getStatus()) {
            case CREATED -> {
                if (status == Status.VERIFIED) {
                    application.setStatus(status);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only verify or delete this application");
                }
            }
            case VERIFIED -> {
                if (status == Status.REJECTED || status == Status.ACCEPTED) {
                    application.setStatus(status);
                    if (status == Status.REJECTED) {
                        application.setReason(reason);
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only accept or reject this application");
                }
            }
            case ACCEPTED -> {
                if (status == Status.PUBLISHED || status == Status.REJECTED) {
                    application.setStatus(status);
                    if (status == Status.PUBLISHED) {
                        generateUniteApplicationNumber(application);
                    }
                    if (status == Status.REJECTED) {
                        application.setReason(reason);
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only publish or reject this application");
                }
            }
            case REJECTED -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not edit rejected application");
            }
        }
        return saveApplication(application, applicationId);
    }

    public void deleteApplication(Long applicationId, String reason) {
        Application application = getApplication(applicationId);

        if (application.getStatus().equals(Status.CREATED)) {
            application.setStatus(Status.DELETED);
            application.setReason(reason);
            saveApplication(application, applicationId);
            applicationRepository.delete(application);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete this application");
        }
    }

    public Revisions<Integer, Application> getAudit(Long id) {
        return applicationRepository.findRevisions(id);
    }

    private void generateUniteApplicationNumber(Application application) {
        while (true) {
            Long uniqueApplicationNumber = new Random().nextLong();
            if (applicationRepository.findByApplicationNumber(uniqueApplicationNumber).isEmpty()) {
                application.setApplicationNumber(uniqueApplicationNumber);
                return;
            }
        }
    }
}
