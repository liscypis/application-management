package com.lisowski.applicationmanagement.model;

import com.lisowski.applicationmanagement.model.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Audited
public class Application {

    @Id
    @GeneratedValue
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String body;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
    private Long applicationNumber;
    private String reason;

}
