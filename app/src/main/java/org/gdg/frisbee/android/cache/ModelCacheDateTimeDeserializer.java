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

package org.gdg.frisbee.android.cache;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.Locale;

class ModelCacheDateTimeDeserializer implements JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonElement jsonElement,
                                Type type,
                                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'").withLocale(Locale.US);
        return fmt.parseDateTime(jsonElement.getAsJsonPrimitive().getAsString());
    }
}
