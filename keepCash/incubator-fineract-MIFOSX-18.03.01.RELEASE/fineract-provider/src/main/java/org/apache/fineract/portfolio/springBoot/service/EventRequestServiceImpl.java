package org.apache.fineract.portfolio.springBoot.service;

import java.util.Date;

import org.apache.fineract.portfolio.springBoot.domain.EventRequest;
import org.apache.fineract.portfolio.springBoot.repository.EventRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventRequestServiceImpl implements EventRequestService{

	private final EventRequestRepository eventRequestRepository;
	
	@Autowired
	public EventRequestServiceImpl(final EventRequestRepository eventRequestRepository) {
		this.eventRequestRepository = eventRequestRepository;
	}
	
	@Override
	public EventRequest saveRequest(String request, Long userId, String action) {
		return eventRequestRepository.save(EventRequest.createRequest(userId, request, new Date(), action));
	}
}
