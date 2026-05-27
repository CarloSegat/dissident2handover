package com.dissident.accesspoint.service;

import com.dissident.accesspoint.model.RegisteredService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class ServiceRepositoryManagerService {
    private final List<RegisteredService> services = new ArrayList<>();

    public void addService(RegisteredService service) {
        services.add(service);
    }

    public void removeService(Integer serviceId) {
        services.removeIf(service -> service.getServiceId().equals(serviceId));
    }

    public List<RegisteredService> getAllServices() {
        return new ArrayList<>(services);
    }

    public Optional<RegisteredService> getServiceById(Integer serviceId) {
        return services.stream()
                .filter(service -> service.getServiceId().equals(serviceId))
                .findFirst();
    }

    // Add any other methods you find necessary for managing the service repository
}
