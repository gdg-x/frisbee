/*
 * Copyright 2014 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.appstate.AppStateStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.Plus;

import java.nio.charset.Charset;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GdgX;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.utils.CryptoUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import butterknife.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 02.04.14
 * Time: 20:44
 * To change this template use File | Settings | File Templates.
 */
public class ArrowActivity extends GdgNavDrawerActivity implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {

    private static String LOG_TAG = "GDG-Arrow";
    private boolean isOrganizer = false;

    private NfcAdapter mNfcAdapter;

    private SharedPreferences arrowPreferences;

    @InjectView(R.id.viewFlipper)
    ViewFlipper viewFlipper;

    @InjectView(R.id.switchToSend)
    Button switchToSend;

    @InjectView(R.id.switchToTag)
    Button switchToReceive;

    @InjectView(R.id.organizerOnly)
    LinearLayout organizerOnly;

    @Override
    protected String getTrackedViewName() {
        return "Arrow";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_arrow);

        if (!mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false))
            finish();


        arrowPreferences = getSharedPreferences("arrow", MODE_PRIVATE);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) showNoNfc();
        switchToReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.setDisplayedChild(0);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];

                    NdefRecord[] records = msgs[i].getRecords();
                    for(int j = 0; j < records.length; j++) {
                        NdefRecord record = records[j];
                        if(record.getTnf() == NdefRecord.TNF_MIME_MEDIA && record.getType().equals(Const.ARROW_MIME.getBytes(Charset.forName("US-ASCII")))) {
                            taggedPerson(new String(record.getPayload()));
                        }
                    }

                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);    //To change body of overridden methods use File | Settings | File Templates.
        handleIntent(intent);
    }

    private void taggedPerson(String msg) {

        try {
            String decrypted = CryptoUtils.decrypt(getString(R.string.arrow_k), msg);

            String[] parts = decrypted.split("|");

            if(parts.length == 2) {

                long dt = Long.parseLong(parts[1]);

                if(dt == new DateMidnight(DateTimeZone.UTC).getMillis()) {
                    score(parts[0]);
                } else {
                    Toast.makeText(this, getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private String previous;
    private int score;

    private void score(final String id) {

        if(id.equals(Plus.PeopleApi.getCurrentPerson(getGoogleApiClient()).getId())) {
            Toast.makeText(this, R.string.arrow_selfie, Toast.LENGTH_LONG).show();
            return;
        }

        AppStateManager.load(getGoogleApiClient(), Const.ARROW_DONE_STATE_KEY).setResultCallback(new ResultCallback<AppStateManager.StateResult>() {
            @Override
            public void onResult(AppStateManager.StateResult stateResult) {
                AppStateManager.StateConflictResult conflictResult
                        = stateResult.getConflictResult();
                AppStateManager.StateLoadedResult loadedResult
                        = stateResult.getLoadedResult();

                if (loadedResult != null) {
                    if(loadedResult.getStatus().getStatusCode() == AppStateStatusCodes.STATUS_OK || loadedResult.getStatus().getStatusCode() == AppStateStatusCodes.STATUS_STATE_KEY_NOT_FOUND) {
                        previous = "";

                        if (loadedResult.getStatus().getStatusCode() == AppStateStatusCodes.STATUS_OK) {
                            previous = new String(loadedResult.getLocalData());

                            if (previous.contains(id)) {
                                Toast.makeText(ArrowActivity.this, R.string.arrow_already_tagged, Toast.LENGTH_LONG);
                            } else {
                                previous = previous + "|" + id;
                                Toast.makeText(ArrowActivity.this, "It worked...you tagged " + id, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else if (conflictResult != null) {
                    Toast.makeText(ArrowActivity.this, getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
                }
            }
        });

                            /*
                            appState.loadState(new OnStateLoadedListener() {
                                @Override
                                public void onStateLoaded(int statusCode, int stateKey, byte[] localData) {

                                    if(statusCode == AppStateClient.STATUS_OK || statusCode == AppStateClient.STATUS_STATE_KEY_NOT_FOUND) {

                                        int previousScore = 0;
                                        if(statusCode == AppStateClient.STATUS_OK) {
                                            previousScore = ByteBuffer.wrap(localData).getInt();
                                        }

                                        score = previousScore + 1;
                                        appState.updateStateImmediate(new OnStateLoadedListener() {
                                            @Override
                                            public void onStateLoaded(int i, int i2, byte[] bytes) {
                                                appState.updateStateImmediate(new OnStateLoadedListener() {
                                                    @Override
                                                    public void onStateLoaded(int i, int i2, byte[] localData) {
                                                        int score = ByteBuffer.wrap(localData).getInt();
                                                        gamesClient.submitScore(Const.ARROW_LB, score);
                                                        Log.i(LOG_TAG, "Submitted new Score");
                                                    }

                                                    @Override
                                                    public void onStateConflict(int i, String s, byte[] bytes, byte[] bytes2) {
                                                        //To change body of implemented methods use File | Settings | File Templates.
                                                        Toast.makeText(ArrowActivity.this, getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
                                                    }
                                                }, Const.ARROW_STATE_KEY, ByteBuffer.allocate(4).putInt(score).array());
                                            }

                                            @Override
                                            public void onStateConflict(int i, String s, byte[] bytes, byte[] bytes2) {
                                                Toast.makeText(ArrowActivity.this, getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
                                            }
                                        }, Const.ARROW_DONE_STATE_KEY, previous.getBytes());
                                    }
                                }

                                @Override
                                public void onStateConflict(int i, String s, byte[] bytes, byte[] bytes2) {
                                    //To change body of implemented methods use File | Settings | File Templates.
                                    Toast.makeText(ArrowActivity.this, getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
                                }
                            }, Const.ARROW_STATE_KEY);     */

    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {

    }

    public void showNoNfc() {
        Toast.makeText(this, "No NFC Adapter detected. Sorry :(", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {

        try {
            String msg = CryptoUtils.encrypt(getString(R.string.arrow_k), Plus.PeopleApi.getCurrentPerson(getGoogleApiClient()).getId()+"|"+DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().getMillis());
            NdefRecord mimeRecord = new NdefRecord(
                    NdefRecord.TNF_MIME_MEDIA ,
                    Const.ARROW_MIME.getBytes(Charset.forName("US-ASCII")),
                    new byte[0],msg.getBytes(Charset.forName("US-ASCII")));
            NdefMessage message = new NdefMessage(mimeRecord);
            return message;
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            Toast.makeText(this, getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        switchToSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOrganizer) {
                    viewFlipper.setDisplayedChild(1);
                    mNfcAdapter.setNdefPushMessageCallback(ArrowActivity.this, ArrowActivity.this);
                    mNfcAdapter.setOnNdefPushCompleteCallback(ArrowActivity.this, ArrowActivity.this);
                }
            }
        });

        GdgX xClient = new GdgX();
        xClient.checkOrganizer(Plus.PeopleApi.getCurrentPerson(getGoogleApiClient()).getId(), new Response.Listener<OrganizerCheckResponse>() {
                    @Override
                    public void onResponse(OrganizerCheckResponse organizerCheckResponse) {
                        if(organizerCheckResponse.getChapters().size() > 0) {
                            isOrganizer = true;
                            organizerOnly.setVisibility(View.VISIBLE);
                        } else {
                            isOrganizer = false;
                            organizerOnly.setVisibility(View.INVISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        isOrganizer = false;
                        organizerOnly.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
