/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.8
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cx.ring.daemon;

public class Ringservice {
  public static void fini() {
    RingserviceJNI.fini();
  }

  public static void pollEvents() {
    RingserviceJNI.pollEvents();
  }

  public static String placeCall(String accountID, String to) {
    return RingserviceJNI.placeCall(accountID, to);
  }

  public static boolean refuse(String callID) {
    return RingserviceJNI.refuse(callID);
  }

  public static boolean accept(String callID) {
    return RingserviceJNI.accept(callID);
  }

  public static boolean hangUp(String callID) {
    return RingserviceJNI.hangUp(callID);
  }

  public static boolean hold(String callID) {
    return RingserviceJNI.hold(callID);
  }

  public static boolean unhold(String callID) {
    return RingserviceJNI.unhold(callID);
  }

  public static boolean muteLocalMedia(String callid, String mediaType, boolean mute) {
    return RingserviceJNI.muteLocalMedia(callid, mediaType, mute);
  }

  public static boolean transfer(String callID, String to) {
    return RingserviceJNI.transfer(callID, to);
  }

  public static boolean attendedTransfer(String transferID, String targetID) {
    return RingserviceJNI.attendedTransfer(transferID, targetID);
  }

  public static StringMap getCallDetails(String callID) {
    return new StringMap(RingserviceJNI.getCallDetails(callID), true);
  }

  public static StringVect getCallList() {
    return new StringVect(RingserviceJNI.getCallList(), true);
  }

  public static void removeConference(String conference_id) {
    RingserviceJNI.removeConference(conference_id);
  }

  public static boolean joinParticipant(String sel_callID, String drag_callID) {
    return RingserviceJNI.joinParticipant(sel_callID, drag_callID);
  }

  public static void createConfFromParticipantList(StringVect participants) {
    RingserviceJNI.createConfFromParticipantList(StringVect.getCPtr(participants), participants);
  }

  public static boolean isConferenceParticipant(String call_id) {
    return RingserviceJNI.isConferenceParticipant(call_id);
  }

  public static boolean addParticipant(String callID, String confID) {
    return RingserviceJNI.addParticipant(callID, confID);
  }

  public static boolean addMainParticipant(String confID) {
    return RingserviceJNI.addMainParticipant(confID);
  }

  public static boolean detachParticipant(String callID) {
    return RingserviceJNI.detachParticipant(callID);
  }

  public static boolean joinConference(String sel_confID, String drag_confID) {
    return RingserviceJNI.joinConference(sel_confID, drag_confID);
  }

  public static boolean hangUpConference(String confID) {
    return RingserviceJNI.hangUpConference(confID);
  }

  public static boolean holdConference(String confID) {
    return RingserviceJNI.holdConference(confID);
  }

  public static boolean unholdConference(String confID) {
    return RingserviceJNI.unholdConference(confID);
  }

  public static StringVect getConferenceList() {
    return new StringVect(RingserviceJNI.getConferenceList(), true);
  }

  public static StringVect getParticipantList(String confID) {
    return new StringVect(RingserviceJNI.getParticipantList(confID), true);
  }

  public static StringVect getDisplayNames(String confID) {
    return new StringVect(RingserviceJNI.getDisplayNames(confID), true);
  }

  public static String getConferenceId(String callID) {
    return RingserviceJNI.getConferenceId(callID);
  }

  public static StringMap getConferenceDetails(String callID) {
    return new StringMap(RingserviceJNI.getConferenceDetails(callID), true);
  }

  public static boolean startRecordedFilePlayback(String filepath) {
    return RingserviceJNI.startRecordedFilePlayback(filepath);
  }

  public static void stopRecordedFilePlayback(String filepath) {
    RingserviceJNI.stopRecordedFilePlayback(filepath);
  }

  public static boolean toggleRecording(String callID) {
    return RingserviceJNI.toggleRecording(callID);
  }

  public static void setRecording(String callID) {
    RingserviceJNI.setRecording(callID);
  }

