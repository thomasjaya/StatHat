package stathat.util;


import java.io.*;
import java.lang.reflect.Type;

import com.google.gson.Gson;

public class fileUtil {
    /*
    - Methods for reading/writing to JSON files
    - Used for modifying settings file.
     */

    private static Gson gson = new Gson();

    private static File ROOT_DIR = new File("StatHat"); //making directory in root (e,g where screenshots folder is)

    public static void init(){
        if(!ROOT_DIR.exists()) { //if root directory doesnt exist
            ROOT_DIR.mkdirs();
        }
    }

    public static Gson getGson() {
        return gson;
    }

    public static File getRootDir() {
        return ROOT_DIR;
    }

    public static boolean writeJsonToFile(File file, Object obj) { // write object to file (usually ArrayList<ConfigElement> in our case)
        try {
            if(!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(gson.toJson(obj).getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T extends Object> T readFromJson(File file, Class<T> c){ // read JSON to object from Class
        try {

            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder builder = new StringBuilder();
            String line;

            while((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();

            return gson.fromJson(builder.toString(), c);

        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    public static <T extends Object> T readFromJsonByType(File file, Type type){ // read JSON to object from type
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder builder = new StringBuilder();
            String line;

            while((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();

            return gson.fromJson(builder.toString(), type);

        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }

    }





}






