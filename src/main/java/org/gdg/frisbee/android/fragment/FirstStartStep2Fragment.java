package org.gdg.frisbee.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusClient;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import roboguice.inject.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 14.06.13
 * Time: 02:52
 */
public class FirstStartStep2Fragment extends RoboSherlockFragment implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private static String LOG_TAG = "GDG-FirstStartStep2Fragment";
    public static final int REQUEST_CODE_RESOLVE_ERR = 7;

    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private Step2Listener mListener;

    @InjectView(R.id.googleSignin)
    Button mSignInButton;

    @InjectView(R.id.skipSignin)
    Button mSkipSignin;

    public static FirstStartStep2Fragment newInstance(Step2Listener listener) {
        FirstStartStep2Fragment fragment = new FirstStartStep2Fragment();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")

        mPlusClient = new PlusClient.Builder(getActivity(), this, this)
                .setScopes("https://www.googleapis.com/auth/youtube", Scopes.PLUS_LOGIN, Scopes.PLUS_PROFILE)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        App.getInstance().getTracker().sendView("First Start Wizard - Step 2");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_step2, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSignInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(mPlusClient != null && !mPlusClient.isConnected()) {
                    if (mConnectionResult == null) {
                        mPlusClient.connect();
                    } else {
                        try {
                            mConnectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLVE_ERR);
                        } catch (IntentSender.SendIntentException e) {
                            // Try connecting again.
                            mConnectionResult = null;
                            mPlusClient.connect();
                        }
                    }
                }
            }
        });

        mSkipSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null)
                    mListener.onSkippedSignIn();
            }
        });
    }

    @Override
    public void onConnected() {
        Log.d(LOG_TAG, "onConnected()");
        final String accountName = mPlusClient.getAccountName();

        if(mListener != null)
            mListener.onSignedIn(accountName);
    }

    @Override
    public void onDisconnected() {
        Log.d(LOG_TAG, "onDisconnected()");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == Activity.RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
        }
    }

    public void setPlusClient(PlusClient plusClient) {
        mPlusClient = plusClient;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed()");
        Log.d(LOG_TAG, "Connection failed: "+ connectionResult.getErrorCode());

        if (connectionResult.hasResolution()) {
            try {
                Log.v(LOG_TAG, "resolve");
                connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                Log.e(LOG_TAG, "send intent",e);
                mConnectionResult = null;
                mPlusClient.connect();
            }
        } else {
            Log.d(LOG_TAG, "no resolution!?");
        }
        // Save the result and resolve the connection failure upon a user click.
        mConnectionResult = connectionResult;
    }

    public Step2Listener getListener() {
        return mListener;
    }

    public void setListener(Step2Listener mListener) {
        this.mListener = mListener;
    }

    public interface Step2Listener {
        void onSignedIn(String accountName);
        void onSkippedSignIn();
    }
}
