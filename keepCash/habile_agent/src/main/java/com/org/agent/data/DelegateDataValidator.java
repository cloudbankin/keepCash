package com.org.agent.data;

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
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.exception.ApiParameterError;
import com.org.agent.command.exception.InvalidJsonException;
import com.org.agent.command.exception.PlatformApiDataValidationException;
import com.org.agent.command.service.ThrowErrorMessage;
import com.org.agent.validation.DataValidatorBuilder;

@Component
public class DelegateDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    
    @Autowired
    public DelegateDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }
    
    private final Set<String> supportedParametersForDelegateCreation = new HashSet<>(Arrays.asList("userName", 
    		"firstName", "lastName",
    		"emailId", "authenticationMode", "password", "faceId", "appUserTypeId", "dateFormat", "locale",
    		"imageEncryption", "image", "contactNo", "dateOfBirth", "isAgreementSignUp", "isActive", "clientId",
    		"location","latitude" , "longitude" , "locationName" , "locationAddress" , "ipAddress" , "deviceId", "faceUniqueId"));
    
    public String validateForCreateDelegateUser(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForDelegateCreation);

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
        
        if(this.fromApiJsonHelper.parameterExists("emailId", element) &&
        		this.fromApiJsonHelper.extractStringNamed("emailId", element) != null) {
        final String emailId = this.fromApiJsonHelper.extractStringNamed("emailId", element);
        baseDataValidator.reset().parameter("emailId").value(emailId).notBlank().notExceedingLengthOf(30).validateEmailAddress(emailId);
        }
        
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
        	
        		baseDataValidator.reset().parameter("isAgreementSignUp").trueOrFalseRequired(isAgreementSignUp);
        	
        }
        
       
        if(this.fromApiJsonHelper.parameterExists("isActive", element)){
        	final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
        	
        		baseDataValidator.reset().parameter("isActive").trueOrFalseRequired(isActive);
        	
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

        return ThrowErrorMessage.errorMessageHandler(dataValidationErrors);
    }
    public String validateForUpdateDelegateUser(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForDelegateCreation);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("updateDelegateUser");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        /*final String userName = this.fromApiJsonHelper.extractStringNamed("userName", element);
        baseDataValidator.reset().parameter("userName").value(userName).notBlank().notExceedingLengthOf(50);

        final String firstName = this.fromApiJsonHelper.extractStringNamed("firstName", element);
        baseDataValidator.reset().parameter("firstName").value(firstName).notBlank().notExceedingLengthOf(30);

        final String lastName = this.fromApiJsonHelper.extractStringNamed("lastName", element);
        baseDataValidator.reset().parameter("lastName").value(lastName).notBlank().notExceedingLengthOf(30);
        
        final String emailId = this.fromApiJsonHelper.extractStringNamed("emailId", element);
        baseDataValidator.reset().parameter("emailId").value(emailId).notBlank().notExceedingLengthOf(30).validateEmailAddress(emailId);

        final String authenticationMode = this.fromApiJsonHelper.extractStringNamed("authenticationMode", element);
        baseDataValidator.reset().parameter("authenticationMode").value(authenticationMode).notBlank().notExceedingLengthOf(30);

        final String password = this.fromApiJsonHelper.extractStringNamed("password", element);
        baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(30);*/
        
		if (this.fromApiJsonHelper.parameterExists("faceId", element)) {
			final String faceId = this.fromApiJsonHelper.extractStringNamed("faceId", element);
			baseDataValidator.reset().parameter("faceId").value(faceId).notExceedingLengthOf(30);
		}

		/*if (this.fromApiJsonHelper.parameterExists("appUserTypeId", element)) {
			final Long appUserTypeId = this.fromApiJsonHelper.extractLongNamed("appUserTypeId", element);
			baseDataValidator.reset().parameter("appUserTypeId").value(appUserTypeId).notBlank();//.longNotEqualToOneAndThree();
		}*/
		if (this.fromApiJsonHelper.parameterExists("contactNo", element)) {
			final String contactNo = this.fromApiJsonHelper.extractStringNamed("contactNo", element);
			baseDataValidator.reset().parameter("contactNo").value(contactNo).minAndMaxLengthOfString(10, 10);
		}
		/*if (this.fromApiJsonHelper.parameterExists("dateOfBirth", element)) {
			final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed("dateOfBirth", element);
			baseDataValidator.reset().parameter("dateOfBirth").value(dateOfBirth).futureDateValidation(dateOfBirth);
		}

        Boolean isAgreementSignUp = null;
        if(this.fromApiJsonHelper.parameterExists("isAgreementSignUp", element)){
        	isAgreementSignUp = this.fromApiJsonHelper.extractBooleanNamed("isAgreementSignUp", element);
        	if(isAgreementSignUp == null){
        		baseDataValidator.reset().parameter("isAgreementSignUp").trueOrFalseRequired(false);
        	}
        }
        */
      /*  Boolean isActive = null;
        if(this.fromApiJsonHelper.parameterExists("isActive", element)){
        	isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
        	if(isActive == null){
        		baseDataValidator.reset().parameter("isActive").trueOrFalseRequired(false);
        	}
        }*/
        

        return ThrowErrorMessage.errorMessageHandler(dataValidationErrors);
    }
    
    public void validateForCreateAgentDocument(final String name) {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("uploadAgentDocument");
        
        String documentName = name;
        baseDataValidator.reset().parameter("name").value(documentName).notBlank()
                .notExceedingLengthOf(30);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForCreateAgentIdentifier(final Long identifierType, final String identifierId) {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("uploadAgentIdentifier");
        
        baseDataValidator.reset().parameter("identifierType").value(identifierType).notBlank()
        .notExceedingLengthOf(30).notLessThanMin(1);
        
        baseDataValidator.reset().parameter("identifierId").value(identifierId).notBlank()
                .notExceedingLengthOf(30).alphaNumericString(identifierId.trim());
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
}
