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
package org.apache.fineract.portfolio.savings.api;

import java.util.Collection;

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

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.springBoot.HttpConnectionTemplate;
import org.apache.fineract.portfolio.springBoot.enumType.AppUserTypes;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpMethod;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Path("/savingsaccounts/{savingsId}/transactions")
@Component
@Scope("singleton")
public class SavingsAccountTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountTransactionsApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            final FromJsonHelper fromApiJsonHelper) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@PathParam("savingsId") final Long savingsId,
    // @QueryParam("command") final String commandParam,
            @Context final UriInfo uriInfo) {   
        
        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        // FIXME - KW - for now just send back generic default information for
        // both deposit/withdrawal templates
        SavingsAccountTransactionData savingsAccount = this.savingsAccountReadPlatformService.retrieveDepositTransactionTemplate(savingsId,
                DepositAccountType.SAVINGS_DEPOSIT);
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        savingsAccount = SavingsAccountTransactionData.templateOnTop(savingsAccount, paymentTypeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingsAccount,
                SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("savingsId") final Long savingsId, @PathParam("transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        SavingsAccountTransactionData transactionData = this.savingsAccountReadPlatformService.retrieveSavingsTransaction(savingsId,
                transactionId, DepositAccountType.SAVINGS_DEPOSIT);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            transactionData = SavingsAccountTransactionData.templateOnTop(transactionData, paymentTypeOptions);
        }

        return this.toApiJsonSerializer.serialize(settings, transactionData,
                SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/history")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTransaction(@PathParam("savingsId") final Long savingsId, @PathParam("transactionId") final Long transactionId,
            @Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("startDate") final String startDate, @QueryParam("endDate") final String endDate, @QueryParam("currentDate") final String currentDate,
            @QueryParam("transactionType") final Integer transactionType, 
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder, @QueryParam("isRating") final boolean isRating,
            @QueryParam("userId") final Long userId) {

    	LocalDate startDate1 = null;
    	LocalDate endDate1 = null;
    	LocalDate currentDate1 = null;
    	if(startDate != null) {
    		startDate1 = new LocalDate(startDate);
    		
    	}
    	if(endDate != null) {
    		endDate1 = new LocalDate(endDate);
    	}
    	if(currentDate != null) {
    		currentDate1 = new LocalDate(currentDate);
    	}
   
    	
    	String result = null;
    	result = retrieveAllTransaction(uriInfo, sqlSearch, savingsId, startDate1, endDate1,currentDate1, transactionType, offset, 
				limit, orderBy, sortOrder, isRating, userId);
    	
    	JsonObject jsonObject = new JsonObject();
    	JsonElement element = fromApiJsonHelper.parse(result);
    	jsonObject.addProperty("totalFilteredRecords", fromApiJsonHelper.extractStringNamed("totalFilteredRecords", element));
    	
    	JsonArray jsonArray = fromApiJsonHelper.extractJsonArrayNamed("pageItems", element);
    	JsonArray tranJsonArray = new JsonArray();
    	Gson gson = new  Gson();
    	
    	for(JsonElement jsonElement : jsonArray) {
    		JsonObject tranTypeJsonObject = new JsonObject();
    		JsonObject elementJsonObject  = gson.fromJson(jsonElement, JsonObject.class);    		
    		JsonElement json = fromApiJsonHelper.extractJsonObjectNamed("transactionType", jsonElement);
    		
    		tranTypeJsonObject.addProperty("id", fromApiJsonHelper.extractLongNamed("id", json));
    		tranTypeJsonObject.addProperty("code", fromApiJsonHelper.extractStringNamed("code", json));
    		tranTypeJsonObject.addProperty("value", fromApiJsonHelper.extractStringNamed("value", json));
    		tranTypeJsonObject.addProperty("deposit", fromApiJsonHelper.extractBooleanNamed("deposit", json));
    		
    		elementJsonObject.remove("transactionType");
    		elementJsonObject.add("transactionType", tranTypeJsonObject);
    		
    		JsonElement toUserDetails = null;
    		if(fromApiJsonHelper.parameterExists("toUserId", elementJsonObject) &&
    				fromApiJsonHelper.extractLongNamed("toUserId", elementJsonObject) != 0) {
    			Long toUserId = fromApiJsonHelper.extractLongNamed("toUserId", elementJsonObject);
    			if(fromApiJsonHelper.extractLongNamed("toUserTypeEnumId", elementJsonObject).intValue() == AppUserTypes.AGENT.getValue() ||
        				fromApiJsonHelper.extractLongNamed("toUserTypeEnumId", elementJsonObject).intValue() ==AppUserTypes.EMPLOYEE.getValue()) {
        			String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + toUserId + "");
            		toUserDetails = fromApiJsonHelper.parse(HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET));
        		}else if(fromApiJsonHelper.extractLongNamed("toUserTypeEnumId", elementJsonObject).equals(AppUserTypes.CUSTOMER.getValue())){
        			String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + toUserId + "");
        			toUserDetails = fromApiJsonHelper.parse(HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET));
        		}
    		}
    		
    		JsonObject toUserDetailsJsonObject = new JsonObject();
    		if(toUserDetails != null) {
    			toUserDetailsJsonObject.addProperty("appUserId", fromApiJsonHelper.extractStringNamed("appUserId", toUserDetails));
    			toUserDetailsJsonObject.addProperty("firstName", fromApiJsonHelper.extractStringNamed("firstName", toUserDetails));
    			toUserDetailsJsonObject.addProperty("lastName", fromApiJsonHelper.extractStringNamed("lastName", toUserDetails));
    			toUserDetailsJsonObject.add("appUserTypeEnum", fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", toUserDetails));
    			toUserDetailsJsonObject.addProperty("image", fromApiJsonHelper.extractStringNamed("image", toUserDetails));
    			toUserDetailsJsonObject.addProperty("imageEncryption", fromApiJsonHelper.extractStringNamed("imageEncryption", toUserDetails));
    		}
    		
    		elementJsonObject.add("toUserDetails", toUserDetailsJsonObject);
    		elementJsonObject.remove("userId");
    		elementJsonObject.remove("toUserId");
    		elementJsonObject.remove("userTypeEnumId");
    		elementJsonObject.remove("toUserTypeEnumId");
    		
    		tranJsonArray.add(elementJsonObject);
    	}
    	
    	jsonObject.add("pageItems", tranJsonArray);
    	String status="success";
        jsonObject.addProperty("status", status);
		
    	return jsonObject.toString();
    	
        
    }

    public String retrieveAllTransaction(final UriInfo uriInfo, final String sqlSearch, final Long savingsId,
    		final LocalDate startDate, final LocalDate endDate, final LocalDate currentDate, final Integer transactionType,
            final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder, final boolean isRating, final Long userId) {

    	final SearchParameters searchParameters = SearchParameters.forTransaction(sqlSearch, savingsId, startDate, endDate, currentDate, transactionType,
        		offset, limit, orderBy, sortOrder, isRating, userId);
    	
    	this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        Page<SavingsAccountTransactionData> transactionDatas = this.savingsAccountReadPlatformService.retrieveTransactionByCreteria(
        		searchParameters, savingsId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
       

        return this.toApiJsonSerializer.serialize(settings, transactionDatas,
                SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    	


    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String transaction(@PathParam("savingsId") final Long savingsId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
        try {
            final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

            CommandProcessingResult result = null;
            if (is(commandParam, "deposit")) {
                final CommandWrapper commandRequest = builder.savingsAccountDeposit(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "withdrawal")) {
                final CommandWrapper commandRequest = builder.savingsAccountWithdrawal(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "postInterestAsOn")) {
                final CommandWrapper commandRequest = builder.savingsAccountInterestPosting(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, SavingsApiConstants.COMMAND_HOLD_AMOUNT)) {
                final CommandWrapper commandRequest = builder.holdAmount(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            }

            if (result == null) {
                //
                throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "deposit", "withdrawal", SavingsApiConstants.COMMAND_HOLD_AMOUNT });
            }

            return this.toApiJsonSerializer.serialize(result);
        } catch (ObjectOptimisticLockingFailureException lockingFailureException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
        } catch (CannotAcquireLockException cannotAcquireLockException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
        }
    }
    
    
    @POST
    @Path("/agentTransaction")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String agentTransaction(@PathParam("savingsId") final Long savingsId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson,  @QueryParam("userId") final Long userId , @QueryParam("clientId") final Long clientId) {
        try {
            final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

            CommandProcessingResult result = null;
            if (is(commandParam, "deposit")) {
                final CommandWrapper commandRequest = builder.agentSavingsAccountDeposit(savingsId,userId, clientId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "withdrawal")) {
                final CommandWrapper commandRequest = builder.savingsAccountWithdrawal(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "postInterestAsOn")) {
                final CommandWrapper commandRequest = builder.savingsAccountInterestPosting(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, SavingsApiConstants.COMMAND_HOLD_AMOUNT)) {
                final CommandWrapper commandRequest = builder.holdAmount(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            }

            if (result == null) {
                //
                throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "deposit", "withdrawal", SavingsApiConstants.COMMAND_HOLD_AMOUNT });
            }

            return this.toApiJsonSerializer.serialize(result);
        } catch (ObjectOptimisticLockingFailureException lockingFailureException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
        } catch (CannotAcquireLockException cannotAcquireLockException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
        }
    }
    
    @POST
    @Path("/agentTopup")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String transactionTopup(@PathParam("savingsId") final Long savingsId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson, @QueryParam("productId") final Long productId) {
        try {
            final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

            CommandProcessingResult result = null;
            if (is(commandParam, "deposit")) {
                final CommandWrapper commandRequest = builder.savingsAccountDepositTopup(savingsId, productId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "withdrawal")) {
                final CommandWrapper commandRequest = builder.savingsAccountWithdrawal(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "postInterestAsOn")) {
                final CommandWrapper commandRequest = builder.savingsAccountInterestPosting(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, SavingsApiConstants.COMMAND_HOLD_AMOUNT)) {
                final CommandWrapper commandRequest = builder.holdAmount(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            }

            if (result == null) {
                //
                throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "deposit", "withdrawal", SavingsApiConstants.COMMAND_HOLD_AMOUNT });
            }

            return this.toApiJsonSerializer.serialize(result);
        } catch (ObjectOptimisticLockingFailureException lockingFailureException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
        } catch (CannotAcquireLockException cannotAcquireLockException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
        }
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String adjustTransaction(@PathParam("savingsId") final Long savingsId, @PathParam("transactionId") final Long transactionId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, SavingsApiConstants.COMMAND_UNDO_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.undoSavingsAccountTransaction(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_ADJUST_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.adjustSavingsAccountTransaction(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam,SavingsApiConstants.COMMAND_RELEASE_AMOUNT)) {
            final CommandWrapper commandRequest = builder.releaseAmount(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { SavingsApiConstants.COMMAND_UNDO_TRANSACTION,
                    SavingsApiConstants.COMMAND_ADJUST_TRANSACTION, SavingsApiConstants.COMMAND_RELEASE_AMOUNT});
        }

        return this.toApiJsonSerializer.serialize(result);
    }
}