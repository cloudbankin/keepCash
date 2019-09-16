package org.apache.fineract.portfolio.springBoot.service;

import org.apache.fineract.portfolio.springBoot.domain.EventRequest;

public interface EventRequestService {

	EventRequest saveRequest(String request, Long userId, String action);
}
