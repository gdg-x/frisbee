package org.gdg.frisbee.android.checkin;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;

public class CheckinSender {
    private final GdgNavDrawerActivity mActivity;

    public CheckinSender(GdgNavDrawerActivity activity) {
        mActivity = activity;
    }

    public void broadcast(ResultCallback<Status> errorCheckingCallback) {
        String displayName = getUserName();
        Message message = new Message(displayName.getBytes());
        Nearby.Messages.publish(mActivity.getGoogleApiClient(), message)
                .setResultCallback(errorCheckingCallback);

    }

    private String getUserName() {
        Person user = Plus.PeopleApi.getCurrentPerson(mActivity.getGoogleApiClient());
        if (user == null) {
            return mActivity.getString(R.string.annoymous);
        }

        return user.getDisplayName();
    }
}
