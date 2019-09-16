package com.org.agent.service.implementation;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.CommandProcessingResultBuilder;
import com.org.agent.command.FromJsonHelper;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.controller.AgentConstants;
import com.org.agent.data.AgentUserData;
import com.org.agent.model.AgentSavingsAccount;
import com.org.agent.model.AgentUserEntity;
import com.org.agent.model.AppUser;
import com.org.agent.repository.AgentSavingsAccountRepository;
import com.org.agent.repository.AgentUserRepository;
import com.org.agent.repository.AppUserRepository;
import com.org.agent.service.AgentUserService;




@Component
public class AgentUserServiceImpl implements AgentUserService {

	private final AgentUserRepository agentUserRepository;
	private final AppUserRepository appUserRepository;
	private final AppUserServiceImpl appUserServiceImpl;
	private final AgentSavingsAccountRepository agentSavingsAccountRepository;
	private final FromJsonHelper fromApiJsonHelper;


	@Autowired
	public AgentUserServiceImpl(final AgentUserRepository agentUserRepository,
			final AppUserRepository appUserRepository, final AppUserServiceImpl appUserServiceImpl,
			final AgentSavingsAccountRepository agentSavingsAccountRepository,final FromJsonHelper fromJsonHelper) {
		this.agentUserRepository = agentUserRepository;
		this.appUserRepository = appUserRepository;
		this.appUserServiceImpl = appUserServiceImpl;
		this.agentSavingsAccountRepository = agentSavingsAccountRepository;
		this.fromApiJsonHelper = fromJsonHelper;
	}

	@Override
	public CommandProcessingResult createAgent(JsonCommand command, Long appUserId, Long parentUserId, Integer transactionPin) {
		JsonElement element = command.jsonElement("location");
		final AgentUserEntity agentUser = AgentUserEntity.createAgentUserEntity(command, appUserServiceImpl.findByIdAppUser(appUserId)
				, appUserServiceImpl.findByIdAppUser(parentUserId), transactionPin);
		if(element != null) {
			String latitude =fromApiJsonHelper.extractStringNamed(AgentConstants.latitudeParamName, element);
	        String longitude = fromApiJsonHelper.extractStringNamed(AgentConstants.longitudeParamName, element);
			String locationName = fromApiJsonHelper.extractStringNamed(AgentConstants.locationNameParamName, element);
			String locationAddress = fromApiJsonHelper.extractStringNamed(AgentConstants.locationAddressParamName, element);
			String ipAddress = fromApiJsonHelper.extractStringNamed(AgentConstants.ipAddressParamName, element);
			String deviceId = fromApiJsonHelper.extractStringNamed(AgentConstants.deviceIdParamName, element);
			agentUser.setLatitude(latitude);
			agentUser.setLongitude(longitude);
			agentUser.setLocationName(locationName);
			agentUser.setLocationAddress(locationAddress);
			agentUser.setIpAddress(ipAddress);
			agentUser.setDeviceId(deviceId);
			agentUser.setTransactionPin(transactionPin);
		}
		  	
		agentUserRepository.save(agentUser);
		
		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.build();
	}

	@Override
	public AgentUserData retrieveAgent(final Long Id) {
		final AgentUserData agentUser = AgentUserReadServiceImpl.retrieveAgentUserEntity(Id);
		return agentUser;
	}
	
	@Override
	public Collection<AgentUserData> retrieveAllAgents() {
		final Collection<AgentUserData> agentUsers = AgentUserReadServiceImpl.getAllAgentUser();
		return agentUsers;
	}
	
	@Override
	public CommandProcessingResult updateClientInAgentDetails(JsonCommand command, Long agentId) {
		final AgentUserEntity agentUser = this.agentUserRepository.getAgentDetailsById(agentId);
		final Long clientId = command.longValueOfParameterNamed("clientId");
		agentUser.setClientId(clientId);
		this.agentUserRepository.saveAndFlush(agentUser);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.withClientId(clientId)
				.withEntityId(agentUser.getId())
				.build();
	}
	
	@Override
	public CommandProcessingResult updateCompanyDetailsInAgentDetails(JsonCommand command, Long agentId) {
		
		final AgentUserEntity agentUser = this.agentUserRepository.getAgentDetailsById(agentId);
		final String companyName = command.stringValueOfParameterNamed(AgentConstants.companyNameParamName);
		final String companyAddress = command.stringValueOfParameterNamed(AgentConstants.comapanyAddressParamName);
		agentUser.setCompanyName(companyName);
		agentUser.setCompanyAddress(companyAddress);
		this.agentUserRepository.saveAndFlush(agentUser);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.withEntityId(agentUser.getId())
				.build();
	}
	
	@Override
	public CommandProcessingResult createAgentSavingsAccount(JsonCommand command, Long agentId, Integer status) {
		
		final AgentSavingsAccount agentSavingsAccount = AgentSavingsAccount.createNew(command, appUserServiceImpl.findByIdAppUser(agentId), status);
		
		this.agentSavingsAccountRepository.saveAndFlush(agentSavingsAccount);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.withEntityId(agentSavingsAccount.getId())
				.build();
	}
	
	@Override
	public Collection<AgentUserData> getByParentUserId(Long userId) {
		final Collection<AgentUserData> agentUsers = AgentUserReadServiceImpl.getDelegatesByParentId(userId);
		return agentUsers;
	}
	@Override
	public CommandProcessingResult updateAgent(Long userId, JsonCommand command) {
		AgentUserEntity agentUserOldData=findByIdAppUser(userId);
		Long id=agentUserOldData.getId();
		AppUser appUser=appUserRepository.findByid(userId);	
		final Map<String, Object> changes = agentUserOldData.update(command);
		
		//final CustomerUserEntity customerUser = CustomerUserEntity.updateCustomerUserEntity(changes,data,appUser);
			/*	appUserServiceImpl.findByIdAppUser(userId));*/
		final AgentUserEntity agentUser =updateAgentUserEntity(changes,agentUserOldData,appUser, command);

		agentUserRepository.save(agentUser);

		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.with(changes)
				.build();
	}
	public AgentUserEntity findByIdAppUser(Long appUserId) {
		return agentUserRepository.getAgentDetailsById(appUserId);
		
	}
	
