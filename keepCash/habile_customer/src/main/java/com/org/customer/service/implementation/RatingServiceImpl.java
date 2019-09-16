package com.org.customer.service.implementation;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.CommandProcessingResultBuilder;
import com.org.customer.command.FromJsonHelper;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.model.Rating;
import com.org.customer.repository.RatingRepository;
import com.org.customer.service.RatingService;

@Component
public class RatingServiceImpl implements RatingService{

	@Autowired
	private final FromJsonHelper fromApiJsonHelper; 
	private final RatingRepository ratingRepository; 
	
	@Autowired
	public RatingServiceImpl(final FromJsonHelper fromApiJsonHelper, final RatingRepository ratingRepository) {
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.ratingRepository = ratingRepository;
	}
	
	@Override
	public CommandProcessingResult saveCustomerRating(JsonCommand command) {
		Long savingsTransactionId = command.longValueOfParameterNamed("transactionId");
		Integer ratingStatus = command.integerValueOfParameterNamed("ratingStatus");
		String feedbackImproved = command.stringValueOfParameterNamed("feedbackImproved");
		Long feedbackId = command.longValueOfParameterNamed("feedback_id");
		LocalDate createdDate = command.localDateValueOfParameterNamed("createdDate");
		boolean isSkip = command.booleanObjectValueOfParameterNamed("isSkip");
		
		Rating newRating = Rating.createNewRating(savingsTransactionId, ratingStatus, feedbackImproved, feedbackId, createdDate, isSkip);
		ratingRepository.save(newRating);
		
		return new CommandProcessingResultBuilder()
				.withCommandId(command.commandId())
				.build();
	}
}
