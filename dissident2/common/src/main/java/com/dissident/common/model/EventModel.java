package com.dissident.common.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.dissident.common.model.events.EventSubGroup;

public class EventModel {
    public EnumEntity srcEntity;
    public EnumEntity trgEntity;
    public long timestamp;
    public Object content;
    public EventSubGroup subGroup;

    public Map<String, Object> toHashMap() {
        final String classname = subGroup.getClass().getName();
        final String[] a = extractGroupAndSubgroup(classname);
        Map<String, Object> map = new HashMap<>();
        map.put("srcEntity", srcEntity);
        map.put("trgEntity", trgEntity);
        map.put("timestamp", timestamp);
        map.put("content", content);
        map.put("group", a[0]);
        map.put("subGroup", a[1]);
        return map;
    }

    private String[] extractGroupAndSubgroup(String input) {
        // Define the regex pattern
        Pattern pattern = Pattern.compile(".*\\.(.*?)\\$(.*)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String group = matcher.group(1); // Extract the word before '$'
            String sub = matcher.group(2); // Extract the word after '$'
            return new String[] { group, sub };
        }

        return new String[] { "unknown group", "unknown subgroup" };
    }
}
