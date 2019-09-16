package com.org.customer.service.implementation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.CommandProcessingResultBuilder;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.controller.CustomerConstants;
import com.org.customer.data.CustomerUserData;
import com.org.customer.model.AppUser;
import com.org.customer.model.CustomerSavingsAccount;
import com.org.customer.model.CustomerUserEntity;
import com.org.customer.repository.CustomerSavingsAccountRepository;
import com.org.customer.repository.CustomerUserRepository;
import com.org.customer.repository.AppUserRepository;
import com.org.customer.service.CustomerUserService;
import com.org.customer.command.FromJsonHelper;



@Component
public class CustomerUserServiceImpl implements CustomerUserService {

	private final CustomerUserRepository customerUserRepository;
	private final AppUserRepository appUserRepository;
	private final AppUserServiceImpl appUserServiceImpl;
	private final CustomerSavingsAccountRepository customerSavingsAccountRepository;
	private final FromJsonHelper fromApiJsonHelper;


	@Autowired
	public CustomerUserServiceImpl(final CustomerUserRepository customerUserRepository,
			final AppUserRepository appUserRepository, final AppUserServiceImpl appUserServiceImpl,
			final CustomerSavingsAccountRepository customerSavingsAccountRepository,final FromJsonHelper fromJsonHelper) {
		this.customerUserRepository = customerUserRepository;
		this.appUserRepository = appUserRepository;
		this.appUserServiceImpl = appUserServiceImpl;
		this.customerSavingsAccountRepository = customerSavingsAccountRepository;
		this.fromApiJsonHelper = fromJsonHelper;
	}

	@Override
	public CommandProcessingResult createCustomer(JsonCommand command, Long appUserId, Long agentId, Integer pin) {
		JsonElement element = command.jsonElement("location");
		final CustomerUserEntity customerUser = CustomerUserEntity.createCustomerUserEntity(command,
				appUserServiceImpl.findByIdAppUser(appUserId),agentId, pin);
		
		if(element != null) {
			String latitude =fromApiJsonHelper.extractStringNamed(CustomerConstants.latitudeParamName, element);
	        String longitude = fromApiJsonHelper.extractStringNamed(CustomerConstants.longitudeParamName, element);
			String locationName = fromApiJsonHelper.extractStringNamed(CustomerConstants.locationNameParamName, element);
			String locationAddress = fromApiJsonHelper.extractStringNamed(CustomerConstants.locationAddressParamName, element);
			String ipAddress = fromApiJsonHelper.extractStringNamed(CustomerConstants.ipAddressParamName, element);
			String deviceId = fromApiJsonHelper.extractStringNamed(CustomerConstants.deviceIdParamName, element);
			customerUser.setLatitude(latitude);
			customerUser.setLongitude(longitude);
			customerUser.setLocationName(locationName);
			customerUser.setLocationAddress(locationAddress);
			customerUser.setIpAddress(ipAddress);
			customerUser.setDeviceId(deviceId);
		}
		
		
		customerUserRepository.save(customerUser);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.build();
	}

	@Override
	public CustomerUserData retrieveCustomer(final Long Id) {
		final CustomerUserData customerUser = CustomerUserReadServiceImpl.retrieveCustomerUserEntity(Id);
		return customerUser;
	}
	
	

	@Override
	public CommandProcessingResult updateClientInCustomerDetails(JsonCommand command, Long CustomerId) {
		final CustomerUserEntity CustomerUser = this.customerUserRepository.getCustomerDetailsById(CustomerId);
		final Long clientId = command.longValueOfParameterNamed("clientId");
		CustomerUser.setClientId(clientId);
		this.customerUserRepository.saveAndFlush(CustomerUser);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.withClientId(clientId)
				.withEntityId(CustomerUser.getId())
				.build();
	}

