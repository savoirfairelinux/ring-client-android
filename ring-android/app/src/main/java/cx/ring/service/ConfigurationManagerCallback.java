/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * Copyright (C) 2004-2016 Savoir-faire Linux Inc.
 *
 * Author: Alexandre Savard <alexandre.savard@savoirfairelinux.com>
 * Author: Adrien Béraud <adrien.beraud@savoirfairelinux.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cx.ring.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cx.ring.BuildConfig;

class ConfigurationManagerCallback extends ConfigurationCallback {

    private static final String TAG = ConfigurationManagerCallback.class.getSimpleName();

    static public final String ACCOUNTS_CHANGED = BuildConfig.APPLICATION_ID + "accounts.changed";
    static public final String ACCOUNTS_DEVICES_CHANGED = BuildConfig.APPLICATION_ID + "accounts.devicesChanged";
    static public final String ACCOUNTS_EXPORT_ENDED = BuildConfig.APPLICATION_ID + "accounts.exportEnded";
    static public final String ACCOUNT_STATE_CHANGED = BuildConfig.APPLICATION_ID + "account.stateChanged";
    static public final String INCOMING_TEXT = BuildConfig.APPLICATION_ID + ".message.incomingTxt";
    static public final String MESSAGE_STATE_CHANGED = BuildConfig.APPLICATION_ID + ".message.stateChanged";
    static public final String NAME_LOOKUP_ENDED = BuildConfig.APPLICATION_ID + ".name.lookupEnded";
    static public final String NAME_REGISTRATION_ENDED = BuildConfig.APPLICATION_ID + ".name.registrationEnded";

    static public final String MESSAGE_STATE_CHANGED_EXTRA_ID = "id";
    static public final String MESSAGE_STATE_CHANGED_EXTRA_STATUS = "status";

    private final DRingService mService;

    public ConfigurationManagerCallback(DRingService context) {
        super();
        mService = context;
    }

    @Override
    public void volumeChanged(String device, int value) {
        super.volumeChanged(device, value);
    }

    @Override
    public void accountsChanged() {
        super.accountsChanged();
        Intent intent = new Intent(ACCOUNTS_CHANGED);
        mService.sendBroadcast(intent);
    }

    @Override
    public void stunStatusFailure(String account_id) {
        Log.d(TAG, "configOnStunStatusFail : (" + account_id);
    }

    @Override
    public void registrationStateChanged(String account_id, String state, int code, String detail_str) {
        Log.w(getClass().getName(), "registrationStateChanged: " + account_id + " " + state + " " + code + " " + detail_str);
        sendAccountStateChangedMessage(account_id, state, code);
    }

    @Override
    public void incomingAccountMessage(String accountID, String from, StringMap messages) {
        String msg = null;
        final String textPlainMime = "text/plain";
        if (null != messages && messages.has_key(textPlainMime)) {
            msg = messages.getRaw(textPlainMime).toJavaString();
        }
        if (msg == null)
            return;

        Log.w(TAG, "incomingAccountMessage : " + accountID + " " + from + " " + msg);

        Intent intent = new Intent(INCOMING_TEXT);
        intent.putExtra("txt", msg);
        intent.putExtra("from", from);
        intent.putExtra("account", accountID);
        mService.sendBroadcast(intent);
    }

    @Override
    public void accountMessageStatusChanged(String id, long messageId, String to, int status) {
        Log.d(TAG, "accountMessageStatusChanged " + messageId + " " + status);
        Intent intent = new Intent(MESSAGE_STATE_CHANGED);
        intent.putExtra(MESSAGE_STATE_CHANGED_EXTRA_ID, messageId);
        intent.putExtra(MESSAGE_STATE_CHANGED_EXTRA_STATUS, status);
        mService.sendBroadcast(intent);
    }

    @Override
    public void errorAlert(int alert) {
        Log.d(TAG, "errorAlert : " + alert);
    }

    private void sendAccountStateChangedMessage(String account, String state, int code) {
        Intent intent = new Intent(ACCOUNT_STATE_CHANGED);
        intent.putExtra("account", account);
        intent.putExtra("state", state);
        intent.putExtra("code", code);
        mService.sendBroadcast(intent);
    }

    @Override
    public void getHardwareAudioFormat(IntVect ret) {
        OpenSlParams audioParams = OpenSlParams.createInstance(mService);
        ret.add(audioParams.getSampleRate());
        ret.add(audioParams.getBufferSize());
        Log.d(TAG, "getHardwareAudioFormat: " + audioParams.getSampleRate() + " " + audioParams.getBufferSize());
    }

    @Override
    public void getAppDataPath(String name, StringVect ret) {
        switch (name) {
            case "files":
                ret.add(mService.getFilesDir().getAbsolutePath());
                break;
            case "cache":
                ret.add(mService.getCacheDir().getAbsolutePath());
                break;
            default:
                ret.add(mService.getDir(name, Context.MODE_PRIVATE).getAbsolutePath());
                break;
        }
    }

    @Override
    public void knownDevicesChanged(String account, StringMap devices) {
        Intent intent = new Intent(ACCOUNTS_DEVICES_CHANGED);
        intent.putExtra("account", account);
        intent.putExtra("devices", devices.toNative());
        mService.sendBroadcast(intent);
    }

    @Override
    public void exportOnRingEnded(String account, int code, String pin) {
        Log.w(TAG, "exportOnRingEnded: " + account + " " + code + " " + pin);
        Intent intent = new Intent(ACCOUNTS_EXPORT_ENDED);
        intent.putExtra("account", account);
        intent.putExtra("code", code);
        intent.putExtra("pin", pin);
        mService.sendBroadcast(intent);
    }

    @Override
    public void nameRegistrationEnded(String account, int state, String name) {
        Log.w(TAG, "nameRegistrationEnded: " + account + " " + state + " " + name);
        Intent intent = new Intent(NAME_REGISTRATION_ENDED);
        intent.putExtra("account", account);
        intent.putExtra("state", state);
        intent.putExtra("name", name);
        mService.sendBroadcast(intent);
    }

    @Override
    public void registeredNameFound(String account, int state, String address, String name) {
        Log.w(TAG, "registeredNameFound: " + account + " " + state + " " + name + " " + address);
        Intent intent = new Intent(NAME_LOOKUP_ENDED);
        intent.putExtra("account", account);
        intent.putExtra("state", state);
        intent.putExtra("name", name);
        intent.putExtra("address", address);
        mService.sendBroadcast(intent);
    }

}
