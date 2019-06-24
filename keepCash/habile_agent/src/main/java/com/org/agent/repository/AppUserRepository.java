package com.org.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.org.agent.model.AppUser;

public interface AppUserRepository extends  CrudRepository<AppUser, Long>{
	
	AppUser findByid(Long id);
	
	/*@Query("SELECT h FROM m_appuser h WHERE h.id=?1 AND h.id=?")
	public AppUser findOne(Long id);*/
	
}
