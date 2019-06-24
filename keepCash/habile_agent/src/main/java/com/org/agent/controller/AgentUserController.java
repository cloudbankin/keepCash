package com.org.agent.controller;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.CommandWrapper;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.command.service.CommandWrapperBuilder;
import com.org.agent.core.data.EnumOptionData;
import com.org.agent.data.AgentDataValidator;
import com.org.agent.data.AgentUserData;
import com.org.agent.model.AppUserTypes;
import com.org.agent.service.AgentUserService;
import com.org.agent.service.implementation.AppUserTypesEnumerations;

@RestController
@RequestMapping("/agentUser")

public class AgentUserController {

	@Autowired
	private final AgentUserService agentUserServiceImpl;
	private final FromJsonHelper fromApiJsonHelper;
    private final AgentDataValidator agentDataValidator;
	public AgentUserController(final AgentUserService agentUserServiceImpl, final FromJsonHelper fromJsonHelper, final AgentDataValidator agentDataValidator) {
		this.agentUserServiceImpl = agentUserServiceImpl;
		this.fromApiJsonHelper = fromJsonHelper;
		this.agentDataValidator = agentDataValidator;
	}

	@PostMapping(path = "/agentCreate", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String create(@RequestParam Long userId, @RequestBody final String apiRequestBodyAsJson) {
		final CommandWrapper wrapper = new CommandWrapperBuilder() //
				.withJson(apiRequestBodyAsJson) //
				.build(); //

		wrapper.getJson();

		final String json = wrapper.getJson();
		JsonCommand command = null;
		final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);

		command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(),
				wrapper.getEntityId(), wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(),
				wrapper.getLoanId(), wrapper.getSavingsId(), wrapper.getTransactionId(), wrapper.getHref(),
				wrapper.getProductId(), wrapper.getCreditBureauId(), wrapper.getOrganisationCreditBureauId());

		String errorMessage = agentDataValidator.validateForCreateAgentUser(apiRequestBodyAsJson);
	 	if(errorMessage != null) {
	 		return errorMessage;
	 	}
		
		final CommandProcessingResult result = agentUserServiceImpl.createAgent(command, userId, null);

		return this.fromApiJsonHelper.toJson(result);
	}

	@GetMapping(path = "/agentret", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String retrieve(@RequestParam Long userId) {
		final AgentUserData AgentData = agentUserServiceImpl.retrieveAgent(userId);
		return this.fromApiJsonHelper.toJson(AgentData);
	}
	
	@PostMapping(path = "/updateClientInAgent", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public CommandProcessingResult updateClientInAgentDetails(@RequestParam Long userId, @RequestBody final String apiRequestBodyAsJson) {
		final CommandWrapper wrapper = new CommandWrapperBuilder() //
				.withJson(apiRequestBodyAsJson) //
				.build(); //

		wrapper.getJson();

		final String json = wrapper.getJson();
		JsonCommand command = null;
		final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);

		command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(),
				wrapper.getEntityId(), wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(),
				wrapper.getLoanId(), wrapper.getSavingsId(), wrapper.getTransactionId(), wrapper.getHref(),
				wrapper.getProductId(), wrapper.getCreditBureauId(), wrapper.getOrganisationCreditBureauId());

		final CommandProcessingResult result = agentUserServiceImpl.updateClientInAgentDetails(command, userId);

		return result;
	}
	
	@PostMapping(path = "/updateCompanyInAgent", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public CommandProcessingResult updateCompanyDetailsInAgentDetails(@RequestParam Long userId, @RequestBody final String apiRequestBodyAsJson) {
		final CommandWrapper wrapper = new CommandWrapperBuilder() //
				.withJson(apiRequestBodyAsJson) //
				.build(); //

		wrapper.getJson();

		final String json = wrapper.getJson();
		JsonCommand command = null;
		final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);

		command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(),
				wrapper.getEntityId(), wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(),
				wrapper.getLoanId(), wrapper.getSavingsId(), wrapper.getTransactionId(), wrapper.getHref(),
				wrapper.getProductId(), wrapper.getCreditBureauId(), wrapper.getOrganisationCreditBureauId());

		final CommandProcessingResult result = agentUserServiceImpl.updateCompanyDetailsInAgentDetails(command, userId);

		return result;
	}

	@GetMapping(path = "/appUserTypes", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String retrieveAppUserTypes() {
		
		final List<EnumOptionData> appUserTypeOptions = AppUserTypesEnumerations.appUserType(AppUserTypes.values());
		
		JsonArray array = new JsonArray();
		JsonParser jsonParser = new JsonParser();
		for(EnumOptionData appUserTypeOption : appUserTypeOptions) {
			JsonObject appUserTypeJson = new JsonObject();
			appUserTypeJson.add("id", jsonParser.parse(appUserTypeOption.getId().toString()));
			appUserTypeJson.add("value", jsonParser.parse(appUserTypeOption.getValue()));
			appUserTypeJson.add("code", jsonParser.parse(appUserTypeOption.getCode()));
			array.add(jsonParser.parse(appUserTypeJson.toString()));
		}
		return array.toString();
	}

	@GetMapping(path = "/parentUser", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String retrieveDelegates(@RequestParam Long userId) {
		final Collection<AgentUserData> AgentDatas = agentUserServiceImpl.getByParentUserId(userId);
		return this.fromApiJsonHelper.toJson(AgentDatas);
	}
	
	@PutMapping(path = "/agentUpdate", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String updateCustomer(@RequestParam Long userId, @RequestBody final String apiRequestBodyAsJson) {
		final CommandWrapper wrapper = new CommandWrapperBuilder() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
	
		 wrapper.getJson();
		 final String json = wrapper.getJson();
	     JsonCommand command = null;	        
	     final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
	    
	     command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(), wrapper.getEntityId(),
	            wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
	            wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(),wrapper.getCreditBureauId(),wrapper.getOrganisationCreditBureauId());
	     
	    String errorMessage = agentDataValidator.validateForUpdateAgentUser(apiRequestBodyAsJson);
	 	if(errorMessage != null) {
	 		return errorMessage;
	 	}
     
		final CommandProcessingResult agentData = agentUserServiceImpl.updateAgent(userId,command);
		
		return this.fromApiJsonHelper.toJson(agentData);
	}
}
