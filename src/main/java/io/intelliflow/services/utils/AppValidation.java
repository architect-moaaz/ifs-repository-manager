package io.intelliflow.services.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.intelliflow.repomanager.model.FileInformation;
import org.bson.Document;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Map;

public class AppValidation {
     private static MongoClient mongoClient;

    private static MongoDatabase db;

    private static MongoCollection<Document> collection;

    public static int limitAllowed(FileInformation fileInformation,String validationType){
    Config config = ConfigProvider.getConfig();
        String uri = config.getValue("quarkus.mongodb.connection-string", String.class);
        String hostAndPort = uri.substring(10);
        String username = config.getValue("quarkus.mongodb.credentials.username", String.class);
        String password = config.getValue("quarkus.mongodb.credentials.password", String.class);
        String dbURI = "mongodb://" + username + ":" + password + "@" + hostAndPort;
        String databaseName = "global";
        String collectionName = "workspace_config";
        String workspace_name = fileInformation.getWorkspaceName().equalsIgnoreCase("Intelliflow")? "master" : fileInformation.getWorkspaceName();
        System.out.println("url: " + dbURI);
        mongoClient = MongoClients.create(dbURI);
        db = mongoClient.getDatabase(databaseName);
        collection = db.getCollection(collectionName);
        Document entry = collection.find(Filters.eq("workspace_name", workspace_name)).first();
        ObjectMapper objectMapper = new ObjectMapper();
        if(validationType.equalsIgnoreCase("app")){
        int totalApps = 999;
        if (entry != null) {
            Object totalNumberOfApps = entry.get("workspace_config");
            Map<String, Object> responseMap = objectMapper.convertValue(totalNumberOfApps, new TypeReference<>() {
            });
            if (responseMap != null && responseMap.containsKey("noOfApps")) {
                totalApps = (int) responseMap.get("noOfApps");
            }
        } else {
            System.out.println("Entry not found.");
        }
        mongoClient.close();
        return totalApps;
    }else{
        int totalForms = 999;
		if (entry != null) {
			Object totalNumberOfForms = entry.get("workspace_config");
			Map<String, Object> responseMap = objectMapper.convertValue(totalNumberOfForms, new TypeReference<>() {
			});
			if (responseMap != null && responseMap.containsKey("noOfFormsPerApp")) {
				totalForms = (int) responseMap.get("noOfFormsPerApp");
			}
		} else {
			System.out.println("Limitation is not there for this workspace");
		}
		mongoClient.close();
		return totalForms;
    }
    }
}
