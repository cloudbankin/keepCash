package com.org.agent.controller;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.org.agent.command.CommandWrapper;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.command.service.CommandWrapperBuilder;
import com.org.agent.web.Example;
import com.org.agent.web.Ssl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

@RestController
@RequestMapping("/agentDetails")
public class AgentDetailsController {
	
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public AgentDetailsController(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}
	
	
	@PostMapping(value = "/agentDetailsCreate", produces = MediaType.APPLICATION_JSON)
	public String createAgentDetails(@RequestBody final String apiRequestBodyAsJson, HttpServletRequest   uriInfo) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		//String url = HttpConnectionRestTemplate.createSpringUrl(uriInfo, "/agentDetails/agentDetailsCreate");
		 String path = uriInfo.getContextPath();
		 String uri = "https://localhost:8443/fineract-provider/api/v1/clients";
		 			   
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
		 
	   //  String agentDetails = command.arrayOfParameterNamed("agentDeatils").get(0).toString();
	     
	     
	   
	     
		return HttpConnectionRestTemplate.connectRestTemplate(uri, apiRequestBodyAsJson, HttpMethod.POST);
		
	}
	

}
