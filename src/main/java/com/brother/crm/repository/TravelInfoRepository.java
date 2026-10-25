package com.brother.crm.repository;

import com.brother.crm.entity.TravelInfo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelInfoRepository extends JpaRepository<TravelInfo, Long> {
  Optional<TravelInfo> findByStudentId(Long studentId);

  List<TravelInfo> findByStudentIdIn(Collection<Long> studentIds);

  List<TravelInfo> findByNeedPickupTrueOrderByArriveDateAscArriveTimeAsc();
}
