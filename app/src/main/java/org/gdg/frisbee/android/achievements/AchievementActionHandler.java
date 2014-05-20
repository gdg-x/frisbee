/*
 * Copyright 2013 The GDG Frisbee Project
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

package org.gdg.frisbee.android.achievements;

import android.content.SharedPreferences;
import android.os.Handler;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.utils.PlayServicesHelper;

/**
 *
 * Simple Achievements action handler used not to clutter activity code with achievements unlocking.
 *
 * @author Bartek Przybylski <bart.p.pl@gmail.com>
 */
public class AchievementActionHandler {
    private Handler mHandler;
    private PlayServicesHelper mPlayHelper;
    private SharedPreferences mPreferences;

    private static final int ONE_SEC_IN_MILLISECONDS = 1000;

    public AchievementActionHandler(Handler handler,
                                    PlayServicesHelper playHelper,
                                    SharedPreferences preferences) {
        mHandler = handler;
        mPlayHelper = playHelper;
        mPreferences = preferences;
    }

    public void handleSignIn() {
        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_SIGNIN);
    }

    public void handleAppStarted() {
        int appStartedCounter = mPreferences.getInt(Const.SETTINGS_APP_STARTS, 0);

        if (appStartedCounter >= 10)
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_RETURN);

        if (appStartedCounter >= 50)
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_KING_OF_THE_HILL);
    }

    public void handleVideoViewed() {
        int videoPlayed = updateVideoViewedCounter(Const.SETTINGS_VIDEOS_PLAYED);

        if(videoPlayed >= 10)
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_CINEPHILE);
    }

    public void handleGDLVideoViewed() {
        int videoPlayed = updateVideoViewedCounter(Const.SETTINGS_GDL_VIDEOS_PLAYED);

        if(videoPlayed >= 5)
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_GDL_ADDICT);
    }

    private int updateVideoViewedCounter(String videoCategoryName) {
        SharedPreferences.Editor editor = mPreferences.edit();
        int videoPlayed = mPreferences.getInt(videoCategoryName, 0);
        videoPlayed++;

        editor.putInt(videoCategoryName, videoPlayed);
        editor.apply();
        return videoPlayed;
    }

    private void postAchievementUnlockedEvent(final String achievementName) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPlayHelper.getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                    @Override
                    public void onGotGamesClient(GoogleApiClient c) {
                        Games.Achievements.unlock(c, achievementName);
                    }
                });
            }
        }, ONE_SEC_IN_MILLISECONDS);
    }
}
