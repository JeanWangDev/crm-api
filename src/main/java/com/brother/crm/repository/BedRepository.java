package com.brother.crm.repository;
import com.brother.crm.entity.Bed;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedRepository extends JpaRepository<Bed, Long> {
  List<Bed> findByRoomIdOrderByBedNoAsc(Long roomId);

  List<Bed> findAllByOrderByBedNoAsc();
}
