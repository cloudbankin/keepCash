package com.org.customer.controller;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.org.customer.command.CommandWrapper;
import com.org.customer.command.FromJsonHelper;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.command.service.CommandWrapperBuilder;
import com.org.customer.service.RatingService;


@RestController
@RequestMapping("rating")
public class RatingController {

	@Autowired  
	private final FromJsonHelper fromJsonHelper;
	private final RatingService ratingService;
	
	@Autowired
	public RatingController(final FromJsonHelper fromJsonHelper, final RatingService ratingService) {
		this.fromJsonHelper = fromJsonHelper;
		this.ratingService = ratingService;
	}
	
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
	public String createRating(@RequestBody final String apiRequestBodyAsJson) {
		String result = null;
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
	     result = fromJsonHelper.toJson(ratingService.saveCustomerRating(command));
	     
	     return result;
	}
	
}
