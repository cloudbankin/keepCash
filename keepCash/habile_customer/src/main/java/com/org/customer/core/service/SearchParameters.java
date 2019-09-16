package com.org.customer.core.service;

import org.apache.commons.lang.StringUtils;

public final class SearchParameters {
	 	private final String sqlSearch;
	    private final Long officeId;
	    private final String externalId;
	    private final String name;
	    private final String hierarchy;
	    private final String firstname;
	    private final String lastname;
	    private final Integer offset;
	    private final Integer limit;
	    private final String orderBy;
	    private final String sortOrder;
	    private final String accountNo;
	    private final String currencyCode;

	    private final Long staffId;

	    private final Long loanId;

	    private final Long savingsId;
	    private final Boolean orphansOnly;

	    // Provisning Entries Search Params
	    private final Long provisioningEntryId;
	    private final Long productId;
	    private final Long categoryId;
	    private final boolean isSelfUser;
	    
	    private final String mobileNo;
	    private final Long userId;
	
	  public static SearchParameters forCustomerUser(final String sqlSearch, final String firstname, final String lastname, 
			  final String mobileNo, final Long userId ,final Integer offset,
	            final Integer limit, final String orderBy, final String sortOrder) {

	        final Integer maxLimitAllowed = getCheckedLimit(limit);


	        return new SearchParameters(sqlSearch, firstname, lastname, mobileNo, userId, offset, maxLimitAllowed,
	                orderBy, sortOrder);
	    }
	  

	public SearchParameters(String sqlSearch, Long officeId, String externalId, String name, String hierarchy,
			String firstname, String lastname, Integer offset, Integer limit, String orderBy, String sortOrder,
			String accountNo, String currencyCode, Long staffId, Long loanId, Long savingsId, Boolean orphansOnly,
			Long provisioningEntryId, Long productId, Long categoryId, boolean isSelfUser, String mobileNo,
			Long userId) {
		super();
		this.sqlSearch = sqlSearch;
		this.officeId = officeId;
		this.externalId = externalId;
		this.name = name;
		this.hierarchy = hierarchy;
		this.firstname = firstname;
		this.lastname = lastname;
		this.offset = offset;
		this.limit = limit;
		this.orderBy = orderBy;
		this.sortOrder = sortOrder;
		this.accountNo = accountNo;
		this.currencyCode = currencyCode;
		this.staffId = staffId;
		this.loanId = loanId;
		this.savingsId = savingsId;	            
		this.orphansOnly = orphansOnly;
		this.provisioningEntryId = provisioningEntryId;
		this.productId = productId;
		this.categoryId = categoryId;
		this.isSelfUser = isSelfUser;
		this.mobileNo = mobileNo;
		this.userId = userId;
	}

	

	private SearchParameters(final String sqlSearch, final String firstname, final String lastname, final String mobileNo, 
			final Long userId ,final Integer offset, final Integer limit, final String orderBy, final String sortOrder) {
		this.sqlSearch = sqlSearch;
		this.firstname = firstname;
		this.lastname = lastname;
		this.mobileNo = mobileNo;
		this.userId = userId;
		this.offset = offset;
		this.orderBy = orderBy;
		this.sortOrder = sortOrder;
		this.limit = limit;
		this.officeId = null;
		this.externalId = null;
		this.name = null;
		this.hierarchy = null;
		this.accountNo = null;
		this.currencyCode = null;
		this.staffId = null;
		this.loanId = null;
		this.savingsId = null;	            
		this.orphansOnly = null;
		this.provisioningEntryId = null;
		this.productId = null;
		this.categoryId = null;
		this.isSelfUser = false;
	
		
	
	}

	
	 public boolean isOrderByRequested() {
	        return StringUtils.isNotBlank(this.orderBy);
	    }

	    public boolean isSortOrderProvided() {
	        return StringUtils.isNotBlank(this.sortOrder);
	    }

	    public static Integer getCheckedLimit(final Integer limit) {

	        final Integer maxLimitAllowed = 200;
	        // default to max limit first off
	        Integer checkedLimit = maxLimitAllowed;

	        if (limit != null && limit > 0) {
	            checkedLimit = limit;
	        } else if (limit != null) {
	            // unlimited case: limit provided and 0 or less
	            checkedLimit = null;
	        }

	        return checkedLimit;
	    }

	    public boolean isOfficeIdPassed() {
	        return this.officeId != null && this.officeId != 0;
	    }

	    public boolean isCurrencyCodePassed() {
	        return this.currencyCode != null;
	    }

	    public boolean isLimited() {
	        return this.limit != null && this.limit.intValue() > 0;
	    }

	    public boolean isOffset() {
	        return this.offset != null;
	    }

	    public boolean isScopedByOfficeHierarchy() {
	        return StringUtils.isNotBlank(this.hierarchy);
	    }

	    public String getSqlSearch() {
	        return this.sqlSearch;
	    }

	    public Long getOfficeId() {
	        return this.officeId;
	    }

	    public String getCurrencyCode() {
	        return this.currencyCode;
	    }

	    public String getExternalId() {
	        return this.externalId;
	    }

	    public String getName() {
	        return this.name;
	    }

	    public String getHierarchy() {
	        return this.hierarchy;
	    }

	    public String getFirstname() {
	        return this.firstname;
	    }

	    public String getLastname() {
	        return this.lastname;
	    }

	    public Integer getOffset() {
	        return this.offset;
	    }

	    public Integer getLimit() {
	        return this.limit;
	    }

	    public String getOrderBy() {
	        return this.orderBy;
	    }

	    public String getSortOrder() {
	        return this.sortOrder;
	    }

	    public boolean isStaffIdPassed() {
	        return this.staffId != null && this.staffId != 0;
	    }

	    public Long getStaffId() {
	        return this.staffId;
	    }

	    public String getAccountNo() {
	        return this.accountNo;
	    }

	    public boolean isLoanIdPassed() {
	        return this.loanId != null && this.loanId != 0;
	    }

	    public boolean isSavingsIdPassed() {
	        return this.savingsId != null && this.savingsId != 0;
	    }

	    public Long getLoanId() {
	        return this.loanId;
	    }

	    public Long getSavingsId() {
	        return this.savingsId;
	    }

	    public Boolean isOrphansOnly() {
	        if (this.orphansOnly != null) { return this.orphansOnly; }
	        return false;
	    }

	    public Long getProvisioningEntryId() {
	        return this.provisioningEntryId;
	    }

	    public boolean isProvisioningEntryIdPassed() {
	        return this.provisioningEntryId != null && this.provisioningEntryId != 0;
	    }

	    public Long getProductId() {
	        return this.productId;
	    }

	    public boolean isProductIdPassed() {
	        return this.productId != null && this.productId != 0;
	    }

	    public Long getCategoryId() {
	        return this.categoryId;
	    }

	    public boolean isCategoryIdPassed() {
	        return this.categoryId != null && this.categoryId != 0;
	    }

	    public boolean isSelfUser() {
	        return this.isSelfUser;
	    }


		public String getMobileNo() {
			return mobileNo;
		}


		public Long getUserId() {
			return userId;
		}
	  
	  

}
