package io.intelliflow.services.repo;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ParentRepositorySingleton {

    private static ParentRepositorySingleton instance;
    private final Map<String, ParentRepository<?>> repositoryMap;

    private ParentRepositorySingleton() {
        repositoryMap = new HashMap<>();
        initializeRepositories();
    }

    public static synchronized ParentRepositorySingleton getInstance() {
        if (instance == null) {
            instance = new ParentRepositorySingleton();
        }
        return instance;
    }

    private void initializeRepositories() {
        repositoryMap.put("appbyname", new AppsByNameRepository());
        repositoryMap.put("databyuser", new DataByUserRepository());
        repositoryMap.put("filesbyapp", new FilesByAppRepository());
        repositoryMap.put("workspacebyname", new WorkspaceByNameRepository());
        repositoryMap.put("deployment", new DeploymentRepository());
    }

    public ParentRepository<?> getRepository(String channel) {
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("Channel cannot be null or empty");
        }
        channel = channel.toLowerCase();
        ParentRepository<?> repository = repositoryMap.get(channel);
        if (repository == null) {
            throw new IllegalArgumentException("Unknown channel " + channel);
        }
        return repository;
    }
}
