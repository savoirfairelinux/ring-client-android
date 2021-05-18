/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cx.ring.daemon;

public class ConversationCallback {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ConversationCallback(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ConversationCallback obj) {
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
        RingserviceJNI.delete_ConversationCallback(swigCPtr);
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
    RingserviceJNI.ConversationCallback_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    RingserviceJNI.ConversationCallback_change_ownership(this, swigCPtr, true);
  }

  public void conversationLoaded(long arg0, String arg1, String arg2, VectMap arg3) {
    if (getClass() == ConversationCallback.class) RingserviceJNI.ConversationCallback_conversationLoaded(swigCPtr, this, arg0, arg1, arg2, VectMap.getCPtr(arg3), arg3); else RingserviceJNI.ConversationCallback_conversationLoadedSwigExplicitConversationCallback(swigCPtr, this, arg0, arg1, arg2, VectMap.getCPtr(arg3), arg3);
  }

  public void messageReceived(String arg0, String arg1, StringMap arg2) {
    if (getClass() == ConversationCallback.class) RingserviceJNI.ConversationCallback_messageReceived(swigCPtr, this, arg0, arg1, StringMap.getCPtr(arg2), arg2); else RingserviceJNI.ConversationCallback_messageReceivedSwigExplicitConversationCallback(swigCPtr, this, arg0, arg1, StringMap.getCPtr(arg2), arg2);
  }

  public void conversationRequestReceived(String arg0, String arg1, StringMap arg2) {
    if (getClass() == ConversationCallback.class) RingserviceJNI.ConversationCallback_conversationRequestReceived(swigCPtr, this, arg0, arg1, StringMap.getCPtr(arg2), arg2); else RingserviceJNI.ConversationCallback_conversationRequestReceivedSwigExplicitConversationCallback(swigCPtr, this, arg0, arg1, StringMap.getCPtr(arg2), arg2);
  }

  public void conversationReady(String arg0, String arg1) {
    if (getClass() == ConversationCallback.class) RingserviceJNI.ConversationCallback_conversationReady(swigCPtr, this, arg0, arg1); else RingserviceJNI.ConversationCallback_conversationReadySwigExplicitConversationCallback(swigCPtr, this, arg0, arg1);
  }

  public void conversationRemoved(String arg0, String arg1) {
    if (getClass() == ConversationCallback.class) RingserviceJNI.ConversationCallback_conversationRemoved(swigCPtr, this, arg0, arg1); else RingserviceJNI.ConversationCallback_conversationRemovedSwigExplicitConversationCallback(swigCPtr, this, arg0, arg1);
  }

  public void conversationMemberEvent(String arg0, String arg1, String arg2, int arg3) {
    if (getClass() == ConversationCallback.class) RingserviceJNI.ConversationCallback_conversationMemberEvent(swigCPtr, this, arg0, arg1, arg2, arg3); else RingserviceJNI.ConversationCallback_conversationMemberEventSwigExplicitConversationCallback(swigCPtr, this, arg0, arg1, arg2, arg3);
  }

  public void onConversationError(String arg0, String arg1, long arg2, String arg3) {
    if (getClass() == ConversationCallback.class) RingserviceJNI.ConversationCallback_onConversationError(swigCPtr, this, arg0, arg1, arg2, arg3); else RingserviceJNI.ConversationCallback_onConversationErrorSwigExplicitConversationCallback(swigCPtr, this, arg0, arg1, arg2, arg3);
  }

  public ConversationCallback() {
    this(RingserviceJNI.new_ConversationCallback(), true);
    RingserviceJNI.ConversationCallback_director_connect(this, swigCPtr, true, true);
  }

}
