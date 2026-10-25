package com.brother.crm.repository;
import com.brother.crm.entity.Student;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
  List<Student> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

  List<Student> findAllByOrderByUpdatedAtDesc();
}
