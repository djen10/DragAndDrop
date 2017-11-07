package com.example.justin.draganddrop.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.justin.draganddrop.data.ContactData;

/**
 * Created by Justin on 2017-10-13.
 */

public class ContactUtil {
    private static Context mContext;

    public static void GetContacts(Context context){
        mContext = context;
        String [] arrProjection = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        String [] arrPhoneProjection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        String [] arrEmailProjection = {ContactsContract.CommonDataKinds.Email.DATA};
        String phone = "";

        Cursor clsCursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, arrProjection, ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1", null, null);
        while(clsCursor.moveToNext()){
            String strContactId = clsCursor.getString(0);
            Log.e("TEST","ID : " + clsCursor.getString(0));
            Log.e("TEST","Name : " + clsCursor.getString(1));

            ContactData.Id.add(clsCursor.getString(0));
            ContactData.Name.add(clsCursor.getString(1));

            Cursor clsPhoneCursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrPhoneProjection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + strContactId, null, null);
            while(clsPhoneCursor.moveToNext()){
                Log.e("TEST", "Phone : " + clsPhoneCursor.getString(0));
                phone = clsPhoneCursor.getString(0);
            }

            ContactData.PhoneNumber.add(phone);
            clsPhoneCursor.close();
        }

    }
}
