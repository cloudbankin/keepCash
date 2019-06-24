package org.apache.fineract.portfolio.springBoot.data;

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
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
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
    
    public void validateForCreateCustomerUser(final String json) {
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
        

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForCreateCustomerDocument(final String name) {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("uploadAgentDocument");
        
        String documentName = name;
        baseDataValidator.reset().parameter("name").value(documentName).notBlank()
                .notExceedingLengthOf(30);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    public void validateForCreateCustomerIdentifier(final Long identifierType, final String identifierId) {
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("uploadAgentIdentifier");
        
        baseDataValidator.reset().parameter("identifierType").value(identifierType).notBlank()
        .notExceedingLengthOf(30).notLessThanMin(1);
        
        baseDataValidator.reset().parameter("identifierId").value(identifierId).notBlank()
                .notExceedingLengthOf(30).alphaNumericString(identifierId.trim());
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    private final Set<String> supportedParametersForDepositSavingsAccount = new HashSet<>(Arrays.asList("transactionDate", 
    		"transactionAmount", "paymentTypeId", "accountNumber", "checkNumber", "routingCode", "receiptNumber", "bankNumber",
    		"dateFormat", "locale"));
    
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
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate",
                element);
        baseDataValidator.reset().parameter("transactionDate").value(transactionDate).notNull().futureDateValidation(transactionDate);
        
        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount", element, Locale.ENGLISH);
        baseDataValidator.reset().parameter("transactionAmount").value(transactionAmount).positiveAmount().notNull();
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
}
