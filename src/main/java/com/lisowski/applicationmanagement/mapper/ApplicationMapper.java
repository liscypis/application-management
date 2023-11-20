package com.lisowski.applicationmanagement.mapper;

import com.lisowski.applicationmanagement.mapper.dto.ApplicationDto;
import com.lisowski.applicationmanagement.model.Application;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    ApplicationDto entityToDTO(Application application);

    Application dtoToEntity(ApplicationDto applicationDto);
}
