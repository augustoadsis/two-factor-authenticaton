package com.augusto.twofactorauthenticaton.user.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Transactional(readOnly = true)
    @Query("SELECT r FROM Role r WHERE r.name LIKE :_param%")
    Page<Role> findAll(@Param("_param") String param, Pageable pageable);

    @Transactional(readOnly = true)
    @Query("SELECT r FROM Role r WHERE r.name LIKE :_param% AND r.name IN ('secretaries', 'secretaries-manager')")
    Page<Role> findAllSecretaries(@Param("_param") String param, Pageable pageable);

    @Query("SELECT r FROM Role  r WHERE r.name = 'members'")
    Optional<Role> findByNameIsMember();

    @Query("SELECT r FROM Role  r WHERE r.name = 'doctors'")
    Optional<Role> findByNameIsDoctor();

    Optional<Role> findByName(String role);
}
