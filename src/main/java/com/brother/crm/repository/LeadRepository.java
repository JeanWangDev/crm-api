package com.brother.crm.repository;
import com.brother.crm.entity.Lead;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
  List<Lead> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

  List<Lead> findAllByOrderByUpdatedAtDesc();
}
