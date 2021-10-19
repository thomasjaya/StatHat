package apitools;

import apitools.objects.ConfigElement;
import apitools.util.fileUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class prepareConfig {
    /*
    Builds a config file on init, reading from existing file and adding any new settings.
     */

    private static File settingsFile = new File(fileUtil.getRootDir(), "settings.json");

    public static void init() throws IOException {

        if(!settingsFile.exists()) {
            settingsFile.createNewFile();
        }

        Gson gson = fileUtil.getGson();

        Type ConfigElementArrayListType = new TypeToken<ArrayList<ConfigElement>>(){}.getType();
        ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settingsFile, ConfigElementArrayListType);

        String readSettingsString = gson.toJson(fileUtil.readFromJson(settingsFile, JsonArray.class));

        ArrayList<ConfigElement> writeSettings = new ArrayList<>(); // if readSettings has a gap e.g toggled, then this will make a list of what's going to be written


        if(!readSettingsString.contains("position")){ // Position 0 in settings file/arraylist
            ConfigElement position = new ConfigElement();
            position.setName("position");
            position.setFloat_(0.0f);

            writeSettings.add(position);
        } else {
            writeSettings.add(readSettings.get(0));
        }

        if(!readSettingsString.contains("shadow")){
            ConfigElement shadow = new ConfigElement();
            shadow.setName("shadow");
            shadow.setBool_(false);

            writeSettings.add(shadow);
        } else {
            writeSettings.add(readSettings.get(1));
        }

        if(!readSettingsString.contains("toggled")){
            ConfigElement toggled = new ConfigElement();
            toggled.setName("toggled");
            toggled.setBool_(true);

            writeSettings.add(toggled);
        } else {
            writeSettings.add(readSettings.get(2));
        }

        if(!readSettingsString.contains("personal")){
            ConfigElement personal = new ConfigElement();
            personal.setName("personal");
            personal.setBool_(false);

            writeSettings.add(personal);
        } else {
            writeSettings.add(readSettings.get(3));
        }

        fileUtil.writeJsonToFile(settingsFile, writeSettings);


        getPlayerLabels.height = writeSettings.get(0).getFloat_();
        getPlayerLabels.shadow = writeSettings.get(1).getBool_();
        getPlayerLabels.toggled = writeSettings.get(2).getBool_();
        getPlayerLabels.personal = writeSettings.get(3).getBool_();
    }
}
