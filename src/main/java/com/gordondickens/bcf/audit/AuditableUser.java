package com.gordondickens.bcf.audit;

import javax.persistence.Entity;

import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.AbstractAuditable;

/**
 * User domain class that uses auditing functionality of Spring Data that can
 * either be aquired implementing {@link Auditable} or extend
 * {@link AbstractAuditable}.
 *
 */
@Entity
public class AuditableUser extends AbstractAuditable<AuditableUser, Long> {
	private static final long serialVersionUID = 1L;

	private String username;

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}