  public static void recordPlaybackSeek(double value) {
    RingserviceJNI.recordPlaybackSeek(value);
  }

  public static boolean getIsRecording(String callID) {
    return RingserviceJNI.getIsRecording(callID);
  }

  public static String getCurrentAudioCodecName(String callID) {
    return RingserviceJNI.getCurrentAudioCodecName(callID);
  }

  public static void playDTMF(String key) {
    RingserviceJNI.playDTMF(key);
  }

  public static void startTone(int start, int type) {
    RingserviceJNI.startTone(start, type);
  }

  public static boolean switchInput(String callID, String resource) {
    return RingserviceJNI.switchInput__SWIG_0(callID, resource);
  }

  public static void sendTextMessage(String callID, StringMap messages, String from, boolean isMixed) {
    RingserviceJNI.sendTextMessage(callID, StringMap.getCPtr(messages), messages, from, isMixed);
  }

  public static StringMap getAccountDetails(String accountID) {
    return new StringMap(RingserviceJNI.getAccountDetails(accountID), true);
  }

  public static StringMap getVolatileAccountDetails(String accountID) {
    return new StringMap(RingserviceJNI.getVolatileAccountDetails(accountID), true);
  }

  public static void setAccountDetails(String accountID, StringMap details) {
    RingserviceJNI.setAccountDetails(accountID, StringMap.getCPtr(details), details);
  }

  public static void setAccountActive(String accountID, boolean active) {
    RingserviceJNI.setAccountActive(accountID, active);
  }

  public static StringMap getAccountTemplate(String accountType) {
    return new StringMap(RingserviceJNI.getAccountTemplate(accountType), true);
  }

  public static String addAccount(StringMap details) {
    return RingserviceJNI.addAccount(StringMap.getCPtr(details), details);
  }

  public static void removeAccount(String accountID) {
    RingserviceJNI.removeAccount(accountID);
  }

  public static StringVect getAccountList() {
    return new StringVect(RingserviceJNI.getAccountList(), true);
  }

  public static void sendRegister(String accountID, boolean enable) {
    RingserviceJNI.sendRegister(accountID, enable);
  }

  public static void registerAllAccounts() {
    RingserviceJNI.registerAllAccounts();
  }

  public static long sendAccountTextMessage(String accountID, String to, StringMap message) {
    return RingserviceJNI.sendAccountTextMessage(accountID, to, StringMap.getCPtr(message), message);
  }

  public static int getMessageStatus(long id) {
    return RingserviceJNI.getMessageStatus(id);
  }

  public static boolean lookupName(String account, String nameserver, String name) {
    return RingserviceJNI.lookupName(account, nameserver, name);
  }

  public static boolean lookupAddress(String account, String nameserver, String address) {
    return RingserviceJNI.lookupAddress(account, nameserver, address);
  }

  public static boolean registerName(String account, String password, String name) {
    return RingserviceJNI.registerName(account, password, name);
  }

  public static StringMap getTlsDefaultSettings() {
    return new StringMap(RingserviceJNI.getTlsDefaultSettings(), true);
  }

  public static UintVect getCodecList() {
    return new UintVect(RingserviceJNI.getCodecList(), true);
  }

  public static StringVect getSupportedTlsMethod() {
    return new StringVect(RingserviceJNI.getSupportedTlsMethod(), true);
  }

  public static StringVect getSupportedCiphers(String accountID) {
    return new StringVect(RingserviceJNI.getSupportedCiphers(accountID), true);
  }

  public static StringMap getCodecDetails(String accountID, long codecId) {
    return new StringMap(RingserviceJNI.getCodecDetails(accountID, codecId), true);
  }

  public static boolean setCodecDetails(String accountID, long codecId, StringMap details) {
    return RingserviceJNI.setCodecDetails(accountID, codecId, StringMap.getCPtr(details), details);
  }

