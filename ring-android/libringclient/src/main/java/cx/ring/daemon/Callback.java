/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cx.ring.daemon;

public class Callback {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Callback(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Callback obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        RingserviceJNI.delete_Callback(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    RingserviceJNI.Callback_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    RingserviceJNI.Callback_change_ownership(this, swigCPtr, true);
  }

  public void callStateChanged(String call_id, String state, int detail_code) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_callStateChanged(swigCPtr, this, call_id, state, detail_code); else RingserviceJNI.Callback_callStateChangedSwigExplicitCallback(swigCPtr, this, call_id, state, detail_code);
  }

  public void transferFailed() {
    if (getClass() == Callback.class) RingserviceJNI.Callback_transferFailed(swigCPtr, this); else RingserviceJNI.Callback_transferFailedSwigExplicitCallback(swigCPtr, this);
  }

  public void transferSucceeded() {
    if (getClass() == Callback.class) RingserviceJNI.Callback_transferSucceeded(swigCPtr, this); else RingserviceJNI.Callback_transferSucceededSwigExplicitCallback(swigCPtr, this);
  }

  public void recordPlaybackStopped(String path) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_recordPlaybackStopped(swigCPtr, this, path); else RingserviceJNI.Callback_recordPlaybackStoppedSwigExplicitCallback(swigCPtr, this, path);
  }

  public void voiceMailNotify(String accountId, int newCount, int oldCount, int urgentCount) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_voiceMailNotify(swigCPtr, this, accountId, newCount, oldCount, urgentCount); else RingserviceJNI.Callback_voiceMailNotifySwigExplicitCallback(swigCPtr, this, accountId, newCount, oldCount, urgentCount);
  }

  public void incomingMessage(String id, String from, StringMap messages) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_incomingMessage(swigCPtr, this, id, from, StringMap.getCPtr(messages), messages); else RingserviceJNI.Callback_incomingMessageSwigExplicitCallback(swigCPtr, this, id, from, StringMap.getCPtr(messages), messages);
  }

  public void incomingCall(String account_id, String call_id, String from) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_incomingCall(swigCPtr, this, account_id, call_id, from); else RingserviceJNI.Callback_incomingCallSwigExplicitCallback(swigCPtr, this, account_id, call_id, from);
  }

  public void incomingCallWithMedia(String account_id, String call_id, String from, VectMap mediaList) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_incomingCallWithMedia(swigCPtr, this, account_id, call_id, from, VectMap.getCPtr(mediaList), mediaList); else RingserviceJNI.Callback_incomingCallWithMediaSwigExplicitCallback(swigCPtr, this, account_id, call_id, from, VectMap.getCPtr(mediaList), mediaList);
  }

  public void mediaChangeRequested(String account_id, String call_id, VectMap mediaList) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_mediaChangeRequested(swigCPtr, this, account_id, call_id, VectMap.getCPtr(mediaList), mediaList); else RingserviceJNI.Callback_mediaChangeRequestedSwigExplicitCallback(swigCPtr, this, account_id, call_id, VectMap.getCPtr(mediaList), mediaList);
  }

  public void recordPlaybackFilepath(String id, String filename) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_recordPlaybackFilepath(swigCPtr, this, id, filename); else RingserviceJNI.Callback_recordPlaybackFilepathSwigExplicitCallback(swigCPtr, this, id, filename);
  }

  public void conferenceCreated(String conf_id) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_conferenceCreated(swigCPtr, this, conf_id); else RingserviceJNI.Callback_conferenceCreatedSwigExplicitCallback(swigCPtr, this, conf_id);
  }

  public void conferenceChanged(String conf_id, String state) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_conferenceChanged(swigCPtr, this, conf_id, state); else RingserviceJNI.Callback_conferenceChangedSwigExplicitCallback(swigCPtr, this, conf_id, state);
  }

  public void conferenceRemoved(String conf_id) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_conferenceRemoved(swigCPtr, this, conf_id); else RingserviceJNI.Callback_conferenceRemovedSwigExplicitCallback(swigCPtr, this, conf_id);
  }

  public void updatePlaybackScale(String filepath, int position, int scale) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_updatePlaybackScale(swigCPtr, this, filepath, position, scale); else RingserviceJNI.Callback_updatePlaybackScaleSwigExplicitCallback(swigCPtr, this, filepath, position, scale);
  }

  public void newCall(String account_id, String call_id, String to) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_newCall(swigCPtr, this, account_id, call_id, to); else RingserviceJNI.Callback_newCallSwigExplicitCallback(swigCPtr, this, account_id, call_id, to);
  }

  public void sipCallStateChange(String call_id, String state, int code) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_sipCallStateChange(swigCPtr, this, call_id, state, code); else RingserviceJNI.Callback_sipCallStateChangeSwigExplicitCallback(swigCPtr, this, call_id, state, code);
  }

  public void recordingStateChanged(String call_id, int code) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_recordingStateChanged(swigCPtr, this, call_id, code); else RingserviceJNI.Callback_recordingStateChangedSwigExplicitCallback(swigCPtr, this, call_id, code);
  }

  public void recordStateChange(String call_id, int state) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_recordStateChange(swigCPtr, this, call_id, state); else RingserviceJNI.Callback_recordStateChangeSwigExplicitCallback(swigCPtr, this, call_id, state);
  }

  public void onRtcpReportReceived(String call_id, IntegerMap stats) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_onRtcpReportReceived(swigCPtr, this, call_id, IntegerMap.getCPtr(stats), stats); else RingserviceJNI.Callback_onRtcpReportReceivedSwigExplicitCallback(swigCPtr, this, call_id, IntegerMap.getCPtr(stats), stats);
  }

  public void onConferenceInfosUpdated(String confId, VectMap infos) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_onConferenceInfosUpdated(swigCPtr, this, confId, VectMap.getCPtr(infos), infos); else RingserviceJNI.Callback_onConferenceInfosUpdatedSwigExplicitCallback(swigCPtr, this, confId, VectMap.getCPtr(infos), infos);
  }

  public void peerHold(String call_id, boolean holding) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_peerHold(swigCPtr, this, call_id, holding); else RingserviceJNI.Callback_peerHoldSwigExplicitCallback(swigCPtr, this, call_id, holding);
  }

  public void connectionUpdate(String id, int state) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_connectionUpdate(swigCPtr, this, id, state); else RingserviceJNI.Callback_connectionUpdateSwigExplicitCallback(swigCPtr, this, id, state);
  }

  public void remoteRecordingChanged(String call_id, String peer_number, boolean state) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_remoteRecordingChanged(swigCPtr, this, call_id, peer_number, state); else RingserviceJNI.Callback_remoteRecordingChangedSwigExplicitCallback(swigCPtr, this, call_id, peer_number, state);
  }

  public void mediaNegotiationStatus(String call_id, String event) {
    if (getClass() == Callback.class) RingserviceJNI.Callback_mediaNegotiationStatus(swigCPtr, this, call_id, event); else RingserviceJNI.Callback_mediaNegotiationStatusSwigExplicitCallback(swigCPtr, this, call_id, event);
  }

  public Callback() {
    this(RingserviceJNI.new_Callback(), true);
    RingserviceJNI.Callback_director_connect(this, swigCPtr, true, true);
  }

}
