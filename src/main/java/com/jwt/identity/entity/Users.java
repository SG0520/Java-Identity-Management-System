package com.jwt.identity.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author mohit.mishra
 */

@Data
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "users")
@XmlRootElement
@NamedQueries({ @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u") })
public class Users implements Serializable { 
	

	private static final long serialVersionUID = -7030929438036698147L;
	@Id 
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 64)
	@Column(name = "USER_GUID")
	private String userGuid;
                                                              
	@Basic(optional = false)
	@NotNull
	      
	@Size(min = 1, max = 100)
	@Column(name = "PASSWORD")
	private String password;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 256)
	@Column(name = "FIRST_NAME")
	private String firstName;
	@Size(max = 256)
	@Column(name = "LAST_NAME")
	private String lastName;
	@Size(max = 6)
	@Column(name = "GENDER")
	private String gender;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 6)
	@Column(name = "STATUS")
	private String userStatus;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 20)
	@Column(name = "TITLE")
	private String title;
	@Basic(optional = false)
	@NotNull
	@Column(name = "CREATED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;
	@Basic(optional = false)
	@NotNull
	@Column(name = "MODIFIED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	@Size(max = 120)
	@Column(name = "EMAIL")
	private String email;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "MODIFIED_BY")
	private String modifiedBy;
	@Column(name = "TENANT_ID")
	private Integer tenantId;
	@JoinColumn(name = "USER_ROLE_ID", referencedColumnName = "USER_ROLE_ID")
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private UserRole userRole;

	@Size(max = 10)
	@Column(name = "TELEPHONE")
	private String telephone;

	public Users() {
	}

	public Users(String userGuid) {
		this.userGuid = userGuid;
	}



	@Override
	public int hashCode() {
		int hash = 0;
		hash += (userGuid != null ? userGuid.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof Users)) {
			return false;
		}
		Users other = (Users) object;
		if ((this.userGuid == null && other.userGuid != null)
				|| (this.userGuid != null && !this.userGuid.equals(other.userGuid))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Users [userGuid=" + userGuid + ", password=" + password + ", firstName=" + firstName + ", lastName="
				+ lastName + ", gender=" + gender + ", userStatus=" + userStatus + ", title=" + title + ", createdDate="
				+ createdDate + ", modifiedDate=" + modifiedDate + ", email=" + email + ", createdBy=" + createdBy
				+ ", modifiedBy=" + modifiedBy + ", tenantId=" + tenantId + ", userRole=" + userRole + ", telephone="
				+ telephone + "]";
	}


}
