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

package org.gdg.frisbee.android.api;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.FieldNamingPolicy;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.GdlShow;
import org.gdg.frisbee.android.api.model.GdlShowList;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 19:05
 * To change this template use File | Settings | File Templates.
 */
public class GoogleDevelopersLive {

    private static final String BASE_URL = "https://developers.google.com";
    private static final String RECORDED_SHOWS = "/live%s/browse";

    public ApiRequest getShow(String showPath, Response.Listener<GdlShow> successListener, Response.ErrorListener errorListener) {
        JsoupRequest<GdlShow> dirReq = new JsoupRequest<GdlShow>(Request.Method.GET,
                BASE_URL+showPath,
                new JsoupRequest.ParseListener<GdlShow>() {
                    @Override
                    public GdlShow parse(Document doc) {
                        GdlShow show = new GdlShow();
                        Element title = doc.select("div#title-container h1").first();
                        Element playerIframe = doc.select("section#player-container iframe").first();

                        String videoId = playerIframe.attr("src");
                        return null;  //To change body of implemented methods use File | Settings | File Templates.
                    }
                },
                successListener,
                errorListener);
        return new ApiRequest(dirReq);
    }

    public ApiRequest getRecordedShows(String category, Response.Listener<GdlShowList> successListener, Response.ErrorListener errorListener) {

        if(category == null || category.equals("")) {
            category = "";
        } else {
            category = "/"+category;
        }
        JsoupRequest<GdlShowList> dirReq = new JsoupRequest<GdlShowList>(Request.Method.GET,
                BASE_URL+String.format(RECORDED_SHOWS, category),
                new JsoupRequest.ParseListener<GdlShowList>() {
                    @Override
                    public GdlShowList parse(Document doc) {

                        Element nextToken = doc.select("div.paging-cursor").first();

                        GdlShowList showList;

                        if(nextToken != null)
                            showList = new GdlShowList(nextToken.attr("data-cursor"));
                        else
                            showList = new GdlShowList(null);

                        Elements shows = doc.select("li");
                        Pattern p = Pattern.compile(".*i\\.ytimg\\.com/vi/([^/]+).*");
                        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM d, YYYY, h:mm").withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")));

                        for(int i = 0; i < shows.size(); i++) {
                            Element show = shows.get(i);
                            Element showLink = show.select("a").first();

                            GdlShow gdlShow = new GdlShow();
                            if(showLink != null)  {
                                gdlShow.setTitle(showLink.text());

                                /*String dateTime = show.text()
                                        .replace(showLink.text(), "")
                                        .replace("a.m.", "")
                                        .replace("p.m.", "")
                                        .trim();
                                gdlShow.setDateTime(fmt.parseDateTime(dateTime).toDateTime(DateTimeZone.getDefault()));*/
                                gdlShow.setUrl(showLink.attr("href"));
                                String thumbLink = showLink.select("img.video-thumbnail").first().attr("src");
                                Matcher matcher = p.matcher(thumbLink);
                                if(matcher.matches()) {
                                    gdlShow.setYoutubeId(matcher.group(1));
                                }
                            }
                            showList.getShows().add(gdlShow);
                        }

                        return showList;  //To change body of implemented methods use File | Settings | File Templates.
                    }
                },
                successListener,
                errorListener);
        return new ApiRequest(dirReq);
    }
}
