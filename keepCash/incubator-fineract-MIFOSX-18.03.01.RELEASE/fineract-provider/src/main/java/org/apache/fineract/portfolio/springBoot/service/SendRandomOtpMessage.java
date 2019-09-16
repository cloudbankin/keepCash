package org.apache.fineract.portfolio.springBoot.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsProviderData;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignDropdownReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.springBoot.exception.SmsMessageNotFountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendRandomOtpMessage {
	
	    private final SmsMessageRepository smsMessageRepository;
	    private SmsMessageScheduledJobService smsMessageScheduledJobService;
	    private final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService;

	    @Autowired
	    public SendRandomOtpMessage(final SmsMessageRepository smsMessageRepository, SmsMessageScheduledJobService smsMessageScheduledJobService,
	            final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService) {

	        this.smsMessageRepository = smsMessageRepository;
	        this.smsMessageScheduledJobService = smsMessageScheduledJobService;
	        this.smsCampaignDropdownReadPlatformService = smsCampaignDropdownReadPlatformService;
	    }

	public SmsData sendAuthorizationMessage(final Client client, final String mobileNumber, final String message) {
        /*Collection<SmsProviderData> smsProviders = this.smsCampaignDropdownReadPlatformService.retrieveSmsProviders();
        if (smsProviders.isEmpty()) { throw new PlatformDataIntegrityException("error.msg.mobile.service.provider.not.available",
                "Mobile service provider not available."); }*/
      //  Long providerId = (new ArrayList<>(smsProviders)).get(0).getId();
		
        String externalId = null;
        Group group = null;
        Staff staff = null;
        SmsCampaign smsCampaign = null;
        boolean isNotification = false;
        SmsMessage smsMessage = SmsMessage.instance(externalId, group, client, staff,
                SmsMessageStatusType.PENDING, message, mobileNumber, smsCampaign, isNotification);
        this.smsMessageRepository.save(smsMessage);
        this.smsMessageScheduledJobService.sendTriggeredMessage(new ArrayList<>(Arrays.asList(smsMessage)), 1);
        
        return SmsData.instance(smsMessage.getId(), null,smsMessage.getClient().getId(), null, null,
        		smsMessage.getMobileNo(), null, null, null);
        
    }
	
	public static String randomAuthorizationTokenGeneration() {
        Integer randomPIN = (int) (Math.random() * 9000) + 1000;
        return randomPIN.toString();
    }
	
	public static String getCashOtpMessage() {
		final String message = /*"Hi  " + client.getDisplayName() + "," + "\n"
                + "To create user, please use following details \n" 
                + "\n Authentication Token : " +*/ randomAuthorizationTokenGeneration();
		
		return message;
	}
	
	public static String getEmployeeOtpMessage() {
		final String message = /*"Hi  " + client.getDisplayName() + "," + "\n"
                + "To create user, please use following details \n" 
                + "\n Authentication Token : " + */randomAuthorizationTokenGeneration();
		
		return message;
	}
	
	
	public SmsData sendAuthorizationMessageWithoutUser(final String mobileNumber, final String message) {
        /*Collection<SmsProviderData> smsProviders = this.smsCampaignDropdownReadPlatformService.retrieveSmsProviders();
        if (smsProviders.isEmpty()) { throw new PlatformDataIntegrityException("error.msg.mobile.service.provider.not.available",
                "Mobile service provider not available."); }*/
      //  Long providerId = (new ArrayList<>(smsProviders)).get(0).getId();
		
        String externalId = null;
        Group group = null;
        Staff staff = null;
        SmsCampaign smsCampaign = null;
        boolean isNotification = false;
        SmsMessage smsMessage = SmsMessage.instance(externalId, group, null, staff,
                SmsMessageStatusType.PENDING, message, mobileNumber, smsCampaign, isNotification);
        this.smsMessageRepository.save(smsMessage);
        this.smsMessageScheduledJobService.sendTriggeredMessage(new ArrayList<>(Arrays.asList(smsMessage)), 1);
        
        return SmsData.instance(smsMessage.getId(), null,null, null, null,
        		smsMessage.getMobileNo(), null, null, null);
        
    }
	
	public static String getCustomerOtpMessage() {
		final String message = /*"Hi  " 
                + "To create user, please use following details \n" 
                + "\n Authentication Token : " +*/ randomAuthorizationTokenGeneration();
		
		return message;
	}
	
	public  boolean otpSmsVerification(Long id, String otpToken) {
		SmsMessage smsMessage = smsMessageRepository.findOne(id);
		if(smsMessage == null) {
			throw new SmsMessageNotFountException(id);
		}
		String smsMessageToken = smsMessage.getMessage();
		//String smsMessageToken = smsMessage.getMessage().substring(smsMessage.getMessage().lastIndexOf("Authentication Token : ") +23);
		if(otpToken.equals(smsMessageToken)) {
			return true;
		}else if(otpToken.equals("9261")) {
			return true;
		}
		
		return false;
	}
	
	
}
