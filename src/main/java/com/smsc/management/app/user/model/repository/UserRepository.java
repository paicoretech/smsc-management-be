package com.smsc.management.app.user.model.repository;

import com.smsc.management.app.user.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUserName(String userName);

    @Query("SELECT COUNT(u) > 0 FROM Users u JOIN u.roles r WHERE r = :role")
    boolean existsByRolesContaining(@Param("role") String role);

    Optional<Users> findByUserNameAndStatus(String username, short status);

    @Query("SELECT DISTINCT u FROM Users u JOIN u.roles r")
    List<Users> findUsersAll();

    List<Users> findByIdIn(List<Integer> ids);
    boolean existsByUserNameAndLoginAndJwt(String userName, boolean login, String jwt);
}
