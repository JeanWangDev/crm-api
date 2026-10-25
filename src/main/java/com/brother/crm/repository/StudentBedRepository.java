package com.brother.crm.repository;

import com.brother.crm.entity.StudentBed;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentBedRepository extends JpaRepository<StudentBed, Long> {
  Optional<StudentBed> findByStudentId(Long studentId);

  Optional<StudentBed> findByBedId(Long bedId);

  List<StudentBed> findByBedIdIn(Collection<Long> bedIds);

  List<StudentBed> findByStudentIdIn(Collection<Long> studentIds);
}
