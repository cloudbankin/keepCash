package org.apache.fineract.portfolio.springBoot.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.joda.time.LocalDateTime;

@Entity
@Table(name = "hab_event_request")
public class EventRequest extends AbstractPersistableCustom<Long>{

	@Column(name = "user_id", length = 20)
	private Long userId;
	
	@Column(name = "request")
	private String request;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date")
	private Date createdDate;
	
	@Column(name = "action")
	private String action;

	public EventRequest(Long userId, String request, Date createdDate, String action) {
		super();
		this.userId = userId;
		this.request = request;
		this.createdDate = createdDate;
		this.action = action;
	}
	
	
	public static EventRequest createRequest(Long userId, String request, Date createdDate, String action ) {
		return new EventRequest(userId, request, createdDate, action);
	}
	
}
