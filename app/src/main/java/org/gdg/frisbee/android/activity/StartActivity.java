package org.gdg.frisbee.android.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.widget.Toast;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 02.07.13
 * Time: 00:03
 * To change this template use File | Settings | File Templates.
 */
public class StartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG && TextUtils.isEmpty(getString(R.string.play_app_id))){
            Toast.makeText(this, "no API keys defined!", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);

        Intent intentForStart = null;
        if(mPreferences.getBoolean(Const.SETTINGS_FIRST_START, true))
            intentForStart = new Intent(StartActivity.this, FirstStartActivity.class);
        else
            intentForStart = new Intent(StartActivity.this, MainActivity.class);

        startActivity(intentForStart);
        finish();
    }
}
