package com.org.agent.service.implementation;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.CommandProcessingResultBuilder;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.data.AgentUserData;
import com.org.agent.model.SavingsAccountDetails;
import com.org.agent.repository.SavingsAccountDetailsRepository;
import com.org.agent.service.SavingsAccountDetailsService;

@Component
public class SavingsAccountDetailsServiceImpl implements SavingsAccountDetailsService{

	@Autowired
	private final SavingsAccountDetailsRepository savingsAccountDetailsRepository;
	private final JdbcTemplate jdbcTemplate; 
	
	@Autowired
	public SavingsAccountDetailsServiceImpl(final SavingsAccountDetailsRepository savingsAccountDetailsRepository, final JdbcTemplate jdbcTemplate) {
		this.savingsAccountDetailsRepository = savingsAccountDetailsRepository;
		this.jdbcTemplate = jdbcTemplate;
		
	}
	
	@Override
	public CommandProcessingResult createSavingsAccountDetails(JsonCommand command) {
		SavingsAccountDetails savingsAccountDetail = SavingsAccountDetails.newSavingsAccountDetails(command);
		savingsAccountDetailsRepository.save(savingsAccountDetail);
		
		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.build();
	}
	
	@Override
	public  Collection<SavingsAccountDetails> getSavingsAccountDetails(Long clientId) {
		
		Collection<SavingsAccountDetails> savingsAccountDetails = (Collection<SavingsAccountDetails>) savingsAccountDetailsRepository.findSavingAccountByClientId(clientId);
		return savingsAccountDetails;
		
	}
	

}
