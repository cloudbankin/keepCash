package com.org.agent.controller;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.org.agent.command.CommandWrapper;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.command.service.CommandWrapperBuilder;
import com.org.agent.service.AccountTransactionDetailsService;

@RestController
@RequestMapping("/accountTransaction")
public class AccountTransactionDetailsController {

	@Autowired
	private final AccountTransactionDetailsService accountTransactionDetailsService;
	private final FromJsonHelper fromJsonHelper;
	
	@Autowired
	public AccountTransactionDetailsController(final AccountTransactionDetailsService accountTransactionDetailsService,
			final FromJsonHelper fromJsonHelper) {
		this.accountTransactionDetailsService = accountTransactionDetailsService;
		this.fromJsonHelper = fromJsonHelper;
	}
	
	@PostMapping(value = "/createTransaction", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
	public String createTransactionDetails(@RequestBody final String apiRequestBodyAsJson) {
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
			return this.fromJsonHelper.toJson(accountTransactionDetailsService.createAccountTransaction(command));
	}
}
