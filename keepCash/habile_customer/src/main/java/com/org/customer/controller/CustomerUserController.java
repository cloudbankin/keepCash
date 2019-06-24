package com.org.customer.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.CommandWrapper;
import com.org.customer.command.FromJsonHelper;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.command.service.CommandWrapperBuilder;
import com.org.customer.core.data.EnumOptionData;
import com.org.customer.data.CustomerDataValidator;
import com.org.customer.data.CustomerUserData;
import com.org.customer.exception.ContentManagementException;
import com.org.customer.exception.ForcePasswordResetException;
import com.org.customer.model.AppUserTypes;
import com.org.customer.service.CustomerUserService;
import com.org.customer.service.implementation.AppUserTypesEnumerations;

@RestController
@RequestMapping("/customerUser")

public class CustomerUserController {

	@Autowired
	private final CustomerUserService customerUserServiceImpl;
	private final FromJsonHelper fromApiJsonHelper;
	private final CustomerDataValidator customerDataValidator;

	public CustomerUserController(final CustomerUserService customerUserServiceImpl, final FromJsonHelper fromJsonHelper, final CustomerDataValidator customerDataValidator) {
		this.customerUserServiceImpl = customerUserServiceImpl;
		this.fromApiJsonHelper = fromJsonHelper;
		this.customerDataValidator = customerDataValidator;
	}
	mv info.txt config/information.txt

	@PostMapping(path = "/customerCreate/{parent_user_id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String create(@RequestParam Long userId,@PathVariable("parent_user_id") Long agentId, @RequestBody final String apiRequestBodyAsJson) {
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

		String errorMessage = customerDataValidator.validateForCreateCustomerUser(apiRequestBodyAsJson);
    	if(errorMessage != null) {
    		return errorMessage;
    	}
    	
		final CommandProcessingResult result = customerUserServiceImpl.createCustomer(command, userId,agentId);
		return this.fromApiJsonHelper.toJson(result);
	}

	@GetMapping(path = "/customerretrieve", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String retrieve(@RequestParam Long custUserId) {
		final CustomerUserData CustomerData = customerUserServiceImpl.retrieveCustomer(custUserId);
		return this.fromApiJsonHelper.toJson(CustomerData);
	}
	

	@PostMapping(path = "/updateClientInCustomer", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public CommandProcessingResult updateClientInCustomerDetails(@RequestParam Long userId, @RequestBody final String apiRequestBodyAsJson) {
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

		final CommandProcessingResult result = customerUserServiceImpl.updateClientInCustomerDetails(command, userId);

		return result;
	}

	@PostMapping(path = "/updateCompanyInCustomer", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public CommandProcessingResult updateCompanyDetailsInCustomerDetails(@RequestParam Long userId, @RequestBody final String apiRequestBodyAsJson) {
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

		final CommandProcessingResult result = customerUserServiceImpl.updateCompanyDetailsInCustomerDetails(command, userId);

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

	@GetMapping(path = "/customersUnderAgentRetrieve", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String retrieveAllCustomersUnderAgent(@RequestParam Long agentId) {
		final Collection<CustomerUserData> agentsCustomerData = customerUserServiceImpl.retrieveAllCustomersUnderAgent(agentId);
		return this.fromApiJsonHelper.toJson(agentsCustomerData);
	}
	
	@PutMapping(path = "/customerUpdate", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
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
     
 
    	//this.customerDataValidator.validateForUpdateCustomerUser(apiRequestBodyAsJson);
    	String errorMessage = customerDataValidator.validateForUpdateCustomerUser(apiRequestBodyAsJson);
    	if(errorMessage != null) {
    		return errorMessage;
    	}
	
		final CommandProcessingResult CustomerData = customerUserServiceImpl.updateCustomer(userId,command);
		
		return this.fromApiJsonHelper.toJson(CustomerData);
	}

	
}
