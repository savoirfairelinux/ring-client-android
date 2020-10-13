package cx.ring.settings.pluginssettings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cx.ring.R;
import cx.ring.client.HomeActivity;
import cx.ring.daemon.Ringservice;
import cx.ring.databinding.FragPluginsListSettingsBinding;
import cx.ring.plugins.PluginUtils;
import cx.ring.utils.AndroidFileUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static cx.ring.plugins.PluginUtils.listAvailablePlugins;

public class PluginsListSettingsFragment extends Fragment implements PluginsListAdapter.PluginListItemListener {

    public static final String TAG = PluginsListSettingsFragment.class.getSimpleName();
    private static final int ARCHIVE_REQUEST_CODE = 42;

    private FragPluginsListSettingsBinding binding;
    private PluginsListAdapter mAdapter;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragPluginsListSettingsBinding.inflate(inflater, container, false);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding.pluginsList.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mAdapter = new PluginsListAdapter(listAvailablePlugins(binding.pluginsList.getContext()), this);
        binding.pluginsList.setAdapter(mAdapter);

        //Fab
        binding.pluginsListSettingsFab.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, ARCHIVE_REQUEST_CODE);
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        ((HomeActivity) requireActivity()).setToolbarTitle(R.string.menu_item_plugin_list);
        super.onResume();
    }

    /**
     * Implements PluginListItemListener.onPluginItemClicked which is called when we click on
     * a plugin list item
     * @param pluginDetails instance of a plugin details that is sent to PluginSettingsFragment
     */
    @Override
    public void onPluginItemClicked(PluginDetails pluginDetails) {
        ((HomeActivity) requireActivity()).gotToPluginSettings(pluginDetails);
    }

    /**
     * Implements PluginListItemListener.onPluginEnabled which is called when the checkbox
     * associated with the plugin list item is called
     * @param pluginDetails instance of a plugin details that is sent to PluginSettingsFragment
     */
    @Override
    public void onPluginEnabled(PluginDetails pluginDetails) {

        if(pluginDetails.isEnabled()) {
            pluginDetails.setEnabled(PluginUtils.loadPlugin(pluginDetails.getRootPath()));
        } else {
            PluginUtils.unloadPlugin(pluginDetails.getRootPath());
        }
        String status = pluginDetails.isEnabled()?"ON":"OFF";
        Toast.makeText(requireContext(), pluginDetails.getName() + " " + status,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ARCHIVE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    installPluginFromUri(uri, false);
                }
            }
         }
    }

    private String installPluginFile(File pluginFile, boolean force) throws IOException{
        int i = Ringservice.installPlugin(pluginFile.getAbsolutePath(), force);
        if (!pluginFile.delete()) {
            Log.e(TAG,"Plugin Jpl file in the cache not freed");
        }
        switch (i) {
            case 0:
                return pluginFile.getName();
            case 100:
                throw new IOException(getResources()
                        .getString(R.string.plugin_same_version_exception,
                                pluginFile.getName()));
            case 200:
                throw new IOException(getResources()
                        .getString(R.string.plugin_recent_version_exception,
                                pluginFile.getName()));
            default:
                throw new IOException(getResources()
                        .getString(R.string.plugin_install_failure,
                                pluginFile.getName()));
        }
    }

    private void installPluginFromUri(Uri uri, boolean force) {
        mCompositeDisposable.add(AndroidFileUtils.getCacheFile(requireContext(), uri)
                .observeOn(AndroidSchedulers.mainThread())
                .map(file -> installPluginFile(file, force))
                .subscribe(filename -> {
                        String[] plugin = filename.split(".jpl");
                        List<PluginDetails> availablePlugins = listAvailablePlugins(requireContext());
                        for (PluginDetails availablePlugin : availablePlugins) {
                            if (availablePlugin.getName().equals(plugin[0])) {
                                availablePlugin.setEnabled(true);
                                onPluginEnabled(availablePlugin);
                            }
                        }
                        mAdapter.updatePluginsList(listAvailablePlugins(requireContext()));
                        Toast.makeText(requireContext(), "Plugin: " + filename + " successfully installed", Toast.LENGTH_LONG).show();
                        mCompositeDisposable.dispose();
                    }, e -> {
                        if (binding != null) {
                            Snackbar sb = Snackbar.make(binding.listLayout, "" + e.getMessage(), Snackbar.LENGTH_LONG);
                            sb.setAction(R.string.plugin_force_install, v -> installPluginFromUri(uri, true));
                            sb.show();
                        }
                    }));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
