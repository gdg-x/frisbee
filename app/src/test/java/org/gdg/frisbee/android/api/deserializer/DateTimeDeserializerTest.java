package org.gdg.frisbee.android.api.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;

import org.joda.time.DateTime;
import org.junit.Test;

public class DateTimeDeserializerTest {

    @Test
    public void testFormat(){
        String date = "10 Jun 2013 23:00 +0200";
        new DateTimeDeserializer().deserialize(new JsonPrimitive(date), DateTime.class, new JsonDeserializationContext() {
            @Override
            public <T> T deserialize(JsonElement jsonElement, Type type) throws JsonParseException {
                return null;
            }
        });
    }
}