  public static UintVect getActiveCodecList(String accountID) {
    return new UintVect(RingserviceJNI.getActiveCodecList(accountID), true);
  }

  public static String exportOnRing(String accountID, String password) {
    return RingserviceJNI.exportOnRing(accountID, password);
  }

  public static StringMap getKnownRingDevices(String accountID) {
    return new StringMap(RingserviceJNI.getKnownRingDevices(accountID), true);
  }

  public static void setActiveCodecList(String accountID, UintVect list) {
    RingserviceJNI.setActiveCodecList(accountID, UintVect.getCPtr(list), list);
  }

  public static StringVect getAudioPluginList() {
    return new StringVect(RingserviceJNI.getAudioPluginList(), true);
  }

  public static void setAudioPlugin(String audioPlugin) {
    RingserviceJNI.setAudioPlugin(audioPlugin);
  }

  public static StringVect getAudioOutputDeviceList() {
    return new StringVect(RingserviceJNI.getAudioOutputDeviceList(), true);
  }

  public static void setAudioOutputDevice(int index) {
    RingserviceJNI.setAudioOutputDevice(index);
  }

  public static void setAudioInputDevice(int index) {
    RingserviceJNI.setAudioInputDevice(index);
  }

  public static void setAudioRingtoneDevice(int index) {
    RingserviceJNI.setAudioRingtoneDevice(index);
  }

  public static StringVect getAudioInputDeviceList() {
    return new StringVect(RingserviceJNI.getAudioInputDeviceList(), true);
  }

  public static StringVect getCurrentAudioDevicesIndex() {
    return new StringVect(RingserviceJNI.getCurrentAudioDevicesIndex(), true);
  }

  public static int getAudioInputDeviceIndex(String name) {
    return RingserviceJNI.getAudioInputDeviceIndex(name);
  }

  public static int getAudioOutputDeviceIndex(String name) {
    return RingserviceJNI.getAudioOutputDeviceIndex(name);
  }

  public static String getCurrentAudioOutputPlugin() {
    return RingserviceJNI.getCurrentAudioOutputPlugin();
  }

  public static boolean getNoiseSuppressState() {
    return RingserviceJNI.getNoiseSuppressState();
  }

  public static void setNoiseSuppressState(boolean state) {
    RingserviceJNI.setNoiseSuppressState(state);
  }

  public static boolean isAgcEnabled() {
    return RingserviceJNI.isAgcEnabled();
  }

  public static void setAgcState(boolean enabled) {
    RingserviceJNI.setAgcState(enabled);
  }

  public static void muteDtmf(boolean mute) {
    RingserviceJNI.muteDtmf(mute);
  }

  public static boolean isDtmfMuted() {
    return RingserviceJNI.isDtmfMuted();
  }

  public static boolean isCaptureMuted() {
    return RingserviceJNI.isCaptureMuted();
  }

  public static void muteCapture(boolean mute) {
    RingserviceJNI.muteCapture(mute);
  }

  public static boolean isPlaybackMuted() {
    return RingserviceJNI.isPlaybackMuted();
  }

  public static void mutePlayback(boolean mute) {
    RingserviceJNI.mutePlayback(mute);
  }

  public static boolean isRingtoneMuted() {
    return RingserviceJNI.isRingtoneMuted();
  }

  public static void muteRingtone(boolean mute) {
    RingserviceJNI.muteRingtone(mute);
  }

  public static String getAudioManager() {
    return RingserviceJNI.getAudioManager();
  }

  public static boolean setAudioManager(String api) {
    return RingserviceJNI.setAudioManager(api);
  }

  public static String getRecordPath() {
    return RingserviceJNI.getRecordPath();
  }

  public static void setRecordPath(String recPath) {
    RingserviceJNI.setRecordPath(recPath);
  }

  public static boolean getIsAlwaysRecording() {
    return RingserviceJNI.getIsAlwaysRecording();
  }

  public static void setIsAlwaysRecording(boolean rec) {
    RingserviceJNI.setIsAlwaysRecording(rec);
  }

