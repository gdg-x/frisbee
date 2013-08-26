package org.gdg.frisbee.android.api.model;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 23.08.13
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public class GcmRegistrationResponse extends MessageResponse {
    private String notificationKey;

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }
}
