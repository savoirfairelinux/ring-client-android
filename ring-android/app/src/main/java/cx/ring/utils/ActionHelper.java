/*
 *  Copyright (C) 2004-2020 Savoir-faire Linux Inc.
 *
 *  Author: Alexandre Lision <alexandre.lision@savoirfairelinux.com>
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
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package cx.ring.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import cx.ring.R;
import net.jami.model.Contact;
import net.jami.model.Conversation;
import net.jami.model.Uri;
import net.jami.utils.Log;

public class ActionHelper {

    public static final String TAG = ActionHelper.class.getSimpleName();
    public static final int ACTION_COPY = 0;
    public static final int ACTION_CLEAR = 1;
    public static final int ACTION_DELETE = 2;
    public static final int ACTION_BLOCK = 3;

    private ActionHelper() {
    }

    public static void launchClearAction(final Context context,
                                                 final Uri uri,
                                                 final Conversation.ConversationActionCallback callback) {
        if (context == null) {
            net.jami.utils.Log.d(TAG, "launchClearAction: activity is null");
            return;
        }

        if (uri == null) {
            net.jami.utils.Log.d(TAG, "launchClearAction: conversation is null");
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.conversation_action_history_clear_title)
                .setMessage(R.string.conversation_action_history_clear_message)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    if (callback != null) {
                        callback.clearConversation(uri);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
                    /* Terminate with no action */
                })
                .show();
    }

    public static void launchDeleteAction(final Context context,
                                                 final Uri uri,
                                                 final Conversation.ConversationActionCallback callback) {
        if (context == null) {
            Log.d(TAG, "launchDeleteAction: activity is null");
            return;
        }

        if (uri == null) {
            net.jami.utils.Log.d(TAG, "launchDeleteAction: conversation is null");
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.conversation_action_remove_this_title)
                .setMessage(R.string.conversation_action_remove_this_message)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    if (callback != null) {
                        callback.removeConversation(uri);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
                    /* Terminate with no action */
                })
                .show();
    }

    public static void launchCopyNumberToClipboardFromContact(final Context context,
                                                              final Uri callContact,
                                                              final Conversation.ConversationActionCallback callback) {
        /*if (callContact == null) {
            Log.d(TAG, "launchCopyNumberToClipboardFromContact: callContact is null");
            return;
        }

        if (context == null) {
            Log.d(TAG, "launchCopyNumberToClipboardFromContact: activity is null");
            return;
        }

        if (callContact.getPhones().isEmpty()) {
            Log.d(TAG, "launchCopyNumberToClipboardFromContact: no number to copy");
            return;
        }

        if (callContact.getPhones().size() == 1 && callback != null) {
            String number = callContact.getPhones().get(0).getNumber().toString();
            callback.copyContactNumberToClipboard(number);
        } else {
            final NumberAdapter adapter = new NumberAdapter(context, callContact, true);
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.conversation_action_select_peer_number)
                    .setAdapter(adapter, (dialog, which) -> {
                        if (callback != null) {
                            Phone selectedPhone = (Phone) adapter.getItem(which);
                            callback.copyContactNumberToClipboard(selectedPhone.getNumber().toString());
                        }
                    })
                    .create();
            final int listViewSidePadding = (int) context
                    .getResources()
                    .getDimension(R.dimen.alert_dialog_side_padding_list_view);
            alertDialog.getListView().setPadding(listViewSidePadding, 0, listViewSidePadding, 0);
            alertDialog.show();
        }*/
    }

    public static Intent getAddNumberIntentForContact(Contact contact) {
        final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);

        ArrayList<ContentValues> data = new ArrayList<>();
        ContentValues values = new ContentValues();

        Uri number = contact.getUri();
        if (number.isHexId()) {
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Im.DATA, number.getRawUriString());
            values.put(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM);
            values.put(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, "Ring");
        } else {
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS, number.getRawUriString());
        }
        data.add(values);
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
        return intent;
    }

    public static void displayContact(Context context, Contact contact) {
        if (context == null) {
            net.jami.utils.Log.d(TAG, "displayContact: context is null");
            return;
        }

        if (contact.getId() != Contact.UNKNOWN_ID) {
            net.jami.utils.Log.d(TAG, "displayContact: contact is known, displaying...");
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                android.net.Uri uri = android.net.Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                        String.valueOf(contact.getId()));
                intent.setData(uri);
                context.startActivity(intent);
            } catch (ActivityNotFoundException exc) {
                net.jami.utils.Log.e(TAG, "Error displaying contact", exc);
            }
        }
    }

    public static String getShortenedNumber(String number) {
        if (number != null && !number.isEmpty() && number.length() > 18) {
            int size = number.length();
            return number.substring(0, 9).concat("\u2026").concat(number.substring(size - 9, size));
        }
        return number;
    }
}