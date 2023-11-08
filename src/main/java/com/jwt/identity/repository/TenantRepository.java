package com.jwt.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jwt.identity.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Integer> {

	Tenant findByTenantName(String tenantName);

}
