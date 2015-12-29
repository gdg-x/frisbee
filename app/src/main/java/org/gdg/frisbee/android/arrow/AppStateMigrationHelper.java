package org.gdg.frisbee.android.arrow;

import android.content.Context;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.utils.PrefUtils;

/**
 * TODO Will be removed eventually once we migrate fully to upgrade to GMS 8+
 */
public class AppStateMigrationHelper {

    private AppStateMigrationHelper() {

    }

    public static void checkSnapshotUpgrade(final Context context, final GoogleApiClient googleApiClient, final String description) {
        if (PrefUtils.isAppStateMigrationSuccessful(context)) {
            return;
        }

        migrateFromAppStateToSnapshot(context, googleApiClient, description);
    }

    private static void migrateFromAppStateToSnapshot(final Context context, final GoogleApiClient googleApiClient, final String description) {
        AppStateManager.load(googleApiClient, Const.ARROW_DONE_STATE_KEY).setResultCallback(
                new ResultCallback<AppStateManager.StateResult>() {
                    @Override
                    public void onResult(AppStateManager.StateResult stateResult) {
                        if (stateResult.getStatus().isSuccess()) {
                            final String serializedOrganizers = new String(stateResult.getLoadedResult().getLocalData());
                            Games.Snapshots.open(googleApiClient, Const.GAMES_SNAPSHOT_ID, true).setResultCallback(
                                    new ResultCallback<Snapshots.OpenSnapshotResult>() {
                                        @Override
                                        public void onResult(Snapshots.OpenSnapshotResult stateResult) {
                                            saveSnapshot(stateResult, serializedOrganizers, description, googleApiClient);
                                            PrefUtils.setAppStateMigrationSuccessful(context);
                                        }
                                    });
                        }
                    }
                });
    }

    private static void saveSnapshot(Snapshots.OpenSnapshotResult stateResult,
                                     String serializedOrganizers,
                                     String description,
                                     GoogleApiClient googleApiClient) {

        final Snapshot loadedResult = stateResult.getSnapshot();
        final int statusCode = stateResult.getStatus().getStatusCode();
        if (statusCode == GamesStatusCodes.STATUS_OK) {
            loadedResult.getSnapshotContents().writeBytes(serializedOrganizers.getBytes());
            int numberOfTaggedOrganizers = serializedOrganizers.split(ArrowActivity.ID_SEPARATOR_FOR_SPLIT).length - 1;
            SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                    .setDescription(description + ": " + numberOfTaggedOrganizers)
                    .build();
            Games.Snapshots.commitAndClose(googleApiClient, loadedResult, metadataChange);
        }
    }
}
