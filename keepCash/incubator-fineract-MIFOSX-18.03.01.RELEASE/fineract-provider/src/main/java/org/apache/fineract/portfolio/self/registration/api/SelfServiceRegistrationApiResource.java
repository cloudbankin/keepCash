/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.portfolio.self.registration.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformService;
import org.apache.fineract.portfolio.springBoot.AgentController;
import org.apache.fineract.portfolio.springBoot.CustomerController;
import org.apache.fineract.portfolio.springBoot.data.AgentDataValidator;
import org.apache.fineract.portfolio.springBoot.data.CustomerDataValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Path("/self/registration")
@Component
@Scope("singleton")
public class SelfServiceRegistrationApiResource {

	@Autowired
    private final SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService;
    private final DefaultToApiJsonSerializer<AppUser> toApiJsonSerializer;
    private final AgentController agentController;
    private final CustomerController customerController;
    private final AuthenticationApiResource authenticationApiResource;
    private final FromJsonHelper fromApiJsonHelper;
    private final AppUserRepository appUserRepository;
    private final PlatformSecurityContext context;
    private final AgentDataValidator agentDataValidator;
    private final CustomerDataValidator customerDataValidator;


    @Autowired
    public SelfServiceRegistrationApiResource(
            final SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService,
            final DefaultToApiJsonSerializer<AppUser> toApiJsonSerializer, final AgentController agentController, final CustomerController customerController, final AuthenticationApiResource authenticationApiResource
            ,final FromJsonHelper fromApiJsonHelper, final AppUserRepository appUserRepository, final PlatformSecurityContext context,
            final AgentDataValidator agentDataValidator,final CustomerDataValidator customerDataValidator) {
        this.selfServiceRegistrationWritePlatformService = selfServiceRegistrationWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.agentController = agentController;
        this.customerController = customerController;
        this.authenticationApiResource = authenticationApiResource;
        this.fromApiJsonHelper= fromApiJsonHelper;
        this.appUserRepository = appUserRepository;
        this.context = context;
        this.agentDataValidator = agentDataValidator;
        this.customerDataValidator = customerDataValidator;
    }

    /*@POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSelfServiceRegistrationRequest(final String apiRequestBodyAsJson) {
        this.selfServiceRegistrationWritePlatformService.createRegistrationRequest(apiRequestBodyAsJson);
        return SelfServiceApiConstants.createRequestSuccessMessage;
    }
*/
    @POST
    @Path("/user")
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSelfServiceUser(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
       // AppUser user = this.selfServiceRegistrationWritePlatformService.createUser(apiRequestBodyAsJson);
    	
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
     
     String result = null;
     
    	 if(appUserRepository.findAppUserByName(command.stringValueOfParameterNamed("userName")) != null) {
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("userName"), command.stringValueOfParameterNamed("password"));
    		 
    	 }else {
    		 
    		 this.agentDataValidator.validateForCreateAgentUser(apiRequestBodyAsJson);
    		 
    		 JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
    		 JsonObject jsonObject = new JsonObject();
    		 jsonObject.addProperty("username", this.fromApiJsonHelper.extractStringNamed("userName", element));
    		 jsonObject.addProperty("password", this.fromApiJsonHelper.extractStringNamed("password", element));
    		 jsonObject.addProperty("firstName", this.fromApiJsonHelper.extractStringNamed("firstName", element));
    		 jsonObject.addProperty("lastName", this.fromApiJsonHelper.extractStringNamed("lastName", element));
    		 jsonObject.addProperty("email", this.fromApiJsonHelper.extractStringNamed("emailId", element));
    		 jsonObject.addProperty("authenticationMode", this.fromApiJsonHelper.extractStringNamed("authenticationMode", element));
    		 
    		 AppUser user = this.selfServiceRegistrationWritePlatformService.newUserRegister(jsonObject.toString(), SelfServiceApiConstants.AGENT_USER_ROLE);
    		 agentController.AgentCreate(apiRequestBodyAsJson, uriInfo, user.getId());
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("userName"), command.stringValueOfParameterNamed("password"));
    		 
    	 }
    	
    	 return result; 
       // return this.toApiJsonSerializer.serialize(CommandProcessingResult.resourceResult(user.getId(), null));
    }
    
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSelfServiceDelegateUser(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, @QueryParam("parentUserId") final Long parentUserId) {		 
		 this.agentDataValidator.validateForCreateAgentUser(apiRequestBodyAsJson);
		
		 JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		 JsonObject jsonObject = new JsonObject();
		 jsonObject.addProperty("username", this.fromApiJsonHelper.extractStringNamed("userName", element));
		 jsonObject.addProperty("password", this.fromApiJsonHelper.extractStringNamed("password", element));
		 jsonObject.addProperty("firstName", this.fromApiJsonHelper.extractStringNamed("firstName", element));
		 jsonObject.addProperty("lastName", this.fromApiJsonHelper.extractStringNamed("lastName", element));
		 jsonObject.addProperty("email", this.fromApiJsonHelper.extractStringNamed("emailId", element));
		 jsonObject.addProperty("authenticationMode", this.fromApiJsonHelper.extractStringNamed("authenticationMode", element));
		 
		 AppUser user = this.selfServiceRegistrationWritePlatformService.newUserRegister(jsonObject.toString(), SelfServiceApiConstants.DELEGATE_USER_ROLE);
		 return agentController.delegateCreate(apiRequestBodyAsJson, uriInfo, user.getId(), parentUserId);
    	
    }
    
    @POST
    @Path("customerUser/{parent_user_id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSelfServiceCustomerUser(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,@PathParam("parent_user_id") Long agentId) {
       // AppUser user = this.selfServiceRegistrationWritePlatformService.createUser(apiRequestBodyAsJson);
    	
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
	     
	     String result = null;
     
    	 if(appUserRepository.findAppUserByName(command.stringValueOfParameterNamed("userName")) != null) {
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("userName"), command.stringValueOfParameterNamed("password"));
    	 }else {
    		 
    		 this.customerDataValidator.validateForCreateCustomerUser(apiRequestBodyAsJson);
    		 
    		 JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
    		 JsonObject jsonObject = new JsonObject();
    		 jsonObject.addProperty("username", this.fromApiJsonHelper.extractStringNamed("userName", element));
    		 jsonObject.addProperty("password", this.fromApiJsonHelper.extractStringNamed("password", element));
    		 jsonObject.addProperty("firstName", this.fromApiJsonHelper.extractStringNamed("firstName", element));
    		 jsonObject.addProperty("lastName", this.fromApiJsonHelper.extractStringNamed("lastName", element));
    		 jsonObject.addProperty("email", this.fromApiJsonHelper.extractStringNamed("emailId", element));
    		 jsonObject.addProperty("authenticationMode", this.fromApiJsonHelper.extractStringNamed("authenticationMode", element));
    		 
    		 AppUser user = this.selfServiceRegistrationWritePlatformService.newUserRegister(jsonObject.toString(), SelfServiceApiConstants.CUSTOMER_USER_ROLE);
    		 customerController.CustomerCreate(apiRequestBodyAsJson, uriInfo, user.getId(),agentId);
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("userName"), command.stringValueOfParameterNamed("password"));
    		 
    	 }
    	
    	 return result; 
    }

}
