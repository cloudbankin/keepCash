package com.org.customer.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.org.customer.core.data.EnumOptionData;

@SuppressWarnings("unused")
public class CustomerUserData {
	
	private Long appUserId;
	private Long office;
	private Long staff;
	private String emailId;
	private String userName;
	private String firstName;
	private String lastName;
	private boolean enabled;
	private boolean isSelfServiceUser;

	private Long clientId;
	private EnumOptionData appUserTypeEnum;
	private String companyName;
	private String companyAddress;
	private String dateOfBirth;
	private String mobileNo;
	private boolean isAgreementSignUp;
	private boolean isActive;
	private String createdOnDate;
	private String authMode;
	private String image;
	private String imageEncryption;
	private Long parentUserId;
	
	private Long goalId;
	private String goalName;
	private BigDecimal goalAmount;
	private String goalStartDate;
	private String goalEndDate;
        private String latitude;
	private String longitude;
	private String locationName;
	private String locationAddress;
	private String ipAddress;
	private String deviceId;
	private String customerFaceUniqueId;

	public CustomerUserData(final Long appUserId, final Long office, final Long staff, final String emailId, final String userName,
			final String firstName, final String lastName, final boolean enabled, final boolean isSelfServiceUser,
			final Long clientId, final EnumOptionData appUserTypeEnum, final String companyName,
			final String companyAddress, final String dateOfBirth,
			final String mobileNo,  final boolean isAgreementSignUp,
			final boolean isActive, final String authMode, final String image,
			final String imageEncryption, final String createdOnDate, final Long parentUserId,
			final Long goalId, final String goalName, final BigDecimal goalAmount, final String startDate, final String endDate,final String latitude,
	        final String longitude,final String locationName,final String locationAddress,
	        final String ipAddress,final String deviceId, final String customerFaceUniqueId) {
		this.appUserId = appUserId;
		this.office = office;
		this.staff = staff;
		this.emailId = emailId;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.enabled = enabled;
		this.isSelfServiceUser = isSelfServiceUser;
		this.clientId = clientId;
		this.appUserTypeEnum = appUserTypeEnum;
		this.companyName = companyName;
		this.companyAddress = companyAddress;
		this.dateOfBirth = dateOfBirth;
		this.mobileNo = mobileNo;
		this.isAgreementSignUp = isAgreementSignUp;
		this.isActive = isActive;
		this.createdOnDate = createdOnDate;
		this.authMode = authMode;
		this.image = image;
		this.imageEncryption = imageEncryption;
		this.parentUserId = parentUserId;
		this.goalId = goalId;
		this.goalName = goalName;
		this.goalAmount = goalAmount;
		this.goalStartDate = startDate;
		this.goalEndDate = endDate;
                this.latitude = latitude;
		this.longitude = longitude;
		this.locationName = locationName;
		this.locationAddress = locationAddress;
		this.ipAddress = ipAddress;
		this.deviceId = deviceId;
		this.customerFaceUniqueId = customerFaceUniqueId;

		
	}

	public static CustomerUserData instance(final Long appUserId) {
		
		return new CustomerUserData(appUserId, null, null, null, null, null, null,
				false, false, null, null, null, null, null,  null, false, false,
				null, null, null, null, null, null, null, null, null, null,null, null, null, null, null, null, null);
	}
	
	public Long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(Long appUserId) {
		this.appUserId = appUserId;
	}

	public Long getOffice() {
		return office;
	}

	public void setOffice(Long office) {
		this.office = office;
	}

	public Long getStaff() {
		return staff;
	}

	public void setStaff(Long staff) {
		this.staff = staff;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isSelfServiceUser() {
		return isSelfServiceUser;
	}

	public void setSelfServiceUser(boolean isSelfServiceUser) {
		this.isSelfServiceUser = isSelfServiceUser;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public EnumOptionData getAppUserTypeEnum() {
		return appUserTypeEnum;
	}

	public void setAppUserTypeEnum(EnumOptionData appUserTypeEnum) {
		this.appUserTypeEnum = appUserTypeEnum;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyAddress() {
		return companyAddress;
	}

	public void setCompanyAddress(String companyAddress) {
		this.companyAddress = companyAddress;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}


	public boolean isAgreementSignUp() {
		return isAgreementSignUp;
	}

	public void setAgreementSignUp(boolean isAgreementSignUp) {
		this.isAgreementSignUp = isAgreementSignUp;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getCreatedOnDate() {
		return createdOnDate;
	}

	public void setCreatedOnDate(String createdOnDate) {
		this.createdOnDate = createdOnDate;
	}

	public String getAuthMode() {
		return authMode;
	}

	public void setAuthMode(String authMode) {
		this.authMode = authMode;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImageEncryption() {
		return imageEncryption;
	}

	public void setImageEncryption(String imageEncryption) {
		this.imageEncryption = imageEncryption;
	}

	public Long getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(Long parentUserId) {
		this.parentUserId = parentUserId;
	}

	public Long getGoalId() {
		return goalId;
	}

	public void setGoalId(Long goalId) {
		this.goalId = goalId;
	}

	public String getGoalName() {
		return goalName;
	}

	public void setGoalName(String goalName) {
		this.goalName = goalName;
	}

	public BigDecimal getGoalAmount() {
		return goalAmount;
	}

	public void setGoalAmount(BigDecimal goalAmount) {
		this.goalAmount = goalAmount;
	}

	public String getGoalStartDate() {
		return goalStartDate;
	}

	public void setGoalStartDate(String goalStartDate) {
		this.goalStartDate = goalStartDate;
	}

	public String getGoalEndDate() {
		return goalEndDate;
	}

	public void setGoalEndDate(String goalEndDate) {
		this.goalEndDate = goalEndDate;
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

	public String getCustomerFcaeUniqueId() {
		return customerFaceUniqueId;
	}

	public void setCustomerFcaeUniqueId(String customerFcaeUniqueId) {
		this.customerFaceUniqueId = customerFcaeUniqueId;
	}
	
	
	
}
