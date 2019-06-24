package com.org.customer.service;

import org.springframework.stereotype.Service;

import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.api.JsonCommand;

@Service
public interface CashInCashOutTransactionService {

	CommandProcessingResult cashInCashOutTransaction(JsonCommand command,  Long agentUserId, Long custUserId);
}
