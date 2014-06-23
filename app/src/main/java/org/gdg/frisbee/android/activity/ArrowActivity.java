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

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.appstate.AppStateStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.OrganizerChecker;
import org.gdg.frisbee.android.utils.CryptoUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import butterknife.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 02.04.14
 * Time: 20:44
 */
public class ArrowActivity extends GdgNavDrawerActivity {

    public static final String ID_SEPARATOR_FOR_SPLIT = "\\|";
    public static final String ID_SPLIT_CHAR = "|";
    private static final int REQUEST_LEADERBOARD = 1;

    private static String LOG_TAG = "GDG-Arrow";
    private String previous;

    private BaseArrowHandler mArrowHandler;
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

    @InjectView(R.id.imageView)
    ImageView scanImageView;

    @InjectView(R.id.organizerPic)
    ImageView organizerPic;

    private String mPendingScore;

    private Handler mHandler = new Handler();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

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
        if (mNfcAdapter == null) {
            showNoNfc();
            mArrowHandler = new BaseArrowHandler();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mArrowHandler = new NfcArrowHandler();
            } else {
                mArrowHandler = new BaseArrowHandler();
            }
        }

        scanImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(ArrowActivity.this);
                integrator.initiateScan();
            }
        });

        switchToReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.setDisplayedChild(0);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.arrow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.arrow_lb:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getGoogleApiClient(), Const.ARROW_LB), REQUEST_LEADERBOARD);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
                        byte[] typeArray = record.getType();
                        String mimeType = new String(typeArray);
                        if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA && mimeType.equals(Const.ARROW_MIME)) {
                            taggedPerson(new String(record.getPayload()));
                        }
                    }

                }
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())){
            if (intent.getData() != null && intent.getDataString().length() > Const.QR_MSG_PREFIX.length()){
                String msg = intent.getDataString().substring(Const.QR_MSG_PREFIX.length());
                taggedPerson(msg);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch ( requestCode ) {
            case IntentIntegrator.REQUEST_CODE:
                if (responseCode == RESULT_CANCELED){
                    return;
                }

                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, responseCode, intent);
                if (result.getContents().startsWith(Const.QR_MSG_PREFIX)) {
                    taggedPerson(result.getContents().substring(Const.QR_MSG_PREFIX.length()));
                }
        }
    }

    private void taggedPerson(String msg) {

        try {
            String decrypted = CryptoUtils.decrypt(Const.ARROW_K, msg);

            String[] parts = decrypted.split(ID_SEPARATOR_FOR_SPLIT);

            if(parts.length == 2) {

                long dt = Long.parseLong(parts[1]);

                if((getNow() - dt) < 60000) {
                    if (getGoogleApiClient().isConnected()) {
                        score(parts[0]);
                    } else {
                        mPendingScore = parts[0];
                        getGoogleApiClient().connect();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.arrow_stale), Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

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
                                Toast.makeText(ArrowActivity.this, R.string.arrow_already_tagged, Toast.LENGTH_LONG).show();
                            } else {
                                addTaggedPersonToCloudSave(id);
                            }
                        } else {
                            addTaggedPersonToCloudSave(id);
                        }
                    } else {

                    }
                } else if (conflictResult != null) {
                    previous = mergeIds(new String(conflictResult.getLocalData()), new String(conflictResult.getServerData()));

                    if (previous.contains(id)) {
                        Toast.makeText(ArrowActivity.this, R.string.arrow_already_tagged, Toast.LENGTH_LONG).show();
                    } else {
                        addTaggedPersonToCloudSave(id);
                    }

                    Toast.makeText(ArrowActivity.this, getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String mergeIds(String list1, String list2) {
        String[] parts1 = list1.split(ID_SEPARATOR_FOR_SPLIT);
        String[] parts2 = list2.split(ID_SEPARATOR_FOR_SPLIT);
        Set<String> mergedSet = new HashSet<String>(Arrays.asList(parts1));
        mergedSet.addAll(Arrays.asList(parts2));
        return TextUtils.join(ID_SPLIT_CHAR, mergedSet);
    }

    private void addTaggedPersonToCloudSave(String id) {
        previous = previous + ID_SPLIT_CHAR + id;
        AppStateManager.update(getGoogleApiClient(), Const.ARROW_DONE_STATE_KEY, previous.getBytes());
        Games.Leaderboards.submitScore(getGoogleApiClient(), Const.ARROW_LB, previous.split("\\|").length-1);

        Plus.PeopleApi.load(getGoogleApiClient(), id).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
            @Override
            public void onResult(People.LoadPeopleResult loadPeopleResult) {
                Person organizer = loadPeopleResult.getPersonBuffer().get(0);
                Toast.makeText(ArrowActivity.this, "It worked...you tagged " + organizer.getDisplayName(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showNoNfc() {
        Toast.makeText(this, R.string.no_nfc_use_qr_scanner, Toast.LENGTH_LONG).show();
    }


    private String getEncryptedMessage() throws Exception {
        return CryptoUtils.encrypt(Const.ARROW_K, Plus.PeopleApi.getCurrentPerson(getGoogleApiClient()).getId() +
                ID_SPLIT_CHAR + getNow());
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        switchToSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOrganizer()) {
                    viewFlipper.setDisplayedChild(1);
                    mArrowHandler.enablePush();
                }
            }
        });
        mArrowHandler.disablePush();

        checkOrganizer(new OrganizerChecker.OrganizerResponseHandler(){
            @Override
            public void onOrganizerResponse(boolean isOrganizer) {
                if (isOrganizer) {
                    organizerOnly.setVisibility(View.VISIBLE);
                    setQrCode();
                } else {
                    organizerOnly.setVisibility(View.GONE);
                }
            }

            @Override
            public void onErrorResponse() {
                organizerOnly.setVisibility(View.GONE);
            }
        });

        if (mPendingScore != null){
            score(mPendingScore);
            mPendingScore = null;
        }
    }

    private Runnable updateQrCode = new Runnable() {
        @Override
        public void run() {
            try {
                String message = Const.QR_MSG_PREFIX + getEncryptedMessage();
                MultiFormatWriter mQrCodeWriter = new MultiFormatWriter();
                int qrCodeSize = getResources().getInteger(R.integer.qr_code_size);
                BitMatrix bitMatrix = mQrCodeWriter.encode(message, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++) {
                    int offset = y * width;
                    for (int x = 0; x < width; x++) {
                        pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
                    }
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height,
                        Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                organizerPic.setImageBitmap(bitmap);

                mHandler.postDelayed(updateQrCode, 60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void setQrCode() {
        mHandler.post(updateQrCode);

    }

    public long getNow() {
        return DateTime.now(DateTimeZone.UTC).getMillis();
    }

    private class BaseArrowHandler {
        public void enablePush() {}

        public void disablePush() {
        }
    }

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private class NfcArrowHandler extends BaseArrowHandler implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback{
        public void enablePush() {
            mNfcAdapter.setNdefPushMessageCallback(this, ArrowActivity.this);
            mNfcAdapter.setOnNdefPushCompleteCallback(this, ArrowActivity.this);
        }

        public void disablePush() {
            mNfcAdapter.setNdefPushMessage(null, ArrowActivity.this);
        }

        @Override
        public NdefMessage createNdefMessage(NfcEvent nfcEvent) {

            try {
                String msg = getEncryptedMessage();
                NdefRecord mimeRecord = new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA,
                        Const.ARROW_MIME.getBytes(Charset.forName("US-ASCII")),
                        new byte[0],
                        msg.getBytes(Charset.forName("US-ASCII")));
                NdefMessage message = new NdefMessage(new NdefRecord[]{mimeRecord});
                return message;
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                Toast.makeText(ArrowActivity.this, ArrowActivity.this.getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        public void onNdefPushComplete(NfcEvent nfcEvent) {

        }
    }
}
