package com.volunteer.thc.volunteerapp.model.type;

/**
 * Created by Vlad on 11.02.2018.
 */
public enum UserType {

    VOLUNTEER("Volunteer"),
    ORGANISER("Organiser");

    /* The value that is saved in SharedPrefs. */
    private String prefsValue;

    UserType(String prefsValue) {
        this.prefsValue = prefsValue;
    }

    public static UserType lookupFromPrefsValue(String userTypeStr) {
        UserType userType = null;
        for (UserType type : UserType.values()) {
            if (type.getPrefsValue().equals(userTypeStr)) {
                userType = type;
                break;
            }
        }
        return userType;
    }

    public String getPrefsValue() {
        return prefsValue;
    }
}
