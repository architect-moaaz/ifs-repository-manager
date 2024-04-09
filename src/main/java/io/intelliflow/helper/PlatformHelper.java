package io.intelliflow.helper;

 /*
    @author rahul.malawadkar@intelliflow.ai
    @created on 06-07-2023
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.db.operational.AppsByName;
import io.intelliflow.repomanager.model.db.operational.Deployment;
import io.intelliflow.services.repo.ParentRepository;
import io.intelliflow.services.repo.ParentRepositorySingleton;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class PlatformHelper {

    @Inject
    ParentRepositorySingleton parentRepositorySingleton;

    public EventResponseModel getBuildHistory(String workSpace, String app,String timeZone,int pageSize,int pageNumber) {
        ParentRepository<Deployment> deploymentParentRepository = (ParentRepository<Deployment>) parentRepositorySingleton.getRepository("deployment");
        ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
        app = app.toLowerCase().replace(" ","-");
        ObjectMapper objectMapper = new ObjectMapper();
        EventResponseModel eventResponseModel = new EventResponseModel();

        AppsByName appsByName = appsByNameParentRepository.modelWithFilters(Map.of("workspacename", workSpace,"appname", app));
        Set<String> allDeployments = new HashSet<>();
        if(appsByName != null){
            if (!appsByName.getAlldeployment().isEmpty()) {
                allDeployments = appsByName.getAlldeployment();
            }
            Log.info("Deployment id's for app : " + app + " : " + allDeployments);
        }
        Map<String, Object> response = new HashMap<>();
        List<String> listOfDeployments = objectMapper.convertValue(allDeployments,new TypeReference<>() {
        });
        double totalCount = listOfDeployments.size();
        double totalPages = Math.ceil(totalCount / pageSize);
        listOfDeployments.sort(Collections.reverseOrder());
        List<Map<String, String>> list = new ArrayList<>();
        int start = (pageNumber - 1) * pageSize;
        int end = pageNumber * pageSize;
        end = Math.min(end,listOfDeployments.size());
        for(int i = start ; i < end ; i++ ) {
            String buildNo = listOfDeployments.get(i);
            List<Deployment> deployments = deploymentParentRepository.listOfModelsWithFilters(Map.of("buildno", buildNo));

            int count = 0;
            String actionedBy;
            Map<String, String> map = new HashMap<>();
            String queuedStatus = null;
            for (Deployment deployment : deployments) {
                ZoneId zoneId = timeZone == null ? ZoneId.systemDefault() : ZoneId.of(timeZone);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                        .withZone(zoneId);
                Instant actionTimeInstant = deployment.getActiontime().toInstant();
                String formattedDate = formatter.format(actionTimeInstant);
                String outCome = deployment.getOutcome();
                if(outCome.equalsIgnoreCase("QUEUED")){
                    queuedStatus = outCome;
                    actionedBy = deployment.getActionedby();
                    map.put("deployedBy",actionedBy);
                    map.put("startTime", formattedDate);
                }
                if(outCome.equalsIgnoreCase("FINISHED")){
                    map.put("endTime", formattedDate);
                }
                count++;
            }
            list.add(map);
            if(count == 1){
                map.put("Status",queuedStatus == null ? "FAILED" : queuedStatus.toUpperCase());
            }else if(count == 2){
                map.put("status","RUNNING");
            }else {
              map.put("status","COMPLETED");
            }
        }
        if(list.isEmpty()){
            eventResponseModel.setMessage("There is no more build history for the app: "+app);
        }else {
            response.put("totalCount",totalCount);
            response.put("totalPages",totalPages);
            response.put("records",list);
            eventResponseModel.setData(response);
            eventResponseModel.setMessage("The build history for app: " + app);
        }
        return eventResponseModel;
    }
}
