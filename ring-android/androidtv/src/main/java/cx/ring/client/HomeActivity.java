/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cx.ring.client;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

import javax.inject.Inject;

import cx.ring.R;
import cx.ring.application.RingApplication;
import cx.ring.model.ServiceEvent;
import cx.ring.model.Settings;
import cx.ring.services.AccountService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.HardwareService;
import cx.ring.services.PreferencesService;
import cx.ring.utils.Log;
import cx.ring.utils.Observable;
import cx.ring.utils.Observer;

/*
 * MainActivity class that loads MainFragment
 */

public class HomeActivity extends Activity implements Observer<ServiceEvent> {
    private static final String TAG = HomeActivity.class.getName();

    private boolean mNoAccountOpened = false;

    public static final int REQUEST_CODE_PHOTO = 5;
    public static final int REQUEST_CODE_GALLERY = 6;
    public static final int REQUEST_PERMISSION_CAMERA = 113;
    public static final int REQUEST_PERMISSION_READ_STORAGE = 114;

    private boolean mIsAskingForPermissions = false;

    @Inject
    AccountService mAccountService;

    @Inject
    DeviceRuntimeService mDeviceRuntimeService;

    @Inject
    PreferencesService mPreferencesService;

    @Inject
    HardwareService mHardwareService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // dependency injection
        ((RingApplication) getApplication()).getRingInjectionComponent().inject(this);

        String[] toRequest = buildPermissionsToAsk();
        ArrayList<String> permissionsWeCanAsk = new ArrayList<>();

        for (String permission : toRequest) {
            if (((RingApplication) getApplication()).canAskForPermission(permission)) {
                permissionsWeCanAsk.add(permission);
            }
        }

        if (!permissionsWeCanAsk.isEmpty()) {
            mIsAskingForPermissions = true;
            ActivityCompat.requestPermissions(this, permissionsWeCanAsk.toArray(new String[permissionsWeCanAsk.size()]), RingApplication.PERMISSIONS_REQUEST);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAccountService.addObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAccountService.removeObserver(this);
    }

    public void loadAccounts() {
        if (!mNoAccountOpened && mAccountService.getAccounts().isEmpty() && !mIsAskingForPermissions) {
            mNoAccountOpened = true;
            Log.d(TAG, "No account found");
            startActivityForResult(new Intent(HomeActivity.this, WizardActivity.class), WizardActivity.ACCOUNT_CREATE_REQUEST);
        }
        else {
            Intent intent = new Intent(HomeActivity.this, CallActivity.class);
            intent.putExtra("account", mAccountService.getCurrentAccount().getAccountID());
            startActivity(intent);
        }
    }

    private String[] buildPermissionsToAsk() {
        ArrayList<String> perms = new ArrayList<>();

        if (!mDeviceRuntimeService.hasAudioPermission()) {
            perms.add(Manifest.permission.RECORD_AUDIO);
        }

        Settings settings = mPreferencesService.loadSettings();

        if (settings.isAllowSystemContacts() && !mDeviceRuntimeService.hasContactPermission()) {
            perms.add(Manifest.permission.READ_CONTACTS);
        }

        if (!mDeviceRuntimeService.hasVideoPermission()) {
            perms.add(Manifest.permission.CAMERA);
        }

        if (settings.isAllowPlaceSystemCalls() && !mDeviceRuntimeService.hasCallLogPermission()) {
            perms.add(Manifest.permission.WRITE_CALL_LOG);
        }

        return perms.toArray(new String[perms.size()]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        android.util.Log.d(TAG, "onRequestPermissionsResult");

        switch (requestCode) {
            case RingApplication.PERMISSIONS_REQUEST: {
                if (grantResults.length == 0) {
                    return;
                }
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                for (int i = 0, n = permissions.length; i < n; i++) {
                    String permission = permissions[i];
                    ((RingApplication) getApplication()).permissionHasBeenAsked(permission);
                    switch (permission) {
                        case Manifest.permission.RECORD_AUDIO:
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                android.util.Log.e(TAG, "Missing required permission RECORD_AUDIO");
                                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                        .setTitle(R.string.start_error_title)
                                        .setMessage(R.string.start_error_mic_required)
                                        .setIcon(R.drawable.ic_mic_black)
                                        .setCancelable(false)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                finish();
                                            }
                                        });
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    });
                                }
                                builder.show();
                                return;
                            }
                            break;
                        case Manifest.permission.READ_CONTACTS:
                            sharedPref.edit().putBoolean(getString(R.string.pref_systemContacts_key), grantResults[i] == PackageManager.PERMISSION_GRANTED).apply();
                            break;
                        case Manifest.permission.CAMERA:
                            sharedPref.edit().putBoolean(getString(R.string.pref_systemCamera_key), grantResults[i] == PackageManager.PERMISSION_GRANTED).apply();
                            // permissions have changed, video params should be reset
                            final boolean isVideoAllowed = mDeviceRuntimeService.hasVideoPermission();
                            if (isVideoAllowed) {
                                mHardwareService.initVideo();
                            }
                    }
                }

                break;
            }
            case REQUEST_PERMISSION_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_CODE_GALLERY);
                } else {
                    return;
                }
                break;
            case REQUEST_PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CODE_PHOTO);
                } else {
                    return;
                }
                break;
        }

        mIsAskingForPermissions = false;
        loadAccounts();
    }

    @Override
    public void update(Observable observable, ServiceEvent event) {
        if (event == null) {
            return;
        }
        Log.d(TAG, "Event : " + event.getEventType());
        switch (event.getEventType()) {
            case ACCOUNTS_CHANGED:
                loadAccounts();
                break;
            default:
                android.util.Log.d(TAG, "Event " + event.getEventType() + " is not handled here");
                break;
        }
    }
}