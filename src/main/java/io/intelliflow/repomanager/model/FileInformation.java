package io.intelliflow.repomanager.model;

import org.eclipse.jgit.lib.PersonIdent;

public class FileInformation {

	private String fileName;
	private String fileType;
	private String fileID;
	private String workspaceName;
	private String miniApp;
	private String content;
	private String comment;
	private String operation;
	private PersonIdent person;
	private String userId;
	private String updatedName;

	private String appDisplayName;

	private String description;

	
	public FileInformation() {
	}

	public FileInformation(String fileName, String fileType, String workspaceName, String miniApp, String content, String comment, String operation, PersonIdent person) {
		this.fileName = fileName;
		this.fileType = fileType;
		this.workspaceName = workspaceName;
		this.miniApp = miniApp;
		this.content = content;
		this.comment = comment;
		this.operation = operation;
		this.person = person;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getMiniApp() {
		return miniApp;
	}

	public void setMiniApp(String miniApp) {
		this.miniApp = miniApp;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public PersonIdent getPerson() {
		return person;
	}

	public void setPerson(PersonIdent person) {
		this.person = person;
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUpdatedName() {
		return updatedName;
	}

	public void setUpdatedName(String updatedName) {
		this.updatedName = updatedName;
	}

	public String getAppDisplayName() {
		return appDisplayName;
	}

	public void setAppDisplayName(String appDisplayName) {
		this.appDisplayName = appDisplayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
