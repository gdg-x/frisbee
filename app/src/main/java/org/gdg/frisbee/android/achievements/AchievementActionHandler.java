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
import android.support.v4.util.Pair;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import org.gdg.frisbee.android.utils.PrefUtils;

import java.util.ArrayList;

/**
 * Simple Achievements action handler used not to clutter activity code with achievements unlocking.
 */
public class AchievementActionHandler {
    private final Context mContext;
    private final GoogleApiClient mGoogleApi;
    private final ArrayList<String> mPending;
    private final ArrayList<Pair<String, Integer>> mPendingIncremental;

    public AchievementActionHandler(GoogleApiClient googleApiClient, Context context) {
        mPending = new ArrayList<>();
        mPendingIncremental = new ArrayList<>();
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
        if (peopleMet < 1) {
            return;
        }

        postAchievementUnlockedEvent(Achievements.ACHIEVEMENT_FEELING_SOCIAL_LEVEL_1);
        postAchievementStepsEvent(Achievements.ACHIEVEMENT_FEELING_SOCIAL_LEVEL_2, peopleMet);
        postAchievementStepsEvent(Achievements.ACHIEVEMENT_FEELING_POPULAR_LEVEL_3, peopleMet);
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
        if (PrefUtils.isAchievementUnlocked(mContext, achievementName)) {
            return;
        }

        if (!mGoogleApi.hasConnectedApi(Games.API)) {
            mPending.add(achievementName);
        } else {
            Games.Achievements.unlock(mGoogleApi, achievementName);
            PrefUtils.setAchievementUnlocked(mContext, achievementName);
        }
    }

    private void postAchievementStepsEvent(final String achievementName, final int steps) {
        if (PrefUtils.hasHigherAchievementSteps(mContext, achievementName, steps)) {
            return;
        }
        if (!mGoogleApi.hasConnectedApi(Games.API)) {
            final Pair<String, Integer> achievement = new Pair<>(achievementName, steps);
            mPendingIncremental.add(achievement);
        } else {
            Games.Achievements.setSteps(mGoogleApi, achievementName, steps);
            PrefUtils.setAchievementSteps(mContext, achievementName, steps);
        }
    }

    public void onConnected() {
        if (!mGoogleApi.hasConnectedApi(Games.API)) {
            return;
        }
        for (String achievement : mPending) {
            postAchievementUnlockedEvent(achievement);
        }
        mPending.clear();
        for (Pair<String, Integer> achievement : mPendingIncremental) {
            postAchievementStepsEvent(achievement.first, achievement.second);
        }
        mPendingIncremental.clear();
    }
}
