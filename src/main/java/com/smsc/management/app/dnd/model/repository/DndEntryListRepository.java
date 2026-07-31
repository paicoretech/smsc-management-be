package com.smsc.management.app.dnd.model.repository;

import com.smsc.management.app.dnd.model.entity.DndEntryList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DndEntryListRepository extends JpaRepository<DndEntryList, Integer>  {
    Optional<DndEntryList> findById(Integer id);
    List<DndEntryList> findAllByOrderByIdDesc();
    boolean existsByName(String name);
}
