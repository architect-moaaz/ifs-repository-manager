package io.intelliflow.repomanager.model;

import java.util.List;

public class StashModel {
	
	private String draftName;
	
	private String commiter;
	
	private Integer index;
	
	private String comment;
	
	private String commitTime;
	
	private String status;
	
	private List<String> changes;

	public List<String> getChanges() {
		return changes;
	}

	public void setChanges(List<String> changes) {
		this.changes = changes;
	}

	public String getDraftName() {
		return draftName;
	}

	public void setDraftName(String draftName) {
		this.draftName = draftName;
	}

	public String getCommiter() {
		return commiter;
	}

	public void setCommiter(String commiter) {
		this.commiter = commiter;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(String commitTime) {
		this.commitTime = commitTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
