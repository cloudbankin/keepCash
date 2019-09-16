package org.apache.fineract.portfolio.springBoot.data;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionAmountParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.documentmanagement.api.ImagesApiResource.ENTITY_TYPE_FOR_IMAGES;
import org.apache.fineract.infrastructure.documentmanagement.exception.InvalidEntityTypeForImageManagementException;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


@Component
public class AgentDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AgentDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }
    
    private final Set<String> supportedParametersForAgentCreation = new HashSet<>(Arrays.asList("userName", 
    		"firstName", "lastName",
    		"emailId", "authenticationMode", "password", "faceId", "appUserTypeId", "dateFormat", "locale",
    		"imageEncryption", "image", "contactNo", "dateOfBirth", "isAgreementSignUp", "isActive", "clientId" ,
    		"location" ));
    
    private final Set<String> supportedParametersForEmployeeCreation = new HashSet<>(Arrays.asList("userName", 
    		"firstName", "lastName",
    		"emailId", "authenticationMode", "password", "faceId", "appUserTypeId", "dateFormat", "locale",
    		"imageEncryption", "image", "contactNo", "dateOfBirth", "isAgreementSignUp", "isActive", "clientId" ,
    		"location", "faceUniqueId" ));
    
    private final Set<String> supportedParametersForAgentUpdate = new HashSet<>(Arrays.asList("userName", 
    		"firstName", "lastName",
    		"emailId", "authenticationMode", "password", "faceId", "appUserTypeId", "dateFormat", "locale",
    		"imageEncryption", "image", "contactNo", "dateOfBirth", "isAgreementSignUp", "isActive", "clientId","location",
    		"employeeId", "faceUniqueId"));
    
    public void validateForCreateAgentUser(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForAgentCreation);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("createAgentUser");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if(this.fromApiJsonHelper.extractStringNamed("userName", element) != null) {
        	final String userName = this.fromApiJsonHelper.extractStringNamed("userName", element);
        	baseDataValidator.reset().parameter("userName").value(userName).notBlank().notExceedingLengthOf(100);
        }
        if(this.fromApiJsonHelper.extractStringNamed("password", element) != null) {
        	final String password = this.fromApiJsonHelper.extractStringNamed("password", element);
            baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(100);
        }

        final String firstName = this.fromApiJsonHelper.extractStringNamed("firstName", element);
        baseDataValidator.reset().parameter("firstName").value(firstName).notBlank().notExceedingLengthOf(100);

	    if(this.fromApiJsonHelper.extractStringNamed("lastName", element) != null) {
	    	final String lastName = this.fromApiJsonHelper.extractStringNamed("lastName", element);
	        baseDataValidator.reset().parameter("lastName").value(lastName).notBlank().notExceedingLengthOf(100);
	    }
    	
    	final String emailId = this.fromApiJsonHelper.extractStringNamed("emailId", element);
    	baseDataValidator.reset().parameter("emailId").value(emailId).notExceedingLengthOf(100).notBlank().validateEmailAddress(emailId);
    	
        final String authenticationMode = this.fromApiJsonHelper.extractStringNamed("authenticationMode", element);
        baseDataValidator.reset().parameter("authenticationMode").value(authenticationMode).notBlank().notExceedingLengthOf(30);

       
        
        
		if (this.fromApiJsonHelper.parameterExists("faceId", element)) {
			final String faceId = this.fromApiJsonHelper.extractStringNamed("faceId", element);
			baseDataValidator.reset().parameter("faceId").value(faceId).notExceedingLengthOf(30);
		}

		if (this.fromApiJsonHelper.parameterExists("appUserTypeId", element)) {
			final Long appUserTypeId = this.fromApiJsonHelper.extractLongNamed("appUserTypeId", element);
			baseDataValidator.reset().parameter("appUserTypeId").value(appUserTypeId).notBlank();
		}
		if (this.fromApiJsonHelper.parameterExists("contactNo", element)) {
			final String contactNo = this.fromApiJsonHelper.extractStringNamed("contactNo", element);
			baseDataValidator.reset().parameter("contactNo").value(contactNo).minAndMaxLengthOfString(10, 10);
		}
		if (this.fromApiJsonHelper.parameterExists("dateOfBirth", element)) {
			final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed("dateOfBirth", element);
			baseDataValidator.reset().parameter("dateOfBirth").value(dateOfBirth).futureDateValidation(dateOfBirth);
		}

        
		 if(this.fromApiJsonHelper.parameterExists("isAgreementSignUp", element)){
        	final Boolean isAgreementSignUp = this.fromApiJsonHelper.extractBooleanNamed("isAgreementSignUp", element);
        	baseDataValidator.reset().parameter("isAgreementSignUp").trueOrFalseRequired(isAgreementSignUp).trueOrFalseRequired1(isAgreementSignUp);
        }
        
        if(this.fromApiJsonHelper.parameterExists("isActive", element)){
        	final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
        	baseDataValidator.reset().parameter("isActive").trueOrFalseRequired(isActive).trueOrFalseRequired1(isActive);
        }
        
        if (this.fromApiJsonHelper.parameterExists("locale", element)) {
			final String locale = this.fromApiJsonHelper.extractStringNamed("locale", element);
			baseDataValidator.reset().parameter("locale").value(locale).notBlank();
		}
               	
    	final JsonElement location = this.fromApiJsonHelper.extractJsonObjectNamed("location", element);
		baseDataValidator.reset().parameter("location").value(location).notBlank();
		
		if(location != null) {
			final String latitude = this.fromApiJsonHelper.extractStringNamed("latitude", location);
			baseDataValidator.reset().parameter("latitude").value(latitude).notExceedingLengthOf(50);
			
			final String longitude = this.fromApiJsonHelper.extractStringNamed("longitude", location);
			baseDataValidator.reset().parameter("longitude").value(longitude).notExceedingLengthOf(50);
			
			final String locationName = this.fromApiJsonHelper.extractStringNamed("locationName", location);
			baseDataValidator.reset().parameter("locationName").value(locationName).notExceedingLengthOf(200);
			
			final String locationAddress = this.fromApiJsonHelper.extractStringNamed("locationAddress", location);
			baseDataValidator.reset().parameter("locationAddress").value(locationAddress);
			
			final String ipAddress = this.fromApiJsonHelper.extractStringNamed("ipAddress", location);
	    	baseDataValidator.reset().parameter("ipAddress").value(ipAddress).notExceedingLengthOf(50);   	
	    	
	    	final String deviceId = this.fromApiJsonHelper.extractStringNamed("deviceId", location);
	    	baseDataValidator.reset().parameter("deviceId").value(deviceId).notExceedingLengthOf(50);
		}
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForCreateDelegateUser(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForEmployeeCreation);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("createDelegateUser");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if(this.fromApiJsonHelper.extractStringNamed("userName", element) != null) {
        	final String userName = this.fromApiJsonHelper.extractStringNamed("userName", element);
        	baseDataValidator.reset().parameter("userName").value(userName).notBlank().notExceedingLengthOf(100);
        }
        if(this.fromApiJsonHelper.extractStringNamed("password", element) != null) {
        	final String password = this.fromApiJsonHelper.extractStringNamed("password", element);
            baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(100);
        }

        final String firstName = this.fromApiJsonHelper.extractStringNamed("firstName", element);
        baseDataValidator.reset().parameter("firstName").value(firstName).notBlank().notExceedingLengthOf(100);

        if(this.fromApiJsonHelper.extractStringNamed("lastName", element) != null) {
	    	final String lastName = this.fromApiJsonHelper.extractStringNamed("lastName", element);
	        baseDataValidator.reset().parameter("lastName").value(lastName).notBlank().notExceedingLengthOf(100);
	    }
        
        final String emailId = this.fromApiJsonHelper.extractStringNamed("emailId", element);
        baseDataValidator.reset().parameter("emailId").value(emailId).notExceedingLengthOf(100).validateEmailAddress(emailId).notBlank();

        final String authenticationMode = this.fromApiJsonHelper.extractStringNamed("authenticationMode", element);
        baseDataValidator.reset().parameter("authenticationMode").value(authenticationMode).notBlank().notExceedingLengthOf(30);
        
		if (this.fromApiJsonHelper.parameterExists("faceId", element)) {
			final String faceId = this.fromApiJsonHelper.extractStringNamed("faceId", element);
			baseDataValidator.reset().parameter("faceId").value(faceId).notExceedingLengthOf(30);
		}

		if (this.fromApiJsonHelper.parameterExists("appUserTypeId", element)) {
			final Long appUserTypeId = this.fromApiJsonHelper.extractLongNamed("appUserTypeId", element);
			baseDataValidator.reset().parameter("appUserTypeId").value(appUserTypeId).notBlank();
		}
	
		final String contactNo = this.fromApiJsonHelper.extractStringNamed("contactNo", element);
		baseDataValidator.reset().parameter("contactNo").value(contactNo).notBlank().minAndMaxLengthOfString(10, 10);
	
		if (this.fromApiJsonHelper.parameterExists("dateOfBirth", element)) {
			final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed("dateOfBirth", element);
			baseDataValidator.reset().parameter("dateOfBirth").value(dateOfBirth).futureDateValidation(dateOfBirth);
		}
 
        if(this.fromApiJsonHelper.parameterExists("isAgreementSignUp", element)){
        	final Boolean isAgreementSignUp = this.fromApiJsonHelper.extractBooleanNamed("isAgreementSignUp", element);
        	baseDataValidator.reset().parameter("isAgreementSignUp").trueOrFalseRequired(isAgreementSignUp).trueOrFalseRequired1(isAgreementSignUp);
        	
        }
        
        if(this.fromApiJsonHelper.parameterExists("isActive", element)){
        	final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
        	baseDataValidator.reset().parameter("isActive").trueOrFalseRequired(isActive).trueOrFalseRequired1(isActive);
        }
        
        if (this.fromApiJsonHelper.parameterExists("locale", element)) {
			final String locale = this.fromApiJsonHelper.extractStringNamed("locale", element);
			baseDataValidator.reset().parameter("locale").value(locale).notBlank();
		}
        
        final JsonElement location = this.fromApiJsonHelper.extractJsonObjectNamed("location", element);
		baseDataValidator.reset().parameter("location").value(location).notBlank();
		
		if(location != null) {
			final String latitude = this.fromApiJsonHelper.extractStringNamed("latitude", location);
			baseDataValidator.reset().parameter("latitude").value(latitude).notExceedingLengthOf(50);
			
			final String longitude = this.fromApiJsonHelper.extractStringNamed("longitude", location);
			baseDataValidator.reset().parameter("longitude").value(longitude).notExceedingLengthOf(50);
			
			final String locationName = this.fromApiJsonHelper.extractStringNamed("locationName", location);
			baseDataValidator.reset().parameter("locationName").value(locationName).notExceedingLengthOf(200);
			
			final String locationAddress = this.fromApiJsonHelper.extractStringNamed("locationAddress", location);
			baseDataValidator.reset().parameter("locationAddress").value(locationAddress);
			
			final String ipAddress = this.fromApiJsonHelper.extractStringNamed("ipAddress", location);
	    	baseDataValidator.reset().parameter("ipAddress").value(ipAddress).notExceedingLengthOf(50);   	
	    	
	    	final String deviceId = this.fromApiJsonHelper.extractStringNamed("deviceId", location);
	    	baseDataValidator.reset().parameter("deviceId").value(deviceId).notExceedingLengthOf(50);
		}
        
        final String faceUniqueId = this.fromApiJsonHelper.extractStringNamed("faceUniqueId", element);
        baseDataValidator.reset().parameter("faceUniqueId").value(faceUniqueId).notBlank().notExceedingLengthOf(10);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForCreateAgentDocument(final String name) {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("uploadAgentDocument");
        
        String documentName = name;
        baseDataValidator.reset().parameter("name").value(documentName).notBlank()
                .notExceedingLengthOf(30);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForCreateAgentIdentifier(final Long identifierType, final String identifierId,final int count) {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("uploadAgentIdentifier");
        
        baseDataValidator.reset().parameter("identifierType").value(identifierType).notBlank()
        .notExceedingLengthOf(30).notLessThanMin(1).notvalidtype(count);
        
        baseDataValidator.reset().parameter("identifierId").value(identifierId).notBlank()
                .notExceedingLengthOf(30).alphaNumericString(identifierId.trim());
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    private final Set<String> supportedParametersForDepositSavingsAccount = new HashSet<>(Arrays.asList("transactionDate", 
    		"transactionAmount", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber",
    		"dateFormat", "locale","location"));
    
    public void validateForDepositTransaction(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForDepositSavingsAccount);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("depsoitTransaction");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

		if (this.fromApiJsonHelper.parameterExists("accountNumber", element)) {
			final String accountNumber = this.fromApiJsonHelper.extractStringNamed("accountNumber", element);
			baseDataValidator.reset().parameter("accountNumber").value(accountNumber).notExceedingLengthOf(30);
		}
		if (this.fromApiJsonHelper.parameterExists("checkNumber", element)) {
			final String checkNumber = this.fromApiJsonHelper.extractStringNamed("checkNumber", element);
			baseDataValidator.reset().parameter("checkNumber").value(checkNumber).notExceedingLengthOf(30);
		}
		if (this.fromApiJsonHelper.parameterExists("emailId", element)) {
			final String routingCode = this.fromApiJsonHelper.extractStringNamed("routingCode", element);
			baseDataValidator.reset().parameter("emailId").value(routingCode).notExceedingLengthOf(30);
		}
		if (this.fromApiJsonHelper.parameterExists("receiptNumber", element)) {
			final String receiptNumber = this.fromApiJsonHelper.extractStringNamed("receiptNumber", element);
			baseDataValidator.reset().parameter("receiptNumber").value(receiptNumber).notExceedingLengthOf(30);
		}
		if (this.fromApiJsonHelper.parameterExists("bankNumber", element)) {
			final String bankNumber = this.fromApiJsonHelper.extractStringNamed("bankNumber", element);
			baseDataValidator.reset().parameter("bankNumber").value(bankNumber).notExceedingLengthOf(30);
		}
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull().futureDateValidation(transactionDate);
        
        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount", element, Locale.ENGLISH);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).positiveAmount().notNull();
        
        final JsonElement location = this.fromApiJsonHelper.extractJsonObjectNamed("location", element);
		baseDataValidator.reset().parameter("location").value(location).notBlank();
		
		if(location != null) {
			final String latitude = this.fromApiJsonHelper.extractStringNamed("latitude", location);
			baseDataValidator.reset().parameter("latitude").value(latitude).notExceedingLengthOf(50);
			
			final String longitude = this.fromApiJsonHelper.extractStringNamed("longitude", location);
			baseDataValidator.reset().parameter("longitude").value(longitude).notExceedingLengthOf(50);
			
			final String locationName = this.fromApiJsonHelper.extractStringNamed("locationName", location);
			baseDataValidator.reset().parameter("locationName").value(locationName).notExceedingLengthOf(200);
			
			final String locationAddress = this.fromApiJsonHelper.extractStringNamed("locationAddress", location);
			baseDataValidator.reset().parameter("locationAddress").value(locationAddress);
			
			final String ipAddress = this.fromApiJsonHelper.extractStringNamed("ipAddress", location);
	    	baseDataValidator.reset().parameter("ipAddress").value(ipAddress).notExceedingLengthOf(50);   	
	    	
	    	final String deviceId = this.fromApiJsonHelper.extractStringNamed("deviceId", location);
	    	baseDataValidator.reset().parameter("deviceId").value(deviceId).notExceedingLengthOf(50);
		}
		
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    
    public void validateForCreateDelegateOtp(final String mobileNo)
    {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
    
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GenerateDelegateOTP");
    
        baseDataValidator.reset().parameter("contactNo").value(mobileNo).notBlank().minAndMaxLengthOfString(10, 10);
    
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForOTPVerification(final Long smsId,final String token){
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("OTPVerification");
        
        baseDataValidator.reset().parameter("smsId").value(smsId).notBlank();
        
        baseDataValidator.reset().parameter("token").value(token).notBlank();
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);


    }
    public void validateAgentODBorrow(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_TRANSACTION_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("submittedOnDate", element);
        baseDataValidator.reset().parameter("submittedOnDate").value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(transactionAmountParamName, element);
        baseDataValidator.reset().parameter(transactionAmountParamName).value(transactionAmount).notNull().positiveAmount();
        
        final BigDecimal overdraftLimit=this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("overdraftLimit", element);
        baseDataValidator.reset().parameter("overdraftLimit").value(overdraftLimit).notNull().positiveAmount();
        
        final JsonElement location = this.fromApiJsonHelper.extractJsonObjectNamed("location", element);
		baseDataValidator.reset().parameter("location").value(location).notBlank();
		
		if(location != null) {
			final String latitude = this.fromApiJsonHelper.extractStringNamed("latitude", location);
			baseDataValidator.reset().parameter("latitude").value(latitude).notExceedingLengthOf(50);
			
			final String longitude = this.fromApiJsonHelper.extractStringNamed("longitude", location);
			baseDataValidator.reset().parameter("longitude").value(longitude).notExceedingLengthOf(50);
			
			final String locationName = this.fromApiJsonHelper.extractStringNamed("locationName", location);
			baseDataValidator.reset().parameter("locationName").value(locationName).notExceedingLengthOf(200);
			
			final String locationAddress = this.fromApiJsonHelper.extractStringNamed("locationAddress", location);
			baseDataValidator.reset().parameter("locationAddress").value(locationAddress);
			
			final String ipAddress = this.fromApiJsonHelper.extractStringNamed("ipAddress", location);
	    	baseDataValidator.reset().parameter("ipAddress").value(ipAddress).notExceedingLengthOf(50);   	
	    	
	    	final String deviceId = this.fromApiJsonHelper.extractStringNamed("deviceId", location);
	    	baseDataValidator.reset().parameter("deviceId").value(deviceId).notExceedingLengthOf(50);
		}

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
    public void validateForCreateAgentOtp(final String mobileNo)
    {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
    
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GenerateCustomerOTP");
    
        baseDataValidator.reset().parameter("contactNo").value(mobileNo).notBlank().minAndMaxLengthOfString(10, 10);
    
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForLogin(final String mobileNo, final String pin){
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("mobileNumber");
        baseDataValidator.reset().parameter("contactNo").value(mobileNo).notBlank().minAndMaxLengthOfString(10, 10);
        baseDataValidator.reset().parameter("transactionPin").value(pin).notBlank().minAndMaxLengthOfString(4, 4);
    
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForPINVerification(final Long userId,final String transactionPin){
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("PINVerification");
        baseDataValidator.reset().parameter("userId").value(userId).notBlank();
        baseDataValidator.reset().parameter("transactionPin").value(transactionPin).notBlank().minAndMaxLengthOfString(4, 4);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForUpdateAgentUser(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForAgentUpdate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("updateAgentUser");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if(this.fromApiJsonHelper.extractStringNamed("firstName", element) != null) {
            final String firstName = this.fromApiJsonHelper.extractStringNamed("firstName", element);
            baseDataValidator.reset().parameter("firstName").value(firstName).notBlank().notExceedingLengthOf(100);
        }
        if(this.fromApiJsonHelper.extractStringNamed("lastName", element) != null) {
        	final String lastName = this.fromApiJsonHelper.extractStringNamed("lastName", element);
            baseDataValidator.reset().parameter("lastName").value(lastName).notBlank().notExceedingLengthOf(100);
        }
        
        if(this.fromApiJsonHelper.extractStringNamed("emailId", element) != null) {
        	final String emailId = this.fromApiJsonHelper.extractStringNamed("emailId", element);
        		baseDataValidator.reset().parameter("emailId").value(emailId).notExceedingLengthOf(100).validateEmailAddress(emailId);
        	
        }
        
        if (this.fromApiJsonHelper.parameterExists("dateOfBirth", element)) {
			final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed("dateOfBirth", element);
			baseDataValidator.reset().parameter("dateOfBirth").value(dateOfBirth).futureDateValidation(dateOfBirth);
		}

		if (this.fromApiJsonHelper.parameterExists("contactNo", element)) {
			final String contactNo = this.fromApiJsonHelper.extractStringNamed("contactNo", element);
			baseDataValidator.reset().parameter("contactNo").value(contactNo).notBlank().minAndMaxLengthOfString(10, 10);
		}
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public static void validateEntityTypeforImage(final String entityName) {
        if (!checkValidEntityType(entityName)) { throw new InvalidEntityTypeForImageManagementException(entityName); }
    }
    
    private static boolean checkValidEntityType(final String entityType) {
        for (final ENTITY_TYPE_FOR_IMAGES entities : ENTITY_TYPE_FOR_IMAGES.values()) {
            if (entities.name().equalsIgnoreCase(entityType)) { return true; }
        }
        return false;
    }
}
