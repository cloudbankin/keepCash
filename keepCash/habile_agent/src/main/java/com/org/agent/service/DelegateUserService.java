package com.org.agent.service;

import org.springframework.stereotype.Service;

import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.data.AgentUserData;
import com.org.agent.model.AppUser;

@Service
public interface DelegateUserService {

	AgentUserData createDelegate(JsonCommand command, Long userId, Long parentUserId, Integer transactionPIN);
	
}