	public AgentUserEntity updateAgentUserEntity(Map<String, Object> changes,AgentUserEntity oldData,AppUser appUser,JsonCommand command){
		AgentUserEntity customerUserDataAfterChanges=oldData;
		if (changes.containsKey(AgentConstants.companyNameParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.companyNameParamName);
            String newCompanyName = null;
            if (newValue != null) {
                newCompanyName = command.stringValueOfParameterNamed(AgentConstants.companyNameParamName);
            }
            customerUserDataAfterChanges.setCompanyName(newCompanyName);
		}
		if (changes.containsKey(AgentConstants.comapanyAddressParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.comapanyAddressParamName);
            String newcomapanyAddress = null;
            if (newValue != null) {
                newcomapanyAddress = command.stringValueOfParameterNamed(AgentConstants.comapanyAddressParamName);
            }
            customerUserDataAfterChanges.setCompanyAddress(newcomapanyAddress);
		}
		if (changes.containsKey(AgentConstants.emailIdParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.emailIdParamName);
            String newemailId = null;
            if (newValue != null) {
                newemailId = command.stringValueOfParameterNamed(AgentConstants.emailIdParamName);
            }
            customerUserDataAfterChanges.setEmailId(newemailId);
		}
		if (changes.containsKey(AgentConstants.contactNoParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.contactNoParamName);
            String newcontactNo = null;
            if (newValue != null) {
                newcontactNo = command.stringValueOfParameterNamed(AgentConstants.contactNoParamName);
            }
            customerUserDataAfterChanges.setMobileNo(newcontactNo);	
		}
		if (changes.containsKey(AgentConstants.faceIdParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.faceIdParamName);
            String newfaceId = null;
            if (newValue != null) {
                newfaceId= command.stringValueOfParameterNamed(AgentConstants.faceIdParamName);
            }
            customerUserDataAfterChanges.setFaceId(newfaceId);
		}
		if (changes.containsKey(AgentConstants.appUserTypeIdParamName)) {
			final Long newValue = command.longValueOfParameterNamed(AgentConstants.appUserTypeIdParamName);
            Long newappUserTypeId = null;
            if (newValue != null) {
            	newappUserTypeId = command.longValueOfParameterNamed(AgentConstants.appUserTypeIdParamName);
            }
            customerUserDataAfterChanges.setAppUserTypeEnum(newappUserTypeId);
		}
		if (changes.containsKey(AgentConstants.dateOfBirthParamName)) {
			final Date newValue = command.DateValueOfParameterNamed(AgentConstants.dateOfBirthParamName);
			Date newdateOfBirth = null;
            if (newValue != null) {
                newdateOfBirth = command.DateValueOfParameterNamed(AgentConstants.dateOfBirthParamName);
            }
            customerUserDataAfterChanges.setDateOfBirth(newdateOfBirth);
		}
		if (changes.containsKey(AgentConstants.createdOnDateParamName)) {
			final Date newValue = command.DateValueOfParameterNamed(AgentConstants.createdOnDateParamName);
			Date newcreatedOnDate = null;
            if (newValue != null) {
                newcreatedOnDate = command.DateValueOfParameterNamed(AgentConstants.createdOnDateParamName);
            }
            customerUserDataAfterChanges.setCreatedOnDate(newcreatedOnDate);
			
		}
		if (changes.containsKey(AgentConstants.imageParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.imageParamName);
            String newimage = null;
            if (newValue != null) {
                newimage = command.stringValueOfParameterNamed(AgentConstants.imageParamName);
            }
            customerUserDataAfterChanges.setImage(newimage);
		}
		if (changes.containsKey(AgentConstants.imageEncryptionParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.imageEncryptionParamName);
            String newimageEncryption = null;
            if (newValue != null) {
                newimageEncryption = command.stringValueOfParameterNamed(AgentConstants.imageEncryptionParamName);
            }
            customerUserDataAfterChanges.setImageEncryption(newimageEncryption);
		}
		if (changes.containsKey(AgentConstants.employeeIdParamName)) {
			final Long newValue = command.longValueOfParameterNamed(AgentConstants.employeeIdParamName);
            Long newEmployeeId = null;
            if (newValue != null) {
            	newEmployeeId = command.longValueOfParameterNamed(AgentConstants.employeeIdParamName);
            }
            customerUserDataAfterChanges.setEmployeeId(newEmployeeId);
		}
		
		if (changes.containsKey(AgentConstants.faceUniqueIdParamName)) {
			final String newValue = command.stringValueOfParameterNamed(AgentConstants.faceUniqueIdParamName);
            String faceUniqueId = null;
            if (newValue != null) {
            	faceUniqueId = command.stringValueOfParameterNamed(AgentConstants.faceUniqueIdParamName);
            }
            customerUserDataAfterChanges.setFaceUniqueId(faceUniqueId);
		}
		
		if (changes.containsKey(AgentConstants.statusParamName)) {
			final Integer newValue = command.integerValueOfParameterNamed(AgentConstants.statusParamName, Locale.ENGLISH);
            Integer status = null;
            if (newValue != null) {
            	status = command.integerValueOfParameterNamed(AgentConstants.statusParamName, Locale.ENGLISH);
            }
            customerUserDataAfterChanges.setStatus(status);
		}
		
		return customerUserDataAfterChanges;
	}
}
