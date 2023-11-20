package com.lisowski.applicationmanagement.mapper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ReasonDto {
    @NotBlank(message = "Missing reason")
    String reason;
}
