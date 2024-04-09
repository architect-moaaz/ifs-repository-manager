package io.intelliflow.services.repo;

 /*
    @author rahul.malawadkar@intelliflow.ai
    @created on 09-08-2023
 */

import com.mongodb.BasicDBObject;
import io.intelliflow.repomanager.model.db.operational.WorkspaceByName;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class WorkspaceByNameRepository implements PanacheMongoRepository<WorkspaceByName>, ParentRepository<WorkspaceByName> {

    @Override
    public void saveModel(WorkspaceByName object) {
        persist(object);
    }

    @Override
    public void updateModel(WorkspaceByName object) {
        update(object);
    }

    @Override
    public WorkspaceByName modelWithFilters(Map<String, Object> params) {
        BasicDBObject query = new BasicDBObject();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String fieldKey = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue.toString().startsWith(".*")) {
                query.put(fieldKey, new BasicDBObject("$regex", fieldValue).append("$options", "i"));
            } else {
                query.put(fieldKey, fieldValue.toString());
            }
        }
        return find(query.toString(), params).firstResult();
    }

    @Override
    public List<WorkspaceByName> listOfModelsWithFilters(Map<String, Object> params) {
        BasicDBObject query = new BasicDBObject();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String fieldKey = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue.toString().startsWith(".*")) {
                query.put(fieldKey, new BasicDBObject("$regex", fieldValue).append("$options", "i"));
            } else {
                query.put(fieldKey, fieldValue.toString());
            }
        }
        return find(query.toString(), params).list();
    }

    @Override
    public void deleteModel(WorkspaceByName object) {
        delete(object);
    }

    @Override
    public void deleteModelWithParams(Map<String, Object> params) {
        BasicDBObject query = new BasicDBObject();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String fieldKey = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue.toString().startsWith(".*")) {
                query.put(fieldKey, new BasicDBObject("$regex", fieldValue).append("$options", "i"));
            } else {
                query.put(fieldKey, fieldValue.toString());
            }
        }
        delete(query.toString(), params);
    }

    @Override
    public List<WorkspaceByName> listOfModelsWithFiltersPaginated(Map<String, Object> params, List<Map<String, String>> sortCriteria, int page, int size) {
        Sort sort = Sort.by();
        BasicDBObject query = new BasicDBObject();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String fieldKey = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue.toString().startsWith(".*")) {
                query.put(fieldKey, new BasicDBObject("$regex", fieldValue).append("$options", "i"));
            } else {
                query.put(fieldKey, fieldValue.toString());
            }
        }

        if (sortCriteria != null && !sortCriteria.isEmpty()) {
            for (Map<String, String> sortMap : sortCriteria) {
                for (Map.Entry<String, String> entry : sortMap.entrySet()) {
                    String direction = entry.getValue().equalsIgnoreCase("asc") ? "Ascending" : "Descending";
                    sort.and(entry.getKey(), Sort.Direction.valueOf(direction));
                }
            }
            return find(String.valueOf(query), sort).page(page, size).list();
        } else {
            return find(String.valueOf(query)).page(page, size).list();
        }
    }

    @Override
    public long getCount() {
        return count();
    }

    @Override
    public long getCount(Map<String, Object> params) {
        return listOfModelsWithFilters(params).size();
    }
}
