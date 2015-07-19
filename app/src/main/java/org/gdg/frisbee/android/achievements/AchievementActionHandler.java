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

package org.gdg.frisbee.android.achievements;

import android.content.Context;
import android.os.Handler;
import android.support.v4.util.Pair;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import org.gdg.frisbee.android.utils.PrefUtils;

import java.util.ArrayList;

/**
 * Simple Achievements action handler used not to clutter activity code with achievements unlocking.
 *
 * @author Bartek Przybylski <bart.p.pl@gmail.com>
 */
public class AchievementActionHandler {
    private static final int ONE_SEC_IN_MILLISECONDS = 1000;
    private final Context mContext;
    private final Handler mHandler;
    private final GoogleApiClient mGoogleApi;
    private final ArrayList<String> mPending;
    private final ArrayList<Pair<String, Integer>> mPendingIncremental;

    public AchievementActionHandler(Handler handler,
                                    GoogleApiClient googleApiClient, Context context) {
        mPending = new ArrayList<>();
        mPendingIncremental = new ArrayList<>();
        mHandler = handler;
        mGoogleApi = googleApiClient;
        mContext = context;
    }

    public void handleSignIn() {
        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_SIGNIN_PALOOZA);
    }

    public void handleAppStarted() {
        int appStartedCounter = PrefUtils.getAppStarts(mContext);

        if (appStartedCounter >= 10) {
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_THANKS_FOR_COMING_BACK);
        }

        if (appStartedCounter >= 50) {
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_KING_OF_THE_HILL);
        }
    }

    public void handleVideoViewed() {
        int videoPlayed = PrefUtils.increaseVideoViewed(mContext);

        if (videoPlayed >= 10) {
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_CINEPHILE);
        }
    }

    public void handleGdgManiac() {
        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_GDG_MANIAC);
    }

    public void handleLookingForExperts() {
        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_LOOKING_FOR_EXPERTS);
    }

    public void handleFeelingSocial(final int peopleMet) {

        //If the user has met more than 5 people
        //Unlock the 2nd level and increment the 3rd level.
        if (peopleMet > 5) {
            postAchievementStepsEvent(Achievements.ACHIEVEMENT_FEELING_POPULAR_LEVEL_3, peopleMet);
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_FEELING_SOCIAL_LEVEL_2);
        } else if (peopleMet > 1) {
            postAchievementStepsEvent(Achievements.ACHIEVEMENT_FEELING_SOCIAL_LEVEL_2, peopleMet);
        }

        //This is not incremental
        if (peopleMet >= 1) {
            postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_FEELING_SOCIAL_LEVEL_1);
        }
    }

    public void handleKissesFromGdgXTeam() {
        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_KISSES_FROM_GDGX_TEAM);
    }

    public void handlePowerUser() {
        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_POWER_USER);
    }

    public void handleCuriousOrganizer() {
        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_CURIOUS_ORGANIZER);
    }


    private void postAchievementUnlockedEvent(final String achievementName) {
        if (!mGoogleApi.isConnected()) {
            mPending.add(achievementName);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Games.Achievements.unlock(mGoogleApi, achievementName);
                }
            }, ONE_SEC_IN_MILLISECONDS);
        }
    }

    private void postAchievementStepsEvent(final String achievementName, final int steps) {
        final Pair<String, Integer> achievement = new Pair<>(achievementName, steps);
        if (!mGoogleApi.isConnected()) {
            mPendingIncremental.add(achievement);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Games.Achievements.setSteps(mGoogleApi, achievement.first, achievement.second);
                }
            }, ONE_SEC_IN_MILLISECONDS);
        }
    }

    public void onConnected() {
        for (String achievement : mPending) {
            postAchievementUnlockedEvent(achievement);
        }
        mPending.clear();
    }
}
