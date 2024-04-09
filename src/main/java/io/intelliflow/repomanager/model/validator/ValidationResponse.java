package io.intelliflow.repomanager.model.validator;
import javax.json.JsonObject;
import javax.json.bind.annotation.JsonbProperty;
public class ValidationResponse {

    @JsonbProperty("data")
    JsonObject data;

    @JsonbProperty("errors")
    public boolean errors;

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

    public boolean isErrors() {
        return errors;
    }

    public void setErrors(boolean errors) {
        this.errors = errors;
    }
}



