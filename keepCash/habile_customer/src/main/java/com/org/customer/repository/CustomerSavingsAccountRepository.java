package com.org.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.org.customer.model.CustomerSavingsAccount;

@Repository
public interface CustomerSavingsAccountRepository extends JpaRepository<CustomerSavingsAccount, Long>, JpaSpecificationExecutor<Long> {

}
