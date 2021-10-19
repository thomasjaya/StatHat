package apitools;

import apitools.dictionaries.duelsTitles;
import apitools.util.renderUtil;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
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

        getPlayerLabels.threadEnded(playerJson, e);

    }

    public JsonObject getJsonObject (String URLString) throws IOException {
        Gson gson = new Gson();
        URL url = new URL(URLString);
        InputStreamReader reader = new InputStreamReader(url.openStream());

        JsonObject json = gson.fromJson(reader, JsonObject.class);

        return json;
    }

}


public class getPlayerLabels
{
    public static long lastLocrawTime = 0; // the time /locraw was last executed

    public static Map<Entity, String[]> playerLabelList = new HashMap<>();


    public static boolean shouldRender = true;

    public static boolean toggled = true;

    public static float height = 0f;

    public static boolean shadow = true;

    public static boolean personal = false;


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

        if(!toggled){
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
        float winLoss = stats.getAsJsonObject("Duels").getAsJsonObject("general").getAsJsonPrimitive("win_loss_ratio").getAsFloat(); //

        return winLoss;
    }


    static String getBestDuelsTitle(JsonObject stats){
        /*
        Method for determining best duels title from stats object
        TODO: Use wins to work out specific duels titles rather then using highest which could be of any duels type
        */

        JsonArray playersTitlesJson = stats.getAsJsonObject("Duels").getAsJsonObject("general").getAsJsonArray("packages"); //

        ArrayList<String> playersTitles = new ArrayList<>(); // ArrayList containing JsonArray
        if (playersTitlesJson != null) { // converting the JsonArray to an ArrayList
            int len = playersTitlesJson.size();
            for (int i=0;i<len;i++){
                playersTitles.add(playersTitlesJson.get(i).toString().replace("\"", "")); // remove unwanted " from start and end of list
            }
        } else {
            return "Rookie";
        }

        String bestTitle = "rookie_all_modes"; // lowest title

        for(String title : playersTitles){ // looping through the players titles, some are auras
            String t = title.split("_")[0]; // e.g if master_uhc, we only get the master
            String bestT = bestTitle.split("_")[0]; // e.g if rookie_uhc, we only get the rookie

            if(!duelsTitles.titles.containsKey(t)){
                continue;
            }

            if(duelsTitles.titles.get(t) > duelsTitles.titles.get(bestT)){ // if the kills from one is more than the other
                bestTitle = title; // set bestTitle to untrimmed version containing gamemode aswell
            }
        }

        if(bestTitle.contains("all")){ // for the case it is x all modes, it only returns legend
            String[] bestTitleList = bestTitle.split("_");
            String overallBestTitle = bestTitleList[0].substring(0,1).toUpperCase() + bestTitleList[0].substring(1).toLowerCase();
            return overallBestTitle;
        }

        String[] bestTitleList = bestTitle.split("_");

        String firsthalf = bestTitleList[0]; // e.g legend
        String secondhalf = bestTitleList[1]; // e.g uhc

        if(secondhalf.equalsIgnoreCase("tnt") || secondhalf.equalsIgnoreCase("uhc") || secondhalf.equalsIgnoreCase("op")){
            secondhalf = secondhalf.toUpperCase(); // capitilizing all of UHC/TNT
        } else {
            secondhalf = secondhalf.substring(0,1).toUpperCase() + secondhalf.substring(1).toLowerCase(); // capitilising only first letter of e.g nodebuff
        }

        firsthalf = firsthalf.substring(0,1).toUpperCase() + firsthalf.substring(1).toLowerCase(); // capitilizing first letter of e.g legend -> Legend

        String bestTitleTrimmed = secondhalf + " " + firsthalf; // so is e.g uhc legend not legend uhc

        return bestTitleTrimmed;
    }



    static Color getTitleColor(String title){ // returning duels title colours from title name
        Color color = new Color(255, 255, 255);

        if(title.toLowerCase().contains("rookie")){
            color = new Color(210,180,140);
        }
        if(title.toLowerCase().contains("iron")){
            color = new Color(220,220,220);
        }
        if(title.toLowerCase().contains("gold")){
            color = new Color(255,215,0);
        }
        if(title.toLowerCase().contains("diamond")){
            color = new Color(0,204,204);
        }
        if(title.toLowerCase().contains("master")){
            color = new Color(0,204,0);
        }
        if(title.toLowerCase().contains("legend")){
            color = new Color(170,0,0,255);
        }
        if(title.toLowerCase().contains("grandmaster")){
            color = new Color(255,255,85,255);
        }
        if(title.toLowerCase().contains("godlike")){
            color = new Color(170,0,170,255); // (152,0,152,255)
        }

        return color;
    }


    @SubscribeEvent
    public void render(RenderWorldLastEvent evt) { //RenderWorldLastEvent RenderPlayerEvent

        if(Minecraft.getMinecraft().isSingleplayer()){
            return;
        }

        if(!toggled){
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
            if(!personal && e == thePlayer){
                continue;
            }

            if(e.isPotionActive(Potion.invisibility)){
                continue;
            }

            String[] lines = playerLabelList.get(e);


            if(lines[0] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[0], 0, 0.6 + height, 0, getTitleColor(lines[0]), shadow);
            }
            if(lines[1] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[1], 0, 0.8 + height, 0, getTitleColor(lines[1]), shadow);
            }
            if(lines[2] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[2], 0, 1 + height, 0, getTitleColor(lines[2]), shadow);
            }

        }


    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }


}


