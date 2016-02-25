/*
 * Copyright 2013-2015 The GDG Frisbee Project
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

package org.gdg.frisbee.android.app;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.analytics.HitBuilders;

import org.gdg.frisbee.android.utils.PrefUtils;

import java.io.IOException;

import timber.log.Timber;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 02.07.13
 * Time: 22:27
 */
public class BackupAgent extends BackupAgentHelper {

    private static final String PREFS_BACKUP_KEY = "gdg_prefs";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper =
            new SharedPreferencesBackupHelper(this, PrefUtils.PREF_NAME);
        addHelper(PREFS_BACKUP_KEY, helper);
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        Timber.d(String.format("Restoring from backup (was saved using version %d)", appVersionCode));

        App.getInstance().getTracker().send(new HitBuilders.EventBuilder()
            .setCategory("backup")
            .setAction("restore")
            .setLabel("" + appVersionCode)
            .build());
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState,
                         BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        super.onBackup(oldState, data, newState);

        App.getInstance().getTracker().send(new HitBuilders.EventBuilder()
            .setCategory("backup")
            .setAction("backup")
            .setLabel("")
            .build());
    }
}
