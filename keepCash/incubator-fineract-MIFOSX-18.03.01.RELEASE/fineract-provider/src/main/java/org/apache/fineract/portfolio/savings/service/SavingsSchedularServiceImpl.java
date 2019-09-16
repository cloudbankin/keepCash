/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.springBoot.enumType.SavingsTransactionDetailsTypeEnum;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingsSchedularServiceImpl implements SavingsSchedularService {

    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
    private final SavingsAccountReadPlatformService savingAccountReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;

    @Autowired
    public SavingsSchedularServiceImpl(final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper,
            final SavingsAccountReadPlatformService savingAccountReadPlatformService,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService) {
        this.savingAccountAssembler = savingAccountAssembler;
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
        this.savingAccountRepositoryWrapper = savingAccountRepositoryWrapper;
        this.savingAccountReadPlatformService = savingAccountReadPlatformService;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
    }

    @CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
    @Override
    public void postInterestForAccounts() throws JobExecutionException {
        final List<SavingsAccount> savingsAccounts = this.savingAccountRepositoryWrapper.findSavingAccountByStatus(SavingsAccountStatusType.ACTIVE
                .getValue());
        StringBuffer sb = new StringBuffer();
        for (final SavingsAccount savingsAccount : savingsAccounts) {
            try {            	
                this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
                boolean postInterestAsOn = false;
                LocalDate transactionDate = null;
                this.savingsAccountWritePlatformService.postInterest(savingsAccount, postInterestAsOn, transactionDate);
            } catch (Exception e) {
                Throwable realCause = e;
                if (e.getCause() != null) {
                    realCause = e.getCause();
                }
                sb.append("failed to post interest for Savings with id " + savingsAccount.getId() + " with message "
                        + realCause.getMessage());
            }
        }
        
        if (sb.length() > 0) { throw new JobExecutionException(sb.toString()); }
    }
    
    @CronTarget(jobName = JobName.POST_INTEREST_FOR_AGENT_SAVINGS)
    @Override
    public void postInterestForAgentAccounts() throws JobExecutionException {
        final List<SavingsAccount> savingsAccounts = this.savingAccountRepositoryWrapper.findSavingAccountByStatus(SavingsAccountStatusType.ACTIVE
                .getValue());
        StringBuffer sb = new StringBuffer();
        for (final SavingsAccount savingsAccount : savingsAccounts) {
            try {
                final List<SavingsAccountTransaction> fullTransactionsSorted = new ArrayList<>();
                fullTransactionsSorted.addAll(savingsAccount.getTransactions());
            	Long productId = new Long(1);
            	if(savingsAccount.productId().equals(productId)) {
                    final List<SavingsAccountTransaction> listOfTransactionsSorted = new ArrayList<>();
                    SearchParameters searchParameters = new SearchParameters(null, savingsAccount.getId(), null, null, null, SavingsTransactionDetailsTypeEnum.TOPUP.getValue(), null, null, null, null, false, null);
                    Page<SavingsAccountTransactionData> SavingsAccountTransactionData = savingsAccountReadPlatformService.retrieveTransactionByCreteria(searchParameters, savingsAccount.getId());
                    for(SavingsAccountTransactionData accountTransactionData : SavingsAccountTransactionData.getPageItems()) {
                    	for(SavingsAccountTransaction savingsAccountTransaction : savingsAccount.getTransactions()) {
                    		if(accountTransactionData.getId().equals(savingsAccountTransaction.getId())) {
                    			listOfTransactionsSorted.add(savingsAccountTransaction);
                    			break;
                    		}
                    	}
                    }
                    savingsAccount.setTransaction(listOfTransactionsSorted);
                    
            	}

                	
                            	
                this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
                boolean postInterestAsOn = false;
                LocalDate transactionDate = null;
                this.savingsAccountWritePlatformService.postInterestTopup(savingsAccount, postInterestAsOn, transactionDate, fullTransactionsSorted);
            } catch (Exception e) {
                Throwable realCause = e;
                if (e.getCause() != null) {
                    realCause = e.getCause();
                }
                sb.append("failed to post interest for Savings with id " + savingsAccount.getId() + " with message "
                        + realCause.getMessage());
            }
        }
        
        if (sb.length() > 0) { throw new JobExecutionException(sb.toString()); }
    }

    @CronTarget(jobName = JobName.UPDATE_SAVINGS_DORMANT_ACCOUNTS)
    @Override
    public void updateSavingsDormancyStatus() throws JobExecutionException {
    	final LocalDate tenantLocalDate = DateUtils.getLocalDateOfTenant();

    	final List<Long> savingsPendingInactive = this.savingAccountReadPlatformService
    													.retrieveSavingsIdsPendingInactive(tenantLocalDate);
    	if(null != savingsPendingInactive && savingsPendingInactive.size() > 0){
    		for(Long savingsId : savingsPendingInactive){
    			this.savingsAccountWritePlatformService.setSubStatusInactive(savingsId);
    		}
    	}

    	final List<Long> savingsPendingDormant = this.savingAccountReadPlatformService
				.retrieveSavingsIdsPendingDormant(tenantLocalDate);
		if(null != savingsPendingDormant && savingsPendingDormant.size() > 0){
			for(Long savingsId : savingsPendingDormant){
				this.savingsAccountWritePlatformService.setSubStatusDormant(savingsId);
			}
		}

    	final List<Long> savingsPendingEscheat = this.savingAccountReadPlatformService
				.retrieveSavingsIdsPendingEscheat(tenantLocalDate);
		if(null != savingsPendingEscheat && savingsPendingEscheat.size() > 0){
			for(Long savingsId : savingsPendingEscheat){
				this.savingsAccountWritePlatformService.escheat(savingsId);
			}
		}
    }
}
