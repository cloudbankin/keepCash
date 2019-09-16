package com.org.agent.service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.data.AgentUserData;
import com.org.agent.model.AppUser;
import com.org.agent.model.AgentUserEntity;

@Service
public interface AgentUserService {
	
	CommandProcessingResult createAgent(JsonCommand command, Long userId, Long parentUserId, Integer pinNumber);
	 
	AgentUserData retrieveAgent(Long userId);
	
	Collection<AgentUserData> retrieveAllAgents();
	
	Collection<AgentUserData> getByParentUserId(Long userId);
	
	CommandProcessingResult updateAgent(Long userId, JsonCommand command);
	
	AgentUserEntity updateAgentUserEntity(Map<String, Object> changes,AgentUserEntity oldData,AppUser appUser,JsonCommand command);

	
	CommandProcessingResult updateClientInAgentDetails(JsonCommand command, Long agentId);

	CommandProcessingResult updateCompanyDetailsInAgentDetails(JsonCommand command, Long agentId);

	CommandProcessingResult createAgentSavingsAccount(JsonCommand command, Long agentId, Integer status);
}
