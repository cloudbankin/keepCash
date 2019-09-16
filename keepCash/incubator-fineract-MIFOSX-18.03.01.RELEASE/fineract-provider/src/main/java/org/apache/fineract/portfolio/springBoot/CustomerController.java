package org.apache.fineract.portfolio.springBoot;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.commands.api.MakercheckersApiResource;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.codes.api.CodeValuesApiResource;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.documentmanagement.api.DocumentManagementApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.service.NotificationReadPlatformService;
import org.apache.fineract.notification.service.NotificationWritePlatformService;
import org.apache.fineract.organisation.staff.exception.MobileNumberAlreadyExists;
import org.apache.fineract.portfolio.client.api.ClientIdentifiersApiResource;
import org.apache.fineract.portfolio.client.api.ClientsApiResource;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.api.SavingsAccountTransactionsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.exception.SavingAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.portfolio.springBoot.data.CustomerDataValidator;
import org.apache.fineract.portfolio.springBoot.enumType.SavingsAccountTypeEnum;
import org.apache.fineract.portfolio.springBoot.enumType.SavingsTransactionDetailsTypeEnum;
import org.apache.fineract.portfolio.springBoot.service.EventRequestService;
import org.apache.fineract.portfolio.springBoot.service.PushNotification;
import org.apache.fineract.portfolio.springBoot.service.SendRandomOtpMessage;
import org.apache.fineract.portfolio.springBoot.service.SmsSender;
import org.apache.fineract.useradministration.api.UsersApiResource;
import org.apache.fineract.useradministration.service.AppUserReadPlatformServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpMethod;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minidev.json.JSONObject;



@Path("/customer")
@Component
@Scope("singleton")
public class CustomerController {

	private final PlatformSecurityContext context;
	private final FromJsonHelper fromApiJsonHelper;
	private final ClientsApiResource clientsApiResource;
	private final SavingsAccountsApiResource savingsAccountsApiResource;
	private final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource;
	private final SavingsAccountRepository savingsAccountRepository;
	private final LoanRepository loanRepository;
	private final DocumentManagementApiResource documentManagementApiResource;
	private final ClientIdentifiersApiResource clientIdentifiersApiResource;
	private final CodeValueRepositoryWrapper codeValueRepository;
	private final SavingsProductReadPlatformService savingProductReadPlatformService;
	private final CodeValueReadPlatformService codeValueReadPlatformService;
	private final CustomerDataValidator customerDataValidator;
	private final SendRandomOtpMessage sendRandomOtpMessage; 
	private final ClientRepositoryWrapper clientRepositoryWrapper;
	private final ToApiJsonSerializer<SmsData> toApiJsonSerializer;
	private final MakercheckersApiResource  makercheckersApiResource;
	private final CodeValuesApiResource codeValuesApiResource;
	private final UsersApiResource usersApiResource;
	private final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl;
	private final NotificationWritePlatformService notificationWritePlatformService;
	private final NotificationReadPlatformService notificationReadPlatformService;
	private final EventRequestService eventRequestService;

