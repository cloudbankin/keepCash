package com.org.agent.controller;


import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.CommandWrapper;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.command.service.CommandWrapperBuilder;
import com.org.agent.model.AgentTopupData;
import com.org.agent.service.AgentTopupReadService;
import com.org.agent.service.AgentUserService;

@RestController
@RequestMapping("/agentSavingsAccount")
public class AgentDepositDetailsController {
	
	private final FromJsonHelper fromJsonHelper;
	private final AgentUserService agentUserServiceImpl;
	private final AgentTopupReadService agentTopupReadService; 
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public AgentDepositDetailsController(final FromJsonHelper fromJsonHelper,
			final AgentUserService agentUserServiceImpl, final AgentTopupReadService agentTopupReadService, final FromJsonHelper fromApiJsonHelper) {
		this.fromJsonHelper = fromJsonHelper;
		this.agentUserServiceImpl = agentUserServiceImpl;
		this.agentTopupReadService = agentTopupReadService;
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	@PostMapping(value = "/createAgentSavingsAccount",consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
	public String agentSavingsAccountDetails(@RequestParam final Long userId, @RequestBody final String apiRequestBodyAsJson) {
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
	     
	     final CommandProcessingResult result = agentUserServiceImpl.createAgentSavingsAccount(command, userId, 300);
	     return result.toString();
	}
	
	@GetMapping(value = "/retrieveAgentTopup", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
	public String getAgentTopupAmount(@RequestParam("agentUserId") final Long agentUserId) {
		AgentTopupData agentTopupData = agentTopupReadService.getAgentTopupTotalAmount(agentUserId);
		return this.fromApiJsonHelper.toJson(agentTopupData);
	}
}
