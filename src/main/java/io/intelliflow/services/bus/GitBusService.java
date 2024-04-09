package io.intelliflow.services.bus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.intelliflow.handler.GITHandler;
import io.intelliflow.repomanager.model.EventResponseModel;
import io.intelliflow.repomanager.model.FileInformation;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class GitBusService {
	
	@Inject
	GITHandler gitHandler;
	
	@ConsumeEvent(value = "repository")
	public Uni<EventResponseModel> consume(FileInformation fileInformation) {

		EventResponseModel eventResponseModel = gitHandler.performFileOperation(fileInformation) ;

		 return Uni.createFrom().item(() -> eventResponseModel);

	}
}
