/*
 *  Copyright (C) 2015 Savoir-Faire Linux Inc.
 *
 *  Author: Alexandre Lision <alexandre.lision@savoirfairelinux.com>
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

package cx.ring.loaders;

import java.util.ArrayList;

import cx.ring.model.CallContact;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.OperationCanceledException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.util.LongSparseArray;

public class ContactsLoader extends AsyncTaskLoader<ContactsLoader.Result>
{
    private static final String TAG = ContactsLoader.class.getSimpleName();

    public static class Result {
        public final ArrayList<CallContact> contacts = new ArrayList<>(512);
        public final ArrayList<CallContact> starred = new ArrayList<>();
    }

    static private final String[] CONTACTS_ID_PROJECTION = new String[] { Contacts._ID };
    static private final String[] CONTACTS_SUMMARY_PROJECTION = new String[] { Contacts._ID, Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME, Contacts.PHOTO_ID, Contacts.STARRED};
    static private final String[] CONTACTS_SIP_PROJECTION = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.Data.MIMETYPE, SipAddress.SIP_ADDRESS, SipAddress.TYPE };
    static private final String SELECT = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND (" + Contacts.HAS_PHONE_NUMBER + "=1) AND (" + Contacts.DISPLAY_NAME + " != '' ))";

    private final Uri baseUri;
    private final LongSparseArray<CallContact> filterFrom;
    private volatile boolean abandon = false;

    public ContactsLoader(Context context) {
        this(context, null, null);
    }

    public ContactsLoader(Context context, Uri base, LongSparseArray < CallContact > filter) {
        super(context);
        baseUri = base;
        filterFrom = filter;
    }

    private boolean checkCancel() {
        return checkCancel(null);
    }
    private boolean checkCancel(Cursor c) {
        if (isLoadInBackgroundCanceled()) {
            Log.w(TAG, "Cancelled");
            if (c != null)
                c.close();
            throw new OperationCanceledException();
        }
        if (abandon) {
            Log.w(TAG, "Abandoned");
            if (c != null)
                c.close();
            return true;
        }
        return false;
    }

    @Override
    public Result loadInBackground() {
        ContentResolver cr = getContext().getContentResolver();

        long startTime = System.nanoTime();
        final Result res = new Result();

        if (baseUri != null) {
            Cursor result = cr.query(baseUri, CONTACTS_ID_PROJECTION, SELECT, null, Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
            if (result == null)
                return res;

            int iID = result.getColumnIndex(Contacts._ID);
            long[] filter_ids = new long[result.getCount()];
            int i = 0;
            while (result.moveToNext()) {
                long cid = result.getLong(iID);
                filter_ids[i++] = cid;
            }
            result.close();
            res.contacts.ensureCapacity(filter_ids.length);
            int n = filter_ids.length;
            for (i = 0; i < n; i++) {
                CallContact c = filterFrom.get(filter_ids[i]);
                res.contacts.add(c);
                if (c.isStared())
                    res.starred.add(c);
            }
        }
        else {
            StringBuilder cids = new StringBuilder();
            LongSparseArray<CallContact> cache;
            {
                Cursor c = cr.query(ContactsContract.Data.CONTENT_URI, CONTACTS_SIP_PROJECTION,
                        ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{Phone.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE}, null);
                if (c != null) {

                    cache = new LongSparseArray<>(c.getCount());
                    cids.ensureCapacity(c.getCount() * 4);

                    final int iID = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                    final int iMime = c.getColumnIndex(ContactsContract.Data.MIMETYPE);
                    final int iNumber = c.getColumnIndex(SipAddress.SIP_ADDRESS);
                    final int iType = c.getColumnIndex(SipAddress.TYPE);
                    while (c.moveToNext()) {
                        long id = c.getLong(iID);
                        CallContact contact = cache.get(id);
                        if (contact == null) {
                            contact = new CallContact(id);
                            cache.put(id, contact);
                            if (cids.length() > 0)
                                cids.append(",");
                            cids.append(id);
                        }
                        if (Phone.CONTENT_ITEM_TYPE.equals(c.getString(iMime))) {
                            //Log.w(TAG, "Phone for " + id + " :" + cSip.getString(iNumber));
                            contact.addPhoneNumber(c.getString(iNumber), c.getInt(iType));
                        } else {
                            //Log.w(TAG, "SIP Phone for " + id + " :" + cSip.getString(iNumber));
                            contact.addNumber(c.getString(iNumber), c.getInt(iType), CallContact.NumberType.SIP);
                        }
                    }
                    c.close();
                } else {
                    cache = new LongSparseArray<>();
                }
            }
            if (checkCancel())
                return null;
            {
                Cursor c = cr.query(Contacts.CONTENT_URI, CONTACTS_SUMMARY_PROJECTION,
                        ContactsContract.Contacts._ID + " in (" + cids.toString() + ")", null,
                        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
                if (c != null) {
                    final int iID = c.getColumnIndex(Contacts._ID);
                    final int iKey = c.getColumnIndex(ContactsContract.Data.LOOKUP_KEY);
                    final int iName = c.getColumnIndex(Contacts.DISPLAY_NAME);
                    final int iPhoto = c.getColumnIndex(Contacts.PHOTO_ID);
                    final int iStarred = c.getColumnIndex(Contacts.STARRED);
                    res.contacts.ensureCapacity(c.getCount());
                    while (c.moveToNext()) {
                        long id = c.getLong(iID);
                        CallContact contact = cache.get(id);
                        if (contact == null)
                            Log.w(TAG, "Can't find contact with ID " + id);
                        else {
                            contact.setContactInfos(c.getString(iKey), c.getString(iName), c.getLong(iPhoto));
                            res.contacts.add(contact);
                            if (c.getInt(iStarred) != 0) {
                                res.starred.add(contact);
                                contact.setStared();
                            }
                        }
                    }
                    c.close();
                }
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        Log.w(TAG, "Loading " + res.contacts.size() + " system contacts took " + duration / 1000. + "s");

        return checkCancel() ? null : res;
    }


    @Override
    protected void onAbandon() {
        super.onAbandon();
        abandon = true;
    }
}
