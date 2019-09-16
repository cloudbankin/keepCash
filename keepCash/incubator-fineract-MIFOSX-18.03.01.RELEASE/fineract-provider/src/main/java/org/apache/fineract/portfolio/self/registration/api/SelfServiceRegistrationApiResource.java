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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.domain.Notification;
import org.apache.fineract.notification.service.NotificationGeneratorWritePlatformService;
import org.apache.fineract.notification.service.NotificationWritePlatformService;
import org.apache.fineract.notification.service.NotificationWritePlatformServiceImpl;
import org.apache.fineract.organisation.staff.exception.MobileNumberAlreadyExists;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformService;
import org.apache.fineract.portfolio.springBoot.AgentController;
import org.apache.fineract.portfolio.springBoot.CustomerController;
import org.apache.fineract.portfolio.springBoot.HttpConnectionTemplate;
import org.apache.fineract.portfolio.springBoot.NotificationMessage;
import org.apache.fineract.portfolio.springBoot.data.AgentDataValidator;
import org.apache.fineract.portfolio.springBoot.data.CustomerDataValidator;
import org.apache.fineract.portfolio.springBoot.domain.EventRequest;
import org.apache.fineract.portfolio.springBoot.service.EventRequestService;
import org.apache.fineract.portfolio.springBoot.service.PushNotification;
import org.apache.fineract.portfolio.springBoot.service.SendRandomOtpMessage;
import org.apache.fineract.portfolio.springBoot.service.SmsSender;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.apache.fineract.useradministration.service.AppUserReadPlatformServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
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
    private final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl;
    private final NotificationWritePlatformService notificationWritePlatformService;
    private final EventRequestService eventRequestService;


    @Autowired
    public SelfServiceRegistrationApiResource(
            final SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService,
            final DefaultToApiJsonSerializer<AppUser> toApiJsonSerializer, final AgentController agentController, final CustomerController customerController, final AuthenticationApiResource authenticationApiResource
            ,final FromJsonHelper fromApiJsonHelper, final AppUserRepository appUserRepository, final PlatformSecurityContext context,
            final AgentDataValidator agentDataValidator,final CustomerDataValidator customerDataValidator,
            final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl, 
            final NotificationWritePlatformService notificationWritePlatformService, 
            final EventRequestService eventRequestService) {
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
        this.appUserReadPlatformServiceImpl = appUserReadPlatformServiceImpl;
        this.notificationWritePlatformService = notificationWritePlatformService;
        this.eventRequestService = eventRequestService;
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
    	eventRequestService.saveRequest(apiRequestBodyAsJson, null, "agentRegistration");
    	System.out.println(apiRequestBodyAsJson);
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
     
    	 if(appUserRepository.findAppUserByName(command.stringValueOfParameterNamed("emailId")) != null) {
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("emailId"), command.stringValueOfParameterNamed("emailId"));
    		 
    	 }else {
    		 
    		 this.agentDataValidator.validateForCreateAgentUser(apiRequestBodyAsJson);
    		 
    		 JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
    		 JsonObject jsonObject = new JsonObject();
    		 jsonObject.addProperty("username", this.fromApiJsonHelper.extractStringNamed("emailId", element));
    		 jsonObject.addProperty("password", this.fromApiJsonHelper.extractStringNamed("emailId", element));
    		 jsonObject.addProperty("firstName", this.fromApiJsonHelper.extractStringNamed("firstName", element));
    		 jsonObject.addProperty("lastName", this.fromApiJsonHelper.extractStringNamed("lastName", element));
    		 jsonObject.addProperty("email", this.fromApiJsonHelper.extractStringNamed("emailId", element));
    		 jsonObject.addProperty("authenticationMode", this.fromApiJsonHelper.extractStringNamed("authenticationMode", element));
    		 
    		 AppUser user = this.selfServiceRegistrationWritePlatformService.newUserRegister(jsonObject.toString(), SelfServiceApiConstants.AGENT_USER_ROLE);
    		 //String pinNumber = selfServiceRegistrationWritePlatformService.sendAuthorizationMailByAgent(user);
    		 agentController.AgentCreate(apiRequestBodyAsJson, uriInfo, user.getId(), "1234");
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("emailId"), command.stringValueOfParameterNamed("emailId"));
    	 }
			Gson gson = new Gson();
			JsonObject jsonObject1 = gson.fromJson(result, JsonObject.class);
			jsonObject1.addProperty("status", "success");
			/*jsonObject1.addProperty("agentUserId", jsonObject1.get("userId").toString());
			jsonObject1.remove("userId");*/
			result=jsonObject1.toString();
    	 return result; 
    }
    
    
    @POST
    @Path("customerUser/{parent_user_id}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSelfServiceCustomerUser(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,@PathParam("parent_user_id") Long agentId) {
       // AppUser user = this.selfServiceRegistrationWritePlatformService.createUser(apiRequestBodyAsJson);
    	eventRequestService.saveRequest(apiRequestBodyAsJson, null, "customerRegistration");
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
     
    	 if(appUserRepository.findAppUserByName(command.stringValueOfParameterNamed("contactNo")) != null) {
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("contactNo"), command.stringValueOfParameterNamed("contactNo"));
    	 }else {
    		 
    		 this.customerDataValidator.validateForCreateCustomerUser(apiRequestBodyAsJson);
    		 
    		 JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
    		 if(!appUserReadPlatformServiceImpl.getUserMobileNo(this.fromApiJsonHelper.extractStringNamed("contactNo", element)).isEmpty()){
    			 throw new MobileNumberAlreadyExists(this.fromApiJsonHelper.extractStringNamed("contactNo", element));
    		 }
    		
    		 JsonObject jsonObject = new JsonObject();
    		 jsonObject.addProperty("username", this.fromApiJsonHelper.extractStringNamed("contactNo", element));
    		 jsonObject.addProperty("password", this.fromApiJsonHelper.extractStringNamed("contactNo", element));
    		 jsonObject.addProperty("firstName", this.fromApiJsonHelper.extractStringNamed("firstName", element));
    		 jsonObject.addProperty("lastName", this.fromApiJsonHelper.extractStringNamed("lastName", element));
    		 jsonObject.addProperty("email", this.fromApiJsonHelper.extractStringNamed("emailId", element));
    		 jsonObject.addProperty("authenticationMode", this.fromApiJsonHelper.extractStringNamed("authenticationMode", element));
    		 
    		 AppUser user = this.selfServiceRegistrationWritePlatformService.newUserRegister(jsonObject.toString(), SelfServiceApiConstants.CUSTOMER_USER_ROLE);
    		 String transactionPIN = SendRandomOtpMessage.randomAuthorizationTokenGeneration();
    		 SmsSender.sendTransactionPIN(transactionPIN, fromApiJsonHelper.extractStringNamed("contactNo", element));
    		 customerController.CustomerCreate(apiRequestBodyAsJson, uriInfo, user.getId(),agentId, transactionPIN);
    		 result = authenticationApiResource.authenticate(command.stringValueOfParameterNamed("contactNo"), command.stringValueOfParameterNamed("contactNo"));

			 String getURL = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentId + "");
			 String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(getURL, HttpMethod.GET);
			 String mobileNo = fromApiJsonHelper.extractStringNamed("mobileNo", fromApiJsonHelper.parse(agentDetails));	
