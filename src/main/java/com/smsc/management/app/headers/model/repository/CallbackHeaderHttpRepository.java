package com.smsc.management.app.headers.model.repository;

import java.util.List;

import com.smsc.management.app.headers.model.entity.CallbackHeaderHttp;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.transaction.annotation.Transactional;

public interface CallbackHeaderHttpRepository extends JpaRepository<CallbackHeaderHttp, Integer> {
	List<CallbackHeaderHttp> findByNetworkId(int id);
    List<CallbackHeaderHttp> findByInterpreterId(int id);
	@Transactional
	void deleteAllByNetworkId(int id);
    @Transactional
    void deleteAllByInterpreterId(int id);
}
