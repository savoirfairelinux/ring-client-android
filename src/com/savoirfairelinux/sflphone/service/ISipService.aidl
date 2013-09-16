package com.savoirfairelinux.sflphone.service;

import com.savoirfairelinux.sflphone.model.SipCall;
import com.savoirfairelinux.sflphone.model.Conference;

interface ISipService {
    
    Map getCallDetails(in String callID);
    void placeCall(in SipCall call);
    void refuse(in String callID);
    void accept(in String callID);
    void hangUp(in String callID);
    void hold(in String callID);
    void unhold(in String callID);
    
    List getAccountList();
    String addAccount(in Map accountDetails);
    void removeAccount(in String accoundId);
    Map getAccountDetails(in String accountID);
    void setAccountDetails(in String accountId, in Map accountDetails);
    void setAudioPlugin(in String callID);
    String getCurrentAudioOutputPlugin();
    List getAudioCodecList(in String accountID);
    
    /* History */
    List getHistory();
    
    /* Notification */
    void createNotification();
    void destroyNotification();
    
    
    /* Recording */
    void setRecordPath(in String path);
    String getRecordPath();
    boolean toggleRecordingCall(in String id);
    boolean startRecordedFilePlayback(in String filepath);
	void stopRecordedFilePlayback(in String filepath);
	boolean isRecording(in String id);
    
    /* IM */
    void sendTextMessage(in String callID, in String message, in String from);
        
    void transfer(in String callID, in String to);
    void attendedTransfer(in String transferID, in String targetID);
    
    /* Conference related methods */

    void removeConference(in String confID);
    void joinParticipant(in String sel_callID, in String drag_callID);

    void addParticipant(in SipCall call, in String confID);
    void addMainParticipant(in String confID);
    void detachParticipant(in String callID);
    void joinConference(in String sel_confID, in String drag_confID);
    void hangUpConference(in String confID);
    void holdConference(in String confID);
    void unholdConference(in String confID);
    boolean isConferenceParticipant(in String callID);
    Map getConferenceList();
    Map getCallList();
    List getParticipantList(in String confID);
    String getConferenceId(in String callID);
    String getConferenceDetails(in String callID);
    
    Conference getCurrentCall();
    
    
    /*   */
    
    SipCall getCall(String callID);
}
