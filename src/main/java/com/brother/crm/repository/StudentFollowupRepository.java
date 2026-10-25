package com.brother.crm.repository;
import com.brother.crm.entity.StudentFollowup;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentFollowupRepository extends JpaRepository<StudentFollowup, Long> {
  List<StudentFollowup> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
