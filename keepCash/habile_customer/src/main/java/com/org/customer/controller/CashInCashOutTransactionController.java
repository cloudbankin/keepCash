package com.org.customer.controller;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.CommandWrapper;
import com.org.customer.command.FromJsonHelper;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.command.service.CommandWrapperBuilder;
import com.org.customer.service.CashInCashOutTransactionService;

@RestController
@RequestMapping("/createCashInCashOut")
public class CashInCashOutTransactionController {

	private final FromJsonHelper fromJsonHelper;
	private final CashInCashOutTransactionService cashInCashOutTransactionService ;
	
	@Autowired
	public CashInCashOutTransactionController(final FromJsonHelper fromJsonHelper,
			final CashInCashOutTransactionService cashInCashOutTransactionService) {
		this.fromJsonHelper = fromJsonHelper;
		this.cashInCashOutTransactionService = cashInCashOutTransactionService;
	}

	@PostMapping(value = "/createCashInCashOutTransaction",consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
	public String agentSavingsAccountDetails(@RequestParam final Long agentUserId, @RequestParam final Long custUserId, @RequestBody final String apiRequestBodyAsJson) {
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
	     
	     final CommandProcessingResult result = cashInCashOutTransactionService.cashInCashOutTransaction(command,  agentUserId, custUserId);
	     return result.toString();
	}
}
