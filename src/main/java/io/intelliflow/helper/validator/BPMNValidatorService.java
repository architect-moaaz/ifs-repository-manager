package io.intelliflow.helper.validator;

import io.intelliflow.repomanager.model.validator.BPMNData;
import io.intelliflow.repomanager.model.validator.ValidationResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "bpmnvalidator-api")
public interface BPMNValidatorService {

    @POST
    @Path("/lint")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ValidationResponse validateBPMN(BPMNData data);
}
