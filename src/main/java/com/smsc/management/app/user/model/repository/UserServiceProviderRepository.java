package com.smsc.management.app.user.model.repository;

import com.smsc.management.app.user.model.entity.UserServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserServiceProviderRepository extends JpaRepository<UserServiceProvider, Integer> {
    List<UserServiceProvider> findByUserId(Integer userId);

    void deleteByUserId(Integer userId);

    @Query("SELECT CASE WHEN COUNT(usp) > 0 THEN true ELSE false END " +
            "FROM UserServiceProvider usp " +
            "WHERE usp.user.id = :userId " +
            "AND usp.serviceProvider.networkId = :networkId")
    boolean existsByUserIdAndServiceProviderNetworkId(Integer userId, Integer networkId);
}
