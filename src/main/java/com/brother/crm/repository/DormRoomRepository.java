package com.brother.crm.repository;
import com.brother.crm.entity.DormRoom;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DormRoomRepository extends JpaRepository<DormRoom, Long> {
  List<DormRoom> findByFloorIdOrderByRoomNoAsc(Long floorId);

  List<DormRoom> findAllByOrderByRoomNoAsc();
}
