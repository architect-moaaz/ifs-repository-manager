package io.intelliflow.helper.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.FileInformation;
import io.intelliflow.repomanager.model.db.ApplicationDetail;
import io.intelliflow.repomanager.model.db.FileDetail;
import io.intelliflow.repomanager.model.db.operational.AppsByName;
import io.intelliflow.repomanager.model.db.operational.DataByUser;
import io.intelliflow.repomanager.model.db.operational.FilesByApp;
import io.intelliflow.repomanager.model.db.operational.WorkspaceByName;
import io.intelliflow.services.repo.ParentRepository;
import io.intelliflow.services.repo.ParentRepositorySingleton;
import io.quarkus.logging.Log;
import net.minidev.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class AssetDataService {

    @Inject
    ParentRepositorySingleton parentRepositorySingleton;

    public DataByUser getDataByUser(String userId){
        ParentRepository<DataByUser> dataByUserParentRepository = (ParentRepository<DataByUser>) parentRepositorySingleton.getRepository("dataByUser");
        return dataByUserParentRepository.modelWithFilters(Map.of("userId", userId));
    }

    public EventResponseModel appDataInWorkspace(String workspaceName, String status, int pageNumber, int pageSize, String sortCriteria,String filters) throws JsonProcessingException {
        ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
        ObjectMapper objectMapper = new ObjectMapper();
        List<ApplicationDetail> applicationDetails = new ArrayList<>();
        EventResponseModel responseModel = new EventResponseModel();
        responseModel.setMessage("App information within " + workspaceName);
        int publishedAppCount = 0;
        int inDevAppCount = 0;
        List<Map<String, String>> sort = new ArrayList<>();
        if(sortCriteria != null) {
            JsonNode sortNode = objectMapper.readTree(sortCriteria);
            sort = objectMapper.convertValue(sortNode, new TypeReference<>() {
            });
            sort = convertToLowerCaseKeys(sort);
        }
        List<Map<String, String>> queryFilter = new ArrayList<>();
        if(filters != null){
            JsonNode filterNode = objectMapper.readTree(filters);
            queryFilter = objectMapper.convertValue(filterNode, new TypeReference<>() {
            });
            queryFilter = convertToLowerCaseKeys(queryFilter);
        }
        Log.info("Apps for " + workspaceName + " loaded");
        double totalAppCount;
        double totalPages;
        Map<String, Object>mapFilter = new HashMap<>();
        if (!queryFilter.isEmpty()) {
            mapFilter.put("workspacename", workspaceName);
            for (Map<String, String> filterMap : queryFilter) {
                mapFilter.putAll(filterMap);
            }
        } else {
            mapFilter = Map.of("workspacename", workspaceName);
        }
        List<AppsByName> result = appsByNameParentRepository.listOfModelsWithFiltersPaginated(mapFilter,
                sort, pageNumber, pageSize);
        totalAppCount = appsByNameParentRepository.getCount(mapFilter);
        totalPages = Math.ceil(totalAppCount / pageSize);
        for (AppsByName appData : result) {
            if (Objects.nonNull(status)) {
                if (appData.getStatus().equalsIgnoreCase(status)) {
                    applicationDetails.add(createAppDetail(appData));
                }
            } else {
                if (!appData.getStatus().equalsIgnoreCase("DELETED")) {
                    applicationDetails.add(createAppDetail(appData));
                }
            }
            if(Objects.nonNull(appData.getStatus())) {
                if(appData.getStatus().equalsIgnoreCase("Active")) {
                    inDevAppCount ++;
                } else if (appData.getStatus().equalsIgnoreCase("FINISHED")) {
                    publishedAppCount ++;
                }
            }
        }

        JSONObject countObj = new JSONObject();
        countObj.put("published", publishedAppCount);
        countObj.put("development", inDevAppCount);

        countObj.put("totalApps", totalAppCount);
        countObj.put("totalPages", totalPages);

        JSONObject dataObj =new JSONObject();
        dataObj.put("count", countObj);
        dataObj.put("apps", applicationDetails);

        responseModel.setData(dataObj);
        return responseModel;
    }

    private static List<Map<String, String>> convertToLowerCaseKeys(List<Map<String, String>> originalList) {
        List<Map<String, String>> resultList = new ArrayList<>();
        for (Map<String, String> originalMap : originalList) {
            Map<String, String> resultMap = new HashMap<>();
            for (Map.Entry<String, String> entry : originalMap.entrySet()) {
                resultMap.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            resultList.add(resultMap);
        }
        return resultList;
    }

    public List<FileDetail> fileDataInApp(String workspaceName, String appName, String status) {
        ParentRepository<FilesByApp> filesByAppParentRepository = (ParentRepository<FilesByApp>) parentRepositorySingleton.getRepository("filesByApp");
        ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");

        AppsByName appData = appsByNameParentRepository.modelWithFilters(Map.of("workspacename",workspaceName, "appname", appName));
        List<FileDetail> fileDetails = new ArrayList<>();
        if(appData != null) {
            Set<String> filesInApp = appData.getFiles();
            for(String fileName : filesInApp) {
                FilesByApp fileData = filesByAppParentRepository.modelWithFilters(Map.of("workspacename",workspaceName, "appname",appName, "fileName",fileName));

                if(Objects.nonNull(status) && Objects.nonNull(fileData)) {
                    if(fileData.getStatus().equalsIgnoreCase(status)) {
                        fileDetails.add(createFileDetail(fileData));
                    }
                } else if(Objects.nonNull(fileData)){
                    fileDetails.add(createFileDetail(fileData));
                }
            }
        }
        return fileDetails;
    }

    static FileDetail createFileDetail(FilesByApp file) {
        FileDetail fileDetail = new FileDetail();
        fileDetail.setWorkspaceName(file.getWorkspacename());
        fileDetail.setAppName(file.getAppname());
        fileDetail.setStatus(file.getStatus());
        fileDetail.setUserId(file.getUserid());
        fileDetail.setCreationTime(file.getCreationtime().toInstant());
        fileDetail.setFileName(file.getFilename());
        fileDetail.setLastUpdatedTime(file.getLastupdatedtime().toInstant());
        return fileDetail;
    }

    static ApplicationDetail createAppDetail(AppsByName appData){
        ApplicationDetail appDetail = new ApplicationDetail();
        appDetail.setWorkspaceName(appData.getWorkspacename());
        appDetail.setAppName(appData.getAppname());
        appDetail.setStatus(appData.getStatus());
        appDetail.setUserId(appData.getUserid());
        appDetail.setCreationTime(appData.getCreationtime().toInstant());
        appDetail.setLogoUrl(appData.getLogourl());
        appDetail.setDeviceSupport(appData.getDevicesupport());
        appDetail.setColorScheme(appData.getColorscheme());
        appDetail.setLastUpdatedTime(appData.getLastupdatedtime().toInstant());
        appDetail.setAppDisplayName(appData.getAppdisplayname());
        appDetail.setDescription(appData.getDescription());
        return appDetail;
    }

    public void updateTimeForData(FileInformation fileInformation) {
        ParentRepository<FilesByApp> filesByAppParentRepository = (ParentRepository<FilesByApp>) parentRepositorySingleton.getRepository("filesByApp");
        ParentRepository<AppsByName> appsByNameParentRepository = (ParentRepository<AppsByName>) parentRepositorySingleton.getRepository("appByName");
        ParentRepository<WorkspaceByName> workspaceByNameParentRepository = (ParentRepository<WorkspaceByName>) parentRepositorySingleton.getRepository("workspaceByName");

        if(Objects.nonNull(fileInformation) && Objects.nonNull(fileInformation.getWorkspaceName())) {
            WorkspaceByName workspaceByName = workspaceByNameParentRepository.modelWithFilters(Map.of("workspacename",fileInformation.getWorkspaceName()));
            if(workspaceByName != null){
              workspaceByName.setLastupdatedtime(new Date());
              workspaceByNameParentRepository.updateModel(workspaceByName);
            }

            if(Objects.nonNull(fileInformation.getMiniApp())) {
                AppsByName appsByName = appsByNameParentRepository.modelWithFilters(Map.of("workspacename",fileInformation.getWorkspaceName() ,"appname",fileInformation.getMiniApp()));
                if(appsByName != null){
                    appsByName.setLastupdatedtime(new Date());
                }

                if(Objects.nonNull(fileInformation.getFileName())) {
                    FilesByApp filesByApp = filesByAppParentRepository.modelWithFilters(Map.of("workspacename", fileInformation.getWorkspaceName(),"appname",  fileInformation.getMiniApp(),"fileName",fileInformation.getFileName()));
                    if(filesByApp != null){
                        filesByApp.setLastupdatedtime(new Date());
                        filesByAppParentRepository.updateModel(filesByApp);
                    }
                }
            }
        }
    }
}
