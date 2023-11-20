package com.lisowski.applicationmanagement.mapper.dto;

import com.lisowski.applicationmanagement.model.enums.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ApplicationDto {
    private Long id;
    @NotBlank(message = "Your application needs a name")
    private String name;
    @NotBlank(message = "Your application needs a body")
    private String body;
    private Status status;
    private Long applicationNumber;
    private String reason;
}
