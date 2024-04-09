package io.intelliflow.repomanager.model.db.operational;

 /*
    @author rahul.malawadkar@intelliflow.ai
    @created on 12-08-2023
 */

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@MongoEntity(collection = "deployment")
public class Deployment {
    public ObjectId id;
    private String buildno;
    private String actionedby;
    private Date actiontime;
    private String outcome;
    private String action;
    private String comment;

    public Deployment() {
    }

    public Deployment(String buildNo, String actionedBy, Date actionTime, String outCome, String action, String comment) {
        this.buildno = buildNo;
        this.actionedby = actionedBy;
        this.actiontime = actionTime;
        this.outcome = outCome;
        this.action = action;
        this.comment = comment;
    }
}
