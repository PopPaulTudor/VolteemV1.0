package com.volunteer.thc.volunteerapp.model.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vlad on 19.02.2018.
 */
public enum EventType {

    SPORTS("Sports"),
    MUSIC("Music"),
    FESTIVAL("Festival"),
    CHARITY("Charity"),
    TRAINING("Training"),
    OTHER("Other");

    /* The value that is displayed. */
    private String displayValue;

    EventType(String displayValue) {
        this.displayValue = displayValue;
    }

    public static EventType lookupFromPrefsValue(String displayValueStr) {
        EventType eventType = null;
        for (EventType type : EventType.values()) {
            if (type.getDisplayValue().equals(displayValueStr)) {
                eventType = type;
                break;
            }
        }
        return eventType;
    }

    public static List<String> getAllAsList() {
        List<String> result = new ArrayList<>();
        for (EventType eventType : EventType.values()) {
            result.add(eventType.getDisplayValue());
        }
        return result;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
