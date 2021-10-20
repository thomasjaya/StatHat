package stathat.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class requestUtil {

    public JsonObject getJsonObject(String URLString) throws IOException { // get JsonObject from URL
        Gson gson = new Gson();
        URL url = new URL(URLString);
        InputStreamReader reader = new InputStreamReader(url.openStream());

        JsonObject json = gson.fromJson(reader, JsonObject.class);

        return json;
    }
}
