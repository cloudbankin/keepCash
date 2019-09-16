package com.org.agent.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import com.org.agent.command.api.JsonCommand;
import com.org.agent.controller.AgentConstants;
import com.org.agent.core.AbstractPersistableCustom;
import com.org.agent.enumType.AgentAccountStatusEnumType;
import com.org.agent.service.implementation.AppUserTypesEnumerations;
import com.org.agent.controller.AgentConstants;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


@Entity
@Table(name = "hab_agent_user", uniqueConstraints = { @UniqueConstraint(columnNames = { "mobile_no" }, name = "mobile_no")})
public class AgentUserEntity extends AbstractPersistableCustom<Long> {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "app_user_id")
	private AppUser appUser;

	@Column(name = "client_id", length = 20)
	private Long clientId;

	@Column(name = "app_user_type_enum", length = 30)
	private Long appUserTypeEnum;

	@Column(name = "company_name", length = 255)
	private String companyName;

	@Column(name = "company_address", length = 255)
	private String companyAddress;

	@Column(name = "date_of_birth")
	@Temporal(TemporalType.DATE)
	private Date dateOfBirth;

	@Column(name = "mobile_no", unique = true)
	private String mobileNo;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "face_id")
	private String faceId;

	@Column(name = "is_agreement_signup")
	private boolean isAgreementSignUp;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "auth_mode")
	private String authMode;

	@Column(name = "image")
	private String image;

	@Column(name = "image_encryption")
	private String imageEncryption;

	@Column(name = "created_on_date")
	@Temporal(TemporalType.DATE)
	private Date createdOnDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_user_id")
	private AppUser parentUserId;
	
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
	
	@Column(name = "employee_id", length = 20)
	private Long employeeId;
	
	@Column(name = "transaction_pin")
	private Integer transactionPin;
	
	@Column(name = "face_unique_id", length = 20, unique = true)
	private String faceUniqueId;
	
	@Column(name = "status", length = 6)
	private Integer status;

	public AgentUserEntity() {

	}

	public AgentUserEntity(final AppUser appUser, final Long clientId, final Long appUserTypeEnum,
			final String companyName, final String companyAddress, final LocalDate dateOfBirth, final String mobileNo,
			final String emailId, final String faceId, final boolean isAgreementSignUp, final boolean isActive,
			final String authMode, final String image, final String imageEncryption, final LocalDate createdOnDate, final AppUser parentUserId,
			final String latitude,final String longitude, final String locationName,final String locationAddress,final String ipAddress,
			final String deviceId,final Long employeeId, final String faceUniqueId ,final Integer transactionPin, final Integer status) {

		this.appUser = appUser;
		this.clientId = clientId;
		this.appUserTypeEnum = appUserTypeEnum;
		this.companyName = companyName;
		this.companyAddress = companyAddress;
		if (dateOfBirth != null) {
			this.dateOfBirth = dateOfBirth.toDate();
		}
		this.mobileNo = mobileNo;
		this.emailId = emailId;
		//this.faceId = faceId;
		this.faceId = faceId;
		this.isAgreementSignUp = isAgreementSignUp;
		this.isActive = isActive;
		this.authMode = authMode;
		this.image = image;
		this.imageEncryption = imageEncryption;
		if (createdOnDate != null) {
			this.createdOnDate = createdOnDate.toDate();
		}
		
		this.parentUserId = parentUserId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.locationName = locationName;
		this.locationAddress = locationAddress;
		this.ipAddress = ipAddress;
		this.deviceId = deviceId;
        this.employeeId = employeeId;
        this.faceUniqueId = faceUniqueId;
        this.transactionPin = transactionPin;
        this.status = status;
	}

	public static AgentUserEntity createAgentUserEntity(JsonCommand command, AppUser appUser, AppUser parentUserId, Integer transactionPIN) {
		Long clientId = command.longValueOfParameterNamed(AgentConstants.clientIdParamName);
		Long appUserTypeEnum = AppUserTypesEnumerations.appUserType(command.integerValueOfParameterNamed(AgentConstants.appUserTypeIdParamName)).getId();
		
		String companyName = null;
		String companyAddress = null;
		LocalDate dateOfBirth = command.localDateValueOfParameterNamed(AgentConstants.dateOfBirthParamName);
		String mobileNo = command.stringValueOfParameterNamed(AgentConstants.contactNoParamName);
		String emailId = command.stringValueOfParameterNamed(AgentConstants.emailIdParamName);
		String faceId = command.stringValueOfParameterNamed(AgentConstants.faceIdParamName);
		boolean isAgreementSignUp = command.booleanPrimitiveValueOfParameterNamed(AgentConstants.isAgreementSignUpParamName);
		boolean isActive = command.booleanPrimitiveValueOfParameterNamed(AgentConstants.isActiveParamName);
		String authMode = command.stringValueOfParameterNamed(AgentConstants.authenticationModeParamName);
		String image = command.stringValueOfParameterNamed(AgentConstants.imageParamName);
		String imageEncryption = command.stringValueOfParameterNamed(AgentConstants.imageEncryptionParamName);
		LocalDate createdOnDate = new LocalDate();
		Long employeeId = command.longValueOfParameterNamed(AgentConstants.employeeIdParamName);
		String faceUniqueId = command.stringValueOfParameterNamed(AgentConstants.faceUniqueIdParamName);
		Integer transactionPin = transactionPIN;
		Integer status = AgentAccountStatusEnumType.TOPUP.getValue();
		
        String latitude = null;
        String longitude = null;
        String locationName = null;
        String locationAddress =null; 
		String ipAddress = null;
		String deviceId = null;
        

		return new AgentUserEntity(appUser, clientId, appUserTypeEnum, companyName, companyAddress, dateOfBirth,
				mobileNo, emailId, faceId, isAgreementSignUp, isActive, authMode, image, imageEncryption,
				createdOnDate, parentUserId,latitude,longitude,locationName,locationAddress,ipAddress,deviceId,employeeId, faceUniqueId, transactionPin,
				status);
	}

	public AppUser getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getAppUserTypeEnum() {
		return appUserTypeEnum;
	}

	public void setAppUserTypeEnum(Long appUserTypeEnum) {
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

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
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

	public Date getCreatedOnDate() {
		return createdOnDate;
	}

	public void setCreatedOnDate(Date createdOnDate) {
		this.createdOnDate = createdOnDate;
	}

	public AppUser getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(AppUser parentUserId) {
		this.parentUserId = parentUserId;
	}
	public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(AgentConstants.faceIdParamName, this.faceId)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.faceIdParamName);
            actualChanges.put(AgentConstants.faceIdParamName, newValue);
            this.faceId = StringUtils.defaultIfEmpty(newValue, null);
        }

        

        if (command.isChangeInStringParameterNamed(AgentConstants.contactNoParamName, this.mobileNo)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.contactNoParamName);
            actualChanges.put(AgentConstants.contactNoParamName, newValue);
            this.mobileNo = StringUtils.defaultIfEmpty(newValue, null);
        }
		
		if (command.isChangeInStringParameterNamed(AgentConstants.emailIdParamName, this.emailId)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.emailIdParamName);
            actualChanges.put(AgentConstants.emailIdParamName, newValue);
            this.emailId = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(AgentConstants.companyNameParamName, this.companyName)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.companyNameParamName);
            actualChanges.put(AgentConstants.companyNameParamName, newValue);
            this.companyName = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(AgentConstants.comapanyAddressParamName, this.companyAddress)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.comapanyAddressParamName);
            actualChanges.put(AgentConstants.comapanyAddressParamName, newValue);
            this.companyAddress = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(AgentConstants.imageEncryptionParamName, this.imageEncryption)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.imageEncryptionParamName);
            actualChanges.put(AgentConstants.imageEncryptionParamName, newValue);
            this.imageEncryption = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(AgentConstants.imageParamName, this.image)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.imageParamName);
            actualChanges.put(AgentConstants.imageParamName, newValue);
            this.image = newValue;
        }
        if (command.isChangeInLongParameterNamed(AgentConstants.appUserTypeIdParamName, getAppUserTypeEnum())) {
            final Long newValue = command.longValueOfParameterNamed(AgentConstants.appUserTypeIdParamName);
            actualChanges.put(AgentConstants.appUserTypeIdParamName, newValue);
        }
        if (command.isChangeInLongParameterNamed(AgentConstants.employeeIdParamName, getEmployeeId())) {
            final Long newValue = command.longValueOfParameterNamed(AgentConstants.employeeIdParamName);
            actualChanges.put(AgentConstants.employeeIdParamName, newValue);
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        

        if (command.isChangeInDateParameterNamed(AgentConstants.dateOfBirthParamName, getDateOfBirth())) {
            final String valueAsInput = command.stringValueOfParameterNamed(AgentConstants.dateOfBirthParamName);
            actualChanges.put(AgentConstants.dateOfBirthParamName, valueAsInput);
            actualChanges.put(AgentConstants.dateFormatParamName, dateFormatAsInput);
            actualChanges.put(AgentConstants.localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(AgentConstants.dateOfBirthParamName);
            this.dateOfBirth = newValue.toDate();
        }

        if (command.isChangeInDateParameterNamed(AgentConstants.createdOnDateParamName, getCreatedOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(AgentConstants.createdOnDateParamName);
            actualChanges.put(AgentConstants.createdOnDateParamName, valueAsInput);
            actualChanges.put(AgentConstants.dateFormatParamName, dateFormatAsInput);
            actualChanges.put(AgentConstants.localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(AgentConstants.createdOnDateParamName);
            this.createdOnDate = newValue.toDate();
        }

        if (command.isChangeInStringParameterNamed(AgentConstants.faceUniqueIdParamName, this.faceUniqueId)) {
            final String newValue = command.stringValueOfParameterNamed(AgentConstants.faceUniqueIdParamName);
            actualChanges.put(AgentConstants.faceUniqueIdParamName, newValue);
            this.faceUniqueId = StringUtils.defaultIfEmpty(newValue, null);
        }
        
        if (command.isChangeInIntegerParameterNamed(AgentConstants.statusParamName, this.status, Locale.ENGLISH)) {
            final Integer newValue = command.integerValueOfParameterNamed(AgentConstants.statusParamName, Locale.ENGLISH);
            actualChanges.put(AgentConstants.statusParamName, newValue);
            this.status = newValue;
        }

        return actualChanges;
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

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public Integer getTransactionPin() {
		return transactionPin;
	}

	public void setTransactionPin(Integer transactionPin) {
		this.transactionPin = transactionPin;
	}

	public String getFaceUniqueId() {
		return faceUniqueId;
	}

	public void setFaceUniqueId(String faceUniqueId) {
		this.faceUniqueId = faceUniqueId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	
}
