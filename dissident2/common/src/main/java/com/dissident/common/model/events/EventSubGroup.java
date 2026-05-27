package com.dissident.common.model.events;

public interface EventSubGroup {

    default EventGroup getGroup(){
        // every instance of EventSubGroup must also be a EventGroup
        return ((EventGroup) this);
    }
}
