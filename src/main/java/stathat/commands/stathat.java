package stathat.commands;

import stathat.dictionaries.titles;
import stathat.objects.UserSettings;
import stathat.playerLabels;
import stathat.objects.ConfigElement;
import stathat.util.fileUtil;
import stathat.generateConfig;

import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class stathat extends CommandBase implements ICommand {

    /*
    Class containing all to do with processing commands.
    - Interprets /stathat [parameter] [parameter]
    - Accesses variables in getPlayerLabels
    - Saves user settings to json file
     */

    @Override
    public String getCommandName() {
        return "stathat";
    }


    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "change stathat settings!";
    }

    @Override
    public void processCommand(ICommandSender s, String[] string) {
        String orange =  "\u00A7" + "6";
        String dark_red =  "\u00A7" + "4";
        String white =  "\u00A7" + "f";


        File settings_file = new File(fileUtil.getRootDir(), "settings.json"); // reference to settings.json
        Type config_element_arrayList_type = new TypeToken<ArrayList<ConfigElement>>(){}.getType();

        UserSettings settings = generateConfig.settings;

        if (s instanceof EntityPlayer) {
            if(string.length == 0){
                return;
            }

            String parameter1 = string[0]; // the first parameter after /stathat

            if(parameter1.equalsIgnoreCase("help")){
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(dark_red + "<============= StatHat Settings Start =============>"));

                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(orange + "/stathat toggle" + white + " : toggles stathat" + " (currently: " + orange + settings.isToggled() + white +")"));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(orange + "/stathat move [up/down]" + white + " : moves stathat" + " (currently: "  + orange +  settings.getHeight() + white + ")"));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(orange + "/stathat shadow" + white + " : toggles text shadow" + " (currently: "  + orange +  settings.isShadow() + white + ")"));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(orange + "/stathat personal" + white + " : toggles StatHat above yourself" + " (currently: "  + orange +  settings.isPersonal() + white + ")"));

                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(orange + "/stathat gamemode [gamemode]" + white + " : changes which gamemode stats are shown" + " (currently: "  + orange +  settings.getGamemode() + white + ")"));
                for(String game : titles.gamemodes.keySet()){
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText( "> " + orange + game));
                }

                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(dark_red + "<============= StatHat Settings End =============>"));



            }

            if(parameter1.equalsIgnoreCase("toggle")){ // toggling on/off stathat
                settings.setToggled(!settings.isToggled()); // toggles toggled setting
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You just set StatHat's state to " + settings.isToggled() + "!")); // tells user the state they set stathat to

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type); // reads settings.json into an ArrayList of ConfigElement objects

                readSettings.get(2).setBool_(settings.isToggled()); // the toggled setting is at element 2 of the ArrayList of ConfigElement objects (see prepareConfig for structure of settings array / settings file)
                fileUtil.writeJsonToFile(settings_file, readSettings); // write updated settings to settings.json
            }


            if(parameter1.equalsIgnoreCase("move")){ // moving stathat up/down
                if(string[1] != null){
                    if(string[1].equalsIgnoreCase("up")){
                        settings.setHeight(settings.getHeight() + 0.1f);
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You moved StatHat up!"));
                    }
                    else if(string[1].equalsIgnoreCase("down")){
                        settings.setHeight(settings.getHeight() - 0.1f);
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You moved StatHat down!"));
                    }
                    else{
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7" + "4" + "Please use /stathat move up or /stathat move down"));
                    }
                } else{
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7" + "4" + "Please use /stathat move up or /stathat move down"));
                }

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type);

                readSettings.get(0).setFloat_(settings.getHeight()); // the move setting is at element 0 of the settings file (see prepareConfig)
                fileUtil.writeJsonToFile(settings_file, readSettings);
            }

            if(parameter1.equalsIgnoreCase("shadow")){
                settings.setShadow(!settings.isShadow());
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You changed the shadow setting to " + settings.isToggled()));

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type);

                readSettings.get(1).setBool_(settings.isShadow());
                fileUtil.writeJsonToFile(settings_file, readSettings);

            }

            if(parameter1.equalsIgnoreCase("personal")){
                settings.setPersonal(!settings.isPersonal());
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You changed the personal setting to " + settings.isPersonal()));

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type);

                readSettings.get(3).setBool_(settings.isPersonal());
                fileUtil.writeJsonToFile(settings_file, readSettings);

            }

            if(parameter1.equalsIgnoreCase("gamemode")){
                String parameter2 = string[1];
                if(parameter2 != null){
                    parameter2 = toTitleCase(parameter2);

                    if(parameter2.equalsIgnoreCase("Op")){ // title case will make user input of whatever to Op when in the settings it must be OP
                        parameter2 = "OP";
                    }

                    if(Arrays.stream(titles.gamemodes.keySet().toArray()).anyMatch(parameter2::equals)) { // parameter2 (e.g bridge) of gamemode type is one of the valid options

                        settings.setGamemode(parameter2); // set gamemode in settings to the chosen option

                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You changed the gamemode setting to " + settings.getGamemode()));

                        ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type); // read settings into a ConfigElement ArrayList

                        readSettings.get(4).setString_(settings.getGamemode()); // set string of 4th element of read settings ArrayList to chosen option
                        fileUtil.writeJsonToFile(settings_file, readSettings); // write updated read settings ArrayList to file
                    } else{
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7" + "4" + parameter2 + " isn't a valid gamemode.")); // not on the list of valid gamemodes
                    }
                } else{
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A7" + "4" + "Please input a gamemode you wish to change to"));
                }
            }

        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

}
