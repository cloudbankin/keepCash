package org.apache.fineract.portfolio.springBoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.api.DocumentManagementApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.portfolio.client.api.ClientIdentifiersApiResource;
import org.apache.fineract.portfolio.client.api.ClientsApiResource;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientChargeNotFoundException;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.products.exception.ResourceNotFoundException;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.api.SavingsAccountTransactionsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.exception.SavingAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.portfolio.springBoot.data.AgentDataValidator;
import org.apache.fineract.portfolio.springBoot.data.CustomerDataValidator;
import org.apache.fineract.portfolio.springBoot.service.SendRandomOtpMessage;
import org.apache.tika.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpMethod;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

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
			final ToApiJsonSerializer<SmsData> toApiJsonSerializer) {

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
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/customerCreate")
	public String CustomerCreate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, Long userId, Long agentId) {
		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerCreate/"+ agentId +"?userId=" + userId + "");
		return HttpConnectionTemplate.restTemplate(url, apiRequestBodyAsJson, HttpMethod.POST);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/{userId}")
	public String RetrieveAgent(@PathParam("userId") final Long userId, @Context final UriInfo uriInfo) {

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
			jsonObject.addProperty("fullname", name.toString());
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
			String currentDate = sdf.format(new Date());
			jsonObject.addProperty("submittedOnDate", currentDate);
			jsonObject.addProperty("activationDate", currentDate);
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
		
		customerDetails = jsonObject.toString();

		return customerDetails;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/cashIn/{agentUserId}/{custUserId}")
	@Transactional(rollbackFor = Exception.class)
	public String depositAgentSavingsAccount(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,
			@PathParam("agentUserId") final Long agentUserId, @PathParam("custUserId") final Long custUserId) {
		String result = null;
		// validation
		this.customerDataValidator.validateForDepositTransaction(apiRequestBodyAsJson);

		JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		String transactionDate = this.fromApiJsonHelper.extractStringNamed("transactionDate", apiRequestBodyAsJsonElement);

		String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerretrieve?custUserId=" + custUserId + "");
		String customerDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		JsonElement element = this.fromApiJsonHelper.parse(customerDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);

		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		/**
		 * Create new savings account if the client does not have any savings account
		 */
		if (savingsAccount == null) {
			final Collection<SavingsProductData> products = this.savingProductReadPlatformService.retrieveAll();
			SavingsProductData savingsProduct = null;
			for (SavingsProductData product : products) {
				savingsProduct = product;
				break;
			}

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
				result = depositSavingsAccountInCustomer(uriInfo, apiRequestBodyAsJson, custUserId);
				
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
				result = depositSavingsAccountInCustomer(uriInfo, apiRequestBodyAsJson, custUserId);
				
			}catch (ObjectOptimisticLockingFailureException lockingFailureException) {
	            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
	                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
	        } catch (CannotAcquireLockException cannotAcquireLockException) {
	            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
	                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
	        }
		}

		/*JsonObject successJson = new JsonObject();
		successJson.add("developerMessage", this.fromApiJsonHelper.parse("\""+"Transaction Done Successfully"+"\""));
		successJson.add("defaultUserMessage", this.fromApiJsonHelper.parse("\""+"Transaction Done Successfully"+"\""));
		successJson.add("userMessageGlobalisationCode", this.fromApiJsonHelper.parse("\""+"transaction.done.successfully"+"\""));*/
		
		return result;
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

		String result = null;
		if (savingsAccount != null) {
			result =  savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "deposit",
					apiRequestBodyAsJson);
		} else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		return result;
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
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		String result = null;
		if (savingsAccount != null) {
			result = savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "withdrawal", apiRequestBodyAsJson);
			
			String cashurl = HttpConnectionTemplate.createBootUrlInCustomer
					(uriInfo, "/createCashInCashOut/createCashInCashOutTransaction?custUserId=" + custUserId + "&agentUserId="+agentUserId);
			HttpConnectionTemplate.restTemplate(cashurl, apiRequestBodyAsJson, HttpMethod.POST);
		}else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		
		return result;
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/cashOut/{agentUserId}/{custUserId}")
	@Transactional(rollbackFor = Exception.class)
	public String cashOutFromSavingAccountInCustomer(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, 
			@PathParam("agentUserId") final Long agentUserId,
			@PathParam("custUserId") final Long custUserId) {
		String cashOutDetails = null;
		
		try {
			withdrawalSavingsAccountInCustomer(uriInfo, apiRequestBodyAsJson, custUserId);
			cashOutDetails = depositSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentUserId);
			
			String cashurl = HttpConnectionTemplate.createBootUrlInCustomer
					(uriInfo, "/createCashInCashOut/createCashInCashOutTransaction?custUserId=" + custUserId + "&agentUserId="+agentUserId);
			HttpConnectionTemplate.restTemplate(cashurl, apiRequestBodyAsJson, HttpMethod.POST);
			
		}  catch (ObjectOptimisticLockingFailureException lockingFailureException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
        } catch (CannotAcquireLockException cannotAcquireLockException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
        }
		
		return cashOutDetails;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/customerWithdrawalAmount")
	public String withdrawalSavingsAccountInCustomer(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
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

		String result = null;
		if (savingsAccount != null) {
			 savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "withdrawal", apiRequestBodyAsJson);
		} else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		return result;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentDepositAmount")
	public String depositSavingsAccountInAgent(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			@QueryParam("agentUserId") final Long agentUserId) {

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		String result = null;
		if (savingsAccount != null) {
			result = savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "deposit", apiRequestBodyAsJson);
		}else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		
		return result;
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
			String url = HttpConnectionTemplate.createBootUrlInCustomer(uriInfo, "/customerUser/customerUpdate?userId=" + userId + "");
			return HttpConnectionTemplate.restTemplateForPutMethod(url, apiRequestBodyAsJson, HttpMethod.PUT);
		}
		
		@POST
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		@Path("/customerCreateOtp")
		public String getCustomerCreateOtp(final String apiRequestBodyAsJson) {
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
			
			Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(command.longValueOfParameterNamed("clientId"));
			String mobileNumber = command.stringValueOfParameterNamed("mobileNumber");
			String message = SendRandomOtpMessage.getDelegateOtpMessage(client);
			SmsData smsData = sendRandomOtpMessage.sendAuthorizationMessage(client, mobileNumber, message);
			//SmsSender.sendOtp(message, mobileNumber);
			
	        return this.toApiJsonSerializer.serialize(smsData);
	 
		}
		
		@POST
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		@Path("/cashOtp")
		public String getCashOtp(final String apiRequestBodyAsJson) {
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
			
			Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(command.longValueOfParameterNamed("clientId"));
			String mobileNumber = command.stringValueOfParameterNamed("mobileNumber");
			String message = SendRandomOtpMessage.getDelegateOtpMessage(client);
			SmsData smsData = sendRandomOtpMessage.sendAuthorizationMessage(client, mobileNumber, message);
			//SmsSender.sendOtp(message, mobileNumber);
			
	        return this.toApiJsonSerializer.serialize(smsData);
	 
		}
}