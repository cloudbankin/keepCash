package org.apache.fineract.portfolio.springBoot.repository;

import org.apache.fineract.portfolio.springBoot.domain.EventRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventRequestRepository extends JpaRepository<EventRequest, Long>, JpaSpecificationExecutor<EventRequest>{

}
