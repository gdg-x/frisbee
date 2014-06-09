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

package org.gdg.frisbee.android;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 20.04.13
 * Time: 12:19
 */
public class Const {

    // Settings
    public static final String SETTINGS_FIRST_START = "gdg_first_start";
    public static final String SETTINGS_SIGNED_IN = "gdg_signed_in";
    public static final String SETTINGS_GCM_REG_ID = "gdg_registration_id";
    public static final String SETTINGS_VERSION_CODE = "gdg_version_code";
    public static final String SETTINGS_HOME_GDG = "gdg_home";
    public static final String SETTINGS_APP_STARTS = "gdg_app_starts";
    public static final String SETTINGS_VIDEOS_PLAYED = "gdg_app_videos_played";
    public static final String SETTINGS_GDL_VIDEOS_PLAYED = "gdg_app_gdl_video_played";
    public static final String SETTINGS_ANALYTICS = "analytics";
    public static final String SETTINGS_GCM = "gcm";
    public static final String SETTINGS_GCM_NOTIFICATION_KEY = "gcm_notification_key";
    public static final String SETTINGS_SEASONS_GREETINGS = "seasons_greetings";

    public static final boolean SETTINGS_FIRST_START_DEFAULT = true;

    public static final String SETTINGS_OPEN_DRAWER_ON_START = "open_drawer_on_start";
    public static final boolean SETTINGS_OPEN_DRAWER_ON_START_DEFAULT = true;

    public static final String GITHUB_ORGA = "gdg-x";
    public static final String GITHUB_REPO = "frisbee";

    public static final String SPECIAL_EVENT_TITLE_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_TITLE";
    public static final String SPECIAL_EVENT_LAYOUT_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_LAYOUT";
    public static final String SPECIAL_EVENT_FRAGMENT_LAYOUT_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_FRAGMENT_LAYOUT";
    public static final String SPECIAL_EVENT_LOGO_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_LOGO";
    public static final String SPECIAL_EVENT_VIEWTAG_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_VIEWTAG";
    public static final String SPECIAL_EVENT_CACHEKEY_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_CACHEKEY";
    public static final String SPECIAL_EVENT_DESCRIPTION_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_DESCRIPTION";
    public static final String SPECIAL_EVENT_START_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_START";
    public static final String SPECIAL_EVENT_END_EXTRA = "org.gdg.frisbee.SPECIAL_EVENT_END";

    public static final int DRAWER_HOME = 0;
    public static final int DRAWER_GDL = 1;
    public static final int DRAWER_PULSE = 2;
    public static final int DRAWER_SPECIAL = 3;
    public static final int DRAWER_ACHIEVEMENTS = 4;
    public static final int DRAWER_GDE = 5;
    public static final int DRAWER_ARROW = 99;
    public static final String QR_MSG_PREFIX = "gdgx://arrow?m=";
    public static final String PREF_ORGANIZER_CHECK_TIME = "pref_organizer_check_time";
    public static final String PREF_ORGANIZER_CHECK_ID = "pref_organizer_check_id";
    public static final long ORGANIZER_CHECK_MAX_TIME = 2592000000L; // 30 days
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_SECTION = "EXTRA_SECTION";
    public static final String URL_DEVELOPERS_GOOGLE_COM = "https://developers.google.com";
    public static final String EVENTS = "events";
    public static final String EXTRA_GROUP_ID = "org.gdg.frisbee.CHAPTER";
    public static final String CACHE_KEY_CHAPTER_LIST_HUB = "chapter_list_hub";

    // Location
    public static int MAX_DISTANCE = 50000; // 50KM
    public static int MAX_TIME = 5*60*60;

    public static final String GOOGLE_DEVELOPERS_YT_ID = "UC_x5XG1OV2P6uZZ5FSM9Ttw";
    public static final String ANDROID_DEVELOPERS_YT_ID = "UCVHFbqXqoYvEWM1Ddxl0QDg";
    public static final String YOUTUBE_DEVELOPERS_YT_ID = "UCtVd0c0tGXuTSbU5d8cSBUg";

    public static final String ARROW_MIME = "application/vnd.org.gdgx.frisbee.arrow";
    public static final String ARROW_LB = "CgkIh5yNxL8MEAIQBw";
    public static final String ARROW_K = "XXXX111122223333";
    public static final int ARROW_STATE_KEY = 1;

    public static final int ARROW_DONE_STATE_KEY = 2;

    public static final class LogLevel {
        public static final int NONE    = 0;
        public static final int ERROR   = 1;
        public static final int INFO    = 2;
        public static final int DEBUG   = 3;
    }

    public static final int LOG_LEVEL = LogLevel.DEBUG;
    public static final boolean DEVELOPER_MODE = false;

    public static final String TRUSTSTORE_PW = "VU%&ibkr45pnq39v53x";
    public static final boolean ALPHA = false;
}