  public static void setHistoryLimit(int days) {
    RingserviceJNI.setHistoryLimit(days);
  }

  public static int getHistoryLimit() {
    return RingserviceJNI.getHistoryLimit();
  }

  public static void setAccountsOrder(String order) {
    RingserviceJNI.setAccountsOrder(order);
  }

  public static StringMap getHookSettings() {
    return new StringMap(RingserviceJNI.getHookSettings(), true);
  }

  public static void setHookSettings(StringMap settings) {
    RingserviceJNI.setHookSettings(StringMap.getCPtr(settings), settings);
  }

  public static VectMap getCredentials(String accountID) {
    return new VectMap(RingserviceJNI.getCredentials(accountID), true);
  }

  public static void setCredentials(String accountID, VectMap details) {
    RingserviceJNI.setCredentials(accountID, VectMap.getCPtr(details), details);
  }

  public static String getAddrFromInterfaceName(String arg0) {
    return RingserviceJNI.getAddrFromInterfaceName(arg0);
  }

  public static StringVect getAllIpInterface() {
    return new StringVect(RingserviceJNI.getAllIpInterface(), true);
  }

  public static StringVect getAllIpInterfaceByName() {
    return new StringVect(RingserviceJNI.getAllIpInterfaceByName(), true);
  }

  public static StringMap getShortcuts() {
    return new StringMap(RingserviceJNI.getShortcuts(), true);
  }

  public static void setShortcuts(StringMap shortcutsMap) {
    RingserviceJNI.setShortcuts(StringMap.getCPtr(shortcutsMap), shortcutsMap);
  }

  public static void setVolume(String device, double value) {
    RingserviceJNI.setVolume(device, value);
  }

  public static double getVolume(String device) {
    return RingserviceJNI.getVolume(device);
  }

  public static StringMap validateCertificatePath(String accountId, String certificate, String privateKey, String privateKeyPassword, String caList) {
    return new StringMap(RingserviceJNI.validateCertificatePath(accountId, certificate, privateKey, privateKeyPassword, caList), true);
  }

  public static StringMap validateCertificate(String accountId, String certificate) {
    return new StringMap(RingserviceJNI.validateCertificate(accountId, certificate), true);
  }

  public static StringMap getCertificateDetails(String certificate) {
    return new StringMap(RingserviceJNI.getCertificateDetails(certificate), true);
  }

  public static StringMap getCertificateDetailsPath(String certificate, String privateKey, String privateKeyPass) {
    return new StringMap(RingserviceJNI.getCertificateDetailsPath(certificate, privateKey, privateKeyPass), true);
  }

  public static StringVect getPinnedCertificates() {
    return new StringVect(RingserviceJNI.getPinnedCertificates(), true);
  }

  public static StringVect pinCertificate(Blob certificate, boolean local) {
    return new StringVect(RingserviceJNI.pinCertificate(Blob.getCPtr(certificate), certificate, local), true);
  }

  public static boolean unpinCertificate(String certId) {
    return RingserviceJNI.unpinCertificate(certId);
  }

  public static void pinCertificatePath(String path) {
    RingserviceJNI.pinCertificatePath(path);
  }

  public static long unpinCertificatePath(String path) {
    return RingserviceJNI.unpinCertificatePath(path);
  }

  public static boolean pinRemoteCertificate(String accountId, String certId) {
    return RingserviceJNI.pinRemoteCertificate(accountId, certId);
  }

  public static boolean setCertificateStatus(String account, String certId, String status) {
    return RingserviceJNI.setCertificateStatus(account, certId, status);
  }

  public static StringVect getCertificatesByStatus(String account, String status) {
    return new StringVect(RingserviceJNI.getCertificatesByStatus(account, status), true);
  }

  public static StringMap getTrustRequests(String accountId) {
    return new StringMap(RingserviceJNI.getTrustRequests(accountId), true);
  }

