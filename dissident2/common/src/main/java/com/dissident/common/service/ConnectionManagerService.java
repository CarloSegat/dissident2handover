package com.dissident.common.service;

import org.springframework.stereotype.Service;

import com.dissident.common.model.ControllerConnectionRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ConnectionManagerService {

    public ConnectionManagerService() {
    }

    private final List<ControllerConnectionRecord> connectionRecords = new ArrayList<>();

    public void addConnectionRecord(String connectionId, String did, String didDocument, boolean isTrusted,
            String status, String role) {
        ControllerConnectionRecord record = new ControllerConnectionRecord(connectionId, did, didDocument, isTrusted,
                status, role);
        connectionRecords.add(record);
    }

    public boolean isTrusted(String did) {
        return connectionRecords.stream()
                .anyMatch(record -> record.getDid().equals(did) && record.isTrusted());
    }

    public boolean isConnectionActiveAndTrusted(String did) {
        synchronized (connectionRecords) {
            return connectionRecords.stream()
                    .anyMatch(record -> record.getDid().equals(did) && record.isTrusted()
                            && "active".equals(record.getStatus()));
        }
    }

    public boolean isAnyActiveDlgPresent() {
        final boolean result = connectionRecords.stream()
                .anyMatch(
                        record -> "dlg".equals(record.getRole().toLowerCase()) &&
                                "active".equals(record.getStatus().toLowerCase()));
        System.out.println("is an active dlg connection present? " + result);
        return result;
    }

    public CompletableFuture<ControllerConnectionRecord> waitForActiveDlgConnection() {
        CompletableFuture<ControllerConnectionRecord> future = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    System.out.println(connectionRecords.size());
                    // Synchronized block to handle concurrent modifications
                    synchronized (connectionRecords) {
                        for (ControllerConnectionRecord record : connectionRecords) {
                            System.out.println("\trecord:\n " + record.getRole());
                            System.out.println("\t" + record.getStatus());
                            if ("dlg".equals(record.getRole().toLowerCase())
                                    && "active".equals(record.getStatus().toLowerCase())) {
                                future.complete(record);
                                return;
                            }
                        }
                    }

                    // Sleep to prevent busy waiting
                    TimeUnit.MILLISECONDS.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                future.completeExceptionally(e); // Complete the future exceptionally
            }
        });

        return future;
    }

    public CompletableFuture<ControllerConnectionRecord> waitForConnectionWithRole(String roleName) {
        CompletableFuture<ControllerConnectionRecord> future = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    // Synchronized block to handle concurrent modifications
                    synchronized (connectionRecords) {
                        for (ControllerConnectionRecord record : connectionRecords) {
                            if (roleName.equals(record.getRole()) && "active".equals(record.getStatus())) {
                                future.complete(record);
                                return;
                            }
                        }
                    }

                    // Sleep to prevent busy waiting
                    TimeUnit.MILLISECONDS.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                future.completeExceptionally(e); // Complete the future exceptionally
                System.out.println("Connection for " + roleName + " is up");

            }
        });

        return future;
    }

    public List<ControllerConnectionRecord> getConnectionRecords() {
        return Collections.unmodifiableList(new ArrayList<>(connectionRecords));
    }

    public void printAllConnectionRecords() {
        if (connectionRecords.isEmpty()) {
            System.out.println("No connection records found.");
            return;
        }

        // System.out.println("Connection Records:");
        // for (ControllerConnectionRecord record : connectionRecords) {
        //     System.out.println("----------------------------");
        //     System.out.println("Connection ID: " + record.getConnectionId());
        //     System.out.println("DID: " + record.getDid());
        //     System.out.println("DID Document: " + record.getDidDocument());
        //     System.out.println("Is Trusted: " + record.isTrusted());
        //     System.out.println("Status: " + record.getStatus());
        //     System.out.println("Role: " + record.getRole());
        // }
        // System.out.println("----------------------------");
    }

    public void updateConnectionRecordsByRole(String connectionId, String did, String didDocument, Boolean isTrusted,
            String status, String role) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (role.equals(record.getRole())) {
                    // Update attributes if provided, otherwise keep the original value
                    if (connectionId != null)
                        record.setConnectionId(connectionId);
                    if (did != null)
                        record.setDid(did);
                    if (didDocument != null)
                        record.setDidDocument(didDocument);
                    if (isTrusted != null)
                        record.setTrusted(isTrusted);
                    if (status != null)
                        record.setStatus(status);
                }
            }
        }

    }

    public void updateConnectionRecordByConnectionId(String connectionId, String did, String didDocument,
            Boolean isTrusted, String status, String role) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (record.getConnectionId().equals(connectionId)) {
                    // Update attributes if provided, otherwise keep the original value
                    if (did != null)
                        record.setDid(did);
                    if (didDocument != null)
                        record.setDidDocument(didDocument);
                    if (isTrusted != null)
                        record.setTrusted(isTrusted);
                    if (status != null)
                        record.setStatus(status);
                    if (role != null)
                        record.setRole(role);
                    return;
                }
            }
        }
    }

    /**
     * Updates the status of a connection record by DID.
     *
     * @param did       the DID of the connection record to be updated.
     * @param newStatus the new status to set.
     */
    public void updateConnectionRecordStatusByDid(String did, String newStatus) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (record.getDid().equals(did)) {
                    record.setStatus(newStatus);
                    return;
                }
            }
        }
    }

    /**
     * Updates the trust status of a connection record by DID.
     *
     * @param did       the DID of the connection record to be updated.
     * @param isTrusted the new trust status to set.
     */
    public void updateConnectionRecordTrustByDid(String did, boolean isTrusted) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (record.getDid().equals(did)) {
                    record.setTrusted(isTrusted);
                    return;
                }
            }
        }
    }

    /**
     * Retrieves a connection record by its DID.
     *
     * @param did the DID of the connection record.
     * @return the connection record if found, null otherwise.
     */
    public ControllerConnectionRecord getConnectionRecordByDid(String did) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (record.getDid().equals(did)) {
                    return record;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the connection ID for the first connection record matching the
     * specified role.
     *
     * @param role the role to match in the connection records.
     * @return the connection ID if found, null otherwise.
     */
    public String getConnectionIdByRole(String role) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (role.equals(record.getRole())) {
                    return record.getConnectionId();
                }
            }
        }
        return null;
    }

    public String getConnectionRoleById(String id) {
        synchronized (connectionRecords) {
            String role = connectionRecords
            .stream()
            .filter(r-> id.equals(r.getConnectionId()))
            .map(ControllerConnectionRecord::getRole)
            .findFirst()
            .orElse(null);

            System.out.println("Id " + id + " corresponds to role " + role);

            return role;
        }
    }

    /**
     * Resolves a DID to its corresponding DIDDocument as a String.
     * This method returns the DIDDocument directly from the connection records.
     *
     * @param did the DID to resolve.
     * @return the corresponding DIDDocument as a String, or null if not found.
     */
    public String getcachedDidDocument(String did) {
        synchronized (connectionRecords) {
            // Check if the DIDDocument is available in the connection records
            for (ControllerConnectionRecord record : connectionRecords) {
                if (record.getDid().equals(did)) {
                    // Return the DIDDocument string directly from the record
                    return record.getDidDocument();
                }
            }
        }

        // Return null if the DIDDocument is not found in the connection records
        return null;
    }

    /**
     * Updates the connection ID of a connection record by its DID.
     *
     * @param did             the DID of the connection record to be updated.
     * @param newConnectionId the new connection ID to set.
     */
    public void updateConnectionIdByDid(String did, String newConnectionId) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (record.getDid().equals(did)) {
                    record.setConnectionId(newConnectionId);
                    return;
                }
            }
        }
    }

    /**
     * Updates the trust status of a connection record by connection ID.
     *
     * @param connectionId the connection ID of the connection record to be updated.
     * @param isTrusted    the new trust status to set.
     */
    public void updateConnectionRecordTrustByConnectionId(String connectionId, boolean isTrusted) {
        synchronized (connectionRecords) {
            for (ControllerConnectionRecord record : connectionRecords) {
                if (record.getConnectionId().equals(connectionId)) {
                    record.setTrusted(isTrusted);
                    return;
                }
            }
        }
    }
}