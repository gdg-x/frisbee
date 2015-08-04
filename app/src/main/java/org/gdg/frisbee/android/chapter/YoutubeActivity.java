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

package org.gdg.frisbee.android.chapter;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgActivity;

/**
 * @author maui
 */
public class YoutubeActivity extends GdgActivity implements YouTubePlayer.OnInitializedListener {

    private static final int PORTRAIT_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
    public static final String EXTRA_VIDEO_ID = "video_id";

    @NonNull
    protected String getTrackedViewName() {
        return "YouTube";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_youtube);

        YouTubePlayerSupportFragment mPlayerFragment = 
                (YouTubePlayerSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.youtube_fragment);
        mPlayerFragment.initialize(BuildConfig.ANDROID_SIMPLE_API_ACCESS_KEY, this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, @NonNull YouTubePlayer youTubePlayer, boolean wasRestored) {

        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(String s) {
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
            }

            @Override
            public void onVideoEnded() {
                getAchievementActionHandler().handleVideoViewed();
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
            }
        });

        if (!wasRestored) {
            youTubePlayer.loadVideo(getIntent().getStringExtra(EXTRA_VIDEO_ID));
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, getString(R.string.youtube_init_failed), Toast.LENGTH_LONG).show();
    }

}
