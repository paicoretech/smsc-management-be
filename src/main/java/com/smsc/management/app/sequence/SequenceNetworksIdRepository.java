package com.smsc.management.app.sequence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SequenceNetworksIdRepository extends JpaRepository<SequenceNetworksId, Integer> {
	SequenceNetworksId findById(int id);
    boolean existsById(int id);
}
