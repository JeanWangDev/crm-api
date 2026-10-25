package com.brother.crm.repository;
import com.brother.crm.entity.Building;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
  List<Building> findAllByOrderByCampusNameAscNameAsc();

  List<Building> findByPlaceIdOrderByNameAsc(Long placeId);
}
