package com.brother.crm.repository;
import com.brother.crm.entity.StudentDorm;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentDormRepository extends JpaRepository<StudentDorm, Long> {
  Optional<StudentDorm> findByStudentId(Long studentId);

  Optional<StudentDorm> findByDormitoryId(Long dormitoryId);
}
