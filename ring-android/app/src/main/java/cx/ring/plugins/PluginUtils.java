package cx.ring.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cx.ring.daemon.Ringservice;
import cx.ring.settings.pluginssettings.PluginDetails;
import cx.ring.utils.Log;

import static android.content.Context.MODE_PRIVATE;

public class PluginUtils {

    public static final String TAG = PluginUtils.class.getSimpleName();

    public static final String PLUGIN_ENABLED = "enabled";

    /**
     * Fetches the plugins folder in the internal storage for plugins subfolder
     * Gathers the details of each plugin in a PluginDetails instance
     * @return List of PluginDetails
     */
    public static List<PluginDetails> listAvailablePlugins(Context mContext){
        tree(mContext.getFilesDir() + File.separator+ "plugins",0);
        tree(mContext.getCacheDir().getAbsolutePath(),0);

        List<PluginDetails> pluginsList = new ArrayList<>();

        List<String> pluginsPaths = Ringservice.listAvailablePlugins();

        for(String pluginPath : pluginsPaths) {
            File pluginFolder = new File(pluginPath);
            if(pluginFolder.isDirectory()){
                //We use the absolute path of a plugin as a preference name for uniqueness
                SharedPreferences sp = mContext.getSharedPreferences(
                        pluginFolder.getName(), MODE_PRIVATE);

                boolean enabled = sp.getBoolean(PLUGIN_ENABLED,false);

                pluginsList.add(new PluginDetails(
                        pluginFolder.getName(),
                        pluginFolder.getAbsolutePath(),enabled));
            }
        }
        return pluginsList;
    }

    public static String getABI(){
        return Build.SUPPORTED_ABIS[0];
    }

    /**
     * Checks if there is a file at the indicated path and converts if to a drawable if possible
     * @param iconPath String representing the absolute icon path
     * @return Drawable of the icon
     */
    public static Drawable getIcon(String iconPath) {
        Drawable icon = null;
        File file = new File(iconPath);
        Log.i(TAG, "Icon path: " + iconPath);
        if(file.exists()) {
            icon = Drawable.createFromPath(iconPath);
        }
        return icon;
    }

    /**
     * Loads the so file and instantiates the plugin init function (toggle on)
     * @param path root path of the plugin
     * @return true if loaded
     */
    public static boolean loadPlugin(String path) {
        return Ringservice.loadPlugin(path);
    }

    /**
     * Toggles the plugin off (destroying any objects created by the plugin)
     * then unloads the so file
     * @param path root path of the plugin
     * @return true if unloaded
     */
    public static boolean unloadPlugin(String path) {
        return Ringservice.unloadPlugin(path);
    }

    /**
     * Creates/Destroys plugin objects
     * @param path root path of the plugin
     * @param toggle boolean on/off
     */
    public static void togglePlugin(String path, boolean toggle) {
        Ringservice.togglePlugin(path, toggle);
    }

    /**
     * Lists the root paths of the loaded plugins
     * @return list of path
     */
    public static List<String> listLoadedPlugins() {
        return Ringservice.listLoadedPlugins();
    }

    /**
     * Displays the content of any directory
     * @param dirPath directory to display
     * @param level default 0, exists because the function is recursive
     */
    public static void tree(String dirPath,  int level) {
        String repeated = new String(new char[level]).replace("\0", "\t|");
        File file = new File(dirPath);
        if(file.exists()) {
            Log.d(TAG, "|"+ repeated + "-- " + file.getName());
            if(file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for(File f : files) {
                        tree(f.getAbsolutePath(),level+1);
                    }
                }
            }
        }
    }

    /**
     * Useful Util method that is available in for android api >=  24
     * We emulate it here
     *
     * @param input        input object that can be null
     * @param defaultValue default NonNull object of the same type as input
     * @return input if not null, defaultValue otherwise
     */
    public static <T> T getOrElse(T input, @NonNull T defaultValue) {
        if (input == null) {
            return defaultValue;
        } else {
            return input;
        }
    }

    /**
     *
     * @param listString List<String>
     * @return String of the form String entries = "[AAA,BBB,CCC]"
     */
    public static String listStringToStringList(List<String> listString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');

        if(!listString.isEmpty()) {
            for(int i=0; i< listString.size()-1; i++){
                stringBuilder.append(listString.get(i)).append(",");
            }
            stringBuilder.append(listString.get(listString.size()-1));
        }

        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    /**
     * Converts a string that contains a list to a java List<String>
     * E.g: String entries = "[AAA,BBB,CCC]" to List<String> l, where l.get(0) = "AAA"
     *
     * @param stringList a string in the form "[AAA,BBB,CCC]"
     */
    public static List<String> stringListToListString(String stringList) {
        List<String> listString = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        if(!stringList.isEmpty()) {
            for (int i = 1; i < stringList.length() - 1; i++) {
                char currentChar = stringList.charAt(i);
                if (currentChar != ',') {
                    currentWord.append(currentChar);
                } else {
                    listString.add(currentWord.toString());
                    currentWord = new StringBuilder();
                }

                if (i == stringList.length() - 2) {
                    listString.add(currentWord.toString());
                    break;
                }
            }
        }
        return listString;
    }
}
