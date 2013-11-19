package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import butterknife.InjectView;
import butterknife.Views;
import com.actionbarsherlock.app.SherlockFragment;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.FirstStartActivity;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 14.06.13
 * Time: 02:52
 */
public class FirstStartStep2Fragment extends SherlockFragment {

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
        View v = inflater.inflate(R.layout.fragment_welcome_step2, null);
        Views.inject(this, v);
        return v;
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
                    } else {
                        activity.onSignInSucceeded();
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
