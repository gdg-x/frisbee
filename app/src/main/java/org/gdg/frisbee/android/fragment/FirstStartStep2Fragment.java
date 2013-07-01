package org.gdg.frisbee.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusClient;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.FirstStartActivity;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import roboguice.inject.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 14.06.13
 * Time: 02:52
 */
public class FirstStartStep2Fragment extends RoboSherlockFragment {

    private static String LOG_TAG = "GDG-FirstStartStep2Fragment";

    @InjectView(R.id.googleSignin)
    Button mSignInButton;

    @InjectView(R.id.skipSignin)
    Button mSkipSignin;

    public static FirstStartStep2Fragment newInstance() {
        FirstStartStep2Fragment fragment = new FirstStartStep2Fragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
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
                if(getActivity() instanceof Step2Listener) {
                    FirstStartActivity activity = (FirstStartActivity)getActivity();
                    if(!activity.getPlayServicesHelper().isSignedIn()) {
                        activity.getPlayServicesHelper().beginUserInitiatedSignIn();
                    }
                }
            }
        });

        mSkipSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() instanceof Step2Listener)
                    ((Step2Listener)getActivity()).onSkippedSignIn();
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    public interface Step2Listener {
        void onSignedIn(String accountName);
        void onSkippedSignIn();
    }
}
