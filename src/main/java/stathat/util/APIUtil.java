package stathat.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import stathat.dictionaries.titles;

public class APIUtil {

    /* Class containing methods which extract specific details from a slothpixel stats JsonObject e.g bridge win/loss */

    public static float getSpecificWinLoss(JsonObject stats, String title){
        JsonObject gameStats = stats.getAsJsonObject("Duels").getAsJsonObject("gamemodes").getAsJsonObject(title); // gets x gamemode object

        int gameWins = (gameStats.getAsJsonPrimitive("wins") == null) ? 0 : gameStats.getAsJsonPrimitive("wins").getAsInt();
        gameWins += (gameWins == 0) ? 1 : 0;

        int gameLosses = (gameStats.getAsJsonPrimitive("losses") == null) ? 0 : gameStats.getAsJsonPrimitive("losses").getAsInt();
        gameLosses += (gameLosses == 0) ? 1 : 0;

        float specificWL = (float)gameWins/gameLosses;

        return specificWL;
    }

    public static float getOverallWinLoss(JsonObject stats){
        /* Method for extracting duels win/loss from stats object*/
        JsonPrimitive jsonWL = stats.getAsJsonObject("Duels").getAsJsonObject("general").getAsJsonPrimitive("win_loss_ratio");
        float WL = (jsonWL == null) ? 0f : jsonWL.getAsFloat();
        return WL;
    }


