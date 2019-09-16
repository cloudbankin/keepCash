package com.org.customer.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;

import com.org.customer.core.AbstractPersistableCustom;

@Entity
@Table(name = "hab_rating")
public class Rating extends AbstractPersistableCustom<Long>{

	@Column(name = "save_transaction_id")
	private Long savingsTransactionId;
	
	@Column(name = "rating_status", length = 6)
	private Integer ratingStatus;
	
	@Column(name = "feedback_improved", length = 500)
	private String feedbackImproved;
	
	@Column(name = "feedback_id", length = 20)
	private Long feedbackId;
	
	@Column(name = "created_date")
	@Temporal(TemporalType.DATE)
	private Date createdDate;
	
	@Column(name = "is_skip")
	private boolean isSkip;

	
	
	
	public Rating(Long savingsTransactionId, Integer ratingStatus, String feedbackImproved, Long feedbackId,
			Date createdDate, boolean isSkip) {
		super();
		this.savingsTransactionId = savingsTransactionId;
		this.ratingStatus = ratingStatus;
		this.feedbackImproved = feedbackImproved;
		this.feedbackId = feedbackId;
		this.createdDate = createdDate;
		this.isSkip = isSkip;
	}
	
	public Rating() {
		
	}

	public static Rating createNewRating(Long savingsTransactionId, Integer ratingStatus, String feedbackImproved, Long feedbackId,
			LocalDate createdDate, boolean isSkip) {
		Date date = null;
		if(createdDate != null) {
			date = createdDate.toDate();
		}
		return new Rating(savingsTransactionId, ratingStatus, feedbackImproved, feedbackId, date, isSkip); 
	}
	
	public Long getSavingsTransactionId() {
		return savingsTransactionId;
	}

	public void setSavingsTransactionId(Long savingsTransactionId) {
		this.savingsTransactionId = savingsTransactionId;
	}

	
	public Integer getRatingStatus() {
		return ratingStatus;
	}

	public void setRatingStatus(Integer ratingStatus) {
		this.ratingStatus = ratingStatus;
	}

	public String getFeedbackImproved() {
		return feedbackImproved;
	}

	public void setFeedbackImproved(String feedbackImproved) {
		this.feedbackImproved = feedbackImproved;
	}

	public Long getFeedbackId() {
		return feedbackId;
	}

	public void setFeedbackId(Long feedbackId) {
		this.feedbackId = feedbackId;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public boolean isSkip() {
		return isSkip;
	}

	public void setSkip(boolean isSkip) {
		this.isSkip = isSkip;
	}

	
	
	
	
	
}
