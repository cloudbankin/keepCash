package com.org.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.org.agent.model.AgentUserEntity;

@Repository
public interface AgentUserRepository extends JpaRepository<AgentUserEntity, Long>, JpaSpecificationExecutor<Long> {

	@Query("select agent from AgentUserEntity agent where agent.appUser.id = :agentId")
	AgentUserEntity getAgentDetailsById(@Param("agentId") Long agentId);
}
