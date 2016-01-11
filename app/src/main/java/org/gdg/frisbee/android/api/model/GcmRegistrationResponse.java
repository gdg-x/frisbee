package org.gdg.frisbee.android.api.model;

public class GcmRegistrationResponse extends MessageResponse {
    private String notificationKey;

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }
}
