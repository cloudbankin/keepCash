package com.org.customer.data;

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

import com.org.customer.command.exception.ApiErrorMessageArg;
import com.org.customer.command.exception.ApiParameterError;
import com.org.customer.validation.DataValidatorBuilder;
import com.org.customer.command.exception.InvalidJsonException;
import com.org.customer.command.exception.PlatformApiDataValidationException;
import com.org.customer.command.service.ThrowErrorMessage;
import com.org.customer.command.FromJsonHelper;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class CustomerDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CustomerDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }
    
    private final Set<String> supportedParametersForAgentCreation = new HashSet<>(Arrays.asList("userName", 
    		"firstName", "lastName",
    		"emailId", "authenticationMode", "password", "faceId", "appUserTypeId", "dateFormat", "locale",
    		"imageEncryption", "image", "contactNo", "dateOfBirth", "isAgreementSignUp", "isActive"));
    
    public String validateForCreateCustomerUser(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForAgentCreation);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("createAgentUser");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String userName = this.fromApiJsonHelper.extractStringNamed("userName", element);
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
        baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(30);
        
		if (this.fromApiJsonHelper.parameterExists("faceId", element)) {
			final String faceId = this.fromApiJsonHelper.extractStringNamed("faceId", element);
			baseDataValidator.reset().parameter("faceId").value(faceId).notExceedingLengthOf(30);
		}

		if (this.fromApiJsonHelper.parameterExists("appUserTypeId", element)) {
			final Long appUserTypeId = this.fromApiJsonHelper.extractLongNamed("appUserTypeId", element);
			baseDataValidator.reset().parameter("appUserTypeId").value(appUserTypeId).notBlank().longNotEqualToOne();
		}
		if (this.fromApiJsonHelper.parameterExists("contactNo", element)) {
			final String contactNo = this.fromApiJsonHelper.extractStringNamed("contactNo", element);
			baseDataValidator.reset().parameter("contactNo").value(contactNo).minAndMaxLengthOfString(10, 10);
		}
		if (this.fromApiJsonHelper.parameterExists("dateOfBirth", element)) {
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
        
        Boolean isActive = null;
        if(this.fromApiJsonHelper.parameterExists("isActive", element)){
        	isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
        	if(isActive == null){
        		baseDataValidator.reset().parameter("isActive").trueOrFalseRequired(false);
        	}
        }
        
        return ThrowErrorMessage.errorMessageHandler(dataValidationErrors);
        //throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    public String validateForUpdateCustomerUser(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForAgentCreation);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("createAgentUser");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String userName = this.fromApiJsonHelper.extractStringNamed("userName", element);
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
        baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(30);
        
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

        Boolean isAgreementSignUp = null;
        if(this.fromApiJsonHelper.parameterExists("isAgreementSignUp", element)){
        	isAgreementSignUp = this.fromApiJsonHelper.extractBooleanNamed("isAgreementSignUp", element);
        	if(isAgreementSignUp == null){
        		baseDataValidator.reset().parameter("isAgreementSignUp").trueOrFalseRequired(false);
        	}
        }
        
        Boolean isActive = null;
        if(this.fromApiJsonHelper.parameterExists("isActive", element)){
        	isActive = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
        	if(isActive == null){
        		baseDataValidator.reset().parameter("isActive").trueOrFalseRequired(false);
        	}
        }
        
        return ThrowErrorMessage.errorMessageHandler(dataValidationErrors);
        //throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
   
    
}
