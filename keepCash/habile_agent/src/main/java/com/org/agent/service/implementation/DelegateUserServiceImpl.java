package com.org.agent.service.implementation;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.serializer.Serializer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.agent.Serialization.ToJsonSerializer;
import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.CommandProcessingResultBuilder;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.core.data.EnumOptionData;
import com.org.agent.data.AgentUserData;
import com.org.agent.model.AgentUserEntity;
import com.org.agent.model.AppUser;
import com.org.agent.repository.AgentUserRepository;
import com.org.agent.repository.AppUserRepository;
import com.org.agent.service.DelegateUserService;

@Component
public class DelegateUserServiceImpl implements DelegateUserService{
	
	@Autowired
	private final AgentUserRepository agentUserRepository;
	private final AppUserServiceImpl appUserServiceImpl;
	private final AppUserRepository appUserRepository;
	private final ToJsonSerializer toJsonSerializer; 
	
	@Autowired
	public DelegateUserServiceImpl(final AgentUserRepository agentUserRepository, final AppUserServiceImpl appUserServiceImpl
			,final AppUserRepository appUserRepository, final ToJsonSerializer toJsonSerializer) {
		this.agentUserRepository = agentUserRepository;
		this.appUserServiceImpl = appUserServiceImpl;
		this.appUserRepository = appUserRepository;
		this.toJsonSerializer = toJsonSerializer;
	}

	@Override
	public AgentUserData createDelegate(JsonCommand command, Long appUserId, Long parentUserId) {
		AgentUserEntity agentUser = AgentUserEntity.createAgentUserEntity(command, appUserServiceImpl.findByIdAppUser(appUserId),
				appUserServiceImpl.findByIdAppUser(parentUserId)	);
		agentUser = agentUserRepository.save(agentUser);
		
        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
        EnumOptionData enumOptionData = AppUserTypesEnumerations.appUserType(agentUser.getAppUserTypeEnum().intValue());
        
    	AppUser appUser = appUserRepository.findById(agentUser.getAppUser().getId()).get();
    	AgentUserData agentUserData = new AgentUserData(appUser.getId(), null, null, appUser.getEmail(), appUser.getUsername(), appUser.getFirstname(),
    			appUser.getLastname(),appUser.isEnabled(), appUser.isSelfServiceUser(), agentUser.getClientId(), enumOptionData, agentUser.getCompanyName(),
    			agentUser.getCompanyAddress(), format.format(agentUser.getDateOfBirth()), agentUser.getMobileNo(), agentUser.getFaceId(), agentUser.isAgreementSignUp(),
    			agentUser.isActive(), agentUser.getAuthMode(), agentUser.getImage(), agentUser.getImageEncryption(), format.format(agentUser.getCreatedOnDate()));
    	
    	return agentUserData;

	}
}
