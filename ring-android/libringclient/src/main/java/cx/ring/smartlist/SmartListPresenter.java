/*
 *  Copyright (C) 2017 Savoir-faire Linux Inc.
 *
 *  Author: Hadrien De Sousa <hadrien.desousa@savoirfairelinux.com>
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
package cx.ring.smartlist;


import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import cx.ring.facades.ConversationFacade;
import cx.ring.model.Account;
import cx.ring.model.CallContact;
import cx.ring.model.Conversation;
import cx.ring.model.ConversationModel;
import cx.ring.model.HistoryCall;
import cx.ring.model.HistoryText;
import cx.ring.model.Phone;
import cx.ring.model.ServiceEvent;
import cx.ring.model.Uri;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import cx.ring.services.ContactService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.services.HistoryService;
import cx.ring.services.PreferencesService;
import cx.ring.services.PresenceService;
import cx.ring.utils.BlockchainInputHandler;
import cx.ring.utils.Log;
import cx.ring.utils.Observable;
import cx.ring.utils.Observer;
import cx.ring.utils.Tuple;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SmartListPresenter extends RootPresenter<SmartListView> implements Observer<ServiceEvent> {

    private static final String TAG = SmartListPresenter.class.getSimpleName();

    private AccountService mAccountService;

    private ContactService mContactService;

    private HistoryService mHistoryService;

    private PreferencesService mPreferencesService;

    private ConversationFacade mConversationFacade;

    private PresenceService mPresenceService;

    private DeviceRuntimeService mDeviceRuntimeService;

    private BlockchainInputHandler mBlockchainInputHandler;
    private String mLastBlockchainQuery = null;

    private ArrayList<SmartListViewModel> mSmartListViewModels;

    private List<ConversationModel> mConversationModels;

    private CallContact mCallContact;

    private String currentAccount;

    @Inject
    public SmartListPresenter(AccountService accountService, ContactService contactService,
                              HistoryService historyService, ConversationFacade conversationFacade,
                              PresenceService presenceService, PreferencesService sharedPreferencesService,
                              DeviceRuntimeService deviceRuntimeService) {
        this.mAccountService = accountService;
        this.mContactService = contactService;
        this.mHistoryService = historyService;
        this.mPreferencesService = sharedPreferencesService;
        this.mConversationFacade = conversationFacade;
        this.mPresenceService = presenceService;
        this.mDeviceRuntimeService = deviceRuntimeService;
    }

    @Override
    public void afterInjection() {

    }

    @Override
    public void unbindView() {
        super.unbindView();
        mAccountService.removeObserver(this);
        mConversationFacade.removeObserver(this);
        mPresenceService.removeObserver(this);
    }

    @Override
    public void bindView(SmartListView view) {
        super.bindView(view);
        mAccountService.addObserver(this);
        mConversationFacade.addObserver(this);
        mPresenceService.addObserver(this);
    }

    public void init() {
        currentAccount = mAccountService.getCurrentAccount().getAccountID();
        loadConversations(currentAccount);
    }

    private void loadConversations(String accountId) {
        if (mSmartListViewModels == null) {
            mSmartListViewModels = new ArrayList<>();
        }
        mSmartListViewModels.clear();
        compositeDisposable.add(mHistoryService.getConversationsForAccount(accountId)
                .flatMapObservable(new Function<List<ConversationModel>, ObservableSource<ConversationModel>>() {
                    @Override
                    public ObservableSource<ConversationModel> apply(@NonNull List<ConversationModel> conversationModels) throws Exception {
                        mConversationModels = conversationModels;
                        Log.d(TAG, Integer.toString(conversationModels.size()));
                        return io.reactivex.Observable.fromIterable(conversationModels);
                    }
                })
                .flatMap(new Function<ConversationModel, ObservableSource<SmartListViewModel>>() {
                    @Override
                    public ObservableSource<SmartListViewModel> apply(@NonNull final ConversationModel conversationModel) throws Exception {
                        return io.reactivex.Observable.fromCallable(new Callable<SmartListViewModel>() {
                            @Override
                            public SmartListViewModel call() throws Exception {
                                return createViewModel(conversationModel);
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.computation())
                .subscribeWith(new DisposableObserver<SmartListViewModel>() {
                    @Override
                    public void onNext(@NonNull SmartListViewModel smartListViewModel) {
                        if (!mSmartListViewModels.contains(smartListViewModel)) {
                            mSmartListViewModels.add(smartListViewModel);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG, e.toString());
                        getView().setLoading(false);
                        getView().displayNoConversationMessage();
                    }

                    @Override
                    public void onComplete() {
                        Collections.sort(mSmartListViewModels, new Comparator<SmartListViewModel>() {
                            @Override
                            public int compare(SmartListViewModel lhs, SmartListViewModel rhs) {
                                return (int) ((rhs.getLastInteractionTime() - lhs.getLastInteractionTime()) / 1000l);
                            }
                        });
                        getView().updateList(mSmartListViewModels);
                        getView().setLoading(false);
                        getView().hideNoConversationMessage();
                        subscribePresence();
                    }
                }));
    }

    private SmartListViewModel createViewModel(ConversationModel conversationModel) throws SQLException {
        CallContact callContact = mContactService.getContact(new Uri(conversationModel.getContactId()));
        Tuple<String, byte[]> tuple = mContactService.loadContactData(callContact);

        HistoryText lastText = mHistoryService.getLastHistoryText(conversationModel.getId());
        HistoryCall lastCall = mHistoryService.getLastHistoryCall(conversationModel.getId());
        long lastInteractionLong = 0;
        int lastEntryType = 0;
        String lastInteraction = "";
        boolean hasUnreadMessage = lastText != null && !lastText.isRead();

        long lastTextTimestamp = lastText != null ? lastText.getDate().getTime() : 0;
        long lastCallTimestamp = lastCall != null ? lastCall.getEndDate().getTime() : 0;
        if (lastTextTimestamp > 0 && lastTextTimestamp > lastCallTimestamp) {
            String msgString = lastText.getMessage();
            if (msgString != null && !msgString.isEmpty() && msgString.contains("\n")) {
                int lastIndexOfChar = msgString.lastIndexOf("\n");
                if (lastIndexOfChar + 1 < msgString.length()) {
                    msgString = msgString.substring(msgString.lastIndexOf("\n") + 1);
                }
            }
            lastInteractionLong = lastTextTimestamp;
            lastEntryType = lastText.isIncoming() ? SmartListViewModel.TYPE_INCOMING_MESSAGE : SmartListViewModel.TYPE_OUTGOING_MESSAGE;
            lastInteraction = msgString;

        } else if (lastCallTimestamp > 0) {
            lastInteractionLong = lastCallTimestamp;
            lastEntryType = lastCall.isIncoming() ? SmartListViewModel.TYPE_INCOMING_CALL : SmartListViewModel.TYPE_OUTGOING_CALL;
            lastInteraction = lastCall.getDurationString();
        }

        Log.d(TAG, "loadConversationData: "
                + "accountId: " + conversationModel.getAccountId() + "\n"
                + "contactId: " + conversationModel.getContactId() + "\n"
                + " " + lastInteraction);

        SmartListViewModel smartListViewModel = new SmartListViewModel(conversationModel.getId(),
                callContact,
                tuple.first,
                tuple.second,
                lastInteractionLong,
                lastEntryType,
                lastInteraction,
                hasUnreadMessage);

        smartListViewModel.setOnline(mPresenceService.isBuddyOnline(callContact.getIds().get(0)));
        return smartListViewModel;
    }

    public void refresh() {
        refreshConnectivity();
        init();
        searchForRingIdInBlockchain();
        getView().hideSearchRow();
    }

    private void refreshConnectivity() {
        boolean mobileDataAllowed = mPreferencesService.getUserSettings().isAllowMobileData();

        boolean isConnected = mDeviceRuntimeService.isConnectedWifi()
                || (mDeviceRuntimeService.isConnectedMobile() && mobileDataAllowed);

        boolean isMobileAndNotAllowed = mDeviceRuntimeService.isConnectedMobile()
                && !mobileDataAllowed;

        if (isConnected) {
            getView().hideErrorPanel();
        } else {
            if (isMobileAndNotAllowed) {
                getView().displayMobileDataPanel();
            } else {
                getView().displayNetworkErrorPanel();
            }
        }
    }

    public void queryTextChanged(String query) {
        if (query.equals("")) {
            getView().hideSearchRow();
        } else {
            Account currentAccount = mAccountService.getCurrentAccount();
            if (currentAccount == null) {
                return;
            }

            if (currentAccount.isSip()) {
                // sip search
                mCallContact = CallContact.buildUnknown(query, null);
                getView().displayNewContactRowWithName(query);
            } else {

                Uri uri = new Uri(query);
                if (uri.isRingId()) {
                    mCallContact = CallContact.buildUnknown(query, null);
                    getView().displayNewContactRowWithName(query);
                } else {
                    getView().hideSearchRow();
                }

                // Ring search
                if (mBlockchainInputHandler == null) {
                    mBlockchainInputHandler = new BlockchainInputHandler(new WeakReference<>(mAccountService));
                }

                // searching for a ringId or a blockchained username
                if (!mBlockchainInputHandler.isAlive()) {
                    mBlockchainInputHandler = new BlockchainInputHandler(new WeakReference<>(mAccountService));
                }

                mBlockchainInputHandler.enqueueNextLookup(query);
                mLastBlockchainQuery = query;
            }
        }

        getView().updateList(filter(mSmartListViewModels, query));
        getView().setLoading(false);
    }

    public void newContactClicked() {
        if (mCallContact == null) {
            return;
        }
        startConversation(mCallContact);
    }

    public void conversationClicked(SmartListViewModel smartListViewModel) {
        ConversationModel conversation = getConversationById(mConversationModels, smartListViewModel.getUuid());
        if (conversation != null && conversation.getContactId() != null) {
            startConversation(mContactService.getContact(new Uri(conversation.getContactId())));
        }
    }

    //TODO
    public void conversationLongClicked(SmartListViewModel smartListViewModel) {
/*        Conversation conversation = getConversationByUuid(mConversations, smartListViewModel.getUuid());
        if (conversation != null) {
            getView().displayConversationDialog(conversation);
        }*/
    }

    public void photoClicked(SmartListViewModel smartListViewModel) {
        ConversationModel conversation = getConversationById(mConversationModels, smartListViewModel.getUuid());
        if (conversation != null && conversation.getContactId() != null) {
            getView().goToContact(mContactService.getContact(new Uri(conversation.getContactId())));
        }
    }

    public void quickCallClicked() {
        if (mCallContact != null) {
            if (mCallContact.getPhones().size() > 1) {
                CharSequence numbers[] = new CharSequence[mCallContact.getPhones().size()];
                int i = 0;
                for (Phone p : mCallContact.getPhones()) {
                    numbers[i++] = p.getNumber().getRawUriString();
                }

                getView().displayChooseNumberDialog(numbers);
            } else {
                getView().goToCallActivity(mCallContact.getPhones().get(0).getNumber().getRawUriString());
            }
        }
    }

    public void fabButtonClicked() {
        getView().displayMenuItem();
    }

    public void startConversation(CallContact c) {
        mContactService.addContact(c);
        getView().goToConversation(c);
    }

    public void deleteConversation(Conversation conversation) {
        mHistoryService.clearHistoryForConversation(conversation);
    }

    public void clickQRSearch() {
        getView().goToQRActivity();
    }

    private void searchForRingIdInBlockchain() {
        List<Conversation> conversations = mConversationFacade.getConversationsList();
        for (Conversation conversation : conversations) {
            CallContact contact = conversation.getContact();
            if (contact == null) {
                continue;
            }

            Uri contactUri = new Uri(contact.getIds().get(0));
            if (!contactUri.isRingId()) {
                continue;
            }

            if (contact.getPhones().isEmpty()) {
                mAccountService.lookupName("", "", contact.getDisplayName());
            } else {
                Phone phone = contact.getPhones().get(0);
                if (phone.getNumber().isRingId()) {
                    mAccountService.lookupAddress("", "", phone.getNumber().getHost());
                }
            }
        }
    }

    private ArrayList<SmartListViewModel> filter(ArrayList<SmartListViewModel> list, String query) {
        ArrayList<SmartListViewModel> filteredList = new ArrayList<>();
        if (list == null || list.size() == 0) {
            return filteredList;
        }
        for (SmartListViewModel smartListViewModel : list) {
            if (smartListViewModel.getContactName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(smartListViewModel);
            }
        }
        return filteredList;
    }

    private ConversationModel getConversationById(List<ConversationModel> conversationModels, long id) {
        for (ConversationModel conversation : conversationModels) {
            if (conversation.getId() == id) {
                return conversation;
            }
        }
        return null;
    }


    private void parseEventState(String name, String address, int state) {
        switch (state) {
            case 0:
                // on found
                if (mLastBlockchainQuery != null && mLastBlockchainQuery.equals(name)) {
                    mCallContact = CallContact.buildUnknown(name, address);
                    getView().displayNewContactRowWithName(name);
                    mLastBlockchainQuery = null;
                } else {
                    if (name.equals("") || address.equals("")) {
                        return;
                    }
                    getView().hideSearchRow();
                    mConversationFacade.updateConversationContactWithRingId(name, address);
                }
                break;
            case 1:
                // invalid name
                Uri uriName = new Uri(name);
                if (uriName.isRingId()
                        && mLastBlockchainQuery != null
                        && mLastBlockchainQuery.equals(name)) {
                    mCallContact = CallContact.buildUnknown(name, address);
                    getView().displayNewContactRowWithName(name);
                } else {
                    getView().hideSearchRow();
                }
                break;
            default:
                // on error
                Uri uriAddress = new Uri(address);
                if (uriAddress.isRingId()
                        && mLastBlockchainQuery != null
                        && mLastBlockchainQuery.equals(name)) {
                    mCallContact = CallContact.buildUnknown(name, address);
                    getView().displayNewContactRowWithName(name);
                } else {
                    getView().hideSearchRow();
                }
                break;
        }
    }

    public void removeContact(String accountId, String contactId) {
        String[] split = contactId.split(":");
        if (split.length > 1 && split[0].equals("ring")) {
            mContactService.removeContact(accountId, split[1]);
        }
    }

    private void subscribePresence() {
        if (mAccountService.getCurrentAccount() == null) {
            return;
        }
        String accountId = mAccountService.getCurrentAccount().getAccountID();
        Set<String> keys = mConversationFacade.getConversations().keySet();
        for (String key : keys) {
            Uri uri = new Uri(key);
            if (uri.isRingId()) {
                mPresenceService.subscribeBuddy(accountId, key, true);
            } else {
                Log.i(TAG, "Trying to subscribe to an invalid uri " + key);
            }
        }
    }

    @Override
    public void update(Observable observable, ServiceEvent event) {
        if (event == null) {
            return;
        }

        switch (event.getEventType()) {
            case REGISTERED_NAME_FOUND:
                String name = event.getEventInput(ServiceEvent.EventInput.NAME, String.class);
                if (mLastBlockchainQuery != null
                        && (mLastBlockchainQuery.equals("") || !mLastBlockchainQuery.equals(name))) {
                    return;
                }
                String address = event.getEventInput(ServiceEvent.EventInput.ADDRESS, String.class);
                int state = event.getEventInput(ServiceEvent.EventInput.STATE, Integer.class);
                parseEventState(name, address, state);
                break;
            case REGISTRATION_STATE_CHANGED:
                refreshConnectivity();
                break;
            case HISTORY_LOADED:
                searchForRingIdInBlockchain();
                break;
            case CONVERSATIONS_CHANGED:
            case INCOMING_MESSAGE:
            case INCOMING_CALL:
                loadConversations(currentAccount);
                getView().scrollToTop();
                break;
            case USERNAME_CHANGED:
                loadConversations(currentAccount);
                break;
        }

        if (observable instanceof PresenceService) {
            switch (event.getEventType()) {
                case NEW_BUDDY_NOTIFICATION:
                    loadConversations(currentAccount);
                    break;
            }
        }
    }
}
