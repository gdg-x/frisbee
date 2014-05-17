package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.gdg.frisbee.android.Const;

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
