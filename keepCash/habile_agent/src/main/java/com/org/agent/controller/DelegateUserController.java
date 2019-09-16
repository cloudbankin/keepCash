package com.org.agent.controller;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.org.agent.command.CommandWrapper;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.command.service.CommandWrapperBuilder;
import com.org.agent.data.DelegateDataValidator;
import com.org.agent.service.DelegateUserService;

@RestController
@RequestMapping("/delegateUser")
public class DelegateUserController {
	
	@Autowired
	private final FromJsonHelper fromApiJsonHelper;
	private final DelegateUserService delegateUserService;
	private final DelegateDataValidator delegateDataValidator;
	@Autowired
	public DelegateUserController(final FromJsonHelper fromApiJsonHelper, final DelegateUserService delegateUserService,final DelegateDataValidator delegateDataValidator) {
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.delegateUserService = delegateUserService;
		this.delegateDataValidator = delegateDataValidator;
	}

	@PostMapping(path = "/delegateCreate", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
	public String create(@RequestParam Long userId, @RequestParam Long parentUserId, @RequestParam Integer transactionPIN, 
			@RequestBody final String apiRequestBodyAsJson) {
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
		String errorMessage = delegateDataValidator.validateForCreateDelegateUser(apiRequestBodyAsJson);
	 	if(errorMessage != null) {
	 		return errorMessage;
	 	}

		return this.fromApiJsonHelper.toJson(delegateUserService.createDelegate(command, userId, parentUserId, transactionPIN));

	}

}
