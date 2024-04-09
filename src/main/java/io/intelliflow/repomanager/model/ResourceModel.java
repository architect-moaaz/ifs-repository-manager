package io.intelliflow.repomanager.model;

import java.io.Serializable;

public class ResourceModel implements Serializable{
	private static final long serialVersionUID = 1L;
	private String resourceName;
	private String resourcePath;
	private String resourceType;
	private boolean lockStatus;
	private String lockOwner;

	public ResourceModel() {
	}

	public ResourceModel(String resourceName, String resourcePath, String resourceType) {
		this.resourceName = resourceName;
		this.resourcePath = resourcePath;
		this.resourceType = resourceType;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public boolean isLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(boolean lockStatus) {
		this.lockStatus = lockStatus;
	}

	public String getLockOwner() {
		return lockOwner;
	}

	public void setLockOwner(String lockOwner) {
		this.lockOwner = lockOwner;
	}
}
