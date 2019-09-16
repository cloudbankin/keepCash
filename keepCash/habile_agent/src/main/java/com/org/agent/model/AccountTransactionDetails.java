package com.org.agent.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.org.agent.command.api.JsonCommand;
import com.org.agent.core.AbstractPersistableCustom;
import com.org.agent.enumType.SavingsTransactionDetailsTypeEnum;
import com.org.agent.controller.AccountTransactionConstants;

@Entity
@Table(name = "hab_savings_transaction_details")
public class AccountTransactionDetails extends AbstractPersistableCustom<Long>{

	@Column(name = "transaction_id", nullable = false, length = 20)
	private Long transactionId;
	
	@Column(name = "client_id", nullable = true, length = 20)
	private Long clientId;
	
	@Column(name = "user_id", nullable = true, length = 20)
	private Long UserId;
	
	@Column(name = "transaction_type", nullable = false, length = 5)
	private Integer transactionType;
	
	@Column(name = "savings_id", nullable = false, length = 5)
	private Long savingsId;
	
	@Column(name = "to_user_id", nullable = true, length = 20)
	private Long toUserId;

	@Column(name = "user_type_id", length = 20)
	private Long userTypeId;
	
	@Column(name = "to_user_type_id", length = 20)
	private Long toUserTypeId;
	
	@Column(name = "latitude")
	private String latitude;
	
	@Column(name = "longitude")
	private String longitude;
	
	@Column(name = "location_name")
	private String locationName;
	
	@Column(name = "location_address")
	private String locationAddress;
	
	@Column(name = "ip_address")
	private String ipAddress;
	
	@Column(name = "device_id")
	private String deviceId;
	
	public AccountTransactionDetails(Long transactionId, Long clientId, Long userId, Integer transactionType,
			Long savingsId, Long toUserId, Long userTypeId, Long toUserTypeId,String latitude,String longitude,
			String locationName,String locationAddress,String ipAddress, String deviceId) {
		super();
		this.transactionId = transactionId;
		this.clientId = clientId;
		this.UserId = userId;
		this.transactionType = transactionType;
		this.savingsId = savingsId;
		this.toUserId = toUserId;
		this.userTypeId = userTypeId;
		this.toUserTypeId = toUserTypeId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.locationName = locationName;
		this.locationAddress = locationAddress;
		this.ipAddress = ipAddress;
		this.deviceId = deviceId;
	}

	public static AccountTransactionDetails newAccountTransactionDetails(JsonCommand command) { 
		Long transactionId = command.longValueOfParameterNamed(AccountTransactionConstants.transactionIdParamName);
		Long clientId = command.longValueOfParameterNamed(AccountTransactionConstants.clientIdParamName);
		Long userId = command.longValueOfParameterNamed(AccountTransactionConstants.userIdParamName);
		Integer transactionType = SavingsTransactionDetailsTypeEnum.fromInt(command.integerValueOfParameterNamed(AccountTransactionConstants.transactionTypeParamName)).getValue();
		Long savingsId = command.longValueOfParameterNamed(AccountTransactionConstants.savingsIdParamName);
		Long toUserId = command.longValueOfParameterNamed(AccountTransactionConstants.toUserIdParamName);
		Long userTypeId = command.longValueOfParameterNamed(AccountTransactionConstants.userTypeIdParamName);
		Long toUserTypeId = command.longValueOfParameterNamed(AccountTransactionConstants.toUserTypeIdParamName);
		String latitude=null;
		String longitude=null;
		String locationName=null;
		String locationAddress=null;
		String ipAddress=null;
		String deviceId=null;
		
		return new AccountTransactionDetails(transactionId, clientId, userId,
				transactionType, savingsId, toUserId, userTypeId, toUserTypeId,
				latitude,longitude,locationName,locationAddress,ipAddress,deviceId);
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getUserId() {
		return UserId;
	}

	public void setUserId(Long userId) {
		UserId = userId;
	}

	public Integer getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(Integer transactionType) {
		this.transactionType = transactionType;
	}

	public Long getSavingsId() {
		return savingsId;
	}

	public void setSavingsId(Long savingsId) {
		this.savingsId = savingsId;
	}

	public Long getToUserId() {
		return toUserId;
	}

	public void setToUserId(Long toUserId) {
		this.toUserId = toUserId;
	}

	public Long getUserTypeId() {
		return userTypeId;
	}

	public void setUserTypeId(Long userTypeId) {
		this.userTypeId = userTypeId;
	}

	public Long getToUserTypeId() {
		return toUserTypeId;
	}

	public void setToUserTypeId(Long toUserTypeId) {
		this.toUserTypeId = toUserTypeId;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationAddress() {
		return locationAddress;
	}

	public void setLocationAddress(String locationAddress) {
		this.locationAddress = locationAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	
	
	
	
}
