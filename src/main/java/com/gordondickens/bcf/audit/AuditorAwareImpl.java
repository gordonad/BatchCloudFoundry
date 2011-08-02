package com.gordondickens.bcf.audit;

import org.springframework.data.domain.AuditorAware;

/**
 * Dummy implementation of {@link AuditorAware}. It will return the configured
 * {@link AuditableUser} as auditor on every call to
 * {@link #getCurrentAuditor()}. Normally you would access the applications
 * security subsystem to return the current user.
 *
 */
public class AuditorAwareImpl implements AuditorAware<AuditableUser> {
	private AuditableUser auditor;

	public void setAuditor(AuditableUser auditor) {
		this.auditor = auditor;
	}

	public AuditableUser getCurrentAuditor() {
		return auditor;
	}
}
