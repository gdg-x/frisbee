/*
 * Copyright 2014-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.arrow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

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

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.OrganizerChecker;
import org.gdg.frisbee.android.utils.CryptoUtils;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import butterknife.InjectView;

public class ArrowActivity extends GdgNavDrawerActivity {

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    public static final String ID_SEPARATOR_FOR_SPLIT = "\\|";
    public static final String ID_SPLIT_CHAR = "|";
    private static final int REQUEST_LEADERBOARD = 1;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
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
    private String taggedPeopleIds;
    private BaseArrowHandler mArrowHandler;
    private NfcAdapter mNfcAdapter;
    private String mPendingScore;
    private Handler mHandler = new Handler();

    @Override
    protected String getTrackedViewName() {
        return "Arrow";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow);

        if (!PrefUtils.isSignedIn(this)) {
            finish();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            showNoNfc();
            mArrowHandler = new BaseArrowHandler();
        } else {
            mArrowHandler = new NfcArrowHandler();
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
        switch (item.getItemId()) {
            case R.id.arrow_lb:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getGoogleApiClient(), Const.ARROW_LB), REQUEST_LEADERBOARD);
                return true;
            case R.id.arrow_tagged:
                startActivity(new Intent(this, ArrowTaggedActivity.class));
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
                    for (NdefRecord record : records) {
                        byte[] typeArray = record.getType();
                        String mimeType = new String(typeArray);
                        if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA && mimeType.equals(Const.ARROW_MIME)) {
                            taggedPerson(new String(record.getPayload()));
                        }
                    }

                }
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            if (intent.getData() != null && intent.getDataString().length() > Const.QR_MSG_PREFIX.length()) {
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
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                if (responseCode == RESULT_CANCELED) {
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

            if (parts.length == 2) {

                long dt = Long.parseLong(parts[1]);

                if ((getNow() - dt) < 60000) {
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

        if (id.equals(Plus.PeopleApi.getCurrentPerson(getGoogleApiClient()).getId())) {
            Toast.makeText(this, R.string.arrow_selfie, Toast.LENGTH_LONG).show();
            return;
        }

        AppStateManager.load(getGoogleApiClient(), Const.ARROW_DONE_STATE_KEY).setResultCallback(new ResultCallback<AppStateManager.StateResult>() {
            @Override
            public void onResult(AppStateManager.StateResult stateResult) {
                AppStateManager.StateConflictResult conflictResult = stateResult.getConflictResult();
                AppStateManager.StateLoadedResult loadedResult = stateResult.getLoadedResult();

                if (loadedResult != null) {
                    final int statusCode = loadedResult.getStatus().getStatusCode();
                    if (statusCode == AppStateStatusCodes.STATUS_OK
                            || statusCode == AppStateStatusCodes.STATUS_STATE_KEY_NOT_FOUND) {
                        taggedPeopleIds = "";

                        if (statusCode == AppStateStatusCodes.STATUS_OK) {
                            taggedPeopleIds = new String(loadedResult.getLocalData());

                            if (taggedPeopleIds.contains(id)) {
                                Toast.makeText(ArrowActivity.this, R.string.arrow_already_tagged, Toast.LENGTH_LONG).show();
                            } else {
                                addTaggedPersonToCloudSave(id);
                            }
                        } else {
                            addTaggedPersonToCloudSave(id);
                        }
                    }
                } else if (conflictResult != null) {
                    taggedPeopleIds = mergeIds(new String(conflictResult.getLocalData()), new String(conflictResult.getServerData()));

                    if (taggedPeopleIds.contains(id)) {
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
        Set<String> mergedSet = new HashSet<>(Arrays.asList(parts1));
        mergedSet.addAll(Arrays.asList(parts2));
        return TextUtils.join(ID_SPLIT_CHAR, mergedSet);
    }

    private void addTaggedPersonToCloudSave(String id) {

        taggedPeopleIds = taggedPeopleIds + ID_SPLIT_CHAR + id;
        AppStateManager.update(getGoogleApiClient(), Const.ARROW_DONE_STATE_KEY, taggedPeopleIds.getBytes());
        int score = taggedPeopleIds.split("\\|").length - 1;
        Games.Leaderboards.submitScore(getGoogleApiClient(), Const.ARROW_LB, score);
        getAchievementActionHandler().handleFeelingSocial(score);

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
        return CryptoUtils.encrypt(Const.ARROW_K, 
                Plus.PeopleApi.getCurrentPerson(getGoogleApiClient()).getId()
                + ID_SPLIT_CHAR
                + getNow());
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        switchToSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.getInstance().isOrganizer()) {
                    viewFlipper.setDisplayedChild(1);
                    mArrowHandler.enablePush();
                }
            }
        });
        mArrowHandler.disablePush();

        App.getInstance().checkOrganizer(getGoogleApiClient(),
                new OrganizerChecker.Callbacks() {
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

        if (mPendingScore != null) {
            score(mPendingScore);
            mPendingScore = null;
        }
    }

    private void setQrCode() {
        mHandler.post(updateQrCode);

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

    public long getNow() {
        return DateTime.now(DateTimeZone.UTC).getMillis();
    }

    private class BaseArrowHandler {
        public void enablePush() {
        }

        public void disablePush() {
        }
    }

    private class NfcArrowHandler extends BaseArrowHandler implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {
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
                        Const.ARROW_MIME.getBytes(CHARSET),
                        new byte[0],
                        msg.getBytes(CHARSET));
                return new NdefMessage(new NdefRecord[]{mimeRecord});
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ArrowActivity.this, ArrowActivity.this.getString(R.string.arrow_oops), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        public void onNdefPushComplete(NfcEvent nfcEvent) {

        }
    }


}
