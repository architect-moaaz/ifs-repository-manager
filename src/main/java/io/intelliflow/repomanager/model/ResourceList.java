package io.intelliflow.repomanager.model;

import java.util.List;
import java.util.Map;


public class ResourceList {
	Map<String , List<ResourceModel>> resources;

	public Map<String, List<ResourceModel>> getResources() {
		return resources;
	}

	public void setResources(Map<String, List<ResourceModel>> resources) {
		this.resources = resources;
	}

}
