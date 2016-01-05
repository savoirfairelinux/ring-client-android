/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * Copyright (C) 2004-2014 Savoir-Faire Linux Inc.
 *
 *  Author: Regis Montoya <r3gis.3R@gmail.com>
 *  Author: Emeric Vigier <emeric.vigier@savoirfairelinux.com>
 *          Alexandre Lision <alexandre.lision@savoirfairelinux.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cx.ring.service;

import android.app.Notification;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.telecom.Call;
import android.util.Log;

import cx.ring.BuildConfig;
import cx.ring.R;
import cx.ring.history.HistoryManager;
import cx.ring.history.HistoryText;
import cx.ring.model.Codec;
import cx.ring.model.Conference;
import cx.ring.model.SecureSipCall;
import cx.ring.model.TextMessage;
import cx.ring.model.account.AccountDetail;
import cx.ring.model.account.AccountDetailBasic;
import cx.ring.model.account.AccountDetailSrtp;
import cx.ring.model.account.AccountDetailTls;
import cx.ring.utils.MediaManager;
import cx.ring.utils.SipNotifications;
import cx.ring.utils.SwigNativeConverter;
import cx.ring.model.SipCall;


public class DRingService extends Service {

    static final String TAG = "DRingService";
    private SipServiceExecutor mExecutor;
    private static HandlerThread executorThread;

    static public final String DRING_CONNECTION_CHANGED = BuildConfig.APPLICATION_ID + ".event.DRING_CONNECTION_CHANGE";

