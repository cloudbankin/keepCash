package com.org.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.org.customer.model.CustomerUserEntity;

@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUserEntity, Long>, JpaSpecificationExecutor<Long> {

	@Query("select customer from CustomerUserEntity customer where customer.appUser.id = :customerId")
	CustomerUserEntity getCustomerDetailsById(@Param("customerId") Long customerId);
}
