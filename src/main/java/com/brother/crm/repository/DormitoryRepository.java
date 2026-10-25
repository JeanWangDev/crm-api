package com.brother.crm.repository;
import com.brother.crm.entity.Dormitory;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DormitoryRepository extends JpaRepository<Dormitory, Long> {
  List<Dormitory> findAllByOrderByBuildingNameAscRoomNoAsc();
}
