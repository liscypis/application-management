package com.lisowski.applicationmanagement.mapper.dto;

import com.lisowski.applicationmanagement.model.enums.Status;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;

@Builder
@Data
public class ApplicationAudDto {
    private Integer revision;
    private Instant revisionDate;
    private RevisionMetadata.RevisionType revisionType;
    private Long id;
    private String name;
    private String body;
    private Status status;
    private Long applicationNumber;
    private String reason;
}
