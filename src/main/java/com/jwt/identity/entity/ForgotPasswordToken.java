package com.jwt.identity.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Entity
@Table(name = "forgot_password_token")
@XmlRootElement
@Data
public class ForgotPasswordToken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5817344072058557557L;

	@Id
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 64)
	@Column(name = "PASSWORD_TOKEN")
	private String passwordToken;
     
	@NotNull
	@Size(min = 1, max = 64)
	@Column(name = "USER_GUID")
	private String userGuid;
	
	@Size(max = 120)
	@Column(name = "EMAIL")
	private String email;

	@Column(name = "TENANT_ID")
	private Integer tenantId;

	@Basic(optional = false)
	@NotNull
	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Basic(optional = false)
	@NotNull
	@Column(name = "EXPIRE_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiryDate;

}
