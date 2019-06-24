package com.org.customer.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import com.org.customer.command.api.JsonCommand;
import com.org.customer.controller.CustomerConstants;
import com.org.customer.core.AbstractPersistableCustom;

@Entity
@Table(name = "hab_customer_user")
public class CustomerUserEntity extends AbstractPersistableCustom<Long> {

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

	@Column(name = "mobile_no")
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
	
	@Column(name = "parent_user_id", length = 20)
	private Long parentUserId;

	public CustomerUserEntity() {

	}

	public CustomerUserEntity(final AppUser appUser, final Long clientId, final Long appUserTypeEnum,
			final String companyName, final String companyAddress, final LocalDate dateOfBirth, final String mobileNo,
			final String emailId, final String faceId, final boolean isAgreementSignUp, final boolean isActive,
			final String authMode, final String image, final String imageEncryption, final LocalDate createdOnDate, final Long parentUserId) {

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
		this.parentUserId=parentUserId;

	}

	public static CustomerUserEntity createCustomerUserEntity(JsonCommand command, AppUser appUser,Long agentId) {
		Long clientId = null;
		Long appUserTypeEnum = command.longValueOfParameterNamed(CustomerConstants.appUserTypeIdParamName);
		String companyName = null;
		String companyAddress = null;
		LocalDate dateOfBirth = command.localDateValueOfParameterNamed(CustomerConstants.dateOfBirthParamName);
		String mobileNo = command.stringValueOfParameterNamed(CustomerConstants.contactNoParamName);
		String emailId = command.stringValueOfParameterNamed(CustomerConstants.emailIdParamName);
		String faceId = command.stringValueOfParameterNamed(CustomerConstants.faceIdParamName);
		boolean isAgreementSignUp = command
				.booleanPrimitiveValueOfParameterNamed(CustomerConstants.isAgreementSignUpParamName);
		boolean isActive = command.booleanPrimitiveValueOfParameterNamed(CustomerConstants.isActiveParamName);
		String authMode = command.stringValueOfParameterNamed(CustomerConstants.authenticationModeParamName);
		String image = command.stringValueOfParameterNamed(CustomerConstants.imageParamName);
		String imageEncryption = command.stringValueOfParameterNamed(CustomerConstants.imageEncryptionParamName);
		LocalDate createdOnDate = new LocalDate();
		Long parentUserId = agentId;

		return new CustomerUserEntity(appUser, clientId, appUserTypeEnum, companyName, companyAddress, dateOfBirth,
				mobileNo, emailId, faceId, isAgreementSignUp, isActive, authMode, image, imageEncryption,
				createdOnDate, parentUserId);
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

	public Long getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(Long parentUserId) {
		this.parentUserId = parentUserId;
	}
	public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(CustomerConstants.faceIdParamName, this.faceId)) {
            final String newValue = command.stringValueOfParameterNamed(CustomerConstants.faceIdParamName);
            actualChanges.put(CustomerConstants.faceIdParamName, newValue);
            this.faceId = StringUtils.defaultIfEmpty(newValue, null);
        }

        

        if (command.isChangeInStringParameterNamed(CustomerConstants.contactNoParamName, this.mobileNo)) {
            final String newValue = command.stringValueOfParameterNamed(CustomerConstants.contactNoParamName);
            actualChanges.put(CustomerConstants.contactNoParamName, newValue);
            this.mobileNo = StringUtils.defaultIfEmpty(newValue, null);
        }
		
		if (command.isChangeInStringParameterNamed(CustomerConstants.emailIdParamName, this.emailId)) {
            final String newValue = command.stringValueOfParameterNamed(CustomerConstants.emailIdParamName);
            actualChanges.put(CustomerConstants.emailIdParamName, newValue);
            this.emailId = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(CustomerConstants.companyNameParamName, this.companyName)) {
            final String newValue = command.stringValueOfParameterNamed(CustomerConstants.companyNameParamName);
            actualChanges.put(CustomerConstants.companyNameParamName, newValue);
            this.companyName = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(CustomerConstants.comapanyAddressParamName, this.companyAddress)) {
            final String newValue = command.stringValueOfParameterNamed(CustomerConstants.comapanyAddressParamName);
            actualChanges.put(CustomerConstants.comapanyAddressParamName, newValue);
            this.companyAddress = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(CustomerConstants.imageEncryptionParamName, this.imageEncryption)) {
            final String newValue = command.stringValueOfParameterNamed(CustomerConstants.imageEncryptionParamName);
            actualChanges.put(CustomerConstants.imageEncryptionParamName, newValue);
            this.imageEncryption = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(CustomerConstants.imageParamName, this.image)) {
            final String newValue = command.stringValueOfParameterNamed(CustomerConstants.imageParamName);
            actualChanges.put(CustomerConstants.imageParamName, newValue);
            this.image = newValue;
        }
        if (command.isChangeInLongParameterNamed(CustomerConstants.appUserTypeIdParamName, getAppUserTypeEnum())) {
            final Long newValue = command.longValueOfParameterNamed(CustomerConstants.appUserTypeIdParamName);
            actualChanges.put(CustomerConstants.appUserTypeIdParamName, newValue);
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        

        if (command.isChangeInDateParameterNamed(CustomerConstants.dateOfBirthParamName, getDateOfBirth())) {
            final String valueAsInput = command.stringValueOfParameterNamed(CustomerConstants.dateOfBirthParamName);
            actualChanges.put(CustomerConstants.dateOfBirthParamName, valueAsInput);
            actualChanges.put(CustomerConstants.dateFormatParamName, dateFormatAsInput);
            actualChanges.put(CustomerConstants.localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(CustomerConstants.dateOfBirthParamName);
            this.dateOfBirth = newValue.toDate();
        }

        if (command.isChangeInDateParameterNamed(CustomerConstants.createdOnDateParamName, getCreatedOnDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(CustomerConstants.createdOnDateParamName);
            actualChanges.put(CustomerConstants.createdOnDateParamName, valueAsInput);
            actualChanges.put(CustomerConstants.dateFormatParamName, dateFormatAsInput);
            actualChanges.put(CustomerConstants.localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(CustomerConstants.createdOnDateParamName);
            this.createdOnDate = newValue.toDate();
        }

        

        return actualChanges;
    }
}
