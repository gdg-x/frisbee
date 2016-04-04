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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
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
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.OrganizerChecker;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.utils.CryptoUtils;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.nio.charset.Charset;

import butterknife.Bind;
import timber.log.Timber;

public class ArrowActivity extends GdgNavDrawerActivity {

    public static final String ID_SEPARATOR_FOR_SPLIT = "\\|";
    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final String ID_SPLIT_CHAR = "|";
    private static final int REQUEST_LEADERBOARD = 1;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    @Bind(R.id.viewFlipper)
    ViewFlipper viewFlipper;
    @Bind(R.id.switchToSend)
    Button switchToSend;
    @Bind(R.id.switchToTag)
    Button switchToReceive;
    @Bind(R.id.organizerOnly)
    LinearLayout organizerOnly;
    @Bind(R.id.imageView)
    ImageView scanImageView;
    @Bind(R.id.organizerPic)
    ImageView organizerPic;
    private String taggedPeopleIds;
    private BaseArrowHandler mArrowHandler;
    private NfcAdapter mNfcAdapter;
    private String mPendingScore;
    private Handler mHandler = new Handler();
    private Runnable updateQrCode = new Runnable() {
        @Override
        public void run() {
            try {
                String encryptedMessage = getEncryptedMessage();
                if (TextUtils.isEmpty(encryptedMessage)) {
                    return;
                }
                String message = Const.QR_MSG_PREFIX + encryptedMessage;
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
                Timber.e(e, "Error while trying to update QR code");
            }
        }
    };

    private static long getNow() {
        return DateTime.now(DateTimeZone.UTC).getMillis();
    }

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

        switchToReceive.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewFlipper.setDisplayedChild(0);
                }
            }
        );
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
                if (getGoogleApiClient().isConnected()) {
                    startActivityForResult(
                        Games.Leaderboards.getLeaderboardIntent(getGoogleApiClient(), Const.ARROW_LB),
                        REQUEST_LEADERBOARD
                    );
                }
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

        if (!PrefUtils.isSignedIn(this)) {
            finish();
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
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
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == IntentIntegrator.REQUEST_CODE && responseCode == RESULT_OK) {
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
            Timber.e(e, "Error while trying to tag person");
        }
    }

    private void score(final String id) {

        if (id.equals(PlusUtils.getCurrentPersonId(getGoogleApiClient()))) {
            Toast.makeText(this, R.string.arrow_selfie, Toast.LENGTH_LONG).show();
            return;
        }

        new StoreSnapshotTask(getGoogleApiClient()).execute(id);
    }

    private void showNoNfc() {
        Toast.makeText(this, R.string.no_nfc_use_qr_scanner, Toast.LENGTH_LONG).show();
    }

    @Nullable
    private String getEncryptedMessage() throws Exception {
        if (getGoogleApiClient().isConnected()) {
            return CryptoUtils.encrypt(Const.ARROW_K,
                PlusUtils.getCurrentPersonId(getGoogleApiClient()) + ID_SPLIT_CHAR + getNow());
        } else {
            return null;
        }
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

    public static class AlreadyTaggedException extends Exception {
    }

    public class StoreSnapshotTask extends AsyncTask<String, Void, String> {

        final GoogleApiClient googleApiClient;

        public StoreSnapshotTask(GoogleApiClient googleApiClient) {
            this.googleApiClient = googleApiClient;
        }

        @Override
        protected String doInBackground(String... params) {
            String id = params[0];

            try {
                Snapshot snapshot = openSnapshot();
                if (snapshot != null) {
                    storeInSnapshot(snapshot, id);
                    String personName = getTaggedPersonName(id);
                    return "It worked...you tagged " + personName;
                }
            } catch (IOException e) {
                Timber.w(e, "Could not store tagged organizer");
            } catch (AlreadyTaggedException e) {
                return getString(R.string.arrow_already_tagged);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            if (message != null) {
                Toast.makeText(ArrowActivity.this, message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ArrowActivity.this, R.string.arrow_oops, Toast.LENGTH_LONG).show();
            }
        }

        @WorkerThread
        private Snapshot openSnapshot() {
            Snapshots.OpenSnapshotResult result = Games.Snapshots.open(
                googleApiClient,
                Const.GAMES_SNAPSHOT_ID,
                true,
                Snapshots.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED
            ).await();

            final int statusCode = result.getStatus().getStatusCode();
            if (statusCode == GamesStatusCodes.STATUS_OK) {
                return result.getSnapshot();
            }

            Timber.e("Error in Snapshot opening.\n"
                + "Status: %s\n"
                + "Status Code: %d", result.getStatus().getStatusMessage(), statusCode);
            return null;
        }

        @WorkerThread
        private void storeInSnapshot(Snapshot snapshot, final String id) throws IOException, AlreadyTaggedException {
            final String previous = new String(snapshot.getSnapshotContents().readFully());

            if (previous.contains(id)) {
                throw new AlreadyTaggedException();
            } else {
                String merged = previous + ID_SPLIT_CHAR + id;
                saveMergedSnapshot(snapshot, merged);
            }
        }

        @WorkerThread
        private void saveMergedSnapshot(Snapshot snapshot, String merged) {
            int numberOfTaggedOrganizers = merged.split(ID_SEPARATOR_FOR_SPLIT).length - 1;

            if (snapshot != null) {
                snapshot.getSnapshotContents().writeBytes(merged.getBytes());
                SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                    .setDescription(getString(R.string.arrow_tagged) + ": " + numberOfTaggedOrganizers)
                    .build();
                Games.Snapshots.commitAndClose(getGoogleApiClient(), snapshot, metadataChange).await();
            }

            Games.Leaderboards.submitScore(getGoogleApiClient(), Const.ARROW_LB, numberOfTaggedOrganizers);
            getAchievementActionHandler().handleFeelingSocial(numberOfTaggedOrganizers);
        }

        @WorkerThread
        private String getTaggedPersonName(String id) {
            People.LoadPeopleResult peopleResult = Plus.PeopleApi.load(getGoogleApiClient(), id).await();

            if (peopleResult.getStatus().isSuccess()) {
                Person organizer = peopleResult.getPersonBuffer().get(0);
                return organizer.getDisplayName();
            }
            return null;
        }
    }

    private class BaseArrowHandler {
        public void enablePush() {
        }

        public void disablePush() {
        }
    }

    private class NfcArrowHandler extends BaseArrowHandler
        implements NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {

        public void enablePush() {
            if (isContextValid()) {
                mNfcAdapter.setNdefPushMessageCallback(this, ArrowActivity.this);
                mNfcAdapter.setOnNdefPushCompleteCallback(this, ArrowActivity.this);
            }
        }

        public void disablePush() {
            if (isContextValid()) {
                mNfcAdapter.setNdefPushMessage(null, ArrowActivity.this);
            }
        }

        @Nullable
        @Override
        public NdefMessage createNdefMessage(NfcEvent nfcEvent) {

            try {
                String msg = getEncryptedMessage();
                if (msg == null) {
                    return null;
                }

                NdefRecord mimeRecord = new NdefRecord(
                    NdefRecord.TNF_MIME_MEDIA,
                    Const.ARROW_MIME.getBytes(CHARSET),
                    new byte[0],
                    msg.getBytes(CHARSET));
                return new NdefMessage(new NdefRecord[]{mimeRecord});
            } catch (Exception e) {
                Timber.e(e, "Error while trying to create NFC message");
                Toast.makeText(ArrowActivity.this, R.string.arrow_oops, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        public void onNdefPushComplete(NfcEvent nfcEvent) {
        }
    }
}
