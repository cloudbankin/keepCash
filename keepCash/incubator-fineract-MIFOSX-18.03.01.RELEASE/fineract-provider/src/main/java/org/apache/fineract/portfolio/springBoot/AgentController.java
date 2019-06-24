package org.apache.fineract.portfolio.springBoot;

import java.io.InputStream;
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
import javax.ws.rs.HeaderParam;
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
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.api.DocumentManagementApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.ClientIdentifiersApiResource;
import org.apache.fineract.portfolio.client.api.ClientsApiResource;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.api.SavingsAccountTransactionsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.portfolio.springBoot.data.AgentDataValidator;
import org.apache.fineract.portfolio.springBoot.service.SendRandomOtpMessage;
import org.apache.fineract.portfolio.springBoot.service.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/agent")
@Component
@Scope("singleton")
public class AgentController {

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
	private final AgentDataValidator agentDataValidator;
	private final SendRandomOtpMessage sendRandomOtpMessage; 
	private final ClientRepositoryWrapper clientRepositoryWrapper;
	private final ToApiJsonSerializer<SmsData> toApiJsonSerializer;

	@Autowired
	public AgentController(final PlatformSecurityContext context, final FromJsonHelper fromApiJsonHelper,
			final ClientsApiResource clientsApiResource, final SavingsAccountsApiResource savingsAccountsApiResource,
			final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource,
			final SavingsAccountRepository savingsAccountRepository, final LoanRepository loanRepository,
			final LoanTransactionsApiResource loanTransactionsApiResource,
			final DocumentManagementApiResource documentManagementApiResource,
			final ClientIdentifiersApiResource clientIdentifiersApiResource,
			final CodeValueRepositoryWrapper codeValueRepository,
			final SavingsProductReadPlatformService savingProductReadPlatformService,
			final CodeValueReadPlatformService codeValueReadPlatformService,
			final AgentDataValidator agentDataValidator, final SendRandomOtpMessage sendRandomOtpMessage, final ClientRepositoryWrapper clientRepositoryWrapper,
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
		this.agentDataValidator = agentDataValidator;
		this.sendRandomOtpMessage = sendRandomOtpMessage;
		this.clientRepositoryWrapper = clientRepositoryWrapper;
		this.toApiJsonSerializer = toApiJsonSerializer;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentCreate")
	public String AgentCreate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, Long userId) {
		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentCreate?userId=" + userId + "");
		return HttpConnectionTemplate.restTemplate(url, apiRequestBodyAsJson, HttpMethod.POST);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/{userId}")
	public String RetrieveAgent(@PathParam("userId") final Long userId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser();

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + userId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		String apiRequestBodyAsJson = agentDetails;
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

			String updateURL = HttpConnectionTemplate.createBootUrl(uriInfo,
					"/agentUser/updateClientInAgent?userId=" + userId + "");
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

		JsonObject jsonObject = new JsonParser().parse(agentDetails).getAsJsonObject();
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
		
		agentDetails = jsonObject.toString();

		return agentDetails;
	}

	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/documentUpload")
	public String createDocument(@QueryParam("entityType") String entityType, @QueryParam("entityId") Long entityId,
			@HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
			@FormDataParam("file") final FormDataContentDisposition fileDetails,
			@FormDataParam("file") final FormDataBodyPart bodyPart, @FormDataParam("name") final String name,
			@FormDataParam("description") final String description, @Context final UriInfo uriInfo,
			@FormDataParam("companyName") final String companyName,
			@FormDataParam("comapanyAddress") final String companyAddress) {

		this.context.authenticatedUser();

		// Validate name paramater
		checkFileExtension(fileDetails.getFileName());
		this.agentDataValidator.validateForCreateAgentDocument(name);

		// Update company details to the agent
		JsonObject json = new JsonObject();
		JsonParser parser = new JsonParser();
		json.add("companyName", parser.parse(companyName));
		json.add("comapanyAddress", parser.parse(companyAddress));
		String updateURL = HttpConnectionTemplate.createBootUrl(uriInfo,
				"/agentUser/updateCompanyInAgent?userId=" + entityId + "");
		HttpConnectionTemplate.restTemplate(updateURL, json.toString(), HttpMethod.POST);
		// chenges end

		String getURL = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + entityId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(getURL, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);

		entityType = "clients";
		entityId = clientId;

		this.documentManagementApiResource.createDocument(entityType, entityId, fileSize, inputStream, fileDetails,
				bodyPart, name, description);
		String clientDocuments = this.documentManagementApiResource.retreiveAllDocuments(uriInfo, "clients", clientId);
		return clientDocuments;

	}

	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/identifierUpload")
	public String createIdentifier(@QueryParam("entityType") String entityType, @QueryParam("entityId") Long entityId,
			@HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
			@FormDataParam("file") final FormDataContentDisposition fileDetails,
			@FormDataParam("file") final FormDataBodyPart bodyPart,
			@FormDataParam("identifierType") final String identifierType,
			@FormDataParam("identifierId") final String identifierId, @Context final UriInfo uriInfo) {

		// validate identifier
		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		try {
			Long.parseLong(identifierType);
		} catch (Exception e) {
			throw new PlatformApiDataValidationException("identifierType value Must be an integer",
					"identifier.type.must.be.integer.value", dataValidationErrors);
		}

		checkFileExtension(fileDetails.getFileName());

		this.agentDataValidator.validateForCreateAgentIdentifier(Long.parseLong(identifierType), identifierId);

		String getURL = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + entityId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(getURL, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		JsonObject json = new JsonObject();
		final CodeValue documentType = this.codeValueRepository
				.findOneWithNotFoundDetection(Long.parseLong(identifierType));

		json.addProperty("documentTypeId", identifierType);
		json.addProperty("status", "Active");
		json.addProperty("documentKey", identifierId);

		String clientIdentifier = this.clientIdentifiersApiResource.createClientIdentifier(clientId, json.toString());
		JsonElement clientIdentifierElement = this.fromApiJsonHelper.parse(clientIdentifier);
		Long clientIdentifierId = this.fromApiJsonHelper.extractLongNamed("resourceId", clientIdentifierElement);

		entityType = "client_identifiers";
		entityId = clientIdentifierId;
		this.documentManagementApiResource.createDocument(entityType, entityId, fileSize, inputStream, fileDetails,
				bodyPart, documentType.label(), null);

		String clientIdentifiers = this.clientIdentifiersApiResource.retrieveAllClientIdentifiers(uriInfo, clientId);

		JsonParser jsonParser = new JsonParser();
		JsonArray clientIdentifiersArray = (JsonArray) jsonParser.parse(clientIdentifiers);

		JsonArray result = new JsonArray();
		for (int i = 0; i < clientIdentifiersArray.size(); i++) {
			final JsonObject jsonObject = clientIdentifiersArray.get(i).getAsJsonObject();

			JsonElement identifierElement = this.fromApiJsonHelper.parse(jsonObject.toString());
			Long id = this.fromApiJsonHelper.extractLongNamed("id", identifierElement);

			String clientIdentifierDocuments = this.documentManagementApiResource.retreiveAllDocuments(uriInfo,
					"client_identifiers", id);
			jsonObject.add("documents", this.fromApiJsonHelper.parse(clientIdentifierDocuments));
			result.add(this.fromApiJsonHelper.parse(jsonObject.toString()));
		}
		return result.toString();

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentDetailsCreate")
	public String createAgentDetailsByUser(final String apiRequestBodyAsJson, @Context UriInfo uriInfo) {

		JsonCommand command = getJsonCommand(apiRequestBodyAsJson);

		String agentDetails = command.arrayOfParameterNamed("agentDeatils").get(0).toString();

		return clientsApiResource.create(agentDetails);

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/deposit")
	public String depositAgentSavingsAccount(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,
			@QueryParam("agentId") final Long agentId) {

		// validation
		this.agentDataValidator.validateForDepositTransaction(apiRequestBodyAsJson);

		JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);

		String transactionDate = this.fromApiJsonHelper.extractStringNamed("transactionDate",
				apiRequestBodyAsJsonElement);
//		if(transactionDate == null || transactionDate == "") {
//			SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
//			transactionDate = format.format(new Date());
//		}

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
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

			String createAgentSavingsurl = HttpConnectionTemplate.createBootUrl(uriInfo,
					"/agentSavingsAccount/createAgentSavingsAccount?userId=" + agentId + "");
			HttpConnectionTemplate.restTemplate(createAgentSavingsurl, apiRequestBodyAsJson, HttpMethod.POST);

			final Collection<SavingsProductData> products = this.savingProductReadPlatformService.retrieveAll();

			SavingsProductData savingsProduct = null;
			for (SavingsProductData product : products) {
				savingsProduct = product;
				break;
			}

			JsonObject createSavingsAccountJson = new JsonObject();
			if (clientId != null && savingsProduct != null) {
				createSavingsAccountJson.addProperty("productId", savingsProduct.getId());
				createSavingsAccountJson.addProperty("nominalAnnualInterestRate",
						savingsProduct.getNominalAnnualInterestRate());
				createSavingsAccountJson.addProperty("withdrawalFeeForTransfers",
						savingsProduct.isWithdrawalFeeForTransfers());
				createSavingsAccountJson.addProperty("allowOverdraft", savingsProduct.isAllowOverdraft());
				createSavingsAccountJson.addProperty("enforceMinRequiredBalance",
						savingsProduct.isEnforceMinRequiredBalance());
				createSavingsAccountJson.addProperty("withHoldTax", savingsProduct.isWithHoldTax());
				createSavingsAccountJson.addProperty("interestCompoundingPeriodType",
						savingsProduct.getInterestCompoundingPeriodType().getId());
				createSavingsAccountJson.addProperty("interestPostingPeriodType",
						savingsProduct.getInterestPostingPeriodType().getId());
				createSavingsAccountJson.addProperty("interestCalculationType",
						savingsProduct.getInterestCalculationType().getId());
				createSavingsAccountJson.addProperty("interestCalculationDaysInYearType",
						savingsProduct.getInterestCalculationDaysInYearType().getId());
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

			String result = null;
			try {
				result = this.savingsAccountsApiResource.submitApplication(createSavingsAccountJson.toString());
				// Approve and activate savings account
				result = approveAndActivateAgentSavingsAccount(transactionDate, uriInfo, agentId);
				result = depositSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentId);
			} catch (Exception e) {
				final String defaultUserMessage = "Transaction failed";
				final ApiParameterError error = ApiParameterError.parameterError("error.msg.transaction.failed",
						defaultUserMessage, "");
				final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
				dataValidationErrors.add(error);
				throw new PlatformApiDataValidationException(dataValidationErrors);
			}
		} else {
			try {
				/** Savings account deposit transaction */
				depositSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentId);
			} catch (Exception e) {
				final String defaultUserMessage = "Transaction failed";
				final ApiParameterError error = ApiParameterError.parameterError("error.msg.transaction.failed",
						defaultUserMessage, "");
				final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
				dataValidationErrors.add(error);
				throw new PlatformApiDataValidationException(dataValidationErrors);
			}
		}

		JsonObject successJson = new JsonObject();
		successJson.add("developerMessage", this.fromApiJsonHelper.parse("\""+"Transaction Done Successfully"+"\""));
		successJson.add("defaultUserMessage", this.fromApiJsonHelper.parse("\""+"Transaction Done Successfully"+"\""));
		successJson.add("userMessageGlobalisationCode", this.fromApiJsonHelper.parse("\""+"transaction.done.successfully"+"\""));
		
		return successJson.toString();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/approveAndActivate")
	public String approveAndActivateAgentSavingsAccount(final String transactionDate, @Context final UriInfo uriInfo,
			@QueryParam("agentId") final Long agentId) {

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
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
	@Path("/depositAmount")
	public String depositSavingsAccountInAgent(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			@QueryParam("agentId") final Long agentId) {

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);

		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		SavingsAccount savingsAccount = null;
		for (SavingsAccount account : savingsAccounts) {
			savingsAccount = account;
		}

		List<Loan> loans = this.loanRepository.findLoanByClientId(clientId);
		BigDecimal totalLoanOutstandingAmount = BigDecimal.ZERO;
		for (Loan loan : loans) {
			totalLoanOutstandingAmount = totalLoanOutstandingAmount.add(loan.getSummary().getTotalOutstanding());
		}

		JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount",
				apiRequestBodyAsJsonElement, Locale.ENGLISH);
		String transactionDate = this.fromApiJsonHelper.extractStringNamed("transactionDate",
				apiRequestBodyAsJsonElement);
//		if(transactionDate == null || transactionDate == "") {
//			SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
//			transactionDate = format.format(new Date());
//		}
//		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
//		transactionDate = formatter.format(new LocalDate(transactionDate).toDate()); 

		if (savingsAccount != null && transactionAmount.subtract(totalLoanOutstandingAmount).doubleValue() > 0) {
			JsonObject createSavingsAccountJson = new JsonObject();
			createSavingsAccountJson.addProperty("transactionDate", transactionDate);
			createSavingsAccountJson.addProperty("transactionAmount", transactionAmount
					.subtract(totalLoanOutstandingAmount).setScale(2, RoundingMode.HALF_EVEN).toString());
			createSavingsAccountJson.addProperty("locale", "en");
			createSavingsAccountJson.addProperty("dateFormat", "dd MMMM yyyy");
			this.savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "deposit",
					createSavingsAccountJson.toString());
		} else {
			return "Transaction failed";
		}

		return "Transaction done successfully";
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

	/** Check file extensions */
	public void checkFileExtension(final String fileName) {

		final String defaultUserMessage = "Invalid File Format";
		
		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
			if (fileExtension.equalsIgnoreCase("gif") || fileExtension.equalsIgnoreCase("doc")
					|| fileExtension.equalsIgnoreCase("docx") || fileExtension.equalsIgnoreCase("jpeg")
					|| fileExtension.equalsIgnoreCase("pdf") || fileExtension.equalsIgnoreCase("png")
					|| fileExtension.equalsIgnoreCase("jpg")) {

			} else {
				final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.format", defaultUserMessage,
						"",fileExtension,"");
				dataValidationErrors.add(error);
				throw new PlatformApiDataValidationException(dataValidationErrors);
			}
		} else {
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.format", defaultUserMessage,
					"");
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/delegateCreate")
	public String delegateCreate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, Long userId,  final Long parentUserId) {
		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/delegateUser/delegateCreate?userId=" + userId +"&parentUserId="+parentUserId);
		return HttpConnectionTemplate.restTemplate(url, apiRequestBodyAsJson, HttpMethod.POST);
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/delegateOtp")
	public String getDelegateOtp(final String apiRequestBodyAsJson) {
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
	@Path("/otpVerification")
	public boolean otpVerification(final String apiRequestBodyAsJson) {
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
				
		Long smsId = command.longValueOfParameterNamed("smsId");
		String token = command.stringValueOfParameterNamed("token");

		
		return sendRandomOtpMessage.otpSmsVerification(smsId, token);
		
        //return this.toApiJsonSerializer.serialize(smsData);
		
 
	}

	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/delegate/{parentId}")
	public String retrieveAgentByParentUserId(@PathParam("parentId") final Long parentId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser();

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/parentUser?userId=" + parentId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		
		return agentDetails;
	}
	//to update changes in agent details
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentUpdate/{agentId}")
	public String CustomerUpdate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,@PathParam("agentId") Long userId) {
		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentUpdate?userId=" + userId + "");
		return HttpConnectionTemplate.restTemplateForPutMethod(url, apiRequestBodyAsJson, HttpMethod.PUT);
	}
}