/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cx.ring.service;

public class RingserviceJNI {
  public final static native long new_StringMap__SWIG_0();
  public final static native long new_StringMap__SWIG_1(long jarg1, StringMap jarg1_);
  public final static native long StringMap_size(long jarg1, StringMap jarg1_);
  public final static native boolean StringMap_empty(long jarg1, StringMap jarg1_);
  public final static native void StringMap_clear(long jarg1, StringMap jarg1_);
  public final static native String StringMap_get(long jarg1, StringMap jarg1_, String jarg2);
  public final static native void StringMap_set(long jarg1, StringMap jarg1_, String jarg2, String jarg3);
  public final static native void StringMap_del(long jarg1, StringMap jarg1_, String jarg2);
  public final static native boolean StringMap_has_key(long jarg1, StringMap jarg1_, String jarg2);
  public final static native void delete_StringMap(long jarg1);
  public final static native long new_StringVect__SWIG_0();
  public final static native long new_StringVect__SWIG_1(long jarg1);
  public final static native long StringVect_size(long jarg1, StringVect jarg1_);
  public final static native long StringVect_capacity(long jarg1, StringVect jarg1_);
  public final static native void StringVect_reserve(long jarg1, StringVect jarg1_, long jarg2);
  public final static native boolean StringVect_isEmpty(long jarg1, StringVect jarg1_);
  public final static native void StringVect_clear(long jarg1, StringVect jarg1_);
  public final static native void StringVect_add(long jarg1, StringVect jarg1_, String jarg2);
  public final static native String StringVect_get(long jarg1, StringVect jarg1_, int jarg2);
  public final static native void StringVect_set(long jarg1, StringVect jarg1_, int jarg2, String jarg3);
  public final static native void delete_StringVect(long jarg1);
  public final static native long new_VectMap__SWIG_0();
  public final static native long new_VectMap__SWIG_1(long jarg1);
  public final static native long VectMap_size(long jarg1, VectMap jarg1_);
  public final static native long VectMap_capacity(long jarg1, VectMap jarg1_);
  public final static native void VectMap_reserve(long jarg1, VectMap jarg1_, long jarg2);
  public final static native boolean VectMap_isEmpty(long jarg1, VectMap jarg1_);
  public final static native void VectMap_clear(long jarg1, VectMap jarg1_);
  public final static native void VectMap_add(long jarg1, VectMap jarg1_, long jarg2, StringMap jarg2_);
  public final static native long VectMap_get(long jarg1, VectMap jarg1_, int jarg2);
  public final static native void VectMap_set(long jarg1, VectMap jarg1_, int jarg2, long jarg3, StringMap jarg3_);
  public final static native void delete_VectMap(long jarg1);
  public final static native long new_IntegerMap__SWIG_0();
  public final static native long new_IntegerMap__SWIG_1(long jarg1, IntegerMap jarg1_);
  public final static native long IntegerMap_size(long jarg1, IntegerMap jarg1_);
  public final static native boolean IntegerMap_empty(long jarg1, IntegerMap jarg1_);
  public final static native void IntegerMap_clear(long jarg1, IntegerMap jarg1_);
  public final static native int IntegerMap_get(long jarg1, IntegerMap jarg1_, String jarg2);
  public final static native void IntegerMap_set(long jarg1, IntegerMap jarg1_, String jarg2, int jarg3);
  public final static native void IntegerMap_del(long jarg1, IntegerMap jarg1_, String jarg2);
  public final static native boolean IntegerMap_has_key(long jarg1, IntegerMap jarg1_, String jarg2);
  public final static native void delete_IntegerMap(long jarg1);
  public final static native long new_IntVect__SWIG_0();
  public final static native long new_IntVect__SWIG_1(long jarg1);
  public final static native long IntVect_size(long jarg1, IntVect jarg1_);
  public final static native long IntVect_capacity(long jarg1, IntVect jarg1_);
  public final static native void IntVect_reserve(long jarg1, IntVect jarg1_, long jarg2);
  public final static native boolean IntVect_isEmpty(long jarg1, IntVect jarg1_);
  public final static native void IntVect_clear(long jarg1, IntVect jarg1_);
  public final static native void IntVect_add(long jarg1, IntVect jarg1_, int jarg2);
  public final static native int IntVect_get(long jarg1, IntVect jarg1_, int jarg2);
  public final static native void IntVect_set(long jarg1, IntVect jarg1_, int jarg2, int jarg3);
  public final static native void delete_IntVect(long jarg1);
  public final static native void sflph_fini();
  public final static native void sflph_poll_events();
  public final static native boolean sflph_call_place(String jarg1, String jarg2, String jarg3);
  public final static native boolean sflph_call_refuse(String jarg1);
  public final static native boolean sflph_call_accept(String jarg1);
  public final static native boolean sflph_call_hang_up(String jarg1);
  public final static native boolean sflph_call_hold(String jarg1);
  public final static native boolean sflph_call_unhold(String jarg1);
  public final static native boolean sflph_call_transfer(String jarg1, String jarg2);
  public final static native boolean sflph_call_attended_transfer(String jarg1, String jarg2);
  public final static native long sflph_call_get_call_details(String jarg1);
  public final static native long sflph_call_get_call_list();
  public final static native void sflph_call_remove_conference(String jarg1);
  public final static native boolean sflph_call_join_participant(String jarg1, String jarg2);
  public final static native void sflph_call_create_conf_from_participant_list(long jarg1, StringVect jarg1_);
  public final static native boolean sflph_call_is_conference_participant(String jarg1);
  public final static native boolean sflph_call_add_participant(String jarg1, String jarg2);
  public final static native boolean sflph_call_add_main_participant(String jarg1);
  public final static native boolean sflph_call_detach_participant(String jarg1);
  public final static native boolean sflph_call_join_conference(String jarg1, String jarg2);
  public final static native boolean sflph_call_hang_up_conference(String jarg1);
  public final static native boolean sflph_call_hold_conference(String jarg1);
  public final static native boolean sflph_call_unhold_conference(String jarg1);
  public final static native long sflph_call_get_conference_list();
  public final static native long sflph_call_get_participant_list(String jarg1);
  public final static native long sflph_call_get_display_names(String jarg1);
  public final static native String sflph_call_get_conference_id(String jarg1);
  public final static native long sflph_call_get_conference_details(String jarg1);
  public final static native boolean sflph_call_play_recorded_file(String jarg1);
  public final static native void sflph_call_stop_recorded_file(String jarg1);
  public final static native boolean sflph_call_toggle_recording(String jarg1);
  public final static native void sflph_call_set_recording(String jarg1);
  public final static native void sflph_call_record_playback_seek(double jarg1);
  public final static native boolean sflph_call_is_recording(String jarg1);
  public final static native String sflph_call_get_current_audio_codec_name(String jarg1);
  public final static native void sflph_call_play_dtmf(String jarg1);
  public final static native void sflph_call_start_tone(int jarg1, int jarg2);
  public final static native void sflph_call_set_sas_verified(String jarg1);
  public final static native void sflph_call_reset_sas_verified(String jarg1);
  public final static native void sflph_call_set_confirm_go_clear(String jarg1);
  public final static native void sflph_call_request_go_clear(String jarg1);
  public final static native void sflph_call_accept_enrollment(String jarg1, boolean jarg2);
  public final static native void sflph_call_send_text_message(String jarg1, String jarg2);
  public final static native void delete_Callback(long jarg1);
  public final static native void Callback_callOnStateChange(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnStateChangeSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnTransferFail(long jarg1, Callback jarg1_);
  public final static native void Callback_callOnTransferFailSwigExplicitCallback(long jarg1, Callback jarg1_);
  public final static native void Callback_callOnTransferSuccess(long jarg1, Callback jarg1_);
  public final static native void Callback_callOnTransferSuccessSwigExplicitCallback(long jarg1, Callback jarg1_);
  public final static native void Callback_callOnRecordPlaybackStopped(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnRecordPlaybackStoppedSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnVoiceMailNotify(long jarg1, Callback jarg1_, String jarg2, int jarg3);
  public final static native void Callback_callOnVoiceMailNotifySwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, int jarg3);
  public final static native void Callback_callOnIncomingMessage(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnIncomingMessageSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnIncomingCall(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnIncomingCallSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnRecordPlaybackFilepath(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnRecordPlaybackFilepathSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnConferenceCreated(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnConferenceCreatedSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnConferenceChanged(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnConferenceChangedSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnUpdatePlaybackScale(long jarg1, Callback jarg1_, String jarg2, int jarg3, int jarg4);
  public final static native void Callback_callOnUpdatePlaybackScaleSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, int jarg3, int jarg4);
  public final static native void Callback_callOnConferenceRemove(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnConferenceRemoveSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnNewCall(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnNewCallSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnSipCallStateChange(long jarg1, Callback jarg1_, String jarg2, String jarg3, int jarg4);
  public final static native void Callback_callOnSipCallStateChangeSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3, int jarg4);
  public final static native void Callback_callOnRecordStateChange(long jarg1, Callback jarg1_, String jarg2, int jarg3);
  public final static native void Callback_callOnRecordStateChangeSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, int jarg3);
  public final static native void Callback_callOnSecureSdesOn(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnSecureSdesOnSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnSecureSdesOff(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnSecureSdesOffSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnSecureZrtpOn(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnSecureZrtpOnSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3);
  public final static native void Callback_callOnSecureZrtpOff(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnSecureZrtpOffSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnShowSas(long jarg1, Callback jarg1_, String jarg2, String jarg3, int jarg4);
  public final static native void Callback_callOnShowSasSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3, int jarg4);
  public final static native void Callback_callOnZrtpNotSuppOther(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnZrtpNotSuppOtherSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2);
  public final static native void Callback_callOnZrtpNegotiationFail(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnZrtpNegotiationFailSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, String jarg3, String jarg4);
  public final static native void Callback_callOnRtcpReceiveReport(long jarg1, Callback jarg1_, String jarg2, long jarg3, IntegerMap jarg3_);
  public final static native void Callback_callOnRtcpReceiveReportSwigExplicitCallback(long jarg1, Callback jarg1_, String jarg2, long jarg3, IntegerMap jarg3_);
  public final static native long new_Callback();
  public final static native void Callback_director_connect(Callback obj, long cptr, boolean mem_own, boolean weak_global);
  public final static native void Callback_change_ownership(Callback obj, long cptr, boolean take_or_release);
  public final static native long sflph_config_get_account_details(String jarg1);
  public final static native void sflph_config_set_account_details(String jarg1, long jarg2, StringMap jarg2_);
  public final static native long sflph_config_get_account_template();
  public final static native String sflph_config_add_account(long jarg1, StringMap jarg1_);
  public final static native void sflph_config_remove_account(String jarg1);
  public final static native long sflph_config_get_account_list();
  public final static native void sflph_config_send_register(String jarg1, boolean jarg2);
  public final static native void sflph_config_register_all_accounts();
  public final static native long sflph_config_get_tls_default_settings();
  public final static native long sflph_config_get_audio_codec_list();
  public final static native long sflph_config_get_supported_tls_method();
  public final static native long sflph_config_get_audio_codec_details(int jarg1);
  public final static native long sflph_config_get_active_audio_codec_list(String jarg1);
  public final static native void sflph_config_set_active_audio_codec_list(long jarg1, StringVect jarg1_, String jarg2);
  public final static native long sflph_config_get_audio_plugin_list();
  public final static native void sflph_config_set_audio_plugin(String jarg1);
  public final static native long sflph_config_get_audio_output_device_list();
  public final static native void sflph_config_set_audio_output_device(int jarg1);
  public final static native void sflph_config_set_audio_input_device(int jarg1);
  public final static native void sflph_config_set_audio_ringtone_device(int jarg1);
  public final static native long sflph_config_get_audio_input_device_list();
  public final static native long sflph_config_get_current_audio_devices_index();
  public final static native int sflph_config_get_audio_input_device_index(String jarg1);
  public final static native int sflph_config_get_audio_output_device_index(String jarg1);
  public final static native String sflph_config_get_current_audio_output_plugin();
  public final static native boolean sflph_config_get_noise_suppress_state();
  public final static native void sflph_config_set_noise_suppress_state(boolean jarg1);
  public final static native boolean sflph_config_is_agc_enabled();
  public final static native void sflph_config_enable_agc(boolean jarg1);
  public final static native void sflph_config_mute_dtmf(boolean jarg1);
  public final static native boolean sflph_config_is_dtmf_muted();
  public final static native boolean sflph_config_is_capture_muted();
  public final static native void sflph_config_mute_capture(boolean jarg1);
  public final static native boolean sflph_config_is_playback_muted();
  public final static native void sflph_config_mute_playback(int jarg1);
  public final static native long sflph_config_get_ringtone_list();
  public final static native String sflph_config_get_audio_manager();
  public final static native boolean sflph_config_set_audio_manager(String jarg1);
  public final static native long sflph_config_get_supported_audio_managers();
  public final static native int sflph_config_is_iax2_enabled();
  public final static native String sflph_config_get_record_path();
  public final static native void sflph_config_set_record_path(String jarg1);
  public final static native boolean sflph_config_is_always_recording();
  public final static native void sflph_config_set_always_recording(boolean jarg1);
  public final static native void sflph_config_set_history_limit(int jarg1);
  public final static native int sflph_config_get_history_limit();
  public final static native void sflph_config_clear_history();
  public final static native void sflph_config_set_accounts_order(String jarg1);
  public final static native long sflph_config_get_hook_settings();
  public final static native void sflph_config_set_hook_settings(long jarg1, StringMap jarg1_);
  public final static native long sflph_config_get_history();
  public final static native long sflph_config_get_tls_settings();
  public final static native void sflph_config_set_tls_settings(long jarg1, StringMap jarg1_);
  public final static native long sflph_config_get_ip2ip_details();
  public final static native long sflph_config_get_credentials(String jarg1);
  public final static native void sflph_config_set_credentials(String jarg1, long jarg2, VectMap jarg2_);
  public final static native String sflph_config_get_addr_from_interface_name(String jarg1);
  public final static native long sflph_config_get_all_ip_interface();
  public final static native long sflph_config_get_all_ip_interface_by_name();
  public final static native long sflph_config_get_shortcuts();
  public final static native void sflph_config_set_shortcuts(long jarg1, StringMap jarg1_);
  public final static native void sflph_config_set_volume(String jarg1, double jarg2);
  public final static native double sflph_config_get_volume(String jarg1);
  public final static native boolean sflph_config_check_for_private_key(String jarg1);
  public final static native boolean sflph_config_check_certificate_validity(String jarg1, String jarg2);
  public final static native boolean sflph_config_check_hostname_certificate(String jarg1, String jarg2);
  public final static native void delete_ConfigurationCallback(long jarg1);
  public final static native void ConfigurationCallback_configOnVolumeChange(long jarg1, ConfigurationCallback jarg1_, String jarg2, int jarg3);
  public final static native void ConfigurationCallback_configOnVolumeChangeSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_, String jarg2, int jarg3);
  public final static native void ConfigurationCallback_configOnAccountsChange(long jarg1, ConfigurationCallback jarg1_);
  public final static native void ConfigurationCallback_configOnAccountsChangeSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_);
  public final static native void ConfigurationCallback_configOnHistoryChange(long jarg1, ConfigurationCallback jarg1_);
  public final static native void ConfigurationCallback_configOnHistoryChangeSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_);
  public final static native void ConfigurationCallback_configOnStunStatusFail(long jarg1, ConfigurationCallback jarg1_, String jarg2);
  public final static native void ConfigurationCallback_configOnStunStatusFailSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_, String jarg2);
  public final static native void ConfigurationCallback_configOnRegistrationStateChange(long jarg1, ConfigurationCallback jarg1_, String jarg2, int jarg3);
  public final static native void ConfigurationCallback_configOnRegistrationStateChangeSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_, String jarg2, int jarg3);
  public final static native void ConfigurationCallback_configOnSipRegistrationStateChange(long jarg1, ConfigurationCallback jarg1_, String jarg2, String jarg3, int jarg4);
  public final static native void ConfigurationCallback_configOnSipRegistrationStateChangeSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_, String jarg2, String jarg3, int jarg4);
  public final static native void ConfigurationCallback_configOnError(long jarg1, ConfigurationCallback jarg1_, int jarg2);
  public final static native void ConfigurationCallback_configOnErrorSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_, int jarg2);
  public final static native long ConfigurationCallback_configGetHardwareAudioFormat(long jarg1, ConfigurationCallback jarg1_);
  public final static native long ConfigurationCallback_configGetHardwareAudioFormatSwigExplicitConfigurationCallback(long jarg1, ConfigurationCallback jarg1_);
  public final static native long new_ConfigurationCallback();
  public final static native void ConfigurationCallback_director_connect(ConfigurationCallback obj, long cptr, boolean mem_own, boolean weak_global);
  public final static native void ConfigurationCallback_change_ownership(ConfigurationCallback obj, long cptr, boolean take_or_release);
  public final static native void init(long jarg1, ConfigurationCallback jarg1_, long jarg2, Callback jarg2_);