	@Autowired
	public CustomerController(final PlatformSecurityContext context, final FromJsonHelper fromApiJsonHelper,
			final ClientsApiResource clientsApiResource, final SavingsAccountsApiResource savingsAccountsApiResource,
			final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource,
			final SavingsAccountRepository savingsAccountRepository, final LoanRepository loanRepository,
			final LoanTransactionsApiResource loanTransactionsApiResource,
			final DocumentManagementApiResource documentManagementApiResource,
			final ClientIdentifiersApiResource clientIdentifiersApiResource,
			final CodeValueRepositoryWrapper codeValueRepository,
			final SavingsProductReadPlatformService savingProductReadPlatformService,
			final CodeValueReadPlatformService codeValueReadPlatformService,
			final CustomerDataValidator customerDataValidator, 
			final SendRandomOtpMessage sendRandomOtpMessage, 
			final ClientRepositoryWrapper clientRepositoryWrapper,
			final ToApiJsonSerializer<SmsData> toApiJsonSerializer,
			final MakercheckersApiResource  makercheckersApiResource,
			final CodeValuesApiResource codeValuesApiResource,
			final UsersApiResource usersApiResource,
			final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl,
			final NotificationWritePlatformService notificationWritePlatformService,
			final NotificationReadPlatformService notificationReadPlatformService,
			final EventRequestService eventRequestService) {

		this.context = context;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.clientsApiResource = clientsApiResource;
		this.savingsAccountsApiResource = savingsAccountsApiResource;
		this.savingsAccountTransactionsApiResource = savingsAccountTransactionsApiResource;
		this.savingsAccountRepository = savingsAccountRepository;
		this.loanRepository = loanRepository;
		this.documentManagementApiResource = documentManagementApiResource;
		this.clientIdentifiersApiResource = clientIdentifiersApiResource;
		this.codeValueRepository = codeValueRepository;
		this.savingProductReadPlatformService = savingProductReadPlatformService;
		this.codeValueReadPlatformService = codeValueReadPlatformService;
		this.customerDataValidator = customerDataValidator;
		this.sendRandomOtpMessage = sendRandomOtpMessage;
		this.clientRepositoryWrapper = clientRepositoryWrapper;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.makercheckersApiResource = makercheckersApiResource;
		this.codeValuesApiResource = codeValuesApiResource;
		this.usersApiResource = usersApiResource;
		this.appUserReadPlatformServiceImpl= appUserReadPlatformServiceImpl;
		this.notificationWritePlatformService = notificationWritePlatformService;
		this.notificationReadPlatformService = notificationReadPlatformService;
		this.eventRequestService = eventRequestService;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/customerCreate")
	public String CustomerCreate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, Long userId, Long agentId, String transactionPIN) {
		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerCreate/"+ agentId +"?userId=" + userId + "&transactionPin="+transactionPIN);
		return HttpConnectionTemplate.restTemplate(url, apiRequestBodyAsJson, HttpMethod.POST);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/{userId}")
	public String retrieveCustomer(@PathParam("userId") final Long userId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser();

		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + userId + "");
		String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		String apiRequestBodyAsJson = customerDetails;
		StringBuilder name = new StringBuilder();
		JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		if (clientId == null) {
			name.append(this.fromApiJsonHelper.extractStringNamed("firstName", element));
			name.append(" " + this.fromApiJsonHelper.extractStringNamed("lastName", element));
			apiRequestBodyAsJson = " {\"address\":[],\"familyMembers\":[],\"officeId\":1,\"legalFormId\":2,"
					+ "\"active\":true,\"locale\":\"en\",\"dateFormat\":\"dd MMMM yyyy\",\"savingsProductId\":null}";
			JsonObject jsonObject = new JsonParser().parse(apiRequestBodyAsJson).getAsJsonObject();
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
			String currentDate = sdf.format(new Date());
			
			Long agnetUserId = fromApiJsonHelper.extractLongNamed("parentUserId", element);
			String agentUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agnetUserId + "");
			String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(agentUrl, HttpMethod.GET);
			
			if(agentDetails != null) {
				JsonElement agentElement = this.fromApiJsonHelper.parse(agentDetails);
				jsonObject.addProperty("externalId", fromApiJsonHelper.extractLongNamed("clientId", agentElement).toString());
			}
			jsonObject.addProperty("legalFormId", LegalForm.PERSON.getValue());
			jsonObject.addProperty("submittedOnDate", currentDate);
			jsonObject.addProperty("activationDate", currentDate);
			jsonObject.addProperty("firstname", fromApiJsonHelper.extractStringNamed("firstName", element));
			jsonObject.addProperty("lastname", fromApiJsonHelper.extractStringNamed("lastName", element));
			jsonObject.addProperty("mobileNo", fromApiJsonHelper.extractStringNamed("mobileNo", element));
			jsonObject.addProperty("emailAddress", fromApiJsonHelper.extractStringNamed("emailId", element));
			apiRequestBodyAsJson = jsonObject.toString();
			String client = this.clientsApiResource.create(apiRequestBodyAsJson);

			clientId = this.fromApiJsonHelper.extractLongNamed("clientId", this.fromApiJsonHelper.parse(client));

			String updateURL = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo,
					"/customerUser/updateClientInCustomer?userId=" + userId + "");
			HttpConnectionTemplate.restTemplate(updateURL, client, HttpMethod.POST);
		}
		String clientDocuments = this.documentManagementApiResource.retreiveAllDocuments(uriInfo, "clients", clientId);
		String clientIdentifiers = this.clientIdentifiersApiResource.retrieveAllClientIdentifiers(uriInfo, clientId);

		JsonParser jsonParser = new JsonParser();
		JsonArray clientIdentifiersArray = (JsonArray) jsonParser.parse(clientIdentifiers);

		JsonArray clientIdentifiersResult = new JsonArray();
		for (int i = 0; i < clientIdentifiersArray.size(); i++) {
			final JsonObject jsonObject = clientIdentifiersArray.get(i).getAsJsonObject();

			JsonElement identifierElement = this.fromApiJsonHelper.parse(jsonObject.toString());
			Long id = this.fromApiJsonHelper.extractLongNamed("id", identifierElement);

			String clientIdentifierDocuments = this.documentManagementApiResource.retreiveAllDocuments(uriInfo,
					"client_identifiers", id);
			jsonObject.add("documents", this.fromApiJsonHelper.parse(clientIdentifierDocuments));
			;
			clientIdentifiersResult.add(this.fromApiJsonHelper.parse(jsonObject.toString()));
		}

		JsonObject jsonObject = new JsonParser().parse(customerDetails).getAsJsonObject();
		jsonObject.remove("clientId");
		jsonObject.add("clientId", this.fromApiJsonHelper.parse(clientId.toString()));
		jsonObject.add("documents", this.fromApiJsonHelper.parse(clientDocuments));
		jsonObject.add("identifiers", this.fromApiJsonHelper.parse(clientIdentifiersResult.toString()));

		final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService
				.retrieveCodeValuesByCode("Customer Identifier");

		JsonArray array = new JsonArray();

		for (CodeValueData codeValue : codeValues) {
			JsonObject appUserTypeJson = new JsonObject();
			appUserTypeJson.add("id", jsonParser.parse(codeValue.getId().toString()));
			appUserTypeJson.add("name", jsonParser.parse("\"" + codeValue.getName() + "\""));
			array.add(jsonParser.parse(appUserTypeJson.toString()));
		}

		jsonObject.add("identifierTypes", this.fromApiJsonHelper.parse(array.toString()));

		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		JsonArray savingsArray = new JsonArray();
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
			JsonObject appUserTypeJson = new JsonObject();
			appUserTypeJson.add("savingsAccountId", jsonParser.parse(savingsAccount.getId().toString()));
			appUserTypeJson.add("currentBalance", jsonParser.parse(savingsAccount.getSummary().getAccountBalance().toString()));
			savingsArray.add(jsonParser.parse(appUserTypeJson.toString()));
		}
		
		jsonObject.add("savingsAccount", this.fromApiJsonHelper.parse(savingsArray.toString()));
		String status="success";
		jsonObject.addProperty("status", status);
		customerDetails = jsonObject.toString();

		return customerDetails;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/cashIn/{agentUserId}/{custUserId}")
	//@Transactional(rollbackFor = DataIntegrityViolationException.class)
	@Transactional(rollbackFor = Exception.class)
