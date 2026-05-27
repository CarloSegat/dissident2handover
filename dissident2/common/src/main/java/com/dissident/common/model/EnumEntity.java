package com.dissident.common.model;

import java.util.HashMap;
import java.util.Map;

public enum EnumEntity {
    AP, CUSTOMER, PROVIDER, DLG, ISSUER;
    
    final static Map<EnumEntity, String[]> entityToAliases;

    static {
        entityToAliases = new HashMap();
        entityToAliases.put(AP, new String[] {
                "accesspoint",
                "access-point",
                "ap",
                "did:sov:SUqWD8ZL3r6KeYTKKQ6zRw" });
        entityToAliases.put(CUSTOMER, new String[] {
                "customer",
                "client",
                "CLIENT",
                "CUSTOMER",
                "did:sov:V9fvKQjtmbsoJb7gLm2Fka"
        });
        entityToAliases.put(PROVIDER, new String[] {
                "provider",
                "PROVIDER",
                "did:sov:KssDMmREv3migEZNLMThjc"
        });
        entityToAliases.put(DLG, new String[] {
                "dlg",
                "did:sov:8eQhKkZMjKXbLwaBNEKRu3"
        });
        entityToAliases.put(ISSUER, new String[] {
                "issuer",
                "did:sov:WQtxQy4ERo6vgxkM1o5BPh"
        });
    }

    public static EnumEntity fromString(String role) {
        return findMatchingEntity(role);
    }

    private static EnumEntity findMatchingEntity(String role) {
        return entityToAliases.entrySet().stream()
                .filter(entry -> containsString(entry.getValue(), role))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static boolean containsString(String[] array, String target) {
        for (String s : array) {
            if (s.equals(target)) {
                return true;
            }
        }
        return false;
    }
}