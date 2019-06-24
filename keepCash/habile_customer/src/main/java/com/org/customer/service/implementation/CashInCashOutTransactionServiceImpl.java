package com.org.customer.service.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.CommandProcessingResultBuilder;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.model.CashInCashOutTransaction;
import com.org.customer.repository.CashInCashOutTransactionRepository;
import com.org.customer.service.CashInCashOutTransactionService;

@Component
public class CashInCashOutTransactionServiceImpl implements CashInCashOutTransactionService{

	@Autowired
	private final AppUserServiceImpl appUserServiceImpl; 
	private final CashInCashOutTransactionRepository cashInCashOutTransactionRepository; 
	
	@Autowired
	public CashInCashOutTransactionServiceImpl(final AppUserServiceImpl appUserServiceImpl,
			final CashInCashOutTransactionRepository cashInCashOutTransactionRepository) {
		this.appUserServiceImpl = appUserServiceImpl;
		this.cashInCashOutTransactionRepository = cashInCashOutTransactionRepository;
	}
	
	@Override
	public CommandProcessingResult cashInCashOutTransaction(JsonCommand command, Long agentUserId, Long custUserId) {
		CashInCashOutTransaction cashInCashOutTransaction = CashInCashOutTransaction.createNew(appUserServiceImpl.findByIdAppUser(agentUserId), appUserServiceImpl.findByIdAppUser(custUserId), command);
		cashInCashOutTransactionRepository.save(cashInCashOutTransaction);
		return new CommandProcessingResultBuilder()
				.withEntityId(cashInCashOutTransaction.getId())
				.build();
	}
}
