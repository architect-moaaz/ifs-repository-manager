package io.intelliflow.services.repo;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public interface ParentRepository<T>{

    void saveModel(T object);
    void updateModel(T object);

    T modelWithFilters(Map<String, Object> params);

    List<T> listOfModelsWithFilters(Map<String, Object> params);

    void deleteModel(T object);

    void deleteModelWithParams(Map<String, Object> params);

    List<T> listOfModelsWithFiltersPaginated(Map<String, Object> params, List<Map<String, String>> sortCriteria, int page, int size);

    long getCount();

    long getCount(Map<String, Object> params);

}
