package com.jwt.identity.entity;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;

/**
 *
 * @author mohit.mishra
 */
@Entity
@Table(name = "user_role")
@XmlRootElement
@Data
@NamedQueries({ @NamedQuery(name = "UserRole.findAll", query = "SELECT u FROM UserRole u") })
public class UserRole implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 20)
	@Column(name = "USER_ROLE_ID")
	private String userRoleId;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 50)
	@Column(name = "USER_ROlE_NAME")
	private String userRoleName;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "userRole", fetch = FetchType.EAGER)
	private Collection<Users> usersCollection;





	@XmlTransient
	public Collection<Users> getUsersCollection() {
		return usersCollection;
	}

	public void setUsersCollection(Collection<Users> usersCollection) {
		this.usersCollection = usersCollection;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (userRoleId != null ? userRoleId.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof UserRole)) {
			return false;
		}
		UserRole other = (UserRole) object;
		if ((this.userRoleId == null && other.userRoleId != null)
				|| (this.userRoleId != null && !this.userRoleId.equals(other.userRoleId))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "gradleproject1.UserRole[ userRoleId=" + userRoleId + " ]";
	}

}
