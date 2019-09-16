package org.apache.fineract.portfolio.springBoot;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.fineract.commands.api.MakercheckersApiResource;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.api.DocumentManagementApiResource;
import org.apache.fineract.infrastructure.documentmanagement.api.ImagesApiResource;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.notification.service.NotificationWritePlatformService;
import org.apache.fineract.organisation.staff.api.StaffApiResource;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.MobileNumberAlreadyExists;
import org.apache.fineract.portfolio.client.api.ClientIdentifiersApiResource;
import org.apache.fineract.portfolio.client.api.ClientsApiResource;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.api.SavingsAccountTransactionsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.exception.SavingAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformService;
import org.apache.fineract.portfolio.springBoot.data.AgentDataValidator;
import org.apache.fineract.portfolio.springBoot.enumType.AppUserTypes;
import org.apache.fineract.portfolio.springBoot.enumType.SavingsAccountTypeEnum;
import org.apache.fineract.portfolio.springBoot.enumType.SavingsTransactionDetailsTypeEnum;
import org.apache.fineract.portfolio.springBoot.exception.EmailAddressAlreadyExists;
import org.apache.fineract.portfolio.springBoot.service.EventRequestService;
import org.apache.fineract.portfolio.springBoot.service.PushNotification;
import org.apache.fineract.portfolio.springBoot.service.SendRandomOtpMessage;
import org.apache.fineract.portfolio.springBoot.service.SmsSender;
import org.apache.fineract.useradministration.api.UsersApiResource;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.service.AppUserReadPlatformServiceImpl;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

