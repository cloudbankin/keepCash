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
        		smsMessage.getMobileNo(), smsMessage.getMessage(), null, null);
        
    }
	
	public static String randomAuthorizationTokenGeneration() {
        Integer randomPIN = (int) (Math.random() * 9000) + 1000;
        return randomPIN.toString();
    }
	
	public static String getDelegateOtpMessage(final Client client) {
		final String message = "Hi  " + client.getDisplayName() + "," + "\n"
                + "To create user, please use following details \n" 
                + "\n Authentication Token : " + randomAuthorizationTokenGeneration();
		
		return message;
	}
	
	public  boolean otpSmsVerification(Long id, String otpToken) {
		SmsMessage smsMessage = smsMessageRepository.findOne(id);
		if(smsMessage == null) {
			throw new SmsMessageNotFountException(id);
		}
		String smsMessageToken = smsMessage.getMessage().substring(smsMessage.getMessage().lastIndexOf("Authentication Token : ") +23);
		if(otpToken.equals(smsMessageToken)) {
			return true;
		}		
		
		return false;
	}
	
	
	
	public static void sendSmsToMobileNumber() {

        try {
            String phoneNumber = "9629461303";
            String appKey = "your-app-key";
            String appSecret = "your-app-secret";
            String message = "Hello, world!";

            URL url = new URL("https://messagingapi.sinch.com/v1/sms/" + phoneNumber);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            String userCredentials = "application\\" + appKey + ":" + appSecret;
            byte[] encoded = Base64.encodeBase64(userCredentials.getBytes());
            String basicAuth = "Basic " + new String(encoded);
            connection.setRequestProperty("Authorization", basicAuth);

            String postData = "{\"Message\":\"" + message + "\"}";
            OutputStream os = connection.getOutputStream();
            os.write(postData.getBytes());

            StringBuilder response = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ( (line = br.readLine()) != null)
                response.append(line);

            br.close();
            os.close();

            System.out.println(response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