  public static void SwigDirector_Callback_callOnStateChange(Callback self, String call_id, String state) {
    self.callOnStateChange(call_id, state);
  }
  public static void SwigDirector_Callback_callOnTransferFail(Callback self) {
    self.callOnTransferFail();
  }
  public static void SwigDirector_Callback_callOnTransferSuccess(Callback self) {
    self.callOnTransferSuccess();
  }
  public static void SwigDirector_Callback_callOnRecordPlaybackStopped(Callback self, String path) {
    self.callOnRecordPlaybackStopped(path);
  }
  public static void SwigDirector_Callback_callOnVoiceMailNotify(Callback self, String call_id, int nd_msg) {
    self.callOnVoiceMailNotify(call_id, nd_msg);
  }
  public static void SwigDirector_Callback_callOnIncomingMessage(Callback self, String id, String from, String msg) {
    self.callOnIncomingMessage(id, from, msg);
  }
  public static void SwigDirector_Callback_callOnIncomingCall(Callback self, String account_id, String call_id, String from) {
    self.callOnIncomingCall(account_id, call_id, from);
  }
  public static void SwigDirector_Callback_callOnRecordPlaybackFilepath(Callback self, String id, String filename) {
    self.callOnRecordPlaybackFilepath(id, filename);
  }
  public static void SwigDirector_Callback_callOnConferenceCreated(Callback self, String conf_id) {
    self.callOnConferenceCreated(conf_id);
  }
  public static void SwigDirector_Callback_callOnConferenceChanged(Callback self, String conf_id, String state) {
    self.callOnConferenceChanged(conf_id, state);
  }
  public static void SwigDirector_Callback_callOnUpdatePlaybackScale(Callback self, String filepath, int position, int scale) {
    self.callOnUpdatePlaybackScale(filepath, position, scale);
  }
  public static void SwigDirector_Callback_callOnConferenceRemove(Callback self, String conf_id) {
    self.callOnConferenceRemove(conf_id);
  }
  public static void SwigDirector_Callback_callOnNewCall(Callback self, String account_id, String call_id, String to) {
    self.callOnNewCall(account_id, call_id, to);
  }
  public static void SwigDirector_Callback_callOnSipCallStateChange(Callback self, String call_id, String state, int code) {
    self.callOnSipCallStateChange(call_id, state, code);
  }
  public static void SwigDirector_Callback_callOnRecordStateChange(Callback self, String call_id, int state) {
    self.callOnRecordStateChange(call_id, state);
  }
  public static void SwigDirector_Callback_callOnSecureSdesOn(Callback self, String call_id) {
    self.callOnSecureSdesOn(call_id);
  }
  public static void SwigDirector_Callback_callOnSecureSdesOff(Callback self, String call_id) {
    self.callOnSecureSdesOff(call_id);
  }
  public static void SwigDirector_Callback_callOnSecureZrtpOn(Callback self, String call_id, String cipher) {
    self.callOnSecureZrtpOn(call_id, cipher);
  }
  public static void SwigDirector_Callback_callOnSecureZrtpOff(Callback self, String call_id) {
    self.callOnSecureZrtpOff(call_id);
  }
  public static void SwigDirector_Callback_callOnShowSas(Callback self, String call_id, String sas, int verified) {
    self.callOnShowSas(call_id, sas, verified);
  }
  public static void SwigDirector_Callback_callOnZrtpNotSuppOther(Callback self, String call_id) {
    self.callOnZrtpNotSuppOther(call_id);
  }
  public static void SwigDirector_Callback_callOnZrtpNegotiationFail(Callback self, String call_id, String reason, String severity) {
    self.callOnZrtpNegotiationFail(call_id, reason, severity);
  }
  public static void SwigDirector_Callback_callOnRtcpReceiveReport(Callback self, String call_id, long stats) {
    self.callOnRtcpReceiveReport(call_id, new IntegerMap(stats, false));
  }
  public static void SwigDirector_ConfigurationCallback_configOnVolumeChange(ConfigurationCallback self, String device, int value) {
    self.configOnVolumeChange(device, value);
  }
  public static void SwigDirector_ConfigurationCallback_configOnAccountsChange(ConfigurationCallback self) {
    self.configOnAccountsChange();
  }
  public static void SwigDirector_ConfigurationCallback_configOnHistoryChange(ConfigurationCallback self) {
    self.configOnHistoryChange();
  }
  public static void SwigDirector_ConfigurationCallback_configOnStunStatusFail(ConfigurationCallback self, String account_id) {
    self.configOnStunStatusFail(account_id);
  }
  public static void SwigDirector_ConfigurationCallback_configOnRegistrationStateChange(ConfigurationCallback self, String account_id, int state) {
    self.configOnRegistrationStateChange(account_id, state);
  }
  public static void SwigDirector_ConfigurationCallback_configOnSipRegistrationStateChange(ConfigurationCallback self, String account_id, String state, int code) {
    self.configOnSipRegistrationStateChange(account_id, state, code);
  }
  public static void SwigDirector_ConfigurationCallback_configOnError(ConfigurationCallback self, int alert) {
    self.configOnError(alert);
  }
  public static long SwigDirector_ConfigurationCallback_configGetHardwareAudioFormat(ConfigurationCallback self) {
    return IntVect.getCPtr(self.configGetHardwareAudioFormat());
  }

  private final static native void swig_module_init();
  static {
    swig_module_init();
  }
}
