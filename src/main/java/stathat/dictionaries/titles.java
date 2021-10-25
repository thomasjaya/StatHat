package stathat.dictionaries;

import java.awt.*;
import java.util.*;

public class titles {

    /*
    Hashmap containing titles and associated number of wins
     */
    public static TreeMap<Integer, String> titles = new TreeMap<Integer, String>() {{
        put(50, "Rookie");
        put(60, "Rookie II");
        put(70, "Rookie III");
        put(80, "Rookie IV");
        put(90, "Rookie V");
        put(100, "Iron");
        put(130, "Iron II");
        put(160, "Iron III");
        put(190, "Iron IV");
        put(220, "Iron V");
        put(250, "Gold");
        put(300, "Gold II");
        put(350, "Gold III");
        put(400, "Gold IV");
        put(450, "Gold V");
        put(500, "Diamond");
        put(600, "Diamond II");
        put(700, "Diamond III");
        put(800, "Diamond IV");
        put(900, "Diamond V");
        put(1000, "Master");
        put(1200, "Master II");
        put(1400, "Master III");
        put(1600, "Master IV");
        put(1800, "Master V");
        put(2000, "Legend");
        put(2600, "Legend II");
        put(3200, "Legend III");
        put(3800, "Legend IV");
        put(4400, "Legend V");
        put(5000, "Grandmaster");
        put(6000, "Grandmaster II");
        put(7000, "Grandmaster III");
        put(8000, "Grandmaster IV");
        put(9000, "Grandmaster V");
        put(10000, "Godlike");
        put(12000, "Godlike II");
        put(14000, "Godlike III");
        put(16000, "Godlike IV");
        put(18000, "Godlike V");
        put(20000, "Godlike VI");
        put(22000, "Godlike VII");
        put(24000, "Godlike VIII");
        put(26000, "Godlike IX");
        put(28000, "Godlike X");
    }};


    public static LinkedHashMap<String, String> gamemodes = new LinkedHashMap<String, String>(){{  // list of accepted gamemodes that stathat can display (linkedhashmap so ordered in insertion order)
        put("Bridge", "bridge"); // Key: Formatted user entered title, Value: unformatted locraw output
        put("Classic", "classic_duel");
        put("Sumo", "sumo");
        put("Skywars", "sw");
        put("Combo", "combo_duel");
        put("OP", "op_duel");
        put("Nodebuff", "potion_duel");
        put( "Blitz", "blitz_duel");
        put("Overall", "overall"); // overall best title and overall best W/L
        put("Auto", "auto"); // automatically displays stats of game currently playing
    }};

    public static Color getTitleColor(String title){ // returning duels title colours from title name
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

}
