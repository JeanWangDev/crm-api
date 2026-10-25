package com.brother.crm.repository;
import com.brother.crm.entity.LeadFollowup;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadFollowupRepository extends JpaRepository<LeadFollowup, Long> {
  List<LeadFollowup> findByLeadIdOrderByCreatedAtDesc(Long leadId);
}
