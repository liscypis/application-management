package com.lisowski.applicationmanagement.repository;

import com.lisowski.applicationmanagement.model.Application;
import com.lisowski.applicationmanagement.model.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, RevisionRepository<Application, Long, Integer> {
    Optional<Application> findByApplicationNumber(Long number);

    @Query("SELECT a FROM Application a WHERE (:name is null or a.name LIKE %:name%)" +
            "and (:status is null or a.status = :status)")
    Page<Application> findByNameOrStatus(@Param("name") String name, @Param("status") Status status, Pageable pageable);
}
