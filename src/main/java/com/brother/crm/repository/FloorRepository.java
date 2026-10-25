package com.brother.crm.repository;
import com.brother.crm.entity.Floor;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorRepository extends JpaRepository<Floor, Long> {
  List<Floor> findByBuildingIdOrderByFloorNoAsc(Long buildingId);

  List<Floor> findAllByOrderByFloorNoAsc();
}
