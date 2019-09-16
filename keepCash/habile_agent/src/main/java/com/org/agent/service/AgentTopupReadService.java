package com.org.agent.service;

import org.springframework.stereotype.Service;

import com.org.agent.model.AgentTopupData;

@Service
public interface AgentTopupReadService {

	AgentTopupData getAgentTopupTotalAmount(Long agentUserId);
}
