package io.intelliflow.repomanager.model.db.operational;

 /*
    @author rahul.malawadkar@intelliflow.ai
    @created on 09-08-2023
 */

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@MongoEntity(collection = "workspace_by_name")
public class WorkspaceByName {
    public ObjectId id;
    private String workspacename;
    private Date creationtime;
    private Date lastupdatedtime;
    private String status;
    private String userid;

    public WorkspaceByName() {
    }

    public WorkspaceByName(String workSpaceName, Date creationTime, Date lastUpdatedTime, String status, String userid) {
        this.workspacename = workSpaceName;
        this.creationtime = creationTime;
        this.lastupdatedtime = lastUpdatedTime;
        this.status = status;
        this.userid = userid;
    }
}
