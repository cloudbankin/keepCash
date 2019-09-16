package com.org.agent.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.org.agent.model.SavingsAccountDetails;

@Repository
public interface SavingsAccountDetailsRepository extends JpaRepository<SavingsAccountDetails, Long>, JpaSpecificationExecutor<Long>{

	  @Query("select s from SavingsAccountDetails s  where s.savingsId = :savings_id")
	  SavingsAccountDetails findSavingAccountBySavingsId(@Param("savings_id") Long savings_id);
	  
	  @Query("select s from SavingsAccountDetails s  where s.clientId = :client_id")
	  Collection<SavingsAccountDetails> findSavingAccountByClientId(@Param("client_id") Long client_id);
	  
	  
	  
}
