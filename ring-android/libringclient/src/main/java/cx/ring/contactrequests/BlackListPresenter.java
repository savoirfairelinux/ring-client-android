/*
 *  Copyright (C) 2017 Savoir-faire Linux Inc.
 *
 *  Author: Aline Bonnet <aline.bonnet@savoirfairelinux.com>
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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cx.ring.contactrequests;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import cx.ring.model.Account;
import cx.ring.model.CallContact;
import cx.ring.model.ServiceEvent;
import cx.ring.mvp.GenericView;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import cx.ring.services.ContactService;
import cx.ring.utils.Log;
import cx.ring.utils.Observable;
import cx.ring.utils.Observer;

public class BlackListPresenter extends RootPresenter<GenericView<BlackListViewModel>> implements Observer<ServiceEvent> {
    static private final String TAG = BlackListPresenter.class.getSimpleName();

    private AccountService mAccountService;
    private ContactService mContactService;

    @Inject
    public BlackListPresenter(AccountService accountService, ContactService contactService) {
        mAccountService = accountService;
        mContactService = contactService;
    }

    @Override
    public void afterInjection() {

    }

    @Override
    public void bindView(GenericView<BlackListViewModel> view) {
        mAccountService.addObserver(this);
        super.bindView(view);
        updateList();
    }

    @Override
    public void unbindView() {
        mAccountService.removeObserver(this);
        super.unbindView();
    }

    public void updateList() {
        if (getView() == null) {
            return;
        }

        Account account = mAccountService.getCurrentAccount();
        if (account == null) {
            return;
        }

        ArrayList<Map<String, String>> list = new ArrayList<>(mContactService.getContacts(account.getAccountID()));
        ArrayList<CallContact> contacts = new ArrayList<>();
        for (Map<String, String> contact : list) {
            if (contact.containsKey("banned") && contact.get("banned").equals("true") && contact.containsKey("id")) {
                contacts.add(CallContact.buildUnknown(contact.get("id")));
            }
        }

        getView().showViewModel(new BlackListViewModel(contacts));
    }

    public void unblock(String contactId) {
        Account account = mAccountService.getCurrentAccount();
        if (account == null || contactId == null) {
            return;
        }

        mContactService.addContact(account.getAccountID(), contactId);
        updateList();
    }

    @Override
    public void update(Observable observable, ServiceEvent event) {
        if (event == null) {
            return;
        }
        Log.d(TAG, "update " + event.getEventType());

        switch (event.getEventType()) {
            case ACCOUNTS_CHANGED:
                updateList();
                break;
            default:
                Log.d(TAG, "Event " + event.getEventType() + " is not handled here");
                break;
        }
    }
}