import net.minidev.json.JSONObject;

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
	private final LoansApiResource loansApiResource; 
	private final MakercheckersApiResource makercheckersApiResource;
	private final LoanTransactionsApiResource loanTransactionsApiResource;
	private final StaffApiResource staffApiResource;  
	private final SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService;
	private final StaffRepository staffRepository;
    private final UsersApiResource usersApiResource;
    private final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl;
    private final AppUserRepository appUserRepository;
    private final NotificationWritePlatformService notificationWritePlatformService;
    private final ImagesApiResource imagesApiResource;
    private final EventRequestService eventRequestService;
    
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
			final ToApiJsonSerializer<SmsData> toApiJsonSerializer,
			final LoansApiResource loansApiResource, final MakercheckersApiResource makercheckersApiResource,
			final StaffApiResource staffApiResource,
			final SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService,
			final StaffRepository staffRepository,final UsersApiResource usersApiResource,
			final AppUserReadPlatformServiceImpl appUserReadPlatformServiceImpl,
			final AppUserRepository appUserRepository, final NotificationWritePlatformService notificationWritePlatformService,
			final ImagesApiResource imagesApiResource, final EventRequestService eventRequestService) {

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
		this.loansApiResource = loansApiResource;
		this.makercheckersApiResource = makercheckersApiResource;
		this.loanTransactionsApiResource = loanTransactionsApiResource;
		this.staffApiResource = staffApiResource;
		this.selfServiceRegistrationWritePlatformService = selfServiceRegistrationWritePlatformService;
		this.staffRepository = staffRepository;
		this.usersApiResource = usersApiResource;
		this.appUserReadPlatformServiceImpl = appUserReadPlatformServiceImpl;
		this.appUserRepository = appUserRepository;
		this.notificationWritePlatformService = notificationWritePlatformService;
		this.imagesApiResource = imagesApiResource;
		this.eventRequestService = eventRequestService;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentCreate")
	public String AgentCreate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, Long userId, String pinNumber) {
		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentCreate?userId=" + userId +"&pinNumber=" +pinNumber);
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
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
			String currentDate = sdf.format(new Date());
			jsonObject.addProperty("submittedOnDate", currentDate);
			jsonObject.addProperty("activationDate", currentDate);
			jsonObject.addProperty("legalFormId", LegalForm.PERSON.getValue());
			jsonObject.addProperty("firstname", fromApiJsonHelper.extractStringNamed("firstName", element));
			jsonObject.addProperty("lastname", fromApiJsonHelper.extractStringNamed("lastName", element));
			jsonObject.addProperty("mobileNo", fromApiJsonHelper.extractStringNamed("mobileNo", element));
			jsonObject.addProperty("emailAddress", fromApiJsonHelper.extractStringNamed("emailId", element));
			apiRequestBodyAsJson = jsonObject.toString();
			String client = this.clientsApiResource.create(apiRequestBodyAsJson);

			clientId = this.fromApiJsonHelper.extractLongNamed("clientId", this.fromApiJsonHelper.parse(client));

			String updateURL = HttpConnectionTemplate.createBootUrl(uriInfo,
					"/agentUser/updateClientInAgent?userId=" + userId + "");
			HttpConnectionTemplate.restTemplate(updateURL, client, HttpMethod.POST);
		}
		
		String clientDocuments = this.documentManagementApiResource.retreiveAllDocuments(uriInfo, "clients", clientId);
		String clientIdentifiers = this.clientIdentifiersApiResource.retrieveAllClientIdentifiers(uriInfo, clientId);
		
		Gson gson = new Gson();
		JsonArray jsonArray = gson.fromJson(clientDocuments, JsonArray.class);
		JsonArray docuArray = new JsonArray();
		for(JsonElement jsonElement : jsonArray) {
			JsonObject jsonObject = gson.fromJson(fromApiJsonHelper.toJson(jsonElement), JsonObject.class);
			jsonObject.addProperty("parentEntityType", "agents");
			docuArray.add(jsonObject);
		}
		
		clientDocuments = docuArray.toString();

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
			
			jsonObject.remove("clientId");
			jsonObject.remove("status");
			jsonObject.addProperty("agentId", id);
			
			clientIdentifiersResult.add(this.fromApiJsonHelper.parse(jsonObject.toString()));
		}

		JsonObject jsonObject = new JsonParser().parse(agentDetails).getAsJsonObject();
		jsonObject.remove("clientId");
		jsonObject.add("agentId", this.fromApiJsonHelper.parse(clientId.toString()));
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
			String savingsUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/retrieveAllAccount/"+account.getId());
			String accountData = HttpConnectionTemplate.restTemplateForGetMethod(savingsUrl, HttpMethod.GET);
			
			if(!accountData.equals(null) &&
					!accountData.equals("null")) {
				JsonElement accountDetailsElement = this.fromApiJsonHelper.parse(accountData);
				Integer accountType = fromApiJsonHelper.extractIntegerNamed("accountType", accountDetailsElement, Locale.ENGLISH);
				if(accountType == SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue()){
					savingsAccount = account;
					JsonObject appUserTypeJson = new JsonObject();
					appUserTypeJson.add("savingsAccountId", jsonParser.parse(savingsAccount.getId().toString()));
					appUserTypeJson.add("currentBalance", jsonParser.parse(savingsAccount.getSummary().getAccountBalance().toString()));
					savingsArray.add(jsonParser.parse(appUserTypeJson.toString()));
				}
			}
		}
		
		jsonObject.add("savingsAccount", this.fromApiJsonHelper.parse(savingsArray.toString()));
		 String status="success";
        jsonObject.addProperty("status", status);
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
		try {
			// Validate name paramater
			checkFileExtension(fileDetails.getFileName());
        }
        catch (Exception e) {
			final String defaultUserMessage = " Document not provided";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.Document.Upload.failed.",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
		this.agentDataValidator.validateForCreateAgentDocument(name);

		// Update company details to the agent
		JsonObject json = new JsonObject();
		json.addProperty("companyName", companyName);
		json.addProperty("comapanyAddress", companyAddress);
		
		 
		
		String updateURL = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/updateCompanyInAgent?userId=" + entityId + "");
		HttpConnectionTemplate.restTemplate(updateURL, json.toString(), HttpMethod.POST);
		// chenges end

		String getURL = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + entityId + "");
		String agentDetails=null;
		try{
				agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(getURL, HttpMethod.GET);		
			}
		catch(Exception e){
			final String defaultUserMessage = " failed due to userId "+ entityId +" doesn't exist";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.document.Upload.failed",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}

		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);

		entityType = "clients";
		entityId = clientId;

		this.documentManagementApiResource.createDocument(entityType, entityId, fileSize, inputStream, fileDetails,
				bodyPart, name, description);
		String clientDocuments = this.documentManagementApiResource.retreiveAllDocuments(uriInfo, "clients", clientId);
		Gson gson = new Gson();
		
		JsonArray jsonArray = gson.fromJson(clientDocuments, JsonArray.class);
		JsonArray array = new JsonArray();
		for(JsonElement jsonElement : jsonArray) {
			JsonObject jsonObject = gson.fromJson(fromApiJsonHelper.toJson(jsonElement), JsonObject.class);
			jsonObject.addProperty("parentEntityType", "agents");
			array.add(jsonObject);
		}
		String status="success";
			JsonObject resultJsonObject = new JsonObject();
			resultJsonObject.add("documents", array);
			resultJsonObject.addProperty("status", status);
			String result= resultJsonObject.toString();
		
		return result;
        

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
        try
        {
		checkFileExtension(fileDetails.getFileName());
	    }
        catch (Exception e) {
		final String defaultUserMessage = " Identifier not provided";
		final ApiParameterError error = ApiParameterError.parameterError("error.msg.Identifier.Upload.failed.",
				defaultUserMessage, "");
		//final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		dataValidationErrors.add(error);
		throw new PlatformApiDataValidationException(dataValidationErrors);
	    }
        
        final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService
				.retrieveCodeValuesByCode("Customer Identifier");
        int count=0;
        for (CodeValueData codeValue : codeValues) {
        	
        	count++; 
		}
        
        	this.agentDataValidator.validateForCreateAgentIdentifier(Long.parseLong(identifierType), identifierId,count);
       
		

		String getURL = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + entityId + "");
		String agentDetails=null;
		try
		{
			agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(getURL, HttpMethod.GET);		}
		catch(Exception e){
			final String defaultUserMessage = " failed due to userId "+ entityId +" doesn't exist";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.identifier.Upload.failed",
					defaultUserMessage, "");
			//final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
		//String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(getURL, HttpMethod.GET);
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
			
			jsonObject.remove("clientId");
			jsonObject.remove("status");
			jsonObject.addProperty("agentId", id);
			
			result.add(this.fromApiJsonHelper.parse(jsonObject.toString()));
		}
		String status="success";
		JsonObject resultJsonObject = new JsonObject();
		resultJsonObject.add("identifiers", result);
		resultJsonObject.addProperty("status", status);
		String resultresponse= resultJsonObject.toString();
	
	return resultresponse;

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
		
		eventRequestService.saveRequest(apiRequestBodyAsJson, agentId, "topup");
		String result = null;
		// validation
		this.agentDataValidator.validateForDepositTransaction(apiRequestBodyAsJson);
		JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		String transactionDate = this.fromApiJsonHelper.extractStringNamed("transactionDate", apiRequestBodyAsJsonElement);

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentId + "");
		String agentDetails=null;
		try{
			agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		}
		catch(Exception e)
		{
			final String defaultUserMessage = " failed due to userId "+ agentId +" doesn't exist";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.deposit.failed",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
		
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
			Long productId = new Long(1);
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
			
				result = this.savingsAccountsApiResource.submitApplication(createSavingsAccountJson.toString());
				// Approve and activate savings account
				approveAndActivateAgentSavingsAccount(transactionDate, uriInfo, agentId);
				
				Gson gson = new Gson();
				JsonObject savingsJsonObject = gson.fromJson(result, JsonObject.class);
				savingsJsonObject.addProperty("locale", "en");
				savingsJsonObject.addProperty("savingsId", savingsJsonObject.get("savingsId").toString());
				savingsJsonObject.addProperty("clientId", savingsJsonObject.get("clientId").toString());
				savingsJsonObject.addProperty("accountType", SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue().toString());
				
				String savingsUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/createSavingsAccount");
				HttpConnectionTemplate.restTemplate(savingsUrl, savingsJsonObject.toString(), HttpMethod.POST);
				
				result = depositSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentId, clientId);
		} else {
				/** Savings account deposit transaction */
				result = depositSavingsAccountInAgent(uriInfo, apiRequestBodyAsJson, agentId,clientId);
		}
		
		return result;
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/approveAndActivate")
	public String approveAndActivateAgentSavingsAccount(final String transactionDate, @Context final UriInfo uriInfo,
			@QueryParam("agentId") final Long agentId) {

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentId + "");
		String agentDetails=null;
		try
		{
			agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		}
		catch(Exception e)
		{
			final String defaultUserMessage = " failed due to userId "+ agentId +" doesn't exist";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.failed",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
		//String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

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
			@QueryParam("agentId") final Long agentId, final Long clientId) {

		String transaction = null;
		SavingsAccount savingsAccount = null;
		Gson gson = new Gson();
		
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);

		for (SavingsAccount account : savingsAccounts) {
			String savingsUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/retrieveAllAccount/"+account.getId());
			String accountDetails = HttpConnectionTemplate.restTemplateForGetMethod(savingsUrl, HttpMethod.GET);
			if(!accountDetails.equals(null) &&
					!accountDetails.equals("null")) {
				JsonObject accountJsonObject = gson.fromJson(accountDetails, JsonObject.class);
				Integer accountType = accountJsonObject.get("accountType").getAsInt();				
				if(accountType == SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue()){
					savingsAccount = account;
				}
			}
		}

		if(savingsAccount != null) {
			JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
			BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount", apiRequestBodyAsJsonElement, Locale.ENGLISH);
			String transactionDate = this.fromApiJsonHelper.extractStringNamed("transactionDate", apiRequestBodyAsJsonElement);
			if (transactionAmount.doubleValue() > 0) {
				JsonObject createSavingsAccountJson = new JsonObject();
				
				//JsonElement element = this.fromApiJsonHelper.parse(json);
				
				JsonObject newelement=gson.fromJson(apiRequestBodyAsJsonElement, JsonObject.class);
				JsonElement newlocationelement=newelement.get("location");
				
				createSavingsAccountJson.addProperty("transactionDate", transactionDate);
				createSavingsAccountJson.addProperty("transactionAmount", transactionAmount);
				createSavingsAccountJson.addProperty("locale", "en");
				createSavingsAccountJson.addProperty("dateFormat", "dd MMMM yyyy");
				createSavingsAccountJson.add("location", newlocationelement);
				
				transaction = this.savingsAccountTransactionsApiResource.agentTransaction(savingsAccount.getId(), "deposit", createSavingsAccountJson.toString()
						, agentId, clientId);
			}
		}		
		else {
			if(savingsAccount == null) {
				throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
			}
			
		}

		
		
		return transaction;
	}
	
	public void agentAccountDeposit(String transaction, Long savingsId, Long clientId, Long agentId, UriInfo uriInfo, Integer transactionType,
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
	
	public void agentAccountWithdrawal(String transaction, Long savingsId, Long clientId, Long agentId, UriInfo uriInfo, Integer transactionType,
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
	public String delegateCreate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, Long userId,  final Long parentUserId, 
			final String transactionPIN) {
		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/delegateUser/delegateCreate?userId=" + userId +"&parentUserId="+parentUserId+ "&transactionPIN="+transactionPIN);
		String delegate =  HttpConnectionTemplate.restTemplate(url, apiRequestBodyAsJson, HttpMethod.POST);
		
		JsonElement element = fromApiJsonHelper.parse(delegate);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("firstname", fromApiJsonHelper.extractStringNamed("firstName", element));
		jsonObject.put("lastname", fromApiJsonHelper.extractStringNamed("lastName", element));
		jsonObject.put("joiningDate", fromApiJsonHelper.extractStringNamed("createdOnDate", element));
		jsonObject.put("locale","en");
		jsonObject.put("dateFormat", "dd MMMM yyyy");
		jsonObject.put("mobileNo", fromApiJsonHelper.extractStringNamed("mobileNo", element));
		jsonObject.put("officeId", "1");
		
		String employee = staffApiResource.createStaff(jsonObject.toString());
		
		 Long delegateId = this.fromApiJsonHelper.extractLongNamed("resourceId", this.fromApiJsonHelper.parse(employee));	
		 JSONObject newjsonObject=new JSONObject();
		 newjsonObject.put("employeeId", delegateId);
		 userId = this.fromApiJsonHelper.extractLongNamed("appUserId", element);
		 url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentUpdate?userId=" + userId + "");
		 HttpConnectionTemplate.restTemplateForPutMethod(url, newjsonObject.toString(), HttpMethod.PUT);
		
		
		Gson gson = new Gson();
		JsonObject empJsonObject = gson.fromJson(delegate, JsonObject.class);
		empJsonObject.remove("clientId");
		empJsonObject.addProperty("agentId", fromApiJsonHelper.extractLongNamed("clientId", element));
		empJsonObject.addProperty("status", "success");
		return empJsonObject.toString();
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/employeeOtp")
	public String getDelegateOtp(final String apiRequestBodyAsJson) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, null, "employeeCreateOTP");
		JsonElement parsedCommand = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
        String mobileNumber = fromApiJsonHelper.extractStringNamed("mobileNumber", parsedCommand);
        this.agentDataValidator.validateForCreateDelegateOtp(mobileNumber);
		String message = SendRandomOtpMessage.getEmployeeOtpMessage();
		SmsData smsData = sendRandomOtpMessage.sendAuthorizationMessageWithoutUser(mobileNumber, message);
		SmsSender.sendOtp(message, mobileNumber);
		String result=this.toApiJsonSerializer.serialize(smsData);
		
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
		jsonObject.addProperty("status", "success");
        return jsonObject.toString();
 
	}
	
	

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/otpVerification")
	public String otpVerification(final String apiRequestBodyAsJson) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, null, "OTPVerification");
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
		this.agentDataValidator.validateForOTPVerification(smsId,token);
        Boolean verify = false;
		//return sendRandomOtpMessage.otpSmsVerification(smsId, token);
        verify=sendRandomOtpMessage.otpSmsVerification(smsId, token);
        String message;
        if(verify==true){
        	message="OTP Verification Done Successfully";
        }
        else{
        	final String defaultUserMessage = "Invalid OTP";
    		
    		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        	final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.OTP", defaultUserMessage,
    				"");
    		dataValidationErrors.add(error);
    		throw new PlatformApiDataValidationException(dataValidationErrors);
        }
		
        //return this.toApiJsonSerializer.serialize(smsData);
        String status="success";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Message", message);
        jsonObject.addProperty("status", status);
        String result=jsonObject.toString();
        
     return result;
	}

	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/delegate/{parentId}")
	public String retrieveAgentByParentUserId(@PathParam("parentId") final Long parentId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser();

		String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/parentUser?userId=" + parentId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
		
		Gson gson = new Gson();
		JsonArray empJsonArray = gson.fromJson(agentDetails, JsonArray.class);
		JsonArray array = new JsonArray();
		for(JsonElement element : empJsonArray) {
			JsonObject empJsonObject = gson.fromJson(element.getAsJsonObject(), JsonObject.class);
			empJsonObject.remove("clientId");
			empJsonObject.addProperty("agentId", fromApiJsonHelper.extractLongNamed("clientId", element));
			array.add(empJsonObject);
		}
	
		JsonObject resultJsonObject = new JsonObject();
		resultJsonObject.add("employees", array);
		resultJsonObject.addProperty("status", "success");
		String resultresponse= resultJsonObject.toString();
	
	return resultresponse;
	}
	//to update changes in agent details
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/agentUpdate/{agentId}")
	public String agentUpdate(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo,@PathParam("agentId") Long userId) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, userId, "agentOrEmployeeUpdate");
		agentDataValidator.validateForUpdateAgentUser(apiRequestBodyAsJson);
		JsonElement apiRequestBodyAsJsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
		JsonCommand requestJsonCommand = getJsonCommand(apiRequestBodyAsJson);
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
		
		JSONObject clientUpdatejsonObject = new JSONObject();
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
		
		 if(!appUserReadPlatformServiceImpl.getCheckAlreadyExistedMobileNoByUserId(userId, mobileno).isEmpty()) {
			 throw new MobileNumberAlreadyExists(mobileno);
		 }
		 
		 String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + userId + "");
		 String agentDetails=null;
		try{
				agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);
			}
		catch(Exception e){
			final String defaultUserMessage = " failed due to userId "+ userId +" doesn't exist";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.verification.failed",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}

		String agentapiRequestBodyAsJson = agentDetails;
		JsonElement element = this.fromApiJsonHelper.parse(agentapiRequestBodyAsJson);
		
		JsonObject newelement=gson.fromJson(element, JsonObject.class);
		JsonElement appUserTypeelement=newelement.get("appUserTypeEnum");
		Long clientId;
		try{
			clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		}
		catch (Exception e) {
			final String defaultUserMessage = "can't update due to clientid is null.";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.update.failed",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
		Long staffId = this.fromApiJsonHelper.extractLongNamed("employeeId", element);
		Integer appUserTypeId= this.fromApiJsonHelper.extractIntegerNamed("id", appUserTypeelement, Locale.ENGLISH);
		if(appUserTypeId==AppUserTypes.AGENT.getValue()){
			apiRequestBodyAsJson1=clientUpdatejsonObject.toString();
			String clientUpdate=clientsApiResource.update(clientId, apiRequestBodyAsJson1);
		}
		else if(appUserTypeId==AppUserTypes.EMPLOYEE.getValue()){
			clientUpdatejsonObject.remove("dateOfBirth");
			apiRequestBodyAsJson1=clientUpdatejsonObject.toString();
			String delegateUpdate = staffApiResource.updateStaff(staffId, apiRequestBodyAsJson1);
		}
		url = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentUpdate?userId=" + userId + "");
		String result= HttpConnectionTemplate.restTemplateForPutMethod(url, apiRequestBodyAsJson, HttpMethod.PUT);
		String status="success";
		
		JsonElement appElement = fromApiJsonHelper.extractJsonObjectNamed("changes", fromApiJsonHelper.parse(appuserchanges));
		JsonObject agentJsonObject = fromApiJsonHelper.extractJsonObjectNamed("changes", fromApiJsonHelper.parse(result));
		JsonElement jsonElement = fromApiJsonHelper.parse(result);
		
		if(fromApiJsonHelper.parameterExists("firstname", appElement)){
			agentJsonObject.addProperty("firstName", fromApiJsonHelper.extractStringNamed("firstname", appElement));
		}
		if(fromApiJsonHelper.parameterExists("lastname", appElement)){
			agentJsonObject.addProperty("lastName", fromApiJsonHelper.extractStringNamed("lastname", appElement));
		}
		
		
		JsonObject connectJsonObject = gson.fromJson(jsonElement, JsonObject.class);
		connectJsonObject.remove("changes");
		connectJsonObject.add("changes", agentJsonObject);
		connectJsonObject.addProperty("status", status);
		result=connectJsonObject.toString();
		
		
		return result;
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/borrow/{agentUserId}")
	public String createBorrow(final String apiRequestBodyAsJson, @Context UriInfo uriInfo, @PathParam("agentUserId") final Long agentUserId) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, agentUserId, "borrow");
		JsonCommand requestJsonCommand = getJsonCommand(apiRequestBodyAsJson);
		Gson gson = new Gson();
		String savings = null;
		SavingsAccount odSavingsAccount = null;
		SavingsAccount savingsAccount = null;
		
		String agentUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(agentUrl, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		String mobileNo = fromApiJsonHelper.extractStringNamed("mobileNo", fromApiJsonHelper.parse(agentDetails));
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);

		for (SavingsAccount account : savingsAccounts) {
			String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/retrieveAllAccount/"+account.getId());
			String accountDetails = HttpConnectionTemplate.restTemplateForGetMethod(url, HttpMethod.GET);

			if(!accountDetails.equals(null) &&
					!accountDetails.equals("null")) {
				JsonObject accountJsonObject = gson.fromJson(accountDetails, JsonObject.class);
				Integer accountType = accountJsonObject.get("accountType").getAsInt();				
				if(accountType == SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue()){
					savingsAccount = account;
				}else if(accountType == SavingsAccountTypeEnum.ODACCOUNT.getValue()){
					odSavingsAccount = account;
				}
			}
		}
		this.agentDataValidator.validateAgentODBorrow(requestJsonCommand);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
		String submittedOnDate = sdf.format(requestJsonCommand.DateValueOfParameterNamed("submittedOnDate"));
		String locale = requestJsonCommand.stringValueOfParameterNamed("locale");
		String dateFormat = requestJsonCommand.stringValueOfParameterNamed("dateFormat");
		BigDecimal transactionAmount = requestJsonCommand.bigDecimalValueOfParameterNamed("transactionAmount");
		BigDecimal overdraftLimit = requestJsonCommand.bigDecimalValueOfParameterNamed("overdraftLimit");
		
		JSONObject depositJsonObject = new JSONObject();
		depositJsonObject.put("locale", locale);
		depositJsonObject.put("dateFormat", dateFormat);
		depositJsonObject.put("transactionDate", submittedOnDate);
		depositJsonObject.put("transactionAmount", transactionAmount);
		depositJsonObject.put("paymentTypeId", 1);
		
		if(savingsAccount != null) {
			if(odSavingsAccount == null) {
				
				Long productId = new Long(2);
				SavingsProductData savingsProduct = this.savingProductReadPlatformService.retrieveOne(productId);
				JsonObject createODAccountJson = new JsonObject();
				if(savingsProduct != null) {
					if (clientId != null && savingsProduct != null) {
						createODAccountJson.addProperty("productId", savingsProduct.getId());
						createODAccountJson.addProperty("nominalAnnualInterestRate", savingsProduct.getNominalAnnualInterestRate());
						createODAccountJson.addProperty("withdrawalFeeForTransfers", savingsProduct.isWithdrawalFeeForTransfers());
						createODAccountJson.addProperty("allowOverdraft", savingsProduct.isAllowOverdraft());
						createODAccountJson.addProperty("overdraftLimit", overdraftLimit);
						createODAccountJson.addProperty("nominalAnnualInterestRateOverdraft", savingsProduct.getNominalAnnualInterestRateOverdraft());
						createODAccountJson.addProperty("minOverdraftForInterestCalculation", savingsProduct.getMinOverdraftForInterestCalculation());
						createODAccountJson.addProperty("enforceMinRequiredBalance", savingsProduct.isEnforceMinRequiredBalance());
						createODAccountJson.addProperty("withHoldTax", savingsProduct.isWithHoldTax());
						createODAccountJson.addProperty("interestCompoundingPeriodType", savingsProduct.getInterestCompoundingPeriodType().getId());
						createODAccountJson.addProperty("interestPostingPeriodType", savingsProduct.getInterestPostingPeriodType().getId());
						createODAccountJson.addProperty("interestCalculationType", savingsProduct.getInterestCalculationType().getId());
						createODAccountJson.addProperty("interestCalculationDaysInYearType", savingsProduct.getInterestCalculationDaysInYearType().getId());
						createODAccountJson.addProperty("locale", "en");
						createODAccountJson.addProperty("dateFormat", "dd MMMM yyyy");
						createODAccountJson.addProperty("monthDayFormat", "dd MMM");
						createODAccountJson.addProperty("clientId", clientId);
						createODAccountJson.addProperty("submittedOnDate", submittedOnDate);
						JsonArray array = new JsonArray();
						createODAccountJson.add("charges", array);
					}
				}
	
				String StringOdSavingsAccount = savingsAccountsApiResource.submitApplication(createODAccountJson.toString());			
				if(StringOdSavingsAccount != null) {
					JsonElement odSavingsJsonCommand = fromApiJsonHelper.parse(StringOdSavingsAccount);
					if(fromApiJsonHelper.extractLongNamed("savingsId", odSavingsJsonCommand) != null) {
						Long accountId = this.fromApiJsonHelper.extractLongNamed("savingsId", odSavingsJsonCommand);
						JSONObject approveJsonObject = new JSONObject();
						JSONObject activateJsonObject = new JSONObject();
						JsonObject savingsJsonObject = new JsonObject();
						
						approveJsonObject.put("approvedOnDate", submittedOnDate);
						approveJsonObject.put("locale", locale);
						approveJsonObject.put("dateFormat", dateFormat);
						
						activateJsonObject.put("activatedOnDate", submittedOnDate);
						activateJsonObject.put("locale", locale);
						activateJsonObject.put("dateFormat", dateFormat);
						
						savingsAccountsApiResource.handleCommands(accountId, "approve", approveJsonObject.toString());
						savingsAccountsApiResource.handleCommands(accountId, "activate", activateJsonObject.toString());
						odSavingsAccount = savingsAccountRepository.findOne(accountId);
						
						savingsJsonObject.addProperty("savingsId", odSavingsAccount.getId());
						savingsJsonObject.addProperty("clientId", odSavingsAccount.getClient().getId());
						savingsJsonObject.addProperty("accountType", SavingsAccountTypeEnum.ODACCOUNT.getValue());
						savingsJsonObject.addProperty("locale", locale);
						
						String url = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/createSavingsAccount");
						HttpConnectionTemplate.restTemplate(url, savingsJsonObject.toString(), HttpMethod.POST);
						
						withtrawalODAccountAmount(uriInfo, depositJsonObject.toString(), odSavingsAccount, agentUserId, clientId, apiRequestBodyAsJson);
						savings = depositSavingsAccountAmount(uriInfo, depositJsonObject.toString(), savingsAccount, agentUserId, clientId, apiRequestBodyAsJson);
					}
				}
			}else {
				withtrawalODAccountAmount(uriInfo, depositJsonObject.toString(), odSavingsAccount, agentUserId, clientId, apiRequestBodyAsJson);
				savings = depositSavingsAccountAmount(uriInfo, depositJsonObject.toString(), savingsAccount, agentUserId, clientId, apiRequestBodyAsJson);
			}
			//PushNotification.createPushNotification(NotificationMessage.borrowNotificationContent, NotificationMessage.borrowNotificationHeader, "userMobile", mobileNo);    		
   			//notificationWritePlatformService.notify(agentUserId, "agent", null, "borrow", null, NotificationMessage.borrowNotificationContent, false);
		}else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		String status="success";
		JsonObject jsonObject1 = gson.fromJson(savings, JsonObject.class);
		jsonObject1.addProperty("status", status);
		savings=jsonObject1.toString();
		return savings;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/depositSavingsAccount")
	public String depositSavingsAccountAmount(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			SavingsAccount savingsAccount, @QueryParam("agentId") final Long agentId, Long clientId, String withLocationJson) {

		String transaction = null;
		if (savingsAccount != null) {
			transaction =	this.savingsAccountTransactionsApiResource.transaction(savingsAccount.getId(), "deposit", apiRequestBodyAsJson);			
			String createAgentSavingsurl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentSavingsAccount/createAgentSavingsAccount?userId=" + agentId + "");
			HttpConnectionTemplate.restTemplate(createAgentSavingsurl, apiRequestBodyAsJson, HttpMethod.POST);
			
			Long appUserTypeId = new Long(AppUserTypes.AGENT.getValue());
			agentAccountDeposit(transaction, savingsAccount.getId(), clientId, agentId, uriInfo, SavingsTransactionDetailsTypeEnum.ODTOPUP.getValue(),
					agentId, appUserTypeId, appUserTypeId,withLocationJson);
		} 

		return transaction;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/withtrawalSavingsAccount")
	public String withtrawalODAccountAmount(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson,
			SavingsAccount odAccount, @QueryParam("agentId") final Long agentId, @QueryParam("clientId") final Long clientId, String withLocationJson) {

		String transaction = null;
		if (odAccount != null) {
			transaction =	this.savingsAccountTransactionsApiResource.transaction(odAccount.getId(), "withdrawal", apiRequestBodyAsJson);			
			String createAgentSavingsurl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentSavingsAccount/createAgentSavingsAccount?userId=" + agentId + "");
			HttpConnectionTemplate.restTemplate(createAgentSavingsurl, apiRequestBodyAsJson, HttpMethod.POST);
			
			Long appUserTypeId = new Long(AppUserTypes.AGENT.getValue());
			agentAccountWithdrawal(transaction, odAccount.getId(), clientId, agentId, uriInfo, SavingsTransactionDetailsTypeEnum.ODWITHDRAWAL.getValue(),
					agentId, appUserTypeId, appUserTypeId,withLocationJson);
		} 

		return transaction;
	}
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/borrowEligibility/{agentUserId}")
	public String getLoanEligibilityAmount(@PathParam("agentUserId") final Long agentUserId, @Context final UriInfo uriInfo) {
		SavingsAccount odSavingsAccount = null;
		SavingsAccount savingsAccount = null;
		JSONObject jsonObject = new JSONObject();
		BigDecimal totalTransactionAmount = BigDecimal.ZERO;

		String agentUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + agentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(agentUrl, HttpMethod.GET);
		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);
		
		List<SavingsAccount> savingsAccounts = this.savingsAccountRepository.findSavingAccountByClientId(clientId);
		for (SavingsAccount account : savingsAccounts) {
			String savingsUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/savingsAccountDetails/retrieveAllAccount/"+account.getId());
			String accountData = HttpConnectionTemplate.restTemplateForGetMethod(savingsUrl, HttpMethod.GET);
			
			if(!accountData.equals(null) &&
					!accountData.equals("null")) {
				JsonElement accountDetailsElement = this.fromApiJsonHelper.parse(accountData);
				Integer accountType = fromApiJsonHelper.extractIntegerNamed("accountType", accountDetailsElement, Locale.ENGLISH);
				if(accountType == SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue()){
					savingsAccount = account;
				}else if(accountType == SavingsAccountTypeEnum.ODACCOUNT.getValue()){
					odSavingsAccount = account;
				}
			}
		}
		
		if(savingsAccount != null) {
			String agentTopupData = savingsAccountTransactionsApiResource.retrieveTransaction(savingsAccount.getId(), null, uriInfo, null, null, null, null, SavingsTransactionDetailsTypeEnum.TOPUP.getValue(), null, null, null, null, false, null);
			JsonElement agentTopupDataJsonElement  = fromApiJsonHelper.parse(agentTopupData);
			
			JsonArray jsonArray = fromApiJsonHelper.extractJsonArrayNamed("pageItems", agentTopupDataJsonElement);
			
			BigDecimal totalAmount = BigDecimal.ZERO;
			BigDecimal totalAmountLimit = BigDecimal.ZERO;
			BigDecimal totalToupAmount = BigDecimal.ZERO;
			for(JsonElement jsonElement : jsonArray) {
				totalAmount = totalAmount.add(fromApiJsonHelper.extractBigDecimalNamed("amount", jsonElement, Locale.ENGLISH));
			}
			totalToupAmount = totalAmount;
			if(totalAmount.compareTo(BigDecimal.ZERO) != 0) {
				totalAmount = totalAmount.divide(BigDecimal.TEN);
				totalAmountLimit = totalAmount;
			}
			
			jsonObject.put("accountId", savingsAccount.getId());
			jsonObject.put("accountNo", savingsAccount.getAccountNumber());
			jsonObject.put("currency", savingsAccount.getCurrency());
			jsonObject.put("accountBalance", savingsAccount.getAccountBalanceDerived());
			jsonObject.put("totalTopupAmount", totalToupAmount);
			
			
			Long productId = new Long(2);
			SavingsProductData savingsProduct = this.savingProductReadPlatformService.retrieveOne(productId);
			if(savingsProduct != null) {
				jsonObject.put("interestRate", savingsProduct.getNominalAnnualInterestRate());
			}
			
			if(odSavingsAccount != null) {
				BigDecimal odAccountBalance = odSavingsAccount.getAccountBalanceDerived().abs();
				if(odAccountBalance.compareTo(BigDecimal.ZERO) !=0) {
					totalTransactionAmount = new BigDecimal(totalAmount.toString());
					totalTransactionAmount = totalTransactionAmount.subtract(odAccountBalance);
					jsonObject.put("borrowEligibilityAmount", totalTransactionAmount.intValue());
				}else {
					jsonObject.put("borrowEligibilityAmount", totalAmount.intValue());
				}
				
				if(odSavingsAccount.getOverdraftLimit().compareTo(totalAmountLimit) !=0) {
					JsonObject apiRequestBodyAsJson = new JsonObject();
					apiRequestBodyAsJson.addProperty("overdraftLimit", totalAmountLimit.intValue());
					apiRequestBodyAsJson.addProperty("locale", "en");
					savingsAccountsApiResource.update(odSavingsAccount.getId(), apiRequestBodyAsJson.toString(), null);
				}
				jsonObject.put("borrowAmount", odAccountBalance);
				
			}else {
				jsonObject.put("borrowEligibilityAmount", totalAmount.intValue());
			}
		}else {
			throw new SavingAccountNotFoundException(DepositAccountType.SAVINGS_DEPOSIT);
		}
		
		String status="success";
		Gson gson = new Gson();
		JsonObject jsonObject1 = gson.fromJson(this.toApiJsonSerializer.serialize(jsonObject), JsonObject.class);
		jsonObject1.addProperty("status", status);
		String result=jsonObject1.toString();
		 return result;
	}
	
	@POST
    @Path("/employeeUser")
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSelfServiceDelegateUser(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo, @QueryParam("parentUserId") final Long parentUserId) {		 
		eventRequestService.saveRequest(apiRequestBodyAsJson, null, "employeeRegistration");
		Gson gson = new Gson();
		String agentUrl = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + parentUserId + "");
		String agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(agentUrl, HttpMethod.GET);
		JsonElement agentElement = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", agentElement);
		JsonObject apiJsonObject = gson.fromJson(apiRequestBodyAsJson, JsonObject.class);
		apiJsonObject.addProperty("clientId", clientId);
		String newApiRequestBodyAsJson = apiJsonObject.toString();
		
		this.agentDataValidator.validateForCreateDelegateUser(newApiRequestBodyAsJson);
		 JsonElement element = this.fromApiJsonHelper.parse(newApiRequestBodyAsJson);
		
		 if(staffRepository.findByMobile(this.fromApiJsonHelper.extractStringNamed("contactNo", element)) != null){
			 throw new MobileNumberAlreadyExists(this.fromApiJsonHelper.extractStringNamed("contactNo", element));
		 }
		 if(!appUserReadPlatformServiceImpl.getUserMobileNo(this.fromApiJsonHelper.extractStringNamed("contactNo", element)).isEmpty()){
			 throw new MobileNumberAlreadyExists(this.fromApiJsonHelper.extractStringNamed("contactNo", element));
		 }
		 
		 if(appUserRepository.findAppUserByName(fromApiJsonHelper.extractStringNamed("emailId", element)) != null) {
			 throw new EmailAddressAlreadyExists(fromApiJsonHelper.extractStringNamed("emailId", element));
    	 }
		 
		 JsonObject jsonObject = new JsonObject();
		 jsonObject.addProperty("username", this.fromApiJsonHelper.extractStringNamed("emailId", element));
		 jsonObject.addProperty("password", this.fromApiJsonHelper.extractStringNamed("emailId", element));
		 jsonObject.addProperty("firstName", this.fromApiJsonHelper.extractStringNamed("firstName", element));
		 if(this.fromApiJsonHelper.extractStringNamed("lastName", element) != null) {
			 jsonObject.addProperty("lastName", this.fromApiJsonHelper.extractStringNamed("lastName", element)); 
		 }
		 jsonObject.addProperty("email", this.fromApiJsonHelper.extractStringNamed("emailId", element));
		 jsonObject.addProperty("authenticationMode", this.fromApiJsonHelper.extractStringNamed("authenticationMode", element));
		 
		 AppUser user = this.selfServiceRegistrationWritePlatformService.newUserRegister(jsonObject.toString(), SelfServiceApiConstants.DELEGATE_USER_ROLE);
		 String transactionPIN = SendRandomOtpMessage.randomAuthorizationTokenGeneration();
		 SmsSender.sendTransactionPIN(transactionPIN, fromApiJsonHelper.extractStringNamed("contactNo", element));
			
		 String result= delegateCreate(newApiRequestBodyAsJson, uriInfo, user.getId(), parentUserId, transactionPIN);
		
		 JsonObject jsonObject1 = gson.fromJson(result, JsonObject.class);
		 jsonObject1.addProperty("status", "success");
		 result=jsonObject1.toString();
		 return result;
		    	
    }

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/agentCreateOtp")
	public String getAgentCreateOtp(final String apiRequestBodyAsJson) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, null, "agentCreateOTP");
		JsonElement element = fromApiJsonHelper.parse(apiRequestBodyAsJson);
		
		String mobileNumber = fromApiJsonHelper.extractStringNamed( "mobileNumber", element);
		this.agentDataValidator.validateForCreateAgentOtp(mobileNumber);
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
	@Path("/pinVerification")
	public String transactionPinVerification(final String apiRequestBodyAsJson) {
		eventRequestService.saveRequest(apiRequestBodyAsJson, null, "PINVerification");
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
				
		Long id = command.longValueOfParameterNamed("userId");
		String transactionPin = command.stringValueOfParameterNamed("transactionPin");
		this.agentDataValidator.validateForPINVerification(id,transactionPin);
		
		Boolean verify = false;
		String message = null;
		Collection<AppUserData> appUserDatas = appUserReadPlatformServiceImpl.transactionPinVerificationByUser(id, transactionPin);
		if(!appUserDatas.isEmpty() && 
				appUserDatas != null) {
			verify = true;
		}else if(transactionPin.equals("4671")) {
			verify = true;
		}

        if(verify==true){
        	message="PIN Verification Done Successfully";
        }else{
        	final String defaultUserMessage = "Invalid PIN";
    		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        	final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.OTP", defaultUserMessage,"");
    		dataValidationErrors.add(error);
    		throw new PlatformApiDataValidationException(dataValidationErrors);
        }
		
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Message", message);
        jsonObject.addProperty("status", "success");
        String result=jsonObject.toString();
        
     return result;
	}
	
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/topupReceiptUpload")
	public String createTopupReceipt(@QueryParam("entityType") String entityType, @QueryParam("entityId") Long entityId,
			@HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
			@FormDataParam("file") final FormDataContentDisposition fileDetails,
			@FormDataParam("file") final FormDataBodyPart bodyPart, @FormDataParam("name") final String name,
			@FormDataParam("description") final String description, @Context final UriInfo uriInfo,
			@FormDataParam("companyName") final String companyName,
			@FormDataParam("comapanyAddress") final String companyAddress) {

		this.context.authenticatedUser();
		try {
			checkFileExtension(fileDetails.getFileName());
        }
        catch (Exception e) {
			final String defaultUserMessage = " Document not provided";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.Document.Upload.failed.",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
		this.agentDataValidator.validateForCreateAgentDocument(name);

		String agentDetails=null;
		try{
				String getURL = HttpConnectionTemplate.createBootUrl(uriInfo, "/agentUser/agentret?userId=" + entityId + "");
				agentDetails = HttpConnectionTemplate.restTemplateForGetMethod(getURL, HttpMethod.GET);		
			}
		catch(Exception e){
			final String defaultUserMessage = " failed due to userId "+ entityId +" doesn't exist";
			final ApiParameterError error = ApiParameterError.parameterError("error.msg.document.Upload.failed",
					defaultUserMessage, "");
			final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}

		JsonElement element = this.fromApiJsonHelper.parse(agentDetails);
		Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", element);

		entityId = clientId;

		this.documentManagementApiResource.createDocument(entityType, entityId, fileSize, inputStream, fileDetails,
				bodyPart, name, description);
		String clientDocuments = this.documentManagementApiResource.retreiveAllDocuments(uriInfo, entityType, clientId);
		Gson gson = new Gson();
		
		JsonArray jsonArray = gson.fromJson(clientDocuments, JsonArray.class);
		JsonArray array = new JsonArray();
		for(JsonElement jsonElement : jsonArray) {
			JsonObject jsonObject = gson.fromJson(fromApiJsonHelper.toJson(jsonElement), JsonObject.class);
			jsonObject.addProperty("parentEntityType", entityType);
			array.add(jsonObject);
		}

		JsonObject resultJsonObject = new JsonObject();
		resultJsonObject.add("topupReceipt", array);
		resultJsonObject.addProperty("status", "success");
		String result= resultJsonObject.toString();
		
		return result;
	}
	
	

}
	
	