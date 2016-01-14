package org.gdg.frisbee.android.arrow;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;

/**
 * TODO Will be removed eventually once we migrate fully to upgrade to GMS 8+
 */
public class AppStateMigrationHelper {

    private AppStateMigrationHelper() {

    }

    public static void checkSnapshotUpgrade(final GdgActivity activity,
                                            final GoogleApiClient googleApiClient,
                                            final String description) {
        if (PrefUtils.isAppStateMigrationSuccessful(activity)) {
            return;
        }

        migrateFromAppStateToSnapshot(activity, googleApiClient, description);
        activity.sendAnalyticsEvent(
                "Play Games",
                "Migration",
                "Started"
        );
    }

    private static void migrateFromAppStateToSnapshot(final GdgActivity activity,
                                                      final GoogleApiClient googleApiClient,
                                                      final String description) {
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

                                            final int statusCode = stateResult.getStatus().getStatusCode();
                                            if (statusCode == GamesStatusCodes.STATUS_OK) {
                                                saveSnapshot(activity,
                                                        googleApiClient,
                                                        description,
                                                        serializedOrganizers,
                                                        stateResult.getSnapshot()
                                                );
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private static void saveSnapshot(final GdgActivity activity,
                                     final GoogleApiClient googleApiClient,
                                     final String description,
                                     final String serializedOrganizers,
                                     final Snapshot snapshot) {

        snapshot.getSnapshotContents().writeBytes(serializedOrganizers.getBytes());
        int numberOfTaggedOrganizers = serializedOrganizers.split(ArrowActivity.ID_SEPARATOR_FOR_SPLIT).length - 1;
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setDescription(description + ": " + numberOfTaggedOrganizers)
                .build();

        Games.Snapshots.commitAndClose(googleApiClient, snapshot, metadataChange)
                .setResultCallback(
                        new ResultCallback<Snapshots.CommitSnapshotResult>() {
                            @Override
                            public void onResult(Snapshots.CommitSnapshotResult result) {

                                if (result.getStatus().isSuccess()) {
                                    PrefUtils.setAppStateMigrationSuccessful(activity);
                                    activity.sendAnalyticsEvent(
                                            "Play Games",
                                            "Migration",
                                            "Successful"
                                    );
                                }
                            }
                        });

        migrateFeelingSocialAchievement(activity, numberOfTaggedOrganizers);
    }

    /**
     * Feeling Social achievement is added at the same time we do this API migration.
     * On the first app open, we do this migration.
     * While we already calculated the "numberOfTaggedOrganizers", it was a good idea to unlock the achievement.
     */
    private static void migrateFeelingSocialAchievement(GdgActivity activity, int numberOfTaggedOrganizers) {
        activity.getAchievementActionHandler().handleFeelingSocial(numberOfTaggedOrganizers);
    }
}
