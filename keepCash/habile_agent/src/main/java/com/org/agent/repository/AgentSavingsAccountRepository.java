package com.org.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.org.agent.model.AgentSavingsAccount;

@Repository
public interface AgentSavingsAccountRepository extends JpaRepository<AgentSavingsAccount, Long>, JpaSpecificationExecutor<Long> {

}
