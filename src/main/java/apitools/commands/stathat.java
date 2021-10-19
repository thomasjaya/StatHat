package apitools.commands;

import apitools.getPlayerLabels;
import apitools.objects.ConfigElement;
import apitools.util.fileUtil;
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
        File settings_file = new File(fileUtil.getRootDir(), "settings.json"); // reference to settings.json
        Type config_element_arrayList_type = new TypeToken<ArrayList<ConfigElement>>(){}.getType();

        if (s instanceof EntityPlayer) {
            if(string.length == 0){
                return;
            }

            String parameter1 = string[0]; // the first parameter after /stathat

            if(parameter1.equalsIgnoreCase("help")){
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("stathat toggle - toggles stathat on and off"));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("stathat move up/down - moves stathat up/down"));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("stathat shadow - toggles text shadow"));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("stathat personal - toggles StatHat above yourself"));
            }

            if(parameter1.equalsIgnoreCase("toggle")){ // toggling on/off stathat
                getPlayerLabels.toggled = !getPlayerLabels.toggled; //changes toggled setting in getPlayerLabels. TODO: create dedicated class for imported options rather then storing in getPlayerLabels
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You just set StatHat's state to " + getPlayerLabels.toggled + "!")); // tells user the state they set stathat to

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type); // reads settings.json into an ArrayList of ConfigElement objects

                readSettings.get(2).setBool_(getPlayerLabels.toggled); // the toggled setting is at element 2 of the ArrayList of ConfigElement objects (see prepareConfig for structure of settings array / settings file)
                fileUtil.writeJsonToFile(settings_file, readSettings); // write updated settings to settings.json
            }


            if(parameter1.equalsIgnoreCase("move")){ // moving stathat up/down
                if(string[1] != null){
                    if(string[1].equalsIgnoreCase("up")){
                        getPlayerLabels.height += 0.1f;
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You moved StatHat up!"));
                    }
                    else if(string[1].equalsIgnoreCase("down")){
                        getPlayerLabels.height -= 0.1f;
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You moved StatHat down!"));
                    }
                    else{
                        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("Please use /stathat move up or /stathat move down"));
                    }
                } else{
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("Please use /stathat move up or /stathat move down"));
                }

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type);

                readSettings.get(0).setFloat_(getPlayerLabels.height); // the move setting is at element 0 of the settings file (see prepareConfig)
                fileUtil.writeJsonToFile(settings_file, readSettings);
            }

            if(parameter1.equalsIgnoreCase("shadow")){
                getPlayerLabels.shadow = !getPlayerLabels.shadow;
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You changed the shadow setting to " + getPlayerLabels.shadow));

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type);

                readSettings.get(1).setBool_(getPlayerLabels.shadow);
                fileUtil.writeJsonToFile(settings_file, readSettings);

            }

            if(parameter1.equalsIgnoreCase("personal")){
                getPlayerLabels.personal = !getPlayerLabels.personal;
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("You changed the personal setting to " + getPlayerLabels.personal));

                ArrayList<ConfigElement> readSettings = fileUtil.readFromJsonByType(settings_file, config_element_arrayList_type);

                readSettings.get(3).setBool_(getPlayerLabels.personal);
                fileUtil.writeJsonToFile(settings_file, readSettings);

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

}
