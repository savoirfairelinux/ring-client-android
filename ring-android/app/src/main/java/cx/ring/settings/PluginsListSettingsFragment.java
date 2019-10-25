package cx.ring.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cx.ring.R;
import cx.ring.client.HomeActivity;
import cx.ring.utils.Log;

public class PluginsListSettingsFragment extends Fragment implements PluginsListAdapter.PluginListItemListener {

    public static final String TAG = PluginsListSettingsFragment.class.getSimpleName();
    private Context mContext;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContext = requireActivity();
        View pluginsSettingsList = inflater.inflate(R.layout.frag_plugins_list_settings, container, false);
        recyclerView = pluginsSettingsList.findViewById(R.id.plugins_list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)

        mAdapter = new PluginsListAdapter(listPlugins(), this);
        recyclerView.setAdapter(mAdapter);

        return pluginsSettingsList;
    }

    /**
     * Fetches the plugins folder in the internal storage for plugins subfolder
     * Gathers the details of each plugin in a PluginDetails instance
     * @return
     */
    private List<PluginDetails> listPlugins(){
        List<PluginDetails> pluginsList = new ArrayList<>();
        File pluginsDir = new File(mContext.getFilesDir(),"plugins");
        Log.i(TAG, "Plugins dir: " + pluginsDir.getAbsolutePath());
        if(pluginsDir.isDirectory()) {
            File[] pluginsFolders = pluginsDir.listFiles();
            if(pluginsFolders != null) {
                for(File pluginFolder : pluginsFolders) {
                    if(pluginFolder.isDirectory()){
                        pluginsList.add(new PluginDetails(
                                pluginFolder.getName(),
                                pluginFolder.getAbsolutePath(),false));
                    }
                }
            }
        }
        return pluginsList;
    }

    /**
     * Implements PluginListItemListener.onPluginItemClicked which is called when we click on
     * a plugin list item
     * @param pluginDetails instance of a plugin details that is sent to PluginSettingsFragment
     */
    @Override
    public void onPluginItemClicked(PluginDetails pluginDetails) {
        ((HomeActivity) mContext).gotToPluginSettings(pluginDetails);
    }

    /**
     * Implements PluginListItemListener.onPluginEnabled which is called when the checkbox
     * associated with the plugin list item is called
     * @param pluginDetails instance of a plugin details that is sent to PluginSettingsFragment
     */
    @Override
    public void onPluginEnabled(PluginDetails pluginDetails) {
        Toast.makeText(mContext,pluginDetails.getName() + " " + pluginDetails.isEnabled(),Toast.LENGTH_SHORT).show();
    }
}
