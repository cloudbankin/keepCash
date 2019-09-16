package com.org.agent.service;

import org.springframework.stereotype.Service;

import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.api.JsonCommand;

@Service
public interface AccountTransactionDetailsService {
	
	CommandProcessingResult createAccountTransaction(JsonCommand command);
}
