package io.intelliflow.repomanager.model.db.operational;

 /*
    @author rahul.malawadkar@intelliflow.ai
    @created on 09-08-2023
 */

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@MongoEntity(collection = "apps_by_name")
public class AppsByName {

    public ObjectId id;
    private String workspacename;

    private String appname;

    private String appdisplayname;

    private String colorscheme;

    private Date creationtime;

    private String deploymentid;

    private String description;

    private String devicesupport;

    private Date lastupdatedtime;

    private String logourl;

    private String status;

    private String userid;

    private Set<String> alldeployment;

    private Set<String> files;


    public String getWorkspacename() {
        return workspacename;
    }

    public void setWorkspacename(String workspacename) {
        this.workspacename = workspacename;
    }

    public AppsByName() {
    }

    public AppsByName(String workspacename, String appname, String appdisplayname, String colorscheme, Date creationtime, String deploymentid, String description, String devicesupport, Date lastupdatedtime, String logourl, String status, String userid, Set<String> alldeployment, Set<String> files) {
        this.workspacename = workspacename;
        this.appname = appname;
        this.appdisplayname = appdisplayname;
        this.colorscheme = colorscheme;
        this.creationtime = creationtime;
        this.deploymentid = deploymentid;
        this.description = description;
        this.devicesupport = devicesupport;
        this.lastupdatedtime = lastupdatedtime;
        this.logourl = logourl;
        this.status = status;
        this.userid = userid;
        this.alldeployment = alldeployment;
        this.files = files;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getAppdisplayname() {
        return appdisplayname;
    }

    public void setAppdisplayname(String appdisplayname) {
        this.appdisplayname = appdisplayname;
    }

    public String getColorscheme() {
        return colorscheme;
    }

    public void setColorscheme(String colorscheme) {
        this.colorscheme = colorscheme;
    }

    public Date getCreationtime() {
        return creationtime;
    }

    public void setCreationtime(Date creationtime) {
        this.creationtime = creationtime;
    }

    public String getDeploymentid() {
        return deploymentid;
    }

    public void setDeploymentid(String deploymentid) {
        this.deploymentid = deploymentid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDevicesupport() {
        return devicesupport;
    }

    public void setDevicesupport(String devicesupport) {
        this.devicesupport = devicesupport;
    }

    public Date getLastupdatedtime() {
        return lastupdatedtime;
    }

    public void setLastupdatedtime(Date lastupdatedtime) {
        this.lastupdatedtime = lastupdatedtime;
    }

    public String getLogourl() {
        return logourl;
    }

    public void setLogourl(String logourl) {
        this.logourl = logourl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Set<String> getAlldeployment() {
        return Objects.requireNonNullElseGet(alldeployment, HashSet::new);
    }

    public void setAlldeployment(Set<String> alldeployment) {
        this.alldeployment = alldeployment;
    }

    public Set<String> getFiles() {
        return Objects.requireNonNullElseGet(files, HashSet::new);
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }
}
