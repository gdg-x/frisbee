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

package org.gdg.frisbee.android;

import android.text.format.DateUtils;

import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 20.04.13
 * Time: 12:19
 */
public class Const {

    //GitHub
    public static final String GITHUB_ORGA = "gdg-x";
    public static final String GITHUB_REPO = "frisbee";
    //Special Events
    public static final String EXTRA_TAGGED_EVENT = "org.gdg.frisbee.TAGGED_EVENT";
    public static final String EXTRA_TAGGED_EVENT_CACHEKEY = "org.gdg.frisbee.TAGGED_EVENT_CACHEKEY";
    //Special Events Dates
    public static final DateTime START_TIME_DEVFEST = new DateTime(2015, 9, 1, 0, 0);
    public static final DateTime START_TIME_WTM = new DateTime(2016, 2, 1, 0, 0);
    public static final DateTime START_TIME_STUDY_JAMS = new DateTime(2016, 1, 15, 0, 0);
    public static final DateTime START_TIME_IOEXTENDED = new DateTime(2016, 5, 1, 0, 0);
    public static final DateTime END_TIME_DEVFEST = new DateTime(2016, 1, 1, 0, 0);
    public static final DateTime END_TIME_WTM = new DateTime(2016, 4, 1, 0, 0);
    public static final DateTime END_TIME_STUDY_JAMS = new DateTime(2016, 5, 1, 0, 0);
    public static final DateTime END_TIME_IOEXTENDED = new DateTime(2016, 6, 1, 0, 0);
    public static final String EXTRA_PLUS_ID = "plus_id";
    //Navigation Drawer
    public static final int DRAWER_HOME = 0;
    public static final int DRAWER_PULSE = 2;
    public static final int DRAWER_ACHIEVEMENTS = 4;
    public static final int DRAWER_GDE = 5;
    public static final int DRAWER_ARROW = 99;
    public static final int DRAWER_SETTINGS = 100;
    public static final int DRAWER_HELP = 101;
    public static final int DRAWER_FEEDBACK = 102;
    public static final int DRAWER_ABOUT = 103;
    public static final int DRAWER_SUBMENU_GAMES = 1000;
    // Drawer Special Event Items
    public static final int DRAWER_DEVFEST = 30;
    public static final int DRAWER_WTM = 31;
    public static final int DRAWER_STUDY_JAM = 32;
    public static final int DRAWER_IO_EXTENDED = 33;
    //Arrow
    public static final String QR_MSG_PREFIX = "gdgx://arrow?m=";
    public static final String PREF_ORGANIZER_CHECK_TIME = "pref_organizer_check_time";
    public static final String PREF_ORGANIZER_CHECK_ID = "pref_organizer_check_id";
    public static final String PREF_ORGANIZER_STATE = "pref_organizer_state";
    public static final long ORGANIZER_CHECK_MAX_TIME = DateUtils.WEEK_IN_MILLIS;
    public static final String ARROW_MIME = "application/vnd.org.gdgx.frisbee.arrow";
    public static final String ARROW_LB = "CgkIh5yNxL8MEAIQBw";
    public static final String ARROW_K = "XXXX111122223333";
    public static final String GAMES_SNAPSHOT_ID = "tagged_organizers.json";
    public static final int ARROW_STATE_KEY = 1;
    public static final int ARROW_DONE_STATE_KEY = 2;
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    public static final String EXTRA_SECTION = "EXTRA_SECTION";
    public static final String URL_DEVELOPERS_GOOGLE_COM = "https://developers.google.com";
    public static final String URL_HELP = "https://support.google.com/developergroups";
    public static final String URL_GDG_RESOURCE_FOLDER =
        "https://drive.google.com/drive/#folders/0B55wxScz_BJtWW9aUnk2LUlNdEk";
    public static final String URL_GDG_WISDOM_BOOK = "http://gdg-wisdom.gitbooks.io/gdg-wisdom-2015/content/";
    public static final String URL_GDG_LEADS_GPLUS_COMMUNITY =
        "https://plus.google.com/communities/101119632372181012379";
    public static final String URL_GDGROUPS_ORG = "https://gdgroups.org";
    public static final String PATH_GDGROUPS_ORG_EVENT = "event";
    //Keys
    public static final String EXTRA_CHAPTER_ID = "org.gdg.frisbee.CHAPTER";
    public static final String CACHE_KEY_CHAPTER_LIST_HUB = "chapter_list_hub";
    public static final String CACHE_KEY_PULSE_GLOBAL = "pulse_global";
    public static final String CACHE_KEY_GDE_LIST = "gde_list";
    public static final String CACHE_KEY_FRISBEE_CONTRIBUTORS = "frisbee_contributor_list";
    public static final String CACHE_KEY_PERSON = "person_";
    public static final String CACHE_KEY_NEWS = "news_";
    public static final String CACHE_KEY_PULSE = "pulse_";
    public static final String GOOGLE_DEVELOPERS_YT_ID = "UC_x5XG1OV2P6uZZ5FSM9Ttw";
    public static final String ANDROID_DEVELOPERS_YT_ID = "UCVHFbqXqoYvEWM1Ddxl0QDg";
    public static final String YOUTUBE_DEVELOPERS_YT_ID = "UCtVd0c0tGXuTSbU5d8cSBUg";
    public static final String TRUSTSTORE_PW = "VU%&ibkr45pnq39v53x";


    private Const() {
    }
}
