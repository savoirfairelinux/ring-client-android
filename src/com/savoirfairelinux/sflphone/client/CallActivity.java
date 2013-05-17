/*
 *  Copyright (C) 2004-2012 Savoir-Faire Linux Inc.
 *
 *  Author: Alexandre Savard <alexandre.savard@savoirfairelinux.com>
 *  Author: Adrien Béraud <adrien.beraud@savoirfairelinux.com>
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
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  If you modify this program, or any covered work, by linking or
 *  combining it with the OpenSSL project's OpenSSL library (or a
 *  modified version of that library), containing parts covered by the
 *  terms of the OpenSSL or SSLeay licenses, Savoir-Faire Linux Inc.
 *  grants you additional permission to convey the resulting work.
 *  Corresponding Source for a non-source form of such a combination
 *  shall include the source code for the parts of OpenSSL used as well
 *  as that of the covered work.
 */

package com.savoirfairelinux.sflphone.client;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.savoirfairelinux.sflphone.R;
import com.savoirfairelinux.sflphone.adapters.ContactPictureLoader;
import com.savoirfairelinux.sflphone.client.receiver.CallListReceiver;
import com.savoirfairelinux.sflphone.model.Attractor;
import com.savoirfairelinux.sflphone.model.Bubble;
import com.savoirfairelinux.sflphone.model.BubbleModel;
import com.savoirfairelinux.sflphone.model.BubblesView;
import com.savoirfairelinux.sflphone.model.CallContact;
import com.savoirfairelinux.sflphone.model.CallContact.Phone;
import com.savoirfairelinux.sflphone.model.SipCall;
import com.savoirfairelinux.sflphone.service.ISipClient;
import com.savoirfairelinux.sflphone.service.ISipService;
import com.savoirfairelinux.sflphone.service.SipService;

public class CallActivity extends Activity //implements IncomingCallFragment.ICallActionListener, OngoingCallFragment.ICallActionListener //OnClickListener
{
	static final String TAG = "CallActivity";
	private ISipService service;
	private String pendingAction;
	private SipCall mCall;

	private BubblesView view;
	private BubbleModel model;
	private PointF screenCenter;
	private DisplayMetrics metrics;

	private HashMap<Bubble, CallContact> contacts = new HashMap<Bubble, CallContact>();

	private ExecutorService infos_fetcher = Executors.newCachedThreadPool();

