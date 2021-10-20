package stathat;

import stathat.dictionaries.DuelsTitles;
import stathat.objects.UserSettings;
import stathat.util.renderUtil;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.potion.Potion;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.List;



class getPlayerData implements Runnable { // stage 3
    /*
    Class which fetches players stats through the hypixel API in a seperate thread
     */

    String ign;
    Entity e;

    JsonObject playerJson;

    public getPlayerData(Entity e) {
        this.ign = e.getName();
        this.e = e;
    }


    @Override
    public void run() {
        try{
            playerJson = getJsonObject("https://api.slothpixel.me/api/players/" + ign);
        } catch (IOException e) {
            return;
        }

        playerLabels.threadEnded(playerJson, e);

    }

    public JsonObject getJsonObject (String URLString) throws IOException {
        Gson gson = new Gson();
        URL url = new URL(URLString);
        InputStreamReader reader = new InputStreamReader(url.openStream());

        JsonObject json = gson.fromJson(reader, JsonObject.class);

        return json;
    }

}


public class playerLabels
{
    private long lastLocrawTime = 0; // the time /locraw was last executed
    private boolean shouldRender = true; // e.g if player is blind set false

    private static Map<Entity, String[]> playerLabelList = new HashMap<>();

    UserSettings settings = generateConfig.settings;


    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event){
        /*
        - Method that determines whether player is in a duels lobby/duels game through output of /locraw
        - If output of locraw has DUELS in it, shouldRender can be made true.
         */

        if(Minecraft.getMinecraft().isSingleplayer()){
             return;
        }

        String message = event.message.getUnformattedText();

        if(Minecraft.getMinecraft().getCurrentServerData().serverIP.contains("hypixel")){ // if the sevrer is hypixel
            if(message.contains("\"server\"")){
                if(message.contains("DUELS")){
                    shouldRender = true;
                } else {
                    shouldRender = false;
                }
                event.setCanceled(true);
                return;
            }
        } else{
            shouldRender = true;
        }


    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e){
        /* When the world unloads, clear the cache */
        if(Minecraft.getMinecraft().isSingleplayer()){
            return;
        }

        playerLabelList.clear();
    }

    @SubscribeEvent // stage 1
    public void onEntityJoinWorld(EntityJoinWorldEvent event){

        if(Minecraft.getMinecraft().isSingleplayer()){
            return;
        }

        if(!settings.isToggled()){
            return;
        }

        if(!event.entity.isEntityAlive()){
            return;
        }

        if (!(event.entity instanceof EntityPlayer)) {
            return;
        }


        if(playerLabelList.containsKey(event.entity)){
            return;
        }

        /*
        If server IP is hypixel (on entity join world) then send command /locraw
        - As long as it's been 30 seconds so the command isn't spammed if the player is moving between worlds quickly
        */
        if(Minecraft.getMinecraft().getCurrentServerData().serverIP.contains("hypixel")) {
            String eName = event.entity.getName();
            String pName = Minecraft.getMinecraft().thePlayer.getName();
            if (eName.equalsIgnoreCase(pName)) { //if the player is loaded into a new world
                long timeSinceLastLocraw = System.currentTimeMillis() - lastLocrawTime;
                if (timeSinceLastLocraw > 3000) { //if the time since last execution of /locraw is longer then 600ms, feel free to do it again
                    Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C01PacketChatMessage("/locraw"));

                    lastLocrawTime = System.currentTimeMillis();
                }

            }
        }

        getPlayerData(event.entity); // run getPlayerData class with entity which just joined the world
    }

    private void getPlayerData(Entity e) { // stage 2
        /* method which adds to playerList for a given entity through the runnable in seperate thread so doesn't lag the player */
        getPlayerData playerdata = new getPlayerData(e);
        Thread thread = new Thread(playerdata);
        thread.start();
    }

    public static void threadEnded(JsonObject playerJson, Entity e) { // stage 4
        /*
        Method called from runnable, which adds target entity and stats formatted into a string array into a hashmap
         */
        JsonObject statsObject = playerJson.getAsJsonObject("stats");

        String bestTitle = getBestDuelsTitle(statsObject);

        Float winLoss = getDuelsWinLoss(statsObject);
        String roundedWinLoss = round(winLoss, 2) + " W/L";

        String[] lines = new String[]{
                bestTitle,
                roundedWinLoss,
                ""
        };

        playerLabelList.put(e, lines); // appending target entity and string array to playerLabelList
    }


    static Float getDuelsWinLoss(JsonObject stats){
        /* Method for extracting duels win/loss from stats object*/
        float winLoss = stats.getAsJsonObject("Duels").getAsJsonObject("general").getAsJsonPrimitive("win_loss_ratio").getAsFloat();

        return winLoss;
    }


    static String getBestDuelsTitle(JsonObject stats){
        int totalWins = stats.getAsJsonObject("Duels").getAsJsonObject("general").getAsJsonPrimitive("wins").getAsInt(); // a players total wins

        for(int wins : DuelsTitles.titles.descendingKeySet()){
            if(wins < Math.round(totalWins/2)){ // for overall title you need double the wins
                return DuelsTitles.titles.get(wins);
            }
        }
        return "Rookie";
    }


    @SubscribeEvent
    public void render(RenderWorldLastEvent evt) { //RenderWorldLastEvent RenderPlayerEvent

        if(Minecraft.getMinecraft().isSingleplayer()){
            return;
        }

        if(!settings.isToggled()){
            return;
        }

        if(!shouldRender){
            return;
        }

        if(Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness)){
            return;
        }

        List<EntityPlayer> entityList = Minecraft.getMinecraft().theWorld.playerEntities;

        for(EntityPlayer e : entityList) {

            if(!playerLabelList.containsKey(e)){
                continue;
            }

            if(e.getDisplayName().getUnformattedText().contains("\u00A7" + "k")){ //§k
                continue;
            }

            if(e.isSneaking()){
                continue;
            }

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if(!settings.isPersonal() && e == thePlayer){
                continue;
            }

            if(e.isPotionActive(Potion.invisibility)){
                continue;
            }

            String[] lines = playerLabelList.get(e);


            if(lines[0] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[0], 0, 0.6 + settings.getHeight(), 0, DuelsTitles.getTitleColor(lines[0]), settings.isShadow());
            }
            if(lines[1] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[1], 0, 0.8 + settings.getHeight(), 0, DuelsTitles.getTitleColor(lines[1]), settings.isShadow());
            }
            if(lines[2] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[2], 0, 1 + settings.getHeight(), 0, DuelsTitles.getTitleColor(lines[2]), settings.isShadow());
            }

        }


    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }


}


