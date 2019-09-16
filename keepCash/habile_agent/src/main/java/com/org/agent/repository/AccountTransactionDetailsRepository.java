package com.org.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.org.agent.model.AccountTransactionDetails;

@Repository
public interface AccountTransactionDetailsRepository extends JpaRepository<AccountTransactionDetails, Long>, JpaSpecificationExecutor<Long>{

}