//	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = MyException.class)

	public String depositAgentSavingsAccount(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,
			@PathParam("agentUserId") final Long agentUserId, @PathParam("custUserId") final Long custUserId) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, agentUserId, "cashIn");
		String result = null;
		BigDecimal savingsAmount = null;
		// validation
		this.customerDataValidator.validateForDepositTransaction(apiRequestBodyAsJson);

		JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		String transactionDate = this.fromApiJsonHelper.extractStringNamed("transactionDate", apiRequestBodyAsJsonElement);
		BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount", apiRequestBodyAsJsonElement, Locale.ENGLISH);

		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
		String customerDetails=null;
		 try{
			 customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
			}
			catch(Exception e)
			{
				final String defaultUserMessage = " failed due to userId "+ custUserId +" doesn't exist";
				final ApiParameterError error = ApiParameterError.parameterError("error.msg.cashIn.failed",
						defaultUserMessage, "");
				final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
				dataValidationErrors.add(error);
				throw new PlatformApiDataValidationException(dataValidationErrors);
			}
		//String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		JsonElement element = this.fromApiJsonHelper.parse(customerDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		String mobileNo = fromApiJsonHelper.extractStringNamed("mobileNo", element);
		BigDecimal goalAmount = fromApiJsonHelper.extractBigDecimalNamed("goalAmount", element, Locale.ENGLISH);


		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		/**
		 * Create new savings account if the client does not have any savings account
		 */
		if (savingsAccount == null) {
			Long productId = new Long(3);
			SavingsProductData savingsProduct = this.savingProductReadPlatformService.retrieveOne(productId);
			JsonObject createSavingsAccountJson = new JsonObject();
			
			if (clientId != null && savingsProduct != null) {
				createSavingsAccountJson.addProperty("productId", savingsProduct.getId());
				createSavingsAccountJson.addProperty("nominalAnnualInterestRate", savingsProduct.getNominalAnnualInterestRate());
				createSavingsAccountJson.addProperty("withdrawalFeeForTransfers", savingsProduct.isWithdrawalFeeForTransfers());
				createSavingsAccountJson.addProperty("allowOverdraft", savingsProduct.isAllowOverdraft());
				createSavingsAccountJson.addProperty("enforceMinRequiredBalance", savingsProduct.isEnforceMinRequiredBalance());
				createSavingsAccountJson.addProperty("withHoldTax", savingsProduct.isWithHoldTax());
				createSavingsAccountJson.addProperty("interestCompoundingPeriodType", savingsProduct.getInterestCompoundingPeriodType().getId());
				createSavingsAccountJson.addProperty("interestPostingPeriodType", savingsProduct.getInterestPostingPeriodType().getId());
				createSavingsAccountJson.addProperty("interestCalculationType", savingsProduct.getInterestCalculationType().getId());
				createSavingsAccountJson.addProperty("interestCalculationDaysInYearType", savingsProduct.getInterestCalculationDaysInYearType().getId());
				createSavingsAccountJson.addProperty("locale", "en");
				createSavingsAccountJson.addProperty("dateFormat", "dd MMMM yyyy");
				createSavingsAccountJson.addProperty("monthDayFormat", "dd MMM");
				createSavingsAccountJson.addProperty("clientId", clientId);
				createSavingsAccountJson.addProperty("submittedOnDate", transactionDate);
				JsonArray array = new JsonArray();
				createSavingsAccountJson.add("charges", array);
			} else {
				final String defaultUserMessage = "Transaction failed";
				final ApiParameterError error = ApiParameterError.parameterError("error.msg.transaction.failed",
						defaultUserMessage, "");
				final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
				dataValidationErrors.add(error);
				throw new PlatformApiDataValidationException(dataValidationErrors);
			}

			try {
				result = this.savingsAccountsApiResource.submitApplication(createSavingsAccountJson.toString());
				// Approve and activate savings account
				result = approveAndActivateAgentSavingsAccount(transactionDate, uriInfo, custUserId);
				withdrawalSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentUserId, custUserId);
				result = depositSavingsAccountInCustomer(uriInfo, apiRequestBodyAsJson, custUserId, agentUserId);

	    		 PushNotification.createPushNotification(NotificationMessage.firstTimeCashInContent, 
	    				 NotificationMessage.firstTimeCashInHeader, "userMobile", mobileNo);    		
	    		 notificationWritePlatformService.notify(custUserId, "customer", null, "cashIn", null, 
	    				 NotificationMessage.firstTimeCashInContent, false);
	    		 
	    		 JsonElement jsonElement = fromApiJsonHelper.parse(result);
				 SavingsAccount notificationAccount = savingsAccountRepository.findByIdAndDepositAccountType(fromApiJsonHelper.extractLongNamed("savingsId", 
							jsonElement), DepositAccountType.SAVINGS_DEPOSIT.getValue());
				 if(notificationAccount.getAccountBalanceDerived() != null) {
					 savingsAmount = notificationAccount.getAccountBalanceDerived().add(transactionAmount);
				 }else {
					 savingsAmount = transactionAmount;
				 }
	    		 achievedGoal(mobileNo, custUserId, goalAmount, savingsAmount);
	    		 
			} catch (ObjectOptimisticLockingFailureException lockingFailureException) {
	            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
	                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
	        } catch (CannotAcquireLockException cannotAcquireLockException) {
	            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
	                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
	        }
		} else {
			try {
				/** Savings account deposit transaction */
				withdrawalSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentUserId, custUserId);
				result = depositSavingsAccountInCustomer(uriInfo, apiRequestBodyAsJson, custUserId, agentUserId);
				
				JsonElement jsonElement = fromApiJsonHelper.parse(result);
				SavingsAccount notificationAccount = savingsAccountRepository.findByIdAndDepositAccountType(fromApiJsonHelper.extractLongNamed("savingsId", 
						jsonElement), DepositAccountType.SAVINGS_DEPOSIT.getValue());
				
				PushNotification.createPushNotification("Congratulations for saving Rs "+transactionAmount+" towards your goal.", 
	    				 NotificationMessage.customerCashInHeader, "userMobile", mobileNo);    		
	    		 notificationWritePlatformService.notify(custUserId, "customer", null, "cashIn", null, 
	    				 "Congratulations for saving Rs "+transactionAmount+" towards your goal.", false);
	    		 
	    		 if(notificationAccount.getAccountBalanceDerived() != null) {
					 savingsAmount = notificationAccount.getAccountBalanceDerived().add(transactionAmount);
				 }else {
					 savingsAmount = transactionAmount;
				 }	    		 
	    		 achievedGoal(mobileNo, custUserId, goalAmount, savingsAmount);
	    		 
			}catch (ObjectOptimisticLockingFailureException lockingFailureException) {
	            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
	                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
	        } catch (CannotAcquireLockException cannotAcquireLockException) {
	            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
	                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
	        }
		}

		String status="success";
		Gson gson = new Gson();
		JsonObject jsonObject1 = gson.fromJson(result, JsonObject.class);
		jsonObject1.addProperty("status", status);
		result=jsonObject1.toString();
		return result;
	}

	public void achievedGoal(String mobileNo, Long userId, BigDecimal goalAmount, BigDecimal accountBal){
		BigDecimal twentyFivePercent = new BigDecimal(25);
		BigDecimal fiftyPercent = new BigDecimal(50);
		BigDecimal seventyFivePercent = new BigDecimal(75);
		BigDecimal ninetyPercent = new BigDecimal(90);
		BigDecimal hundredPercent = new BigDecimal(100);
		BigDecimal achieveAmount = BigDecimal.ZERO;
		boolean twentyFive = false;
		boolean fifty = false;
		boolean seventyFive = false;
		boolean ninety = false;
		boolean hundreed = false;
		
		if(goalAmount != null) {
			BigDecimal totalPercentage = accountBal.divide(goalAmount).multiply(hundredPercent);
			
			Page<NotificationData> notificationDatas = notificationReadPlatformService.getNotificationsByAction(userId, "achievedGoal");
			
			if(!notificationDatas.getPageItems().isEmpty()) {
				for(NotificationData notificationData : notificationDatas.getPageItems()) {
					String content = notificationData.getContent();					
					Pattern p = Pattern.compile("\\d+");
			        Matcher m = p.matcher(content);
			        String part = null;
			        while(m.find()) {
			        	part = m.group();
			        }
			        
			        if(part != null) {
			        	if(part.equals("25")) {
							twentyFive = true;
						}else if(part.equals("50")) {
							fifty = true;
						}else if(part.equals("75")) {
							seventyFive = true;
						}else if(part.equals("90")) {
							ninety = true;
						}else if(part.equals("100")) {
							hundreed = true;
						}
			        }
				}
			}
			
			if(totalPercentage.compareTo(twentyFivePercent) == 0 ||
					totalPercentage.compareTo(twentyFivePercent) == 1 &&
					totalPercentage.compareTo(fiftyPercent) == -1 &&
					!twentyFive) {
				achieveAmount = twentyFivePercent;
			}else if(totalPercentage.compareTo(fiftyPercent) == 0 ||
					totalPercentage.compareTo(fiftyPercent) == 1 &&
					totalPercentage.compareTo(seventyFivePercent) == -1 &&
					!fifty) {
				achieveAmount = fiftyPercent;
			}else if(totalPercentage.compareTo(seventyFivePercent) == 0 ||
					totalPercentage.compareTo(seventyFivePercent) == 1 &&
					totalPercentage.compareTo(ninetyPercent) == -1 &&
					!seventyFive) {
				achieveAmount = seventyFivePercent;
			}else if(totalPercentage.compareTo(ninetyPercent) == 0 ||
					totalPercentage.compareTo(ninetyPercent) == 1 &&
					totalPercentage.compareTo(hundredPercent) == -1 &&
					!ninety) {
				achieveAmount = ninetyPercent;
			}else if(totalPercentage.compareTo(hundredPercent) == 0 || 
					totalPercentage.compareTo(hundredPercent) == 1 &&
					!hundreed) {
				achieveAmount = hundredPercent;
			}
			
			if(achieveAmount.compareTo(BigDecimal.ZERO) != 0){
				PushNotification.createPushNotification("Congratulations! you have achieved the "+ achieveAmount +"% percent towards your goal. ", 
						 NotificationMessage.achievedHeader, "userMobile", mobileNo);    		
				 notificationWritePlatformService.notify(userId, "customer", null, "achievedGoal", null, 
						 "Congratulations! you have achieved the "+ achieveAmount +"% percent towards your goal. ", false);
			}
			 
		}
		
		
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/approveAndActivate")
	public String approveAndActivateAgentSavingsAccount(final String transactionDate, @Context final UriInfo uriInfo,
			@QueryParam("custUserId") final Long custUserId) {

		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
		String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		JsonElement element = this.fromApiJsonHelper.parse(customerDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);

		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		if (savingsAccount != null) {
			JsonObject createSavingsAccountJson = new JsonObject();
			// Approve savings account
			createSavingsAccountJson.addProperty("approvedOnDate", transactionDate);
			createSavingsAccountJson.addProperty("locale", "en");
			createSavingsAccountJson.addProperty("dateFormat", "dd MMMM yyyy");
			this.savingsAccountsApiResource.handleCommands(savingsAccount.getId(), "approve",
					createSavingsAccountJson.toString());

			// Activate savings account
			createSavingsAccountJson.remove("approvedOnDate");
			createSavingsAccountJson.addProperty("activatedOnDate", transactionDate);
			this.savingsAccountsApiResource.handleCommands(savingsAccount.getId(), "activate",
					createSavingsAccountJson.toString());

		} else {
			return "Transaction failed";
		}

		return "Transaction done successfully";
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/customerDepositAmount")
	public String depositSavingsAccountInCustomer(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			@QueryParam("custUserId") final Long custUserId, @QueryParam("agentUserId") final Long agentUserId) {

		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
		String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(customerDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		JsonElement cusUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", element); 
		Long cusUserTypeId = fromApiJsonHelper.extractLongNamed("id", cusUserTypeElement); 

		String agentUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(agentUrl, HttpMethod.GET);
		JsonElement agnetElement = this.fromApiJsonHelper.parse(agentDetails);
		JsonElement ageUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", agnetElement); 
		Long ageUserTypeId = fromApiJsonHelper.extractLongNamed("id", ageUserTypeElement); 
		
		
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		String transaction = null;
		if (savingsAccount != null) {
			transaction =  savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "deposit", apiRequestBodyAsJson);			
			savingsAccountDeposit(transaction, savingsAccount.getId(), clientId, custUserId, uriInfo, SavingsTransactionDetailsTypeEnum.CASHIN.getValue(),
					agentUserId, cusUserTypeId, ageUserTypeId,apiRequestBodyAsJson);
		} else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		return transaction;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentWithdrawalAmount")
	public String withdrawalSavingsAccountInAgent(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			@QueryParam("agentUserId") final Long agentUserId, @QueryParam("custUserId") final Long custUserId) {

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		JsonElement ageUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", element); 
		Long ageUserTypeId = fromApiJsonHelper.extractLongNamed("id", ageUserTypeElement); 
		
		String custurl = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
		String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(custurl, HttpMethod.GET);
		JsonElement customerElement = this.fromApiJsonHelper.parse(customerDetails);
		JsonElement cusUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", customerElement); 
		Long cusUserTypeId = fromApiJsonHelper.extractLongNamed("id", cusUserTypeElement); 
		
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			String savingsUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/retrieveAllAccount/"+account.getId());
			String accountData = HttpConnectionTemplate.restTemplateForGetMethod(savingsUrl, HttpMethod.GET);
			if(!accountData.equals(null) &&
					!accountData.equals("null")) {
				JsonElement accountDetailsElement = this.fromApiJsonHelper.parse(accountData);
				Integer accountType = fromApiJsonHelper.extractIntegerNamed("accountType", accountDetailsElement, Locale.ENGLISH);
				
				if(accountType == SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue()){
					savingsAccount = account;
				}
			}
		}

		String transaction = null;
		if (savingsAccount != null) {
			transaction = savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "withdrawal", apiRequestBodyAsJson);
			
			String cashurl = HttpConnectionTemplate.createBootUrlInCustomer
					(uriInfo, "/createCashInCashOut/createCashInCashOutTransaction?custUserId=" + custUserId + "&agentUserId="+agentUserId);
			HttpConnectionTemplate.restTemplate(cashurl, apiRequestBodyAsJson, HttpMethod.POST);
			
			accountWithdrawal(transaction, savingsAccount.getId(), clientId, agentUserId, uriInfo, SavingsTransactionDetailsTypeEnum.CASHOUT.getValue(),
					custUserId, ageUserTypeId, cusUserTypeId, apiRequestBodyAsJson);
		}else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		
		return transaction;
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/cashOut/{agentUserId}/{custUserId}")
	@Transactional(rollbackFor = Exception.class)
	public String cashOutFromSavingAccountInCustomer(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, 
			@PathParam("agentUserId") final Long agentUserId,
			@PathParam("custUserId") final Long custUserId) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, agentUserId, "cashOut");
		String cashOutDetails = null;
		this.customerDataValidator.validateForCashOutTransaction(apiRequestBodyAsJson);
		try {
			withdrawalSavingsAccountInCustomer(uriInfo, apiRequestBodyAsJson, custUserId, agentUserId);
			cashOutDetails = depositSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentUserId, custUserId);
			
			String cashurl = HttpConnectionTemplate.createBootUrlInCustomer
					(uriInfo, "/createCashInCashOut/createCashInCashOutTransaction?custUserId=" + custUserId + "&agentUserId="+agentUserId);
			HttpConnectionTemplate.restTemplate(cashurl, apiRequestBodyAsJson, HttpMethod.POST);
			
			String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
			String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
			JsonElement element = this.fromApiJsonHelper.parse(customerDetails);
			String mobileNo = fromApiJsonHelper.extractStringNamed("mobileNo", element);	
			 
			PushNotification.createPushNotification(NotificationMessage.customerCashOutContent, 
   				 NotificationMessage.customerCashOutHeader, "userMobile", mobileNo);    		
   		 notificationWritePlatformService.notify(custUserId, "customer", null, "cashOut", null, 
   				NotificationMessage.customerCashOutContent, false);
   		 
		}  catch (ObjectOptimisticLockingFailureException lockingFailureException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
        } catch (CannotAcquireLockException cannotAcquireLockException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
        }
		String status="success";
		Gson gson = new Gson();
		JsonObject jsonObject1 = gson.fromJson(cashOutDetails, JsonObject.class);
		jsonObject1.addProperty("status", status);
		cashOutDetails=jsonObject1.toString();
		
		return cashOutDetails;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/customerWithdrawalAmount")
	public String withdrawalSavingsAccountInCustomer(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			@QueryParam("custUserId") final Long custUserId, @QueryParam("agentUserId") final Long agentUserId) {

		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
		String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(customerDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		JsonElement cusUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", element); 
		Long cusUserTypeId = fromApiJsonHelper.extractLongNamed("id", cusUserTypeElement); 

		String agentUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(agentUrl, HttpMethod.GET);
		JsonElement agnetElement = this.fromApiJsonHelper.parse(agentDetails);
		JsonElement ageUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", agnetElement); 
		Long ageUserTypeId = fromApiJsonHelper.extractLongNamed("id", ageUserTypeElement); 
		
		
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		String transaction = null;
		if (savingsAccount != null) {
			transaction = savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "withdrawal", apiRequestBodyAsJson);
			accountWithdrawal(transaction, savingsAccount.getId(), clientId, custUserId, uriInfo, SavingsTransactionDetailsTypeEnum.CASHOUT.getValue(),
					agentUserId, cusUserTypeId, ageUserTypeId,apiRequestBodyAsJson);
		} else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		return transaction;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentDepositAmount")
	public String depositSavingsAccountInAgent(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			@QueryParam("agentUserId") final Long agentUserId, @QueryParam("custUserId") final Long custUserId) {

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		JsonElement ageUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", element); 
		Long ageUserTypeId = fromApiJsonHelper.extractLongNamed("id", ageUserTypeElement); 
		
		String custurl = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
		String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(custurl, HttpMethod.GET);
		JsonElement customerElement = this.fromApiJsonHelper.parse(customerDetails);
		JsonElement cusUserTypeElement = fromApiJsonHelper.extractJsonObjectNamed("appUserTypeEnum", customerElement); 
		Long cusUserTypeId = fromApiJsonHelper.extractLongNamed("id", cusUserTypeElement); 
		
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			String savingsUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/retrieveAllAccount/"+account.getId());
			String accountData = HttpConnectionTemplate.restTemplateForGetMethod(savingsUrl, HttpMethod.GET);
			if(!accountData.equals(null) &&
					!accountData.equals("null")) {
				JsonElement accountDetailsElement = this.fromApiJsonHelper.parse(accountData);
				Integer accountType = fromApiJsonHelper.extractIntegerNamed("accountType", accountDetailsElement, Locale.ENGLISH);
				
				if(accountType == SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue()){
					savingsAccount = account;
				}
			}
		}
		
		String transaction = null;
		if (savingsAccount != null) {
			transaction = savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "deposit", apiRequestBodyAsJson);
			savingsAccountDeposit(transaction, savingsAccount.getId(), clientId, agentUserId, uriInfo, SavingsTransactionDetailsTypeEnum.CASHIN.getValue(),
					custUserId, ageUserTypeId, cusUserTypeId, apiRequestBodyAsJson);
			
		}else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		
		return transaction;
	}