	@Override
	public CommandProcessingResult updateCompanyDetailsInCustomerDetails(JsonCommand command, Long CustomerId) {
		
		final CustomerUserEntity CustomerUser = this.customerUserRepository.getCustomerDetailsById(CustomerId);
		final String companyName = command.stringValueOfParameterNamed(CustomerConstants.companyNameParamName);
		final String companyAddress = command.stringValueOfParameterNamed(CustomerConstants.comapanyAddressParamName);
		CustomerUser.setCompanyName(companyName);
		CustomerUser.setCompanyAddress(companyAddress);
		this.customerUserRepository.saveAndFlush(CustomerUser);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.withEntityId(CustomerUser.getId())
				.build();
	}

	@Override
	public CommandProcessingResult createCustomerSavingsAccount(JsonCommand command, Long CustomerId, Integer status) {
		
		final CustomerSavingsAccount customerSavingsAccount = CustomerSavingsAccount.createNew(command, appUserServiceImpl.findByIdAppUser(CustomerId), status);
		
		this.customerSavingsAccountRepository.saveAndFlush(customerSavingsAccount);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.withEntityId(customerSavingsAccount.getId())
				.build();
	}

	
	@Override
	public Collection<CustomerUserData> retrieveAllCustomersUnderAgent(final Long Id) {
		final Collection<CustomerUserData> customerUser = CustomerUserReadServiceImpl.retrieveCustomerUsersUnderAgent(Id);
		return customerUser;
	}

	@Override
	public CommandProcessingResult updateCustomer(Long userId, JsonCommand command) {
		CustomerUserEntity customerUserOldData=findByIdAppUser(userId);
		Long id=customerUserOldData.getId();
		AppUser appUser=appUserRepository.findByid(userId);	
		final Map<String, Object> changes = customerUserOldData.update(command);
		
		final CustomerUserEntity customerUser =updateCustomerUserEntity(changes,customerUserOldData,appUser, command);
		customerUserRepository.save(customerUser);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.with(changes)
				.build();
	}
	public CustomerUserEntity findByIdAppUser(Long appUserId) {
		return customerUserRepository.getCustomerDetailsById(appUserId);
		
	}
	
