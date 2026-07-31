package com.smsc.management.app.sequence;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SequenceNetworksIdGenerator {
	private final SequenceNetworksIdRepository sequenceGwRepo;

	@Transactional
    public Integer getNextNetworkIdSequenceValue(String networkType) {
		try {
			SequenceNetworksId newSeqGw = new SequenceNetworksId(networkType);
			SequenceNetworksId result = sequenceGwRepo.save(newSeqGw);
            return result.getId();
        } catch (Exception e) {
			log.error("Error to generate new sequence for network id - > {}", e.getMessage());
		}
        
        return null;
    }
}
