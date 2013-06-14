package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import org.gdg.frisbee.android.R;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 14.06.13
 * Time: 02:52
 */
public class FirstStartStep2Fragment extends RoboSherlockFragment {

    public static FirstStartStep2Fragment newInstance() {
        FirstStartStep2Fragment fragment = new FirstStartStep2Fragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_step2, null);
    }
}
