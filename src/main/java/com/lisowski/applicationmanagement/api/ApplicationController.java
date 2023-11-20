package com.lisowski.applicationmanagement.api;


import com.lisowski.applicationmanagement.mapper.ApplicationAudMapper;
import com.lisowski.applicationmanagement.mapper.ApplicationMapper;
import com.lisowski.applicationmanagement.mapper.dto.ApplicationAudDto;
import com.lisowski.applicationmanagement.mapper.dto.ApplicationDto;
import com.lisowski.applicationmanagement.mapper.dto.ReasonDto;
import com.lisowski.applicationmanagement.model.enums.Status;
import com.lisowski.applicationmanagement.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/applications")
@RestController
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final ApplicationAudMapper applicationAudMapper;

    @GetMapping("/{id}")
    public ApplicationDto getApplication(@PathVariable Long id) {
        return applicationMapper.entityToDTO(applicationService.getApplication(id));
    }

    @GetMapping
    public Page<ApplicationDto> getApplications(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10) Pageable pageable) {
        return applicationService.getApplications(pageable, name, status).map(applicationMapper::entityToDTO);
    }

    @PostMapping
    public ApplicationDto createApplication(@Valid @RequestBody ApplicationDto applicationDto) {
        applicationDto.setId(null);
        applicationDto.setStatus(Status.CREATED);

        return applicationMapper.entityToDTO(applicationService
                .saveApplication(applicationMapper.dtoToEntity(applicationDto), null));
    }

    @PutMapping("/{id}")
    public ApplicationDto updateApplication(@PathVariable Long id, @Valid @RequestBody ApplicationDto applicationDto) {
        return applicationMapper.entityToDTO(
                applicationService.updateApplication(
                        applicationMapper.dtoToEntity(applicationDto), id));
    }

    @PatchMapping("/{id}/verify")
    public ApplicationDto verifyApplication(@PathVariable Long id) {
        return applicationMapper
                .entityToDTO(applicationService
                        .updateApplicationStatus(Status.VERIFIED, id));
    }

    @PatchMapping("/{id}/accept")
    public ApplicationDto acceptApplication(@PathVariable Long id) {
        return applicationMapper
                .entityToDTO(applicationService
                        .updateApplicationStatus(Status.ACCEPTED, id));
    }

    @PatchMapping("/{id}/publish")
    public ApplicationDto publishApplication(@PathVariable Long id) {
        return applicationMapper
                .entityToDTO(applicationService
                        .updateApplicationStatus(Status.PUBLISHED, id));
    }

    @PatchMapping("/{id}/reject")
    public ApplicationDto rejectApplication(@PathVariable Long id, @Valid @RequestBody ReasonDto reasonDto) {
        return applicationMapper
                .entityToDTO(applicationService
                        .updateApplicationStatus(Status.REJECTED, id, reasonDto.getReason()));
    }

    @DeleteMapping("/{id}")
    public void deleteApplication(@PathVariable Long id, @Valid @RequestBody ReasonDto reasonDto) {
        applicationService.deleteApplication(id, reasonDto.getReason());
    }

    @GetMapping("/{id}/audit")
    public List<ApplicationAudDto> getAudit(@PathVariable Long id) {
        return applicationService.getAudit(id)
                .getContent()
                .stream()
                .map(applicationAudMapper::map)
                .collect(Collectors.toList());
    }
}


