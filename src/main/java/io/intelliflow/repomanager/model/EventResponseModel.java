package io.intelliflow.repomanager.model;

public class EventResponseModel {
	
	private String message;
	private Object data;

	private Object created;

	public EventResponseModel() {
	}

	public EventResponseModel(String message, Object data) {
		this.message = message;
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getCreated() {
		return created;
	}

	public void setCreated(Object created) {
		this.created = created;
	}
}
