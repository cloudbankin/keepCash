package com.org.agent.service.implementation;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.serializer.Serializer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.org.agent.Serialization.ToJsonSerializer;
import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.CommandProcessingResultBuilder;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.controller.AgentConstants;
import com.org.agent.core.data.EnumOptionData;
import com.org.agent.data.AgentUserData;
import com.org.agent.enumType.AgentAccountStatusEnumType;
import com.org.agent.model.AgentUserEntity;
import com.org.agent.model.AppUser;
import com.org.agent.repository.AgentUserRepository;
import com.org.agent.repository.AppUserRepository;
import com.org.agent.service.DelegateUserService;
import com.org.agent.command.FromJsonHelper;

@Component
public class DelegateUserServiceImpl implements DelegateUserService{
	
	@Autowired
	private final AgentUserRepository agentUserRepository;
	private final AppUserServiceImpl appUserServiceImpl;
	private final AppUserRepository appUserRepository;
	private final ToJsonSerializer toJsonSerializer; 
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public DelegateUserServiceImpl(final AgentUserRepository agentUserRepository, final AppUserServiceImpl appUserServiceImpl
			,final AppUserRepository appUserRepository, final ToJsonSerializer toJsonSerializer,final FromJsonHelper fromJsonHelper) {
		this.agentUserRepository = agentUserRepository;
		this.appUserServiceImpl = appUserServiceImpl;
		this.appUserRepository = appUserRepository;
		this.toJsonSerializer = toJsonSerializer;
		this.fromApiJsonHelper = fromJsonHelper;
	}

	@Override
	public AgentUserData createDelegate(JsonCommand command, Long appUserId, Long parentUserId, Integer transactionPIN) {
		JsonElement element = command.jsonElement("location");
		AgentUserEntity agentUser = AgentUserEntity.createAgentUserEntity(command, appUserServiceImpl.findByIdAppUser(appUserId),
				appUserServiceImpl.findByIdAppUser(parentUserId), transactionPIN);
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
		}
		Integer status = AgentAccountStatusEnumType.ACTIVE.getValue();
		agentUser.setStatus(status);
		
		agentUser = agentUserRepository.save(agentUser);
		
		
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
        EnumOptionData enumOptionData = AppUserTypesEnumerations.appUserType(agentUser.getAppUserTypeEnum().intValue());
        
    	AppUser appUser = appUserRepository.findById(agentUser.getAppUser().getId()).get();
    	AgentUserData agentUserData = new AgentUserData(appUser.getId(), null, null, appUser.getEmail(), appUser.getUsername(), appUser.getFirstname(),
    			appUser.getLastname(),appUser.isEnabled(), appUser.isSelfServiceUser(), agentUser.getClientId(), enumOptionData, agentUser.getCompanyName(),
    			agentUser.getCompanyAddress(), null, agentUser.getMobileNo(),  agentUser.isAgreementSignUp(),
    			agentUser.isActive(), agentUser.getAuthMode(), agentUser.getImage(), agentUser.getImageEncryption(), format.format(agentUser.getCreatedOnDate()),
    		    agentUser.getLatitude(),agentUser.getLongitude(),agentUser.getLocationName(),agentUser.getLocationAddress(),agentUser.getIpAddress(),agentUser.getDeviceId(),agentUser.getEmployeeId(), null,
    		    null);
    	
    	return agentUserData;

	}
}
