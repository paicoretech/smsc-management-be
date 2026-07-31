package com.smsc.management.app.broadcast.model.repository;

import com.smsc.management.app.broadcast.model.entity.BroadcastFile;
import org.springframework.data.repository.CrudRepository;

public interface BroadcastFileRepository extends CrudRepository<BroadcastFile, Integer> {
    BroadcastFile findById(int id);
}
