/*
 *  Copyright (C) 2016 Savoir-faire Linux Inc.
 *
 *  Author: Thibault Wittemberg <thibault.wittemberg@savoirfairelinux.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package cx.ring.services;

import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import cx.ring.daemon.Callback;
import cx.ring.daemon.IntegerMap;
import cx.ring.daemon.Ringservice;
import cx.ring.daemon.StringMap;
import cx.ring.model.DaemonEvent;
import cx.ring.utils.FutureUtils;
import cx.ring.utils.Log;

public class CallService extends Observable {

    private final static String TAG = CallService.class.getName();

    @Inject
    ExecutorService mExecutor;

    Callback mCallbackHandler;

    public CallService(DaemonService daemonService) {
        mCallbackHandler = new CallbackHandler();
    }

    public Callback getCallbackHandler() {
        return mCallbackHandler;
    }

    public String placeCall(final String account, final String number, final boolean video) {

        Future<String> result = mExecutor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Log.i(TAG, "placeCall() thread running... " + number + " video: " + video);
                String callId = Ringservice.placeCall(account, number);
                if (!video) {
                    Ringservice.muteLocalMedia(callId, "MEDIA_TYPE_VIDEO", true);
                }
                return callId;
            }
        });

        return FutureUtils.getFutureResult(result);
    }

    public void refuse(final String callID) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "refuse() thread running...");
                Ringservice.refuse(callID);
                Ringservice.hangUp(callID);
            }
        });
    }

    public void accept(final String callID) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "accept() thread running...");
                Ringservice.accept(callID);
            }
        });
    }

    public void hangUp(final String callID) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "hangUp() thread running...");
                Ringservice.hangUp(callID);
            }
        });
    }

    public void hold(final String callID) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "hold() thread running...");
                Ringservice.hold(callID);
            }
        });
    }

    public void unhold(final String callID) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "unhold() thread running...");
                Ringservice.unhold(callID);
            }
        });
    }


    public Map<String, String> getCallDetails(final String callID) {

        Future<Map<String, String>> result = mExecutor.submit(new Callable<Map<String, String>>() {
            @Override
            public Map<String, String> call() throws Exception {
                Log.i(TAG, "getCallDetails() thread running...");
                return Ringservice.getCallDetails(callID).toNative();
            }
        });

        return FutureUtils.getFutureResult(result);
    }

    public void muteRingTone(boolean mute) {
        Log.d(TAG, (mute ? "Muting." : "Unmuting.") + " ringtone.");
        Ringservice.muteRingtone(mute);
    }

    public void setAudioPlugin(final String audioPlugin) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "setAudioPlugin() thread running...");
                Ringservice.setAudioPlugin(audioPlugin);
            }
        });
    }

    public String getCurrentAudioOutputPlugin() {

        Future<String> result = mExecutor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Log.i(TAG, "getCurrentAudioOutputPlugin() thread running...");
                return Ringservice.getCurrentAudioOutputPlugin();
            }
        });

        return FutureUtils.getFutureResult(result);
    }

    protected class CallbackHandler extends Callback {

        @Override
        public void callStateChanged(String callId, String newState, int detailCode) {
            Log.d(TAG, "call state changed: " + callId + ", " + newState + ", " + detailCode);
            setChanged();
            DaemonEvent event = new DaemonEvent(DaemonEvent.EventType.CALL_STATE_CHANGED);
            event.addEventInput(DaemonEvent.EventInput.CALL_ID, callId);
            event.addEventInput(DaemonEvent.EventInput.STATE, newState);
            event.addEventInput(DaemonEvent.EventInput.DETAIL_CODE, detailCode);
            notifyObservers(event);
        }

        @Override
        public void incomingCall(String accountId, String callId, String from) {
            Log.d(TAG, "incoming call: " + accountId + ", " + callId + ", " + from);
            setChanged();
            DaemonEvent event = new DaemonEvent(DaemonEvent.EventType.INCOMING_CALL);
            event.addEventInput(DaemonEvent.EventInput.CALL_ID, callId);
            event.addEventInput(DaemonEvent.EventInput.ACCOUNT_ID, accountId);
            event.addEventInput(DaemonEvent.EventInput.FROM, from);
            notifyObservers(event);
        }

        @Override
        public void conferenceCreated(final String confId) {
            Log.d(TAG, "conference created: " + confId);
            setChanged();
            DaemonEvent event = new DaemonEvent(DaemonEvent.EventType.CONFERENCE_CREATED);
            event.addEventInput(DaemonEvent.EventInput.CONF_ID, confId);
            notifyObservers(event);
        }

        @Override
        public void incomingMessage(String callId, String from, StringMap messages) {
            Log.d(TAG, "incoming message: " + callId + ", " + from);
            setChanged();
            DaemonEvent event = new DaemonEvent(DaemonEvent.EventType.INCOMING_MESSAGE);
            event.addEventInput(DaemonEvent.EventInput.CALL_ID, callId);
            event.addEventInput(DaemonEvent.EventInput.FROM, from);
            event.addEventInput(DaemonEvent.EventInput.MESSAGES, messages);
            notifyObservers(event);
        }

        @Override
        public void conferenceRemoved(String confId) {
            Log.d(TAG, "conference removed: " + confId);
            setChanged();
            DaemonEvent event = new DaemonEvent(DaemonEvent.EventType.CONFERENCE_REMOVED);
            event.addEventInput(DaemonEvent.EventInput.CONF_ID, confId);
            notifyObservers(event);
        }

        @Override
        public void conferenceChanged(String confId, String state) {
            Log.d(TAG, "conference changed: " + confId + ", " + state);
            setChanged();
            DaemonEvent event = new DaemonEvent(DaemonEvent.EventType.CONFERENCE_CHANGED);
            event.addEventInput(DaemonEvent.EventInput.CONF_ID, confId);
            event.addEventInput(DaemonEvent.EventInput.STATE, state);
            notifyObservers(event);
        }

        @Override
        public void recordPlaybackFilepath(String id, String filename) {
            Log.d(TAG, "record playback filepath: " + id + ", " + filename);
            // todo needs more explainations on that
        }

        @Override
        public void onRtcpReportReceived(String callId, IntegerMap stats) {
            Log.i(TAG, "on RTCP report received: " + callId);
            setChanged();
            DaemonEvent event = new DaemonEvent(DaemonEvent.EventType.CONFERENCE_CHANGED);
            event.addEventInput(DaemonEvent.EventInput.CALL_ID, callId);
            event.addEventInput(DaemonEvent.EventInput.STATS, stats);
            notifyObservers(event);
        }
    }
}
