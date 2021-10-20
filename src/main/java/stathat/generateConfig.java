package stathat;

import stathat.objects.ConfigElement;
import stathat.objects.UserSettings;
import stathat.util.fileUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class generateConfig {
    /*
    Builds a config file on init, reading from existing file and adding any new settings.
     */
    public static UserSettings settings = new UserSettings();

    public static void init() throws IOException {

        File settingsFile = new File(fileUtil.getRootDir(), "settings.json");

        if(!settingsFile.exists()) {
            settingsFile.createNewFile();
        }

        Gson gson = fileUtil.getGson();

        Type ConfigElementArrayListType = new TypeToken<ArrayList<ConfigElement>>(){}.getType();
        ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settingsFile, ConfigElementArrayListType);

        String readSettingsString = gson.toJson(fileUtil.readFromJson(settingsFile, JsonArray.class));

        ArrayList<ConfigElement> writeSettings = new ArrayList<>(); // if readSettings has a gap e.g toggled, then this will make a list of what's going to be written


        if(!readSettingsString.contains("position")){ // position at element 0 of settings ArrayList / settings file
            ConfigElement position = new ConfigElement();
            position.setName("position");
            position.setFloat_(0.5f);

            writeSettings.add(position);
        } else {
            writeSettings.add(readSettings.get(0));
        }

        if(!readSettingsString.contains("shadow")){ // shadow at element 1
            ConfigElement shadow = new ConfigElement();
            shadow.setName("shadow");
            shadow.setBool_(false);

            writeSettings.add(shadow);
        } else {
            writeSettings.add(readSettings.get(1));
        }

        if(!readSettingsString.contains("toggled")){ // toggled at element 2
            ConfigElement toggled = new ConfigElement();
            toggled.setName("toggled");
            toggled.setBool_(true);

            writeSettings.add(toggled);
        } else {
            writeSettings.add(readSettings.get(2));
        }

        if(!readSettingsString.contains("personal")){ // personal at element 3
            ConfigElement personal = new ConfigElement();
            personal.setName("personal");
            personal.setBool_(false);

            writeSettings.add(personal);
        } else {
            writeSettings.add(readSettings.get(3));
        }

        fileUtil.writeJsonToFile(settingsFile, writeSettings);

        settings.setHeight(writeSettings.get(0).getFloat_());
        settings.setShadow(writeSettings.get(1).getBool_());
        settings.setToggled(writeSettings.get(2).getBool_());
        settings.setPersonal(writeSettings.get(3).getBool_());

    }
}
