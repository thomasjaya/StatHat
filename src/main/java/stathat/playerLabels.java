package stathat;

import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import stathat.dictionaries.titles;
import stathat.objects.UserSettings;
import stathat.util.APIUtil;
import stathat.util.requestUtil;
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

import java.io.IOException;
import java.util.*;
import java.util.List;


public class playerLabels
{
    private long lastLocrawTime = 0; // the time /locraw was last

    private boolean inDuels = true;
    private String currentGame = "overall";
    UserSettings settings = generateConfig.settings;

    private Map<Entity, JsonObject> playerLabelList = new HashMap<>(); // all PlayerEntities and their respective JsonObjects

    private ArrayList<Entity> requestQueue = new ArrayList<>(); // the queue of unsent requests to the API
    private long lastRequestTime = 0;

    private ArrayList<Long> requestsPerMinute = new ArrayList<>(); // arrayList containing time of request. If request is older then a minute removed. Size of arrayList is requests in the last minute.



    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event){
        /* 1)
        - entity joins world and validated
        - Entity is added to request queue
         */

        if(Minecraft.getMinecraft().isSingleplayer()){ return; }

        if(!settings.isToggled()){ return; }

        if(!event.entity.isEntityAlive()){ return; }

        if (!(event.entity instanceof EntityPlayer)) { return; }

        if(playerLabelList.containsKey(event.entity)){ return; }

        for(Entity e : playerLabelList.keySet()){
            if(event.entity.getName().equalsIgnoreCase(e.getName())){
                return;
            }
        }
        if(!(event.entity instanceof EntityPlayer)) {
            return;
        }

        /*
        If server IP is hypixel (on entity join world) then send command /locraw
        - As long as it's been 3 seconds so the command isn't spammed if the player is moving between worlds quickly
        */
        if(Minecraft.getMinecraft().getCurrentServerData().serverIP.contains("hypixel")) {
            String eName = event.entity.getName();
            String pName = Minecraft.getMinecraft().thePlayer.getName();
            if (eName.equalsIgnoreCase(pName)) { //if the player is loaded into a new world
                long timeSinceLastLocraw = System.currentTimeMillis() - lastLocrawTime;
                if (timeSinceLastLocraw > 3000) { //if the time since last execution of /locraw is longer then 3 seconds, do /locraw again
                    Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C01PacketChatMessage("/locraw"));

                    lastLocrawTime = System.currentTimeMillis();
                }
            }
        }

        requestQueue.add(event.entity); // add entity to requestQueue so req can be sent every x time rather then all at once when the player loads

        requestsPerMinute.removeIf(t -> (System.currentTimeMillis() - t) > 60000f); // remove all occurrences of a request being made if it was made over 60 seconds ago
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick){
        /* 2)
        getPlayerData is called on the request queue:
        - if it has been longer then 400ms
        - if there are less then 60 requests that have been made in the last minute
         */

        long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime; // time since last request

        if(timeSinceLastRequest < 400){ // send request every 400 ms
            return;
        }

        if(requestsPerMinute.size() > 59) { // if less then 60 requests sent in last minute, all good but 'the API has a rate limit of 60 requests/minute'
            return;
        }

        if(requestQueue.isEmpty()){
            return;
        }

        getPlayerData(requestQueue.get(0)); // gets player data of oldest member of queue

        requestQueue.remove(requestQueue.get(0));
        lastRequestTime = System.currentTimeMillis();
    }


    private void getPlayerData(Entity e) {
        /* 3)
        - Runnable which fetches players stats through the hypixel API in a seperate thread
        - Then appends entity and string array containing above head stats to hashmap
        */

        new Thread(() -> {
            try {
                requestsPerMinute.add(System.currentTimeMillis()); // every time request is made, add the time of request to arraylist

                requestUtil requests = new requestUtil();
                JsonObject playerJson = requests.getJsonObject("https://api.slothpixel.me/api/players/" + e.getName()); // get JsonObject containing all data on player from slothpixel

                JsonObject statsObject = playerJson.getAsJsonObject("stats"); // get more specific stats JsonObject

                playerLabelList.put(e, statsObject); // appending target entity and string array to playerLabelList
            } catch (IOException ioException) {
                // Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(ioException.getMessage() + " " + requestsPerMinute.size()));
            }
        }).start();

    }



    @SubscribeEvent
    public void render(RenderWorldLastEvent evt) {

        if(Minecraft.getMinecraft().isSingleplayer()){ return; }

        if(!settings.isToggled()){ return; }

        if(!inDuels){ return; }

        if(Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness)){ return; }

        List<EntityPlayer> entityList = Minecraft.getMinecraft().theWorld.playerEntities;

        for(EntityPlayer e : entityList) {

            if(!playerLabelList.containsKey(e)){ // if the target isn't contained in the playerLabelList, then omit (return true)
                continue;
            }

            if(e.getDisplayName().getUnformattedText().contains("\u00A7" + "k")){ // §k is the obfuscation character
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


            JsonObject statsObject = playerLabelList.get(e);

            String gamemode = settings.getGamemode(); // which gamemode to show stats for e.g bridge or overall

            String[] lines = {"", "", ""};

            if(gamemode.equalsIgnoreCase("auto")){
                gamemode = currentGame; // everything kept in formatted form (e.g Skywars) as key, and when unformatted locraw output needed just use .get(FormattedForm)
            }

            if(gamemode.equalsIgnoreCase("overall")) {
                float duelsWL = APIUtil.getOverallWinLoss(statsObject);
                String roundedDuelsWL = round(duelsWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getOverallTitle(statsObject),
                        roundedDuelsWL,
                        ""
                };
            }
            else if(gamemode.equalsIgnoreCase("Bridge")){
                float bridgeWL = APIUtil.getBridgeWinLoss(statsObject);
                String roundedBridgeWL = round(bridgeWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getBridgeTitle(statsObject),
                        roundedBridgeWL,
                        ""
                };
            } else if(gamemode.equalsIgnoreCase("Skywars")){
                float skywarsWL = APIUtil.getSkywarsWinLoss(statsObject);
                String roundedSkywarsWL = round(skywarsWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getSkywarsTitle(statsObject),
                        roundedSkywarsWL,
                        ""
                };
            } else { // none of above edge cases so can use general methods
                float duelsWL = APIUtil.getSpecificWinLoss(statsObject, gamemode);
                String roundedDuelsWL = round(duelsWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getSpecificTitle(statsObject, gamemode),
                        roundedDuelsWL,
                        ""
                };
            }

            if(lines[0] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[0], 0, 0.6 + settings.getHeight(), 0, titles.getTitleColor(lines[0]), settings.isShadow());
            }
            if(lines[1] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[1], 0, 0.8 + settings.getHeight(), 0, titles.getTitleColor(lines[1]), settings.isShadow());
            }
            if(lines[2] != ""){
                renderUtil.renderLivingLabel(evt, e, lines[2], 0, 1 + settings.getHeight(), 0, titles.getTitleColor(lines[2]), settings.isShadow());
            }

        }

    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event){
        /*
        - Method that determines whether player is in a duels lobby/duels game through output of /locraw
        - If output of locraw has DUELS in it, inDuels can be made true.
         */

        if(Minecraft.getMinecraft().isSingleplayer()){
            return;
        }

        String message = event.message.getUnformattedText();

        if(Minecraft.getMinecraft().getCurrentServerData().serverIP.contains("hypixel")){ // if the sevrer is hypixel
            if(message.contains("\"server\"")){
                if(message.contains("DUELS")){
                    inDuels = true;

                    for (Map.Entry<String, String> entry : titles.gamemodes.entrySet()) {
                        String formattedTitle = entry.getKey(); // user input
                        String unformattedTitle = entry.getValue(); // locraw output
                        if(message.toLowerCase().contains(unformattedTitle.toLowerCase())){
                            currentGame = formattedTitle;
                            break;
                        }
                        currentGame = "overall";
                    }

                } else {
                    inDuels = false;
                }

                event.setCanceled(true);
                return;
            }
        } else{
            inDuels = true;
        }

    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e){
        /* When the world unloads, clear the cache */
        playerLabelList.clear();
        requestQueue.clear();
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

}