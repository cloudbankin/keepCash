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
package org.apache.fineract.commands.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.AuditSearchData;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.springBoot.HttpConnectionTemplate;
import org.apache.fineract.portfolio.springBoot.enumType.AgentAccountStatusEnumType;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.service.AppUserReadPlatformServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Path("/makercheckers")
@Component
@Scope("singleton")
public class MakercheckersApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "actionName", "entityName", "resourceId",
            "subresourceId", "maker", "madeOnDate", "checker", "checkedOnDate", "processingResult", "commandAsJson", "officeName",
            "groupLevelName", "groupName", "clientName", "loanAccountNo", "savingsAccountNo", "clientId", "loanId"));

    private final AuditReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit;
    private final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService writePlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final CommandSourceRepository commandSourceRepository;
    private final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final SavingsAccountRepository savingsAccountRepository;

    @Autowired
    public MakercheckersApiResource(final AuditReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializerAudit,
            final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate,
            final ApiRequestParameterHelper apiRequestParameterHelper, final PortfolioCommandSourceWritePlatformService writePlatformService,
            final FromJsonHelper fromApiJsonHelper, final CommandSourceRepository commandSourceRepository,
            final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            final SavingsAccountRepository savingsAccountRepository) {
        this.readPlatformService = readPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializerAudit = toApiJsonSerializerAudit;
        this.toApiJsonSerializerSearchTemplate = toApiJsonSerializerSearchTemplate;
        this.writePlatformService = writePlatformService;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.commandSourceRepository = commandSourceRepository;
        this.appUserReadPlatformServiceImpl = appUserReadPlatformServiceImpl;
        this.savingsAccountReadPlatformService =savingsAccountReadPlatformService;
        this.savingsAccountRepository = savingsAccountRepository;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCommands(@Context final UriInfo uriInfo, @QueryParam("actionName") final String actionName,
            @QueryParam("entityName") final String entityName, @QueryParam("resourceId") final Long resourceId,
            @QueryParam("makerId") final Long makerId, @QueryParam("makerDateTimeFrom") final String makerDateTimeFrom,
            @QueryParam("makerDateTimeTo") final String makerDateTimeTo, @QueryParam("officeId") final Integer officeId,
            @QueryParam("groupId") final Integer groupId, @QueryParam("clientId") final Integer clientId,
            @QueryParam("loanid") final Integer loanId, @QueryParam("savingsAccountId") final Integer savingsAccountId) {

        final String extraCriteria = getExtraCriteria(actionName, entityName, resourceId, makerId, makerDateTimeFrom, makerDateTimeTo,
                officeId, groupId, clientId, loanId, savingsAccountId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final Collection<AuditData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked(extraCriteria,
                settings.isIncludeJson());

        return this.toApiJsonSerializerAudit.serialize(settings, entries, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/searchtemplate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final AuditSearchData auditSearchData = this.readPlatformService.retrieveSearchTemplate("makerchecker");

        final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<>(Arrays.asList("appUsers", "actionNames",
                "entityNames"));

        return this.toApiJsonSerializerSearchTemplate.serialize(settings, auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
    }
    
    @GET
    @Path("/makercheckersdeposit")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCommandsInDeposit(@Context final UriInfo uriInfo, @QueryParam("actionName") final String actionName,
            @QueryParam("entityName") final String entityName, @QueryParam("resourceId") final Long resourceId,
            @QueryParam("makerId") final Long makerId, @QueryParam("makerDateTimeFrom") final String makerDateTimeFrom,
            @QueryParam("makerDateTimeTo") final String makerDateTimeTo, @QueryParam("officeId") final Integer officeId,
            @QueryParam("groupId") final Integer groupId, @QueryParam("clientId") final Integer clientId,
            @QueryParam("loanid") final Integer loanId, @QueryParam("savingsAccountId") final Integer savingsAccountId) {

        final String extraCriteria = getExtraCriteria(actionName, entityName, resourceId, makerId, makerDateTimeFrom, makerDateTimeTo,
                officeId, groupId, clientId, loanId, savingsAccountId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final Collection<AuditData> entries = this.readPlatformService.retrieveAllEntriesToBeChecked(extraCriteria,
                settings.isIncludeJson());

        return this.toApiJsonSerializerAudit.serialize(settings, entries, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String approveMakerCheckerEntry(@PathParam("auditId") final Long auditId, @QueryParam("command") final String commandParam, 
    		@Context UriInfo uriInfo) {

    	AuditData auditData = readPlatformService.retrieveAuditEntry(auditId);
    	
        CommandProcessingResult result = null;
        Integer status = null;
        List<SavingsAccountTransactionData> savingsAccountTransactionData = null;
        Collection<AppUserData> agentUserDatas = null;
        
        if(auditData.getActionName().equals("TOPUP")) {
        	agentUserDatas = appUserReadPlatformServiceImpl.getAgnetUserByClientId(auditData.getClientId());
        	List<SavingsAccount>  savingsAccounts = savingsAccountRepository.findSavingAccountByClientId(auditData.getClientId());
        	for(SavingsAccount savingsAccount : savingsAccounts) {
        		Long productId = new Long(1);
        		if(savingsAccount.productId().equals(productId)) {
        	        Page<SavingsAccountTransactionData> transactionDatas = this.savingsAccountReadPlatformService.retrieveTransactionByCreteria(
        	        	null, savingsAccount.getId());
        	        savingsAccountTransactionData = new ArrayList<SavingsAccountTransactionData>();
        	        savingsAccountTransactionData = transactionDatas.getPageItems();
        		}
        	}
        }
        
        if (is(commandParam, "approve")) {
            result = this.writePlatformService.approveEntry(auditId);
            
            status = AgentAccountStatusEnumType.ACTIVE.getValue();
            if(savingsAccountTransactionData.size() <= 1) {
        		Long userId = null;
            	for(AppUserData appUserData : agentUserDatas) {
            		userId = appUserData.getId();
            	}
            	
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("status", status);
            	String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentUpdate?userId=" + userId + "");
        		HttpConnectionTemplate.restTemplateForPutMethod(url, jsonObject.toString(), HttpMethod.PUT);
        	}
        } else if (is(commandParam, "reject")) {
        	status = AgentAccountStatusEnumType.INACTIVE.getValue();
        	final Long id = this.writePlatformService.rejectEntry(auditId);
            result = CommandProcessingResult.commandOnlyResult(id);
            
            if(savingsAccountTransactionData.isEmpty()) {
        		Long userId = null;
            	for(AppUserData appUserData : agentUserDatas) {
            		userId = appUserData.getId();
            	}
            	
            	JsonObject jsonObject = new JsonObject();
            	jsonObject.addProperty("status", status);
            	String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentUpdate?userId=" + userId + "");
        		HttpConnectionTemplate.restTemplateForPutMethod(url, jsonObject.toString(), HttpMethod.PUT);
        	}
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        
        
     
    		
        
        
        
        return this.toApiJsonSerializerAudit.serialize(result);
    }
    
    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

   /* public void accountDeposit(String transaction, Long userId, UriInfo uriInfo, Integer transactionType,String json) {
		if(transaction != null) {
			JsonObject transactionJsonObject =  new JsonObject();
			
			JsonElement element = this.fromApiJsonHelper.parse(json);
			Gson gson=new Gson();
			JsonObject newelement=gson.fromJson(element, JsonObject.class);
			JsonElement newlocationelement=newelement.get("location");
			
			JsonElement transactionJsonCommand = fromApiJsonHelper.parse(transaction);
			transactionJsonObject.addProperty("savingsId", fromApiJsonHelper.extractLongNamed("savingsId", transactionJsonCommand));
			transactionJsonObject.addProperty("clientId", fromApiJsonHelper.extractLongNamed("clientId", transactionJsonCommand));
			transactionJsonObject.addProperty("userId", userId);
			transactionJsonObject.addProperty("transactionType", transactionType);
			transactionJsonObject.addProperty("locale", "en");
			transactionJsonObject.addProperty("transactionId", fromApiJsonHelper.extractLongNamed("resourceId",transactionJsonCommand));
			transactionJsonObject.add("location", newlocationelement);
		
			if(transactionJsonObject !=null) {
				String transactionUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/accountTransaction/createTransaction");
				HttpConnectionTemplate.restTemplate(transactionUrl, transactionJsonObject.toString(), HttpMethod.POST);
			}
		}
	}*/

    
    @DELETE
    @Path("{auditId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteMakerCheckerEntry(@PathParam("auditId") final Long auditId) {

        final Long id = this.writePlatformService.deleteEntry(auditId);

        return this.toApiJsonSerializerAudit.serialize(CommandProcessingResult.commandOnlyResult(id));
    }

    private String getExtraCriteria(final String actionName, final String entityName, final Long resourceId, final Long makerId,
            final String makerDateTimeFrom, final String makerDateTimeTo, final Integer officeId, final Integer groupId,
            final Integer clientId, final Integer loanId, final Integer savingsAccountId) {

        String extraCriteria = "";

        if (actionName != null) {
            extraCriteria += " and aud.action_name = " + ApiParameterHelper.sqlEncodeString(actionName);
        }
        if (entityName != null) {
            extraCriteria += " and aud.entity_name like " + ApiParameterHelper.sqlEncodeString(entityName + "%");
        }

        if (resourceId != null) {
            extraCriteria += " and aud.resource_id = " + resourceId;
        }
        if (makerId != null) {
            extraCriteria += " and aud.maker_id = " + makerId;
        }
        if (makerDateTimeFrom != null) {
            extraCriteria += " and aud.made_on_date >= " + ApiParameterHelper.sqlEncodeString(makerDateTimeFrom);
        }
        if (makerDateTimeTo != null) {
            extraCriteria += " and aud.made_on_date <= " + ApiParameterHelper.sqlEncodeString(makerDateTimeTo);
        }

        if (officeId != null) {
            extraCriteria += " and aud.office_id = " + officeId;
        }

        if (groupId != null) {
            extraCriteria += " and aud.group_id = " + groupId;
        }

        if (clientId != null) {
            extraCriteria += " and aud.client_id = " + clientId;
        }

        if (loanId != null) {
            extraCriteria += " and aud.loan_id = " + loanId;
        }

        if (savingsAccountId != null) {
            extraCriteria += " and aud.savings_account_id = " + savingsAccountId;
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }
}