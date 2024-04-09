package io.intelliflow.repomanager.model;

import java.util.List;

public class MessageEventModel {

    private String workspacename;
    private String appname;
    private String bpmnName;
    private List<String> startMessages;
    private List<String> endMessages;

    public String getWorkspacename() {
        return workspacename;
    }

    public void setWorkspacename(String workspacename) {
        this.workspacename = workspacename;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getBpmnName() {
        return bpmnName;
    }

    public void setBpmnName(String bpmnName) {
        this.bpmnName = bpmnName;
    }

    public List<String> getStartMessages() {
        return startMessages;
    }

    public void setStartMessages(List<String> startMessages) {
        this.startMessages = startMessages;
    }

    public List<String> getEndMessages() {
        return endMessages;
    }

    public void setEndMessages(List<String> endMessages) {
        this.endMessages = endMessages;
    }
}
