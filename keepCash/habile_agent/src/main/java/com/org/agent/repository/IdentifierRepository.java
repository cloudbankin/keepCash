package com.org.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.org.agent.model.Identifier;

@Repository
public interface IdentifierRepository extends JpaRepository<Identifier, Long>, JpaSpecificationExecutor<Long>{

}