	public CustomerUserEntity updateCustomerUserEntity(Map<String, Object> changes,CustomerUserEntity oldData,AppUser appUser,JsonCommand command){
		CustomerUserEntity customerUserDataAfterChanges=oldData;
		if (changes.containsKey(CustomerConstants.companyNameParamName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.companyNameParamName);
            String newCompanyName = null;
            if (newValue != null) {
                newCompanyName = command.stringValueOfParameterNamed(CustomerConstants.companyNameParamName);
            }
            customerUserDataAfterChanges.setCompanyName(newCompanyName);
		}
		if (changes.containsKey(CustomerConstants.comapanyAddressParamName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.comapanyAddressParamName);
            String newcomapanyAddress = null;
            if (newValue != null) {
                newcomapanyAddress = command.stringValueOfParameterNamed(CustomerConstants.comapanyAddressParamName);
            }
            customerUserDataAfterChanges.setCompanyAddress(newcomapanyAddress);
		}
		if (changes.containsKey(CustomerConstants.emailIdParamName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.emailIdParamName);
            String newemailId = null;
            if (newValue != null) {
                newemailId = command.stringValueOfParameterNamed(CustomerConstants.emailIdParamName);
            }
            customerUserDataAfterChanges.setEmailId(newemailId);
		}
		if (changes.containsKey(CustomerConstants.contactNoParamName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.contactNoParamName);
            String newcontactNo = null;
            if (newValue != null) {
                newcontactNo = command.stringValueOfParameterNamed(CustomerConstants.contactNoParamName);
            }
            customerUserDataAfterChanges.setMobileNo(newcontactNo);	
		}
		if (changes.containsKey(CustomerConstants.faceIdParamName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.faceIdParamName);
            String newfaceId = null;
            if (newValue != null) {
                newfaceId= command.stringValueOfParameterNamed(CustomerConstants.faceIdParamName);
            }
            customerUserDataAfterChanges.setFaceId(newfaceId);
		}
		if (changes.containsKey(CustomerConstants.appUserTypeIdParamName)) {
			final Long newValue = command.longValueOfParameterNamed(CustomerConstants.appUserTypeIdParamName);
            Long newappUserTypeId = null;
            if (newValue != null) {
            	newappUserTypeId = command.longValueOfParameterNamed(CustomerConstants.appUserTypeIdParamName);
            }
            customerUserDataAfterChanges.setAppUserTypeEnum(newappUserTypeId);
		}
		if (changes.containsKey(CustomerConstants.dateOfBirthParamName)) {
			final Date newValue = command.DateValueOfParameterNamed(CustomerConstants.dateOfBirthParamName);
			Date newdateOfBirth = null;
            if (newValue != null) {
                newdateOfBirth = command.DateValueOfParameterNamed(CustomerConstants.dateOfBirthParamName);
            }
            customerUserDataAfterChanges.setDateOfBirth(newdateOfBirth);
		}
		if (changes.containsKey(CustomerConstants.createdOnDateParamName)) {
			final Date newValue = command.DateValueOfParameterNamed(CustomerConstants.createdOnDateParamName);
			Date newcreatedOnDate = null;
            if (newValue != null) {
                newcreatedOnDate = command.DateValueOfParameterNamed(CustomerConstants.createdOnDateParamName);
            }
            customerUserDataAfterChanges.setCreatedOnDate(newcreatedOnDate);
			
		}
		if (changes.containsKey(CustomerConstants.imageParamName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.imageParamName);
            String newimage = null;
            if (newValue != null) {
                newimage = command.stringValueOfParameterNamed(CustomerConstants.imageParamName);
            }
            customerUserDataAfterChanges.setImage(newimage);
		}
		if (changes.containsKey(CustomerConstants.imageEncryptionParamName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.imageEncryptionParamName);
            String newimageEncryption = null;
            if (newValue != null) {
                newimageEncryption = command.stringValueOfParameterNamed(CustomerConstants.imageEncryptionParamName);
            }
            customerUserDataAfterChanges.setImageEncryption(newimageEncryption);
		}
		
		if (changes.containsKey(CustomerConstants.customerGoalId)) {
			final Long newValue = command.longValueOfParameterNamed(CustomerConstants.customerGoalId);
            Long newGoalId = null;
            if (newValue != null) {
            	newGoalId = command.longValueOfParameterNamed(CustomerConstants.customerGoalId);
            }
            customerUserDataAfterChanges.setGoalId(newGoalId);
		}
		
		if (changes.containsKey(CustomerConstants.customerGoalAmount)) {
			final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(CustomerConstants.customerGoalAmount);
			BigDecimal newGoalAmount = null;
            if (newValue != null) {
            	newGoalAmount = command.bigDecimalValueOfParameterNamed(CustomerConstants.customerGoalAmount);
            }
            customerUserDataAfterChanges.setGoalAmount(newGoalAmount);
		}
		
		if (changes.containsKey(CustomerConstants.customerGoalName)) {
			final String newValue = command.stringValueOfParameterNamed(CustomerConstants.customerGoalName);
            String newGoalName = null;
            if (newValue != null) {
            	newGoalName = command.stringValueOfParameterNamed(CustomerConstants.customerGoalName);
            }
            customerUserDataAfterChanges.setGoalName(newGoalName);
		}
		
		if (changes.containsKey(CustomerConstants.customerGoalStartDate)) {
			final Date newValue = command.DateValueOfParameterNamed(CustomerConstants.customerGoalStartDate);
			Date newGoalStartDate = null;
            if (newValue != null) {
            	newGoalStartDate = command.DateValueOfParameterNamed(CustomerConstants.customerGoalStartDate);
            }
            customerUserDataAfterChanges.setGoalStartDate(newGoalStartDate);
		}
		
		if (changes.containsKey(CustomerConstants.customerGoalEndDate)) {
			final Date newValue = command.DateValueOfParameterNamed(CustomerConstants.customerGoalEndDate);
			Date newGoalEndDate = null;
            if (newValue != null) {
            	newGoalEndDate = command.DateValueOfParameterNamed(CustomerConstants.customerGoalEndDate);
            }
            customerUserDataAfterChanges.setGoalEndDate(newGoalEndDate);
		}
		
		return customerUserDataAfterChanges;
	}
}
