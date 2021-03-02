package com.ngm.bussnisscard;

public class SettingsEntry {
    final static String KEY_SEND_AFTER_CALL_END = "send_after_call_end";
    final static String KEY_SEND_AFTER_MISSED_CALL = "send_after_missed_call";
    final static String KEY_CONTACT_TYPE = "contact_type";

    public boolean CheckAfterCallEnd;
    public boolean CheckAfterMissedCall;
    public SettingsActivity.ContactType ContactType;
}
