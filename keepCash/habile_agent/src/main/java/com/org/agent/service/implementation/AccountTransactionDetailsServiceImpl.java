package com.org.agent.service.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.org.agent.command.CommandProcessingResult;
import com.org.agent.command.CommandProcessingResultBuilder;
import com.org.agent.command.api.JsonCommand;
import com.org.agent.controller.AccountTransactionConstants;
import com.org.agent.model.AccountTransactionDetails;
import com.org.agent.repository.AccountTransactionDetailsRepository;
import com.org.agent.service.AccountTransactionDetailsService;
import com.org.agent.command.FromJsonHelper;

@Component
public class AccountTransactionDetailsServiceImpl implements AccountTransactionDetailsService{

	@Autowired
	private final AccountTransactionDetailsRepository accountTransactionDetailsRepository; 
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public AccountTransactionDetailsServiceImpl(final AccountTransactionDetailsRepository accountTransactionDetailsRepository,final FromJsonHelper fromJsonHelper) {
		this.accountTransactionDetailsRepository = accountTransactionDetailsRepository;
		this.fromApiJsonHelper = fromJsonHelper;
	}
	
	@Override
	public CommandProcessingResult createAccountTransaction(JsonCommand command) {
		JsonElement element = command.jsonElement("location");
		AccountTransactionDetails accountTransactionDetails = AccountTransactionDetails.newAccountTransactionDetails(command);
		
		String latitude =fromApiJsonHelper.extractStringNamed(AccountTransactionConstants.latitudeParamName, element);
        String longitude = fromApiJsonHelper.extractStringNamed(AccountTransactionConstants.longitudeParamName, element);
		String locationName = fromApiJsonHelper.extractStringNamed(AccountTransactionConstants.locationNameParamName, element);
		String locationAddress = fromApiJsonHelper.extractStringNamed(AccountTransactionConstants.locationAddressParamName, element);
		String ipAddress = fromApiJsonHelper.extractStringNamed(AccountTransactionConstants.ipAddressParamName, element);
		String deviceId = fromApiJsonHelper.extractStringNamed(AccountTransactionConstants.deviceIdParamName, element);
		accountTransactionDetails.setLatitude(latitude);
		accountTransactionDetails.setLongitude(longitude);
		accountTransactionDetails.setLocationName(locationName);
		accountTransactionDetails.setLocationAddress(locationAddress);
		accountTransactionDetails.setIpAddress(ipAddress);
		accountTransactionDetails.setDeviceId(deviceId);
		
		this.accountTransactionDetailsRepository.save(accountTransactionDetails);
		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.build();
	}
}
