package io.intelliflow.repomanager.model.db.operational;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@MongoEntity(collection = "data_by_user")
public class DataByUser {

    public ObjectId id;
    private String userid;

    private Set<String> workspaces;

    private Set<String> apps;

    private Set<String> files;

    public DataByUser() {
    }

    public DataByUser(String userId, Set<String> apps, Set<String> files, Set<String> workSpaces) {
        this.userid = userId;
        this.apps = apps;
        this.files = files;
        this.workspaces = workSpaces;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Set<String> getWorkspaces() {
        // Return an empty HashSet if files is null
        return Objects.requireNonNullElseGet(workspaces, HashSet::new);
    }

    public void setWorkspaces(Set<String> workspaces) {
        this.workspaces = workspaces;
    }

    public Set<String> getApps() {
        // Return an empty HashSet if files is null
        return Objects.requireNonNullElseGet(apps, HashSet::new);
    }

    public void setApps(Set<String> apps) {
        this.apps = apps;
    }

    public Set<String> getFiles() {
        // Return an empty HashSet if files is null
        return Objects.requireNonNullElseGet(files, HashSet::new);
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }
}
