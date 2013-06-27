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

package org.gdg.frisbee.android.api.deserializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.lang.reflect.Type;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api.deserializer
 * <p/>
 * User: maui
 * Date: 23.04.13
 * Time: 04:51
 */
public class DateTimeSerializer implements JsonSerializer<DateTime> {
    @Override
    public JsonElement serialize(DateTime dateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZZ");
        return new JsonPrimitive(dateTime.toString(fmt));
    }
}