    public static float getBridgeWinLoss(JsonObject stats){
        /* Method for extracting bridge duels win/loss*/

        JsonObject bridgeJson = stats.getAsJsonObject("Duels").getAsJsonObject("gamemodes").getAsJsonObject("bridge"); // gets bridge gamemode object

        int bridgeWins = 0;
        bridgeWins += (bridgeJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins").getAsInt();
        bridgeWins += (bridgeJson.getAsJsonObject("duels").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("duels").getAsJsonPrimitive("wins").getAsInt();
        bridgeWins += (bridgeJson.getAsJsonObject("2v2v2v2").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("2v2v2v2").getAsJsonPrimitive("wins").getAsInt();
        bridgeWins += (bridgeJson.getAsJsonObject("3v3v3v3").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("3v3v3v3").getAsJsonPrimitive("wins").getAsInt(); // total bridge wins on all bridge subsets
        bridgeWins += (bridgeWins == 0) ? 1 : 0; // to prevent zero division

        int bridgeLosses = 0;
        bridgeLosses += (bridgeJson.getAsJsonObject("doubles").getAsJsonPrimitive("losses") == null) ? 0 : bridgeJson.getAsJsonObject("doubles").getAsJsonPrimitive("losses").getAsInt();
        bridgeLosses += (bridgeJson.getAsJsonObject("duels").getAsJsonPrimitive("losses") == null) ? 0 : bridgeJson.getAsJsonObject("duels").getAsJsonPrimitive("losses").getAsInt();
        bridgeLosses += (bridgeJson.getAsJsonObject("2v2v2v2").getAsJsonPrimitive("losses") == null) ? 0 : bridgeJson.getAsJsonObject("2v2v2v2").getAsJsonPrimitive("losses").getAsInt();
        bridgeLosses += (bridgeJson.getAsJsonObject("3v3v3v3").getAsJsonPrimitive("losses") == null) ? 0 : bridgeJson.getAsJsonObject("3v3v3v3").getAsJsonPrimitive("losses").getAsInt(); // total bridge wins on all bridge subsets
        bridgeLosses += (bridgeLosses == 0) ? 1 : 0; // to prevent zero division

        float winLoss = (float)bridgeWins/bridgeLosses;

        return winLoss;
    }

    public static float getSkywarsWinLoss(JsonObject stats){
        JsonObject skywarsJson = stats.getAsJsonObject("Duels").getAsJsonObject("gamemodes").getAsJsonObject("skywars");

        int skywarsWins = 0;
        skywarsWins += (skywarsJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins") == null) ? 0 : skywarsJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins").getAsInt();
        skywarsWins += (skywarsJson.getAsJsonObject("duels").getAsJsonPrimitive("wins") == null) ? 0 : skywarsJson.getAsJsonObject("duels").getAsJsonPrimitive("wins").getAsInt();
        skywarsWins += (skywarsWins == 0) ? 1 : 0; // to prevent zero division

        int skywarsLosses = 0;
        skywarsLosses += (skywarsJson.getAsJsonObject("doubles").getAsJsonPrimitive("losses") == null) ? 0 : skywarsJson.getAsJsonObject("doubles").getAsJsonPrimitive("losses").getAsInt();
        skywarsLosses += (skywarsJson.getAsJsonObject("duels").getAsJsonPrimitive("losses") == null) ? 0 : skywarsJson.getAsJsonObject("duels").getAsJsonPrimitive("losses").getAsInt();
        skywarsLosses += (skywarsLosses == 0) ? 1 : 0; // to prevent zero division

        float skywarsWL = (float)skywarsWins/skywarsLosses;
        return skywarsWL;
    }


    public static String getBestSpecificTitle(JsonObject stats, String title){
        JsonObject gameStats = stats.getAsJsonObject("Duels").getAsJsonObject("gamemodes").getAsJsonObject(title); // gets x gamemode object

        int gameWins = (gameStats.getAsJsonPrimitive("wins") == null) ? 0 : gameStats.getAsJsonPrimitive("wins").getAsInt();

        String formattedTitle = toTitleCase(title.replace("_", " ")); //classic_duel -> Classic Duel

        for(int wins : titles.titles.descendingKeySet()){
            if(wins < Math.round(gameWins)){
                return formattedTitle + " " + titles.titles.get(wins);
            }
        }
        return formattedTitle + " " + "Rookie";
    }


    public static String getBestOverallTitle(JsonObject stats){
        JsonPrimitive jsonWins = stats.getAsJsonObject("Duels").getAsJsonObject("general").getAsJsonPrimitive("wins");

        int totalWins = (jsonWins == null) ? 0 : jsonWins.getAsInt(); // a players total wins

        for(int wins : titles.titles.descendingKeySet()){
            if(wins < Math.round(totalWins/2)){ // for overall title you need double the wins
                return titles.titles.get(wins);
            }
        }
        return "Rookie";
    }

    public static String getBestBridgeTitle(JsonObject stats){
        JsonObject bridgeJson = stats.getAsJsonObject("Duels").getAsJsonObject("gamemodes").getAsJsonObject("bridge"); // gets bridge gamemode object

        int bridgeWins = 0;
        bridgeWins += (bridgeJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins").getAsInt();
        bridgeWins += (bridgeJson.getAsJsonObject("duels").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("duels").getAsJsonPrimitive("wins").getAsInt();
        bridgeWins += (bridgeJson.getAsJsonObject("2v2v2v2").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("2v2v2v2").getAsJsonPrimitive("wins").getAsInt();
        bridgeWins += (bridgeJson.getAsJsonObject("3v3v3v3").getAsJsonPrimitive("wins") == null) ? 0 : bridgeJson.getAsJsonObject("3v3v3v3").getAsJsonPrimitive("wins").getAsInt(); // total bridge wins on all bridge subsets

        for(int wins : titles.titles.descendingKeySet()){
            if(wins < Math.round(bridgeWins)){
                return "Bridge " + titles.titles.get(wins);
            }
        }
        return "Bridge Rookie";
    }

    public static String getBestSkywarsTitle(JsonObject stats){
        JsonObject skywarsJson = stats.getAsJsonObject("Duels").getAsJsonObject("gamemodes").getAsJsonObject("skywars");

        int skywarsWins = 0;
        skywarsWins += (skywarsJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins") == null) ? 0 : skywarsJson.getAsJsonObject("doubles").getAsJsonPrimitive("wins").getAsInt();
        skywarsWins += (skywarsJson.getAsJsonObject("duels").getAsJsonPrimitive("wins") == null) ? 0 : skywarsJson.getAsJsonObject("duels").getAsJsonPrimitive("wins").getAsInt();

        for(int wins : titles.titles.descendingKeySet()){
            if(wins < Math.round(skywarsWins)){
                return "Skywars " + titles.titles.get(wins);
            }
        }
        return "Skywars Rookie";
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
