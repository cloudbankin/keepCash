package com.org.agent.controller;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.org.agent.command.CommandWrapper;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.command.service.CommandWrapperBuilder;
import com.org.agent.repository.SavingsAccountDetailsRepository;
import com.org.agent.service.SavingsAccountDetailsService;

@RestController
@RequestMapping("/savingsAccountDetails")
public class SavingsAccountDetailsController {

	@Autowired
	private final SavingsAccountDetailsService savingsAccountDetailsService;
	private final FromJsonHelper fromJsonHelper;
	private final SavingsAccountDetailsRepository savingsAccountDetailsRepository;
	
	
	@Autowired
	public SavingsAccountDetailsController(final SavingsAccountDetailsService savingsAccountDetailsService,final FromJsonHelper fromJsonHelper,
			final SavingsAccountDetailsRepository savingsAccountDetailsRepository) {
		this.savingsAccountDetailsService = savingsAccountDetailsService;
		this.fromJsonHelper = fromJsonHelper;
		this.savingsAccountDetailsRepository = savingsAccountDetailsRepository;
	}
	
	@PostMapping(value = "/createSavingsAccount", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
	public String createSavingsAccount(@RequestBody final String apiRequestBodyAsJson) {
		final CommandWrapper wrapper = new CommandWrapperBuilder() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
	
	 wrapper.getJson();
	 
	 final String json = wrapper.getJson();
     JsonCommand command = null;	        
     final JsonElement parsedCommand = this.fromJsonHelper.parse(json);
 
     command = JsonCommand.from(json, parsedCommand, this.fromJsonHelper, wrapper.getEntityName(), wrapper.getEntityId(),
         wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
         wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(),wrapper.getCreditBureauId(),wrapper.getOrganisationCreditBureauId());
     
		return savingsAccountDetailsService.createSavingsAccountDetails(command).toString();
		
	}
	
	@GetMapping(path = "/retrieveAllAccount/{savingsId}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String retrieveAllAccount(@PathVariable("savingsId") final Long savingsId) {
		return this.fromJsonHelper.toJson(savingsAccountDetailsRepository.findSavingAccountBySavingsId(savingsId));
	}
	
	@GetMapping(path = "/retrieveAccountByClient/{clientId}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String retrieveAllAccountByClient(@PathVariable("clientId") final Long clientId) {
		
		return this.fromJsonHelper.toJson(savingsAccountDetailsService.getSavingsAccountDetails(clientId));
		//return this.fromJsonHelper.toJson(savingsAccountDetailsRepository.findSavingAccountByClientId(clientId));
	}
}