//to retrieve all customers under a agent using parent user id
		@GET
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		@Path("/underAgent/{parentUserId}")
		public String RetrieveCustomersUnderAgent(@PathParam("parentUserId") final Long agentId, @Context final UriInfo uriInfo) {

			String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customersUnderAgentRetrieve?agentId=" + agentId + "");
			String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
			return customerDetails;
		}
		//to update changes in customer details
		@PUT
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		@Path("/customerUpdate/{customerId}")
		public String CustomerUpdate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,@PathParam("customerId") Long userId) {
			eventRequestService.saveRequest(apiRequestBodyAsJson, userId, "customerUpdate");
			customerDataValidator.validateForUpdateCustomerUser(apiRequestBodyAsJson);
			JsonCommand requestJsonCommand = getJsonCommand(apiRequestBodyAsJson);
			JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
			String firstname = this.fromApiJsonHelper.extractStringNamed("firstName",apiRequestBodyAsJsonElement );
			String lastname = this.fromApiJsonHelper.extractStringNamed("lastName",apiRequestBodyAsJsonElement );
			String email = this.fromApiJsonHelper.extractStringNamed("emailId",apiRequestBodyAsJsonElement );
			String mobileno =this.fromApiJsonHelper.extractStringNamed("contactNo", apiRequestBodyAsJsonElement);
			String dateofbirth = this.fromApiJsonHelper.extractStringNamed("dateOfBirth",apiRequestBodyAsJsonElement);
			String locale = requestJsonCommand.stringValueOfParameterNamed("locale");
			String dateFormat = requestJsonCommand.stringValueOfParameterNamed("dateFormat");
			Gson gson = new Gson();
			JsonObject jsonObject =new JsonObject();
			if(firstname != null) {
				jsonObject.addProperty("firstname",firstname );
			}
			if(lastname != null) {
				jsonObject.addProperty("lastname",lastname );
			}
			if(email != null) {
				jsonObject.addProperty("email",email );

			}
			String apiRequestBodyAsJson1=jsonObject.toString();
			String appuserchanges = usersApiResource.update(userId, apiRequestBodyAsJson1);
			
			JSONObject clientUpdatejsonObject =new JSONObject();
			if(firstname != null) {
				clientUpdatejsonObject.put("firstname",firstname );
			}
			if(lastname != null) {
				clientUpdatejsonObject.put("lastname",lastname );
			}
			if(email != null) {
				clientUpdatejsonObject.put("emailAddress",email );

			}
			if(mobileno != null) {
				clientUpdatejsonObject.put("mobileNo",mobileno );
			}
			if(dateofbirth != null) {
				clientUpdatejsonObject.put("dateOfBirth",dateofbirth );			
				clientUpdatejsonObject.put("dateFormat",dateFormat );
			}
			clientUpdatejsonObject.put("locale",locale );
			
			apiRequestBodyAsJson1=clientUpdatejsonObject.toString();
			
			if(!appUserReadPlatformServiceImpl.getCheckAlreadyExistedMobileNoByUserId(userId, mobileno).isEmpty()) {
				 throw new MobileNumberAlreadyExists(mobileno);
			 }
			
			String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + userId + "");
			String customerDetails=null;
			 try
				{
				 	customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
				}
				catch(Exception e){
					final String defaultUserMessage = " failed due to userId "+ userId +" doesn't exist";
					final ApiParameterError error = ApiParameterError.parameterError("error.msg.Update.failed",
							defaultUserMessage, "");
					final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
					dataValidationErrors.add(error);
					throw new PlatformApiDataValidationException(dataValidationErrors);
				}
			//String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
			
			String agentapiRequestBodyAsJson = customerDetails;
			JsonElement element = this.fromApiJsonHelper.parse(agentapiRequestBodyAsJson);
			Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
			String clientUpdate=clientsApiResource.update(clientId, apiRequestBodyAsJson1);
			
			url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerUpdate?userId=" + userId + "");
			String result=HttpConnectionTemplate.restTemplateForPutMethod(url, apiRequestBodyAsJson, HttpMethod.PUT);
			String status="success";
			
			JsonElement appElement = fromApiJsonHelper.extractJsonObjectNamed("changes", fromApiJsonHelper.parse(appuserchanges));
			JsonObject customerJsonObject = fromApiJsonHelper.extractJsonObjectNamed("changes", fromApiJsonHelper.parse(result));
			JsonElement jsonElement = fromApiJsonHelper.parse(result);

			if(fromApiJsonHelper.parameterExists("firstname", appElement)){
			customerJsonObject.addProperty("firstName", fromApiJsonHelper.extractStringNamed("firstname", appElement));
			}
			if(fromApiJsonHelper.parameterExists("lastname", appElement)){
			customerJsonObject.addProperty("lastName", fromApiJsonHelper.extractStringNamed("lastname", appElement));
			}
			
			JsonObject connectJsonObject = gson.fromJson(jsonElement, JsonObject.class);
			connectJsonObject.remove("changes");
			connectJsonObject.add("changes", customerJsonObject);
			connectJsonObject.addProperty("status", status);
			result=connectJsonObject.toString();
			
			return result;
		}
		
		@POST
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		@Path("/customerCreateOtp")
		public String getCustomerCreateOtp(final String apiRequestBodyAsJson) {
			eventRequestService.saveRequest(apiRequestBodyAsJson, null, "customerCreateOTP");
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
			
			//Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(command.longValueOfParameterNamed("clientId"));
			String mobileNumber = command.stringValueOfParameterNamed("mobileNumber");
			this.customerDataValidator.validateForCreateCustomerOtp(mobileNumber);
			String message = SendRandomOtpMessage.getCustomerOtpMessage();
			SmsData smsData = sendRandomOtpMessage.sendAuthorizationMessageWithoutUser(mobileNumber, message);
			SmsSender.sendOtp(message, mobileNumber);
			
			String status="success";
			Gson gson = new Gson();
			JsonObject jsonObject1 = gson.fromJson(this.toApiJsonSerializer.serialize(smsData), JsonObject.class);
			jsonObject1.addProperty("status", status);
			String result=jsonObject1.toString();
	        return result;
	 
		}
		
		@POST
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		@Path("/cashOtp")
		public String getCashOtp(final String apiRequestBodyAsJson) {
			eventRequestService.saveRequest(apiRequestBodyAsJson, null, "cashInOrOutOTP");
			JsonElement parsedCommand = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
	        String mobileNumber = fromApiJsonHelper.extractStringNamed("mobileNumber", parsedCommand);

			this.customerDataValidator.validateForCreateCustomerOtp(mobileNumber);
			String message = SendRandomOtpMessage.getCashOtpMessage();
			SmsData smsData = sendRandomOtpMessage.sendAuthorizationMessageWithoutUser(mobileNumber, message);
			SmsSender.sendOtp(message, mobileNumber);
			
			Gson gson = new Gson();
			JsonObject jsonObject1 = gson.fromJson(this.toApiJsonSerializer.serialize(smsData), JsonObject.class);
			jsonObject1.addProperty("status", "success");
			String result=jsonObject1.toString();
	        return result;
	 
		}
		
		@GET
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		@Path("/customerIdentityVerify")
		public String getCustomerIdVerification(@QueryParam("mobileNumber") final String mobileNumber, @Context UriInfo uriInfo) {
			this.customerDataValidator.validateForMobileNo(mobileNumber);
			String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerRetrieveAll?mobileNumber=" + mobileNumber + "");
			String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
			JsonCommand command2 = getJsonCommand(customerDetails);
			JsonObject jsonElement = (JsonObject)command2.arrayOfParameterNamed("pageItems").get(0);
			
			String result= retrieveCustomer(jsonElement.get("appUserId").getAsLong(), uriInfo);
			String status="success";
			Gson gson = new Gson();
			JsonObject jsonObject1 = gson.fromJson(result, JsonObject.class);
			jsonObject1.addProperty("status", status);
			result=jsonObject1.toString();
			return result;

		}
		
		public JsonCommand getJsonCommand(final String apiRequestBodyAsJson) {
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

			return command;
		}
		
		public void savingsAccountDeposit(String transaction, Long savingsId, Long clientId, Long agentId, UriInfo uriInfo, Integer transactionType, 
				Long toUserId, Long userTypeId, Long toUserTypeId,String json) {
			if(transaction != null) {
				JsonElement transactionJsonCommand = fromApiJsonHelper.parse(transaction);
				JsonObject transactionJsonObject =  new JsonObject();
				
				JsonElement element = this.fromApiJsonHelper.parse(json);
				Gson gson=new Gson();
				JsonObject newelement=gson.fromJson(element, JsonObject.class);
				JsonElement newlocationelement=newelement.get("location");
				
				
				transactionJsonObject.addProperty("savingsId", savingsId);
				transactionJsonObject.addProperty("clientId", clientId);
				transactionJsonObject.addProperty("userId", agentId);
				transactionJsonObject.addProperty("transactionType", transactionType);
				transactionJsonObject.addProperty("locale", "en");
				transactionJsonObject.addProperty("toUserId", toUserId);
				transactionJsonObject.addProperty("userTypeId", userTypeId);
				transactionJsonObject.addProperty("toUserTypeId", toUserTypeId);
				transactionJsonObject.addProperty("transactionId", fromApiJsonHelper.extractLongNamed("resourceId",transactionJsonCommand));
				transactionJsonObject.add("location", newlocationelement);
			
				if(transactionJsonObject !=null) {
					String transactionUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/accountTransaction/createTransaction");
					HttpConnectionTemplate.restTemplate(transactionUrl, transactionJsonObject.toString(), HttpMethod.POST);
				}
			}
		}
		
		public void accountWithdrawal(String transaction, Long savingsId, Long clientId, Long agentId, UriInfo uriInfo, Integer transactionType,
				Long toUserId, Long userTypeId, Long toUserTypeId,String json) {
			if(transaction != null) {
				JsonObject transactionJsonObject =  new JsonObject();
				
				JsonElement element = this.fromApiJsonHelper.parse(json);
				Gson gson=new Gson();
				JsonObject newelement=gson.fromJson(element, JsonObject.class);
				JsonElement newlocationelement=newelement.get("location");
				
				JsonElement transactionJsonCommand = fromApiJsonHelper.parse(transaction);
				transactionJsonObject.addProperty("savingsId", savingsId);
				transactionJsonObject.addProperty("clientId", clientId);
				transactionJsonObject.addProperty("userId", agentId);
				transactionJsonObject.addProperty("transactionType", transactionType);
				transactionJsonObject.addProperty("locale", "en");
				transactionJsonObject.addProperty("transactionId", fromApiJsonHelper.extractLongNamed("resourceId",transactionJsonCommand));
				transactionJsonObject.addProperty("toUserId", toUserId);
				transactionJsonObject.addProperty("userTypeId", userTypeId);
				transactionJsonObject.addProperty("toUserTypeId", toUserTypeId);
				transactionJsonObject.add("location", newlocationelement);			
			
				if(transactionJsonObject !=null) {
					String transactionUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/accountTransaction/createTransaction");
					HttpConnectionTemplate.restTemplate(transactionUrl, transactionJsonObject.toString(), HttpMethod.POST);
				}
			}
		}
		
		@GET
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		@Path("/customerGoal")
		public String getCustomerGoal(@QueryParam("codeValue") final String codeValue, @Context UriInfo uriInfo) {
			Gson gson=new Gson();
			//codeValuesApiResource.retrieveCodeValue(uriInfo, codeValueId);
			String status="success";
			JsonObject jsonObject =new JsonObject();
			JsonElement jsonElement=gson.fromJson(this.toApiJsonSerializer.serialize(codeValueReadPlatformService.retrieveCodeValuesByCode("Customer Goal")), JsonElement.class);
			jsonObject.add("customerGoal",jsonElement);
	        jsonObject.addProperty("status", status);
			String customerGoalDetails = jsonObject.toString();
			return customerGoalDetails;

		}
		
		@POST
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		@Path("/rating")
		public String ratingCreate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
			eventRequestService.saveRequest(apiRequestBodyAsJson, null, "rating");
			String result = null;
			String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/rating/create");
			result =  HttpConnectionTemplate.restTemplate(url, apiRequestBodyAsJson, HttpMethod.POST);
			
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
			jsonObject.addProperty("status", "success");
			return jsonObject.toString();
		}
		
		@GET
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		@Path("/code")
		public String getFeedback(@QueryParam("codeValue") final String codeValue, @Context UriInfo uriInfo) {
			Gson gson=new Gson();
			
			JsonObject jsonObject =new JsonObject();
			JsonElement jsonElement=gson.fromJson(this.toApiJsonSerializer.serialize(codeValueReadPlatformService.retrieveCodeValuesByCode(codeValue)), JsonElement.class);
			jsonObject.add("codes",jsonElement);
	        jsonObject.addProperty("status", "success");
			
			return jsonObject.toString();

		}
		
		
		
		
		
}