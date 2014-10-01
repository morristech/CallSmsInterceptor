package com.intercepter.phone;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.widget.Toast;

public class SmsContentObserver extends ContentObserver {

    private static final String CONTENT_SMS = "content://sms/";
    private static final String SMS_ID = "_id";
    private static final String SMS_TYPE = "type";
    private static final String SMS_ADDRESS = "address";
    private static final String SMS_MESSAGE_TYPE_FAILED = "5";
    private static final String SMS_MESSAGE_TYPE_QUEUED = "6";
    private static final String SMS_MESSAGE_TYPE_SENT = "2";

    Context context;
    private Uri smsUri;

    public SmsContentObserver(Context context) {
        super(null);
        this.context = context;
        smsUri = Uri.parse(CONTENT_SMS);
    }


    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        Cursor cursor = context.getContentResolver().query(smsUri, null, null, null, null);
        if (cursor.moveToNext()) {
            String messageId = cursor.getString(cursor.getColumnIndex(SMS_ID));
            String messageType = cursor.getString(cursor.getColumnIndex(SMS_TYPE));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(SMS_ADDRESS));
            if (MainActivity.isInNumbers(phoneNumber)) {
                if (messageType.equals(SMS_MESSAGE_TYPE_QUEUED) || messageType.equals(SMS_MESSAGE_TYPE_SENT)) {
                    ContentValues values = new ContentValues();
                    values.put(SMS_TYPE, SMS_MESSAGE_TYPE_FAILED);
                    String where = Telephony.Sms._ID + "=" + messageId;
                    context.getContentResolver().update(smsUri, values, where, null);
                    Toast.makeText(context, phoneNumber + " message state changed", Toast.LENGTH_LONG).show();
                } else if (messageType.equals(SMS_MESSAGE_TYPE_FAILED)) {
                    String where = SMS_ID + "=?";
                    context.getContentResolver().delete(smsUri, where, new String[]{SMS_ID});
                    Toast.makeText(context, phoneNumber + " message blocked", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