  public static boolean acceptTrustRequest(String accountId, String from) {
    return RingserviceJNI.acceptTrustRequest(accountId, from);
  }

  public static boolean discardTrustRequest(String accountId, String from) {
    return RingserviceJNI.discardTrustRequest(accountId, from);
  }

  public static void sendTrustRequest(String accountId, String to, Blob payload) {
    RingserviceJNI.sendTrustRequest(accountId, to, Blob.getCPtr(payload), payload);
  }

  public static int exportAccounts(StringVect accountIDs, String toDir, String password) {
    return RingserviceJNI.exportAccounts(StringVect.getCPtr(accountIDs), accountIDs, toDir, password);
  }

  public static int importAccounts(String archivePath, String password) {
    return RingserviceJNI.importAccounts(archivePath, password);
  }

  public static void connectivityChanged() {
    RingserviceJNI.connectivityChanged();
  }

  public static void setVideoFrame(byte[] arg0, int arg1, long arg2, int arg3, int arg4, int arg5) {
    RingserviceJNI.setVideoFrame(arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public static long acquireNativeWindow(Object arg0) {
    return RingserviceJNI.acquireNativeWindow(arg0);
  }

  public static void releaseNativeWindow(long arg0) {
    RingserviceJNI.releaseNativeWindow(arg0);
  }

  public static void setNativeWindowGeometry(long arg0, int arg1, int arg2) {
    RingserviceJNI.setNativeWindowGeometry(arg0, arg1, arg2);
  }

  public static void registerVideoCallback(String arg0, long arg1) {
    RingserviceJNI.registerVideoCallback(arg0, arg1);
  }

  public static void unregisterVideoCallback(String arg0, long arg1) {
    RingserviceJNI.unregisterVideoCallback(arg0, arg1);
  }

  public static void setDefaultDevice(String name) {
    RingserviceJNI.setDefaultDevice(name);
  }

  public static String getDefaultDevice() {
    return RingserviceJNI.getDefaultDevice();
  }

  public static void startCamera() {
    RingserviceJNI.startCamera();
  }

  public static void stopCamera() {
    RingserviceJNI.stopCamera();
  }

  public static boolean hasCameraStarted() {
    return RingserviceJNI.hasCameraStarted();
  }

  public static boolean switchInput(String resource) {
    return RingserviceJNI.switchInput__SWIG_1(resource);
  }

  public static boolean switchToCamera() {
    return RingserviceJNI.switchToCamera();
  }

  public static StringMap getSettings(String name) {
    return new StringMap(RingserviceJNI.getSettings(name), true);
  }

  public static void applySettings(String name, StringMap settings) {
    RingserviceJNI.applySettings(name, StringMap.getCPtr(settings), settings);
  }

  public static void addVideoDevice(String node) {
    RingserviceJNI.addVideoDevice(node);
  }

  public static void removeVideoDevice(String node) {
    RingserviceJNI.removeVideoDevice(node);
  }

  public static SWIGTYPE_p_unsigned_char obtainFrame(int length) {
    long cPtr = RingserviceJNI.obtainFrame(length);
    return (cPtr == 0) ? null : new SWIGTYPE_p_unsigned_char(cPtr, false);
  }

  public static void releaseFrame(SWIGTYPE_p_unsigned_char frame) {
    RingserviceJNI.releaseFrame(SWIGTYPE_p_unsigned_char.getCPtr(frame));
  }

  public static void registerSinkTarget(String sinkId, SWIGTYPE_p_DRing__SinkTarget target) {
    RingserviceJNI.registerSinkTarget(sinkId, SWIGTYPE_p_DRing__SinkTarget.getCPtr(target));
  }

  public static void init(ConfigurationCallback confM, Callback callM, VideoCallback videoM) {
    RingserviceJNI.init(ConfigurationCallback.getCPtr(confM), confM, Callback.getCPtr(callM), callM, VideoCallback.getCPtr(videoM), videoM);
  }

}
