package io.intelliflow.centralCustomExceptionHandler;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CustomExceptionHandler implements ExceptionMapper<CustomException> {

    @Override
    public Response toResponse(CustomException e) {
        return Response.status(e.getStatusCode().getCode())
                .entity(e.getResponseModel())
                .header("Error", e.getResponseModel().getMessage())
                .build();
    }
}