	public interface CallFragment
	{
		void setCall(SipCall c);
	}
	/*
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String signalName = intent.getStringExtra(CallManagerCallBack.SIGNAL_NAME);
			Log.d(TAG, "Signal received: " + signalName);

			if (signalName.equals(CallManagerCallBack.NEW_CALL_CREATED)) {
			} else if (signalName.equals(CallManagerCallBack.CALL_STATE_CHANGED)) {
				processCallStateChangedSignal(intent);
			} else if (signalName.equals(CallManagerCallBack.INCOMING_CALL)) {
			}
		}
	};
	 */
	private ISipClient callback = new ISipClient.Stub() {

		@Override
		public void incomingCall(Intent call) throws RemoteException {
			Log.i(TAG, "Incoming call transfered from Service");
			SipCall.CallInfo infos = new SipCall.CallInfo(call);
			SipCall c = new SipCall(infos);
			//
		}

		@Override
		public void callStateChanged(Intent callState) throws RemoteException {
			Bundle b = callState.getBundleExtra("com.savoirfairelinux.sflphone.service.newstate");
			String cID = b.getString("CallID");
			String state = b.getString("State");
			Log.i(TAG, "callStateChanged" + cID + "    " + state);
			processCallStateChangedSignal(cID, state);
		}

		@Override
		public void incomingText(Intent msg) throws RemoteException {
			Bundle b = msg.getBundleExtra("com.savoirfairelinux.sflphone.service.newtext");
			b.getString("CallID");
			String from = b.getString("From");
			String mess = b.getString("Msg");
			Toast.makeText(getApplicationContext(), "text from "+from+" : " + mess , Toast.LENGTH_LONG).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bubbleview_layout);

		model = new BubbleModel();
		metrics = getResources().getDisplayMetrics();
		screenCenter = new PointF(metrics.widthPixels / 2, metrics.heightPixels / 3);
		//radiusCalls = metrics.widthPixels / 2 - 150;
		// model.listBubbles.add(new Bubble(this, metrics.widthPixels / 2, metrics.heightPixels / 4, 150, R.drawable.me));
		// model.listBubbles.add(new Bubble(this, metrics.widthPixels / 2, metrics.heightPixels / 4 * 3, 150, R.drawable.callee));

		view = (BubblesView) findViewById(R.id.main_view);
		view.setModel(model);


		Bundle b = getIntent().getExtras();
		// Parcelable value = b.getParcelable("CallInfo");
		//		SipCall.CallInfo info = b.getParcelable("CallInfo");
		//		Log.i(TAG, "Starting activity for call " + info.mCallID);
		//		mCall = new SipCall(info);
		//
		Intent intent = new Intent(this, SipService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		//setCallStateDisplay(mCall.getCallStateString());

		String action = b.getString("action");
		if(action.equals("call")) {
			CallContact contact = b.getParcelable("CallContact");

			SipCall.CallInfo info = new SipCall.CallInfo();
			Random random = new Random();
			String callID = Integer.toString(random.nextInt());
			Phone phone = contact.getSipPhone();

			info.mCallID = callID;
			info.mAccountID = ""+contact.getId();
			info.mDisplayName = contact.getmDisplayName();
			info.mPhone = phone==null?null:phone.toString();
			info.mEmail = contact.getmEmail();
			info.mCallType = SipCall.CALL_TYPE_OUTGOING;

			mCall = CallListReceiver.getCallInstance(info);
			//mCallbacks.onCallSelected(call);

			pendingAction = action;

			/*	try {
				service.placeCall(info.mAccountID, info.mCallID, info.mPhone);
			} catch (RemoteException e) {
				Log.e(TAG, "Cannot call service method", e);
			}*/

			callContact(contact);
		} else if(action.equals("incoming")) {

		}

		/*
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(CallManagerCallBack.NEW_CALL_CREATED));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(CallManagerCallBack.CALL_STATE_CHANGED));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(CallManagerCallBack.INCOMING_CALL));
		 */
	}

	private void callContact(final CallContact contact) {
		// TODO off-thread image loading
		Bubble contact_bubble;
		if(contact.getPhoto_id() > 0) {
			Bitmap photo = ContactPictureLoader.loadContactPhoto(getContentResolver(), contact.getPhoto_id());
			contact_bubble = new Bubble(this, screenCenter.x, screenCenter.y, 150, photo);
		} else {
			contact_bubble = new Bubble(this, screenCenter.x, screenCenter.y, 150, R.drawable.ic_contact_picture);
		}

		model.attractors.clear();
		model.attractors.add(new Attractor(new PointF(metrics.widthPixels/2, metrics.heightPixels*.8f), new Attractor.Callback() {
			@Override
			public void onBubbleSucked(Bubble b)
			{
				Log.w(TAG, "Bubble sucked ! ");
				onCallEnded();
			}
		}));

		model.listBubbles.add(contact_bubble);
	}

	private void callIncoming() {
		model.attractors.clear();
		model.attractors.add(new Attractor(new PointF(3*metrics.widthPixels/4, metrics.heightPixels/4), new Attractor.Callback() {
			@Override
			public void onBubbleSucked(Bubble b)
			{
				onCallAccepted();
			}
		}));
		model.attractors.add(new Attractor(new PointF(metrics.widthPixels/4, metrics.heightPixels/4), new Attractor.Callback() {
			@Override
			public void onBubbleSucked(Bubble b)
			{
				onCallRejected();
			}
		}));

	}

	@Override
	protected void onDestroy()
	{
		Log.i(TAG, "Destroying Call Activity for call " + mCall.getCallId());
		//LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		unbindService(mConnection);
		super.onDestroy();
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder)
		{
			service = ISipService.Stub.asInterface(binder);
			try {
				service.registerClient(callback);
			} catch (RemoteException e) {
				Log.e(TAG, e.toString());
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0)
		{
		}
	};

	private void processCallStateChangedSignal(String callID, String newState)
	{
		/*Bundle bundle = intent.getBundleExtra("com.savoirfairelinux.sflphone.service.newstate");
		String callID = bundle.getString("CallID");
		String newState = bundle.getString("State");*/

		if (newState.equals("INCOMING")) {
			mCall.setCallState(SipCall.CALL_STATE_INCOMING);
			setCallStateDisplay(newState);
		} else if (newState.equals("RINGING")) {
			mCall.setCallState(SipCall.CALL_STATE_RINGING);
			setCallStateDisplay(newState);
		} else if (newState.equals("CURRENT")) {
			mCall.setCallState(SipCall.CALL_STATE_CURRENT);
			setCallStateDisplay(newState);
		} else if (newState.equals("HUNGUP")) {
			mCall.setCallState(SipCall.CALL_STATE_HUNGUP);
			setCallStateDisplay(newState);
			finish();
		} else if (newState.equals("BUSY")) {
			mCall.setCallState(SipCall.CALL_STATE_BUSY);
			setCallStateDisplay(newState);
		} else if (newState.equals("FAILURE")) {
			mCall.setCallState(SipCall.CALL_STATE_FAILURE);
			setCallStateDisplay(newState);
		} else if (newState.equals("HOLD")) {
			mCall.setCallState(SipCall.CALL_STATE_HOLD);
			setCallStateDisplay(newState);
		} else if (newState.equals("UNHOLD")) {
			mCall.setCallState(SipCall.CALL_STATE_CURRENT);
			setCallStateDisplay("CURRENT");
		} else {
			mCall.setCallState(SipCall.CALL_STATE_NONE);
			setCallStateDisplay(newState);
		}

		Log.w(TAG, "processCallStateChangedSignal " + newState);

	}

	private void setCallStateDisplay(String newState)
	{
		if (newState == null || newState.equals("NULL")) {
			newState = "INCOMING";
		}

		Log.w(TAG, "setCallStateDisplay " + newState);

		/*	mCall.printCallInfo();

		FragmentManager fm = getFragmentManager();
		Fragment newf, f = fm.findFragmentByTag("call_fragment");
		boolean replace = true;
		if (newState.equals("INCOMING") && !(f instanceof IncomingCallFragment)) {
			newf = new IncomingCallFragment();
		} else if (!newState.equals("INCOMING") && !(f instanceof OngoingCallFragment)) {
			newf = new OngoingCallFragment();
		} else {
			replace = false;
			newf = f;
		}

		((CallFragment) newf).setCall(mCall);

		if (replace) {
			FragmentTransaction ft = fm.beginTransaction();
			if(f != null) // do not animate if there is no previous fragment
				ft.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
			//ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.replace(R.id.fragment_layout, newf, "call_fragment").commit();
		}*/
	}

	public void onCallAccepted()
	{
		mCall.notifyServiceAnswer(service);
	}

	public void onCallRejected()
	{
		if (mCall.notifyServiceHangup(service))
			finish();
	}

	public void onCallEnded()
	{
		if (mCall.notifyServiceHangup(service))
			finish();
	}

	public void onCallSuspended()
	{
		mCall.notifyServiceHold(service);
	}

	public void onCallResumed()
	{
		mCall.notifyServiceUnhold(service);
	}

	public void onCalltransfered(String to) {
		mCall.notifyServiceTransfer(service, to);

	}

	public void onRecordCall() {
		mCall.notifyServiceRecord(service);

	}

	public void onSendMessage(String msg) {
		mCall.notifyServiceSendMsg(service,msg);

	}

}