    private Handler handler = new Handler();
    private static int POLLING_TIMEOUT = 50;
    private Runnable pollEvents = new Runnable() {
        @Override
        public void run() {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Ringservice.pollEvents();
                }
            });
            handler.postDelayed(this, POLLING_TIMEOUT);
        }
    };
    private boolean isPjSipStackStarted = false;

    protected HistoryManager mHistoryManager;

    private ConfigurationManagerCallback configurationCallback;
    private CallManagerCallBack callManagerCallBack;

    @Override
    public boolean onUnbind(Intent i) {
        super.onUnbind(i);
        Log.i(TAG, "onUnbind(intent)");
        return true;
    }

    @Override
    public void onRebind(Intent i) {
        super.onRebind(i);
    }

    /* called once by startService() */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreated");
        super.onCreate();

        getExecutor().execute(new StartRunnable());

        mHistoryManager = new HistoryManager(this);
    }

    /* called for each startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand " + (intent == null ? "null" : intent.getAction()) + " " + flags + " " + startId);
        return START_STICKY; /* started and stopped explicitly */
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        getExecutor().execute(new FinalizeRunnable());
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBound");
        return mBinder;
    }

    private static Looper createLooper() {
        if (executorThread == null) {
            Log.d(TAG, "Creating new handler thread");
            // ADT gives a fake warning due to bad parse rule.
            executorThread = new HandlerThread("DRingService.Executor");
            executorThread.start();
        }
        return executorThread.getLooper();
    }

    public SipServiceExecutor getExecutor() {
        // create mExecutor lazily
        if (mExecutor == null) {
            mExecutor = new SipServiceExecutor();
        }
        return mExecutor;
    }

    // Executes immediate tasks in a single executorThread.
    public static class SipServiceExecutor extends Handler {

        SipServiceExecutor() {
            super(createLooper());
        }

        public void execute(Runnable task) {
            // TODO: add wakelock
            Message.obtain(SipServiceExecutor.this, 0/* don't care */, task).sendToTarget();
            //Log.w(TAG, "SenT!");
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Runnable) {
                executeInternal((Runnable) msg.obj);
            } else {
                Log.w(TAG, "can't handle msg: " + msg);
            }
        }

        private void executeInternal(Runnable task) {
            try {
                task.run();
            } catch (Throwable t) {
                Log.e(TAG, "run task: " + task, t);
            }
        }

        public final boolean executeSynced(final Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("runnable must not be null");
            }
            if (Looper.myLooper() == getLooper()) {
                r.run();
                return true;
            }

            BlockingRunnable br = new BlockingRunnable(r);
            return br.postAndWait(this, 0);
        }
        public final <T> T executeAndReturn(final SipRunnableWithReturn<T> r) {
            if (r == null) {
                throw new IllegalArgumentException("runnable must not be null");
            }
            if (Looper.myLooper() == getLooper()) {
                r.run();
                return r.getVal();
            }

            BlockingRunnable br = new BlockingRunnable(r);
            if (!br.postAndWait(this, 0))
                throw new RuntimeException("Can't execute runnable");
            return r.getVal();
        }

        private static final class BlockingRunnable implements Runnable {
            private final Runnable mTask;
            private boolean mDone;

            public BlockingRunnable(Runnable task) {
                mTask = task;
            }

            @Override
            public void run() {
                try {
                    mTask.run();
                } catch(Exception e){
                    e.printStackTrace();
                } finally {
                    synchronized (this) {
                        mDone = true;
                        notifyAll();
                    }
                }
            }

            public boolean postAndWait(Handler handler, long timeout) {
                if (!handler.post(this)) {
                    return false;
                }

                synchronized (this) {
                    if (timeout > 0) {
                        final long expirationTime = SystemClock.uptimeMillis() + timeout;
                        while (!mDone) {
                            long delay = expirationTime - SystemClock.uptimeMillis();
                            if (delay <= 0) {
                                return false; // timeout
                            }
                            try {
                                wait(delay);
                            } catch (InterruptedException ex) {
                            }
                        }
                    } else {
                        while (!mDone) {
                            try {
                                wait();
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                }
                return true;
            }
        }
    }

    private void stopDaemon() {
        handler.removeCallbacks(pollEvents);
        if (isPjSipStackStarted) {
            Ringservice.fini();
            isPjSipStackStarted = false;
            Log.i(TAG, "PjSIPStack stopped");
            Intent intent = new Intent(DRING_CONNECTION_CHANGED);
            intent.putExtra("connected", isPjSipStackStarted);
            sendBroadcast(intent);
        }
    }

    private void startPjSipStack() throws SameThreadException {
        if (isPjSipStackStarted)
            return;

        try {
            System.loadLibrary("ringjni");
            isPjSipStackStarted = true;

        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Problem with the current Pj stack...", e);
            isPjSipStackStarted = false;
            return;
        } catch (Exception e) {
            Log.e(TAG, "Problem with the current Pj stack...", e);
            isPjSipStackStarted = false;
        }

        configurationCallback = new ConfigurationManagerCallback(this);
        callManagerCallBack = new CallManagerCallBack(this);
        Ringservice.init(configurationCallback, callManagerCallBack);
        handler.postDelayed(pollEvents, POLLING_TIMEOUT);
        Log.i(TAG, "PjSIPStack started");
        Intent intent = new Intent(DRING_CONNECTION_CHANGED);
        intent.putExtra("connected", isPjSipStackStarted);
        sendBroadcast(intent);
    }

    // Enforce same thread contract to ensure we do not call from somewhere else
    public class SameThreadException extends Exception {
        private static final long serialVersionUID = -905639124232613768L;

        public SameThreadException() {
            super("Should be launched from a single worker thread");
        }
    }

    public abstract static class SipRunnable implements Runnable {
        protected abstract void doRun() throws SameThreadException, RemoteException;

        @Override
        public void run() {
            try {
                doRun();
            } catch (SameThreadException e) {
                Log.e(TAG, "Not done from same thread");
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public abstract class SipRunnableWithReturn<T> implements Runnable {
        private T obj = null;

        protected abstract T doRun() throws SameThreadException, RemoteException;

        public T getVal() {
            return obj;
        }

        @Override
        public void run() {
            try {
                if (isPjSipStackStarted)
                    obj = doRun();
                else
                    Log.e(TAG, "Can't perform operation: daemon not started.");
                //done = true;
            } catch (SameThreadException e) {
                Log.e(TAG, "Not done from same thread");
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    class StartRunnable extends SipRunnable {
        @Override
        protected void doRun() throws SameThreadException {
            startPjSipStack();
        }
    }

    class FinalizeRunnable extends SipRunnable {
        @Override
        protected void doRun() throws SameThreadException {
            stopDaemon();
        }
    }

    /* ************************************
     *
     * Implement public interface for the service
     *
     * *********************************
     */

    protected final IDRingService.Stub mBinder = new IDRingService.Stub() {

        @Override
        public String placeCall(final String account, final String number) {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<String>() {
                @Override
                protected String doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.placeCall() thread running... " + number);
                    return Ringservice.placeCall(account, number);
                }
            });
        }

        @Override
        public void refuse(final String callID) {
            Log.e(TAG, "REFUSE");
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.refuse() thread running...");
                    Ringservice.refuse(callID);
                    Ringservice.hangUp(callID);
                }
            });
        }

        @Override
        public void accept(final String callID) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.accept() thread running...");
                    Ringservice.accept(callID);
                }
            });
        }

        @Override
        public void hangUp(final String callID) {
            Log.e(TAG, "HANGING UP " + callID);
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.hangUp() thread running...");
                    Ringservice.hangUp(callID);
                }
            });
        }

        @Override
        public void hold(final String callID) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.hold() thread running...");
                    Ringservice.hold(callID);
                }
            });
        }

        @Override
        public void unhold(final String callID) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.unhold() thread running...");
                    Ringservice.unhold(callID);
                }
            });
        }

        @Override
        public boolean isStarted() throws RemoteException {
            return isPjSipStackStarted;
        }

        @Override
        public Map<String, String> getCallDetails(final String callID) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCallDetails() thread running...");
                    return Ringservice.getCallDetails(callID).toNative();
                }
            });
        }

        @Override
        public void setAudioPlugin(final String audioPlugin) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.setAudioPlugin() thread running...");
                    Ringservice.setAudioPlugin(audioPlugin);
                }
            });
        }

        @Override
        public String getCurrentAudioOutputPlugin() {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<String>() {
                @Override
                protected String doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCurrentAudioOutputPlugin() thread running...");
                    return Ringservice.getCurrentAudioOutputPlugin();
                }
            });
        }

        @Override
        public List<String> getAccountList() {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<List<String>>() {
                @Override
                protected List<String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getAccountList() thread running...");
                    return new ArrayList<>(Ringservice.getAccountList());
                }
            });
        }

        @Override
        public void setAccountOrder(final String order) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.setAccountsOrder() thread running...");
                    Ringservice.setAccountsOrder(order);
                }
            });
        }

        @Override
        public Map<String, String> getAccountDetails(final String accountID) {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getAccountDetails() thread running...");
                    return Ringservice.getAccountDetails(accountID).toNative();
                }
            });
        }

        @SuppressWarnings("unchecked")
        // Hashmap runtime cast
        @Override
        public void setAccountDetails(final String accountId, final Map map) {
            Log.i(TAG, "DRingService.setAccountDetails() " + map.get("Account.hostname"));
            final StringMap swigmap = StringMap.toSwig(map);

            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {

                    Ringservice.setAccountDetails(accountId, swigmap);
                    Log.i(TAG, "DRingService.setAccountDetails() thread running... " + swigmap.get("Account.hostname"));
                }

            });
        }

        @Override
        public void setAccountActive(final String accountId, final boolean active) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.setAccountActive() thread running... " + accountId + " -> " + active);
                    Ringservice.setAccountActive(accountId, active);
                }
            });
        }

        @Override
        public void setAccountsActive(final boolean active) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.setAccountsActive() thread running... " + active);
                    StringVect list = Ringservice.getAccountList();
                    for (int i=0, n=list.size(); i<n; i++)
                        Ringservice.setAccountActive(list.get(i), active);
                }
            });
        }

        @Override
        public Map<String, String> getVolatileAccountDetails(final String accountId) {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getVolatileAccountDetails() thread running...");
                    return Ringservice.getVolatileAccountDetails(accountId).toNative();
                }
            });
        }

        @Override
        public Map<String, String> getAccountTemplate(final String accountType) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getAccountTemplate() thread running...");
                    return Ringservice.getAccountTemplate(accountType).toNative();
                }
            });
        }

        @SuppressWarnings("unchecked")
        // Hashmap runtime cast
        @Override
        public String addAccount(final Map map) {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<String>() {
                @Override
                protected String doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.addAccount() thread running...");
                    return Ringservice.addAccount(StringMap.toSwig(map));
                }
            });
        }

        @Override
        public void removeAccount(final String accountId) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.setAccountDetails() thread running...");
                    Ringservice.removeAccount(accountId);
                }
            });
        }

        /*************************
         * Transfer related API
         *************************/

        @Override
        public void transfer(final String callID, final String to) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.transfer() thread running...");
                    if (Ringservice.transfer(callID, to)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("CallID", callID);
                        bundle.putString("State", "HUNGUP");
                        Intent intent = new Intent(CallManagerCallBack.CALL_STATE_CHANGED);
                        intent.putExtra("com.savoirfairelinux.sflphone.service.newstate", bundle);
                        sendBroadcast(intent);
                    } else
                        Log.i(TAG, "NOT OK");
                }
            });

        }

        @Override
        public void attendedTransfer(final String transferID, final String targetID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.attendedTransfer() thread running...");
                    if (Ringservice.attendedTransfer(transferID, targetID)) {
                        Log.i(TAG, "OK");
                    } else
                        Log.i(TAG, "NOT OK");
                }
            });

        }

        /*************************
         * Conference related API
         *************************/

        @Override
        public void removeConference(final String confID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.createConference() thread running...");
                    Ringservice.removeConference(confID);
                }
            });

        }

        @Override
        public void joinParticipant(final String sel_callID, final String drag_callID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.joinParticipant() thread running...");
                    Ringservice.joinParticipant(sel_callID, drag_callID);
                    // Generate a CONF_CREATED callback
                }
            });
            Log.i(TAG, "After joining participants");
        }

        @Override
        public void addParticipant(final String callID, final String confID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.addParticipant() thread running...");
                    Ringservice.addParticipant(callID, confID);
                }
            });

        }

        @Override
        public void addMainParticipant(final String confID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.addMainParticipant() thread running...");
                    Ringservice.addMainParticipant(confID);
                }
            });

        }

        @Override
        public void detachParticipant(final String callID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.detachParticipant() thread running... " + callID);
                    Ringservice.detachParticipant(callID);
                }
            });

        }

        @Override
        public void joinConference(final String sel_confID, final String drag_confID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.joinConference() thread running...");
                    Ringservice.joinConference(sel_confID, drag_confID);
                }
            });

        }

        @Override
        public void hangUpConference(final String confID) throws RemoteException {
            Log.e(TAG, "HANGING UP CONF");
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.hangUpConference() thread running...");
                    Ringservice.hangUpConference(confID);
                }
            });

        }

        @Override
        public void holdConference(final String confID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.holdConference() thread running...");
                    Ringservice.holdConference(confID);
                }
            });

        }

        @Override
        public void unholdConference(final String confID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.unholdConference() thread running...");
                    Ringservice.unholdConference(confID);
                }
            });

        }

        @Override
        public boolean isConferenceParticipant(final String callID) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Boolean>() {
                @Override
                protected Boolean doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.isRecording() thread running...");
                    return Ringservice.isConferenceParticipant(callID);
                }
            });
        }

        @Override
        public Map<String, ArrayList<String>> getConferenceList() throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, ArrayList<String>>>() {
                @Override
                protected Map<String, ArrayList<String>> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getConferenceList() thread running...");
                    StringVect call_ids = Ringservice.getCallList();
                    HashMap<String, ArrayList<String>> confs = new HashMap<>(call_ids.size());
                    for (int i=0; i<call_ids.size(); i++) {
                        String call_id = call_ids.get(i);
                        String conf_id = Ringservice.getConferenceId(call_id);
                        if (conf_id == null || conf_id.isEmpty())
                            conf_id = call_id;
                        ArrayList<String> calls = confs.get(conf_id);
                        if (calls == null) {
                            calls = new ArrayList<>();
                            confs.put(conf_id, calls);
                        }
                        calls.add(call_id);
                    }
                    return confs;
                }
            });
        }

        @Override
        public List<String> getParticipantList(final String confID) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<List<String>>() {
                @Override
                protected List<String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getParticipantList() thread running...");
                    return new ArrayList<>(Ringservice.getParticipantList(confID));
                }
            });
        }

        @Override
        public String getConferenceId(String callID) throws RemoteException {
            Log.e(TAG, "getConferenceId not implemented");
            return Ringservice.getConferenceId(callID);
        }

        @Override
        public String getConferenceDetails(final String callID) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<String>() {
                @Override
                protected String doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getConferenceDetails() thread running...");
                    return Ringservice.getConferenceDetails(callID).get("CONF_STATE");
                }
            });
        }

        @Override
        public String getRecordPath() throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<String>() {
                @Override
                protected String doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getRecordPath() thread running...");
                    return Ringservice.getRecordPath();
                }
            });
        }

        @Override
        public boolean toggleRecordingCall(final String id) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Boolean>() {
                @Override
                protected Boolean doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.toggleRecordingCall() thread running...");
                    return Ringservice.toggleRecording(id);
                }
            });
        }

        @Override
        public boolean startRecordedFilePlayback(final String filepath) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.setRecordingCall() thread running...");
                    Ringservice.startRecordedFilePlayback(filepath);
                }
            });
            return false;
        }

        @Override
        public void stopRecordedFilePlayback(final String filepath) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.stopRecordedFilePlayback() thread running...");
                    Ringservice.stopRecordedFilePlayback(filepath);
                }
            });
        }

        @Override
        public void setRecordPath(final String path) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.setRecordPath() " + path + " thread running...");
                    Ringservice.setRecordPath(path);
                }
            });
        }

        @Override
        public void sendTextMessage(final String callID, final TextMessage message) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.sendTextMessage() thread running...");
                    message.setCallId(callID);
                    mHistoryManager.insertNewTextMessage(new HistoryText(message));
                    StringMap messages  = new StringMap();
                    messages.set("text/plain", message.getMessage());
                    Ringservice.sendTextMessage(callID, messages, "", false);
                    Intent intent = new Intent(ConfigurationManagerCallback.INCOMING_TEXT);
                    intent.putExtra("txt", message);
                    sendBroadcast(intent);
                }
            });
        }

        @Override
        public void sendAccountTextMessage(final String accountID, final String to, final String msg) {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.sendAccountTextMessage() thread running... " + accountID + " " + to + " " + msg);
                    TextMessage message = new TextMessage(false, msg, to, null, accountID);
                    mHistoryManager.insertNewTextMessage(new HistoryText(message));
                    StringMap msgs = new StringMap();
                    msgs.set("text/plain", msg);
                    Ringservice.sendAccountTextMessage(accountID, to, msgs);
                    Intent intent = new Intent(ConfigurationManagerCallback.INCOMING_TEXT);
                    intent.putExtra("txt", message);
                    sendBroadcast(intent);
                }
            });
        }

        @Override
        public List<Codec> getCodecList(final String accountID) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<ArrayList<Codec>>() {
                @Override
                protected ArrayList<Codec> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCodecList() thread running...");
                    ArrayList<Codec> results = new ArrayList<>();

                    UintVect active_payloads = Ringservice.getActiveCodecList(accountID);
                    for (int i = 0; i < active_payloads.size(); ++i) {
                        Log.i(TAG, "DRingService.getCodecDetails(" + accountID +", "+ active_payloads.get(i) +")");
                        results.add(new Codec(active_payloads.get(i), Ringservice.getCodecDetails(accountID, active_payloads.get(i)), true));

                    }
                    UintVect payloads = Ringservice.getCodecList();

                    cl : for (int i = 0; i < payloads.size(); ++i) {
                        for (Codec co : results)
                            if (co.getPayload() == payloads.get(i))
                                continue cl;
                        StringMap details = Ringservice.getCodecDetails(accountID, payloads.get(i));
                        if (details.size() > 1)
                            results.add(new Codec(payloads.get(i), details, false));
                        else
                            Log.i(TAG, "Error loading codec " + i);
                    }
                    return results;
                }
            });
        }

        /*
        @Override
        public Map getRingtoneList() throws RemoteException {
            class RingtoneList extends SipRunnableWithReturn {

                @Override
                protected StringMap doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getRingtoneList() thread running...");
                    return Ringservice.getR();
                }
            }

            RingtoneList runInstance = new RingtoneList();
            getExecutor().execute(runInstance);
            while (!runInstance.isDone()) {
            }
            StringMap ringtones = (StringMap) runInstance.getVal();

            for (int i = 0; i < ringtones.size(); ++i) {
                // Log.i(TAG,"ringtones "+i+" "+ ringtones.);
            }

            return null;
        }


        @Override
        public boolean checkForPrivateKey(final String pemPath) throws RemoteException {
            class hasPrivateKey extends SipRunnableWithReturn {

                @Override
                protected Boolean doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.isCaptureMuted() thread running...");
                    return Ringservice.sflph_config_check_for_private_key(pemPath);
                }
            }

            hasPrivateKey runInstance = new hasPrivateKey();
            getExecutor().execute(runInstance);
            while (!runInstance.isDone()) {
            }

            return (Boolean) runInstance.getVal();
        }

        @Override
        public boolean checkCertificateValidity(final String pemPath) throws RemoteException {
            class isValid extends SipRunnableWithReturn {

                @Override
                protected Boolean doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.isCaptureMuted() thread running...");
                    return Ringservice.sflph_config_check_certificate_validity(pemPath, pemPath);
                }
            }

            isValid runInstance = new isValid();
            getExecutor().execute(runInstance);
            while (!runInstance.isDone()) {
            }

            return (Boolean) runInstance.getVal();
        }

        @Override
        public boolean checkHostnameCertificate(final String certificatePath, final String host, final String port) throws RemoteException {
            class isValid extends SipRunnableWithReturn {

                @Override
                protected Boolean doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.isCaptureMuted() thread running...");
                    return Ringservice.sflph_config_check_hostname_certificate(host, port);
                }
            }

            isValid runInstance = new isValid();
            getExecutor().execute(runInstance);
            while (!runInstance.isDone()) {
            }

            return (Boolean) runInstance.getVal();
        }
*/

        @Override
        public Map<String, String> validateCertificatePath(final String accountID, final String certificatePath, final String privateKeyPath, final String privateKeyPass) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.validateCertificatePath() thread running...");
                    return Ringservice.validateCertificatePath(accountID, certificatePath, privateKeyPath, "", "").toNative();
                }
            });
        }

        @Override
        public Map<String, String> validateCertificate(final String accountID, final String certificate) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.validateCertificate() thread running...");
                    return Ringservice.validateCertificate(accountID, certificate).toNative();
                }
            });
        }

        @Override
        public Map<String, String> getCertificateDetailsPath(final String certificatePath) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCertificateDetailsPath() thread running...");
                    return Ringservice.getCertificateDetails(certificatePath).toNative();
                }
            });
        }

        @Override
        public Map<String, String> getCertificateDetails(final String certificateRaw) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCertificateDetails() thread running...");
                    return Ringservice.getCertificateDetails(certificateRaw).toNative();
                }
            });
        }

        @Override
        public void setActiveCodecList(final List codecs, final String accountID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.setActiveAudioCodecList() thread running...");
                    UintVect list = new UintVect();
                    for (Object codec : codecs) {
                        list.add((Long) codec);
                    }
                    Ringservice.setActiveCodecList(accountID, list);
                }
            });
        }

        @Override
        public void playDtmf(final String key) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.playDtmf() thread running...");
                    Ringservice.playDTMF(key);
                }
            });
        }

        @Override
        public Map<String, String> getConference(final String id) throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Map<String, String>>() {
                @Override
                protected Map<String, String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCredentials() thread running...");
                    return Ringservice.getConferenceDetails(id).toNative();
                }
            });
        }

        @Override
        public void setMuted(final boolean mute) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.setMuted() thread running...");
                    Ringservice.muteCapture(mute);
                }
            });
        }

        @Override
        public boolean isCaptureMuted() throws RemoteException {
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<Boolean>() {

                @Override
                protected Boolean doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.isCaptureMuted() thread running...");
                    return Ringservice.isCaptureMuted();
                }
            });
        }

        @Override
        public void confirmSAS(final String callID) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.confirmSAS() thread running...");
                    Ringservice.setSASVerified(callID);
                }
            });
        }


        @Override
        public List<String> getTlsSupportedMethods(){
            return getExecutor().executeAndReturn(new SipRunnableWithReturn<List<String>>() {
                @Override
                protected List<String> doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCredentials() thread running...");
                    return SwigNativeConverter.convertSwigToNative(Ringservice.getSupportedTlsMethod());
                }
            });
        }

        @Override
        public List getCredentials(final String accountID) throws RemoteException {
            class Credentials extends SipRunnableWithReturn {

                @Override
                protected List doRun() throws SameThreadException {
                    Log.i(TAG, "DRingService.getCredentials() thread running...");
                    return Ringservice.getCredentials(accountID).toNative();
                }
            }

            Credentials runInstance = new Credentials();
            getExecutor().executeSynced(runInstance);
            return (List) runInstance.getVal();
        }

        @Override
        public void setCredentials(final String accountID, final List creds) throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.setCredentials() thread running...");
                    Ringservice.setCredentials(accountID, SwigNativeConverter.convertFromNativeToSwig(creds));
                }
            });
        }

        @Override
        public void registerAllAccounts() throws RemoteException {
            getExecutor().execute(new SipRunnable() {
                @Override
                protected void doRun() throws SameThreadException, RemoteException {
                    Log.i(TAG, "DRingService.registerAllAccounts() thread running...");
                    Ringservice.registerAllAccounts();
                }
            });
        }

    };
}