//    		 PushNotification.createPushNotification(NotificationMessage.clientRegistrationContent, 
//    				 NotificationMessage.clientRegistrationHeader, "userMobile", mobileNo);    		
//    		 notificationWritePlatformService.notify(agentId, "agent", null, "clientRegistered", null, 
//    				 NotificationMessage.clientRegistrationContent, false);
    		 
    		 notificationWritePlatformService.notify(user.getId(), "customer", null, "clientRegistered", null, 
    				 NotificationMessage.clientRegistrationWelcomeContent, false);
    	 }
    	    String status="success";
			Gson gson = new Gson();
			JsonObject jsonObject1 = gson.fromJson(result, JsonObject.class);
			jsonObject1.addProperty("status", status);
			result=jsonObject1.toString();
    	
			
    	 return result; 
    }
    
    @GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/faceAuthentication")
	public String getfaceId(@Context UriInfo uriInfo) {
		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/face/faceId");
		String faceIdValues = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(faceIdValues, JsonObject.class);
		jsonObject.addProperty("status", "success");
		faceIdValues = jsonObject.toString();
		
		return faceIdValues;
	}
	
    @GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/genUniqueId")
	public String generateUniqueId(@Context UriInfo uriInfo) {
    	JsonObject jsonObject = new JsonObject();
    	while(true) {
    		Integer randomPIN = (int) (Math.random() * 90000000) + 10000000;
    		Collection<AppUserData> appUserDatas = appUserReadPlatformServiceImpl.checkFaceUniqueId(randomPIN);
    		if(appUserDatas.isEmpty() ||
    				appUserDatas == null) {
    			jsonObject.addProperty("status", "success");
    	        jsonObject.addProperty("uniqueId", randomPIN);
    	        break;
    		}
    	}
        
        return jsonObject.toString();
	}
    

    @POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/customerLogin")
	public String customerLogin(final String apiRequestBodyAsJson) {
    	eventRequestService.saveRequest(apiRequestBodyAsJson, null, "customerLogin");
    	String result = null;
    	JsonElement command = fromApiJsonHelper.parse(apiRequestBodyAsJson);
    	String mobileNo = fromApiJsonHelper.extractStringNamed("mobileNumber", command);
    	String pin = fromApiJsonHelper.extractStringNamed("transactionPin", command);
    	agentDataValidator.validateForLogin(mobileNo, pin);
    	
    	Collection<AppUserData> appUserDatas = appUserReadPlatformServiceImpl.getCustomerUserByMobileNoAndPin(mobileNo, pin);
    	if(!appUserDatas.isEmpty()) {
    		AppUserData userData = null;
    		for(AppUserData appUserData : appUserDatas) {
    			userData = appUserData;
    		}
    		result = authenticationApiResource.authenticate(userData.getUsername(), userData.getUsername());
    		Gson gson = new Gson();
    		JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
    		jsonObject.addProperty("status", "success");
    		result = jsonObject.toString();
    	}else {
    		throw new UserNotFoundException(mobileNo);
    	}

        return result;
	}
    
    @POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/agentLogin")
	public String agentLogin(final String apiRequestBodyAsJson) {
    	eventRequestService.saveRequest(apiRequestBodyAsJson, null, "agentLogin");
    	String result = null;
    	JsonElement command = fromApiJsonHelper.parse(apiRequestBodyAsJson);
    	String mobileNo = fromApiJsonHelper.extractStringNamed("mobileNumber", command);
    	String pin = fromApiJsonHelper.extractStringNamed("transactionPin", command);
    	agentDataValidator.validateForLogin(mobileNo, pin);
    	
    	Collection<AppUserData> appUserDatas = appUserReadPlatformServiceImpl.getAgentUserByMobileNoAndPin(mobileNo);
    	if(!appUserDatas.isEmpty()) {
    		AppUserData userData = null;
    		for(AppUserData appUserData : appUserDatas) {
    			userData = appUserData;
    		}
    		result = authenticationApiResource.authenticate(userData.getUsername(), userData.getUsername());
    		Gson gson = new Gson();
    		JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
    		jsonObject.addProperty("status", "success");
    		result = jsonObject.toString();
    	}else {
    		throw new UserNotFoundException(mobileNo);
    	}

        return result;
	}
}
