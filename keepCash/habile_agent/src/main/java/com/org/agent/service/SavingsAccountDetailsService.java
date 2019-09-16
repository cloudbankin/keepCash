package com.org.agent.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.model.SavingsAccountDetails;

@Service
public interface SavingsAccountDetailsService {

	CommandProcessingResult createSavingsAccountDetails(JsonCommand command);
	
	Collection<SavingsAccountDetails> getSavingsAccountDetails(Long clientId);
}
