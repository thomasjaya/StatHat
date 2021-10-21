package stathat;

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
    private long lastLocrawTime = 0; // the time /locraw was last executed
    private boolean inDuels = true;

    //private static Map<Entity, String[]> playerLabelList = new HashMap<>();
    private static Map<Entity, JsonObject> playerLabelList = new HashMap<>();

    UserSettings settings = generateConfig.settings;


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
        /*
        - Runnable which fetches players stats through the hypixel API in a seperate thread
        - Then appends entity and string array containing above head stats to hashmap
        */
        new Thread(() -> {
            try {
                requestUtil requests = new requestUtil();
                JsonObject playerJson = requests.getJsonObject("https://api.slothpixel.me/api/players/" + e.getName()); // get JsonObject containing all data on player from slothpixel

                JsonObject statsObject = playerJson.getAsJsonObject("stats"); // get more specific stats JsonObject

                playerLabelList.put(e, statsObject); // appending target entity and string array to playerLabelList

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }).start();
    }

    private boolean shouldRender(){ // should render all stathats?
        if(Minecraft.getMinecraft().isSingleplayer()){
            return false;
        }

        if(!settings.isToggled()){
            return false;
        }

        if(!inDuels){
            return false;
        }

        if(Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness)){
            return false;
        }

        return true;
    }

    private boolean omitRender(EntityPlayer e){ // should skip on rendering x players stathat (e.g if they're invisible or shifted)
        if(!playerLabelList.containsKey(e)){ // if the target isn't contained in the playerLabelList, then omit (return true)
            return true;
        }

        if(e.getDisplayName().getUnformattedText().contains("\u00A7" + "k")){ // §k is the obfuscation character
            return true;
        }

        if(e.isSneaking()){
            return true;
        }

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if(!settings.isPersonal() && e == thePlayer){
            return true;
        }

        if(e.isPotionActive(Potion.invisibility)){
            return true;
        }

        return false;
    }



    @SubscribeEvent
    public void render(RenderWorldLastEvent evt) { //RenderWorldLastEvent RenderPlayerEvent

        if(!shouldRender()){
            return;
        }


        List<EntityPlayer> entityList = Minecraft.getMinecraft().theWorld.playerEntities;

        for(EntityPlayer e : entityList) {

            if(omitRender(e)){
                continue;
            }

            JsonObject statsObject = playerLabelList.get(e);

            String gamemode = settings.getGamemode(); // which gamemode to show stats for e.g bridge or overall

            String[] lines = {"", "", ""};

            if(gamemode.equalsIgnoreCase("overall")) {
                float duelsWL = APIUtil.getOverallWinLoss(statsObject);
                String roundedDuelsWL = round(duelsWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getBestOverallTitle(statsObject),
                        roundedDuelsWL,
                        ""
                };
            }
            else if(gamemode.equalsIgnoreCase("bridge")){
                float bridgeWL = APIUtil.getBridgeWinLoss(statsObject);
                String roundedBridgeWL = round(bridgeWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getBestBridgeTitle(statsObject),
                        roundedBridgeWL,
                        ""
                };
            } else if(gamemode.equalsIgnoreCase("skywars")){
                float skywarsWL = APIUtil.getSkywarsWinLoss(statsObject);
                String roundedSkywarsWL = round(skywarsWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getBestSkywarsTitle(statsObject),
                        roundedSkywarsWL,
                        ""
                };
            } else { // none of above edge cases so can use general methods
                float duelsWL = APIUtil.getSpecificWinLoss(statsObject, gamemode);
                String roundedDuelsWL = round(duelsWL, 2) + " W/L";

                lines = new String[]{
                        APIUtil.getBestSpecificTitle(statsObject, gamemode),
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

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }



}