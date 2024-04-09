package io.intelliflow.helper;

import io.intelliflow.centralCustomExceptionHandler.CustomException;
import io.intelliflow.helper.validator.BPMNValidatorService;
import io.intelliflow.helper.validator.DMNValidatorService;
import io.intelliflow.repomanager.model.FileInformation;
import io.intelliflow.repomanager.model.GITResponseModel;
import io.intelliflow.repomanager.model.validator.BPMNData;
import io.intelliflow.repomanager.model.validator.DMNData;
import io.intelliflow.repomanager.model.validator.ValidationResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

@ApplicationScoped
public class FileHelper {

    @Inject
    @RestClient
    BPMNValidatorService validatorService;
    @Inject
    @RestClient
    DMNValidatorService DmnvalidatorService;

    /*
       Method to delete files with directories
    */
    public static void removeRecursively(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                removeRecursively(c);
            }
        }
        f.delete();
    }

    public ValidationResponse validateBPMN(FileInformation fileInformation) {
        GITFileHelper helper = new GITFileHelper();
        BPMNData bpmnData = new BPMNData();
        ValidationResponse response = null;
        if(Objects.nonNull(fileInformation.getContent())){
            bpmnData.setBpmn(fileInformation.getContent().getBytes());
            return validatorService.validateBPMN(bpmnData);

        } else {
            try {
                GITResponseModel fileData = helper.fetchFileContentFromDir(fileInformation);
                bpmnData.setBpmn(((String) fileData.getData()).getBytes());
                return validatorService.validateBPMN(bpmnData);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (CustomException e) {
                throw new RuntimeException(e);
            }
        }
        return response;
    }
    public ValidationResponse validateDMN(FileInformation fileInformation) {
        GITFileHelper helper = new GITFileHelper();
        DMNData dmnData = new DMNData();
        ValidationResponse response = null;
        if(Objects.nonNull(fileInformation.getContent())){
            dmnData.setDmn(fileInformation.getContent().getBytes());
            return  DmnvalidatorService.validateDmn(dmnData);

        } else {
            try {
                GITResponseModel fileData = helper.fetchFileContentFromDir(fileInformation);
                dmnData.setDmn(((String) fileData.getData()).getBytes());
                return DmnvalidatorService.validateDmn(dmnData);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (CustomException e) {
                throw new RuntimeException(e);
            }
        }
        return response;
    }
}
