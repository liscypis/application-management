package com.lisowski.applicationmanagement.mapper;

import com.lisowski.applicationmanagement.mapper.dto.ApplicationAudDto;
import com.lisowski.applicationmanagement.model.Application;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;

@Service
public class ApplicationAudMapper {
    public ApplicationAudDto map(Revision<Integer, Application> revision) {
        return ApplicationAudDto.builder()
                .revision(revision.getMetadata().getRequiredRevisionNumber())
                .revisionDate(revision.getMetadata().getRevisionInstant().orElse(null))
                .revisionType(revision.getMetadata().getRevisionType())
                .id(revision.getEntity().getId())
                .name(revision.getEntity().getName())
                .body(revision.getEntity().getBody())
                .status(revision.getEntity().getStatus())
                .applicationNumber(revision.getEntity().getApplicationNumber())
                .reason(revision.getEntity().getReason())
                .build();
    }
}
