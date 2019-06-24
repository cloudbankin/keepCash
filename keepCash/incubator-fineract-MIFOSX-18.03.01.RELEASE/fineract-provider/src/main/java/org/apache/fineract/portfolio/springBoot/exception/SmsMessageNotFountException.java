package org.apache.fineract.portfolio.springBoot.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class SmsMessageNotFountException extends AbstractPlatformResourceNotFoundException {

	public SmsMessageNotFountException(final Long id) {
		 super("error.msg.sms.id.invalid", " sms with identifier " + id + " does not exist", id);
	}
}
