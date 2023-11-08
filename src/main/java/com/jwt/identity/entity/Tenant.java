package com.jwt.identity.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author mohit.mishra
 */
@Entity
@Table(name = "tenant")
@XmlRootElement
@NamedQueries({ @NamedQuery(name = "Tenant.findAll", query = "SELECT t FROM Tenant t") })
public class Tenant implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "TENANT_ID")
	private Integer tenantId;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 20)
	@Column(name = "TENANT_NAME")
	private String tenantName;
	@Size(max = 150)
	@Column(name = "TENANT_DESCRIPTION")
	private String tenantDescription;
	@Size(max = 20)
	@Column(name = "STATUS")
	private String status;
	@Basic(optional = false)
	@NotNull
	@Column(name = "MODIFIED_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 45)
	@Column(name = "TENANT_TYPE")
	private String tenantType;
	/*
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "tenantId", fetch =
	 * FetchType.LAZY) private Collection<SchoolClassTeacher>
	 * schoolClassTeacherCollection;
	 * 
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "tenantId", fetch =
	 * FetchType.LAZY) private Collection<TagStateMapping>
	 * tagStateMappingCollection;
	 * 
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "tenantId", fetch =
	 * FetchType.LAZY) private Collection<SchUsrGroupMember>
	 * schUsrGroupMemberCollection;
	 * 
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "tenantId", fetch =
	 * FetchType.LAZY) private Collection<School> schoolCollection;
	 * 
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "tenantId", fetch =
	 * FetchType.LAZY) private Collection<SchoolUser> schoolUserCollection;
	 * 
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "tenantId", fetch =
	 * FetchType.LAZY) private Collection<SchUsrGroup> schUsrGroupCollection;
	 * 
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "tenantId", fetch =
	 * FetchType.LAZY) private Collection<Users> usersCollection;
	 */

	public Tenant() {
	}

	public Tenant(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public Tenant(Integer tenantId, String tenantName, Date modifiedDate, String tenantType) {
		this.tenantId = tenantId;
		this.tenantName = tenantName;
		this.modifiedDate = modifiedDate;
		this.tenantType = tenantType;
	}

	public Integer getTenantId() {
		return tenantId;
	}

	public void setTenantId(Integer tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getTenantDescription() {
		return tenantDescription;
	}

	public void setTenantDescription(String tenantDescription) {
		this.tenantDescription = tenantDescription;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getTenantType() {
		return tenantType;
	}

	public void setTenantType(String tenantType) {
		this.tenantType = tenantType;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (tenantId != null ? tenantId.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Tenant)) {
			return false;
		}
		Tenant other = (Tenant) object;
		if ((this.tenantId == null && other.tenantId != null)
				|| (this.tenantId != null && !this.tenantId.equals(other.tenantId))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "gradleproject1.Tenant[ tenantId=" + tenantId + " ]";
	}

}
