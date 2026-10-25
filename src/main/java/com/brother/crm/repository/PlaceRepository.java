package com.brother.crm.repository;
import com.brother.crm.entity.Place;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
  List<Place> findAllByOrderByNameAsc();
}
