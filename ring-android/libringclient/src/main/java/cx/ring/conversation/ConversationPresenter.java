package cx.ring.conversation;

import java.util.List;

import javax.inject.Inject;

import cx.ring.facades.ConversationFacade;
import cx.ring.model.Account;
import cx.ring.model.CallContact;
import cx.ring.model.Conference;
import cx.ring.model.Conversation;
import cx.ring.model.Phone;
import cx.ring.model.ServiceEvent;
import cx.ring.model.Uri;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import cx.ring.services.ContactService;
import cx.ring.services.HistoryService;
import cx.ring.utils.Observable;
import cx.ring.utils.Observer;
import cx.ring.utils.Tuple;

/**
 * Created by hdsousa on 17-03-21.
 */

public class ConversationPresenter extends RootPresenter<ConversationView> implements Observer<ServiceEvent> {

    private ContactService mContactService;
    private AccountService mAccountService;
    private ConversationFacade mConversationFacade;
    private HistoryService mHistoryService;

    private Conversation mConversation;
    private String mConversationId;
    private Uri mPreferredNumber;

    @Inject
    public ConversationPresenter(ContactService mContactService,
                                 AccountService mAccountService,
                                 ConversationFacade mConversationFacade,
                                 HistoryService mHistoryService) {
        this.mContactService = mContactService;
        this.mAccountService = mAccountService;
        this.mConversationFacade = mConversationFacade;
        this.mHistoryService = mHistoryService;
    }

    @Override
    public void afterInjection() {

    }

    @Override
    public void unbindView() {
        super.unbindView();
        mAccountService.removeObserver(this);
        mConversationFacade.removeObserver(this);
    }

    public void init(String conversationId, Uri number) {
        mConversationId = conversationId;
        mPreferredNumber = number;

        mAccountService.addObserver(this);
        mConversationFacade.addObserver(this);

    }

    public void pause() {
        if (mConversation != null) {
            mConversationFacade.readConversation(mConversation);
            mConversation.setVisible(false);
        }
    }

    public void resume() {
        if (mConversation != null) {
            mConversation.setVisible(true);
            mConversationFacade.readConversation(mConversation);
        }
        loadConversation();
    }

    public void prepareMenu() {
        getView().displayAddContact(mConversation != null && mConversation.getContact().getId() < 0);
    }

    public void addContact() {
        getView().goToAddContact(mConversation.getContact());
    }

    public void deleteAction() {
        getView().displayDeleteDialog(mConversation);
    }

    public void copyToClipboard() {
        getView().displayCopyToClipboard(mConversation.getContact());
    }

    public void sendTextMessage(String message, Uri number) {
        if (message != null && !message.equals("")) {
            getView().clearMsgEdit();
            Conference conference = mConversation == null ? null : mConversation.getCurrentCall();
            if (conference == null || !conference.isOnGoing()) {
                Tuple<Account, Uri> guess = guess(number);
                if (guess == null || guess.first == null) {
                    return;
                }
                mConversationFacade.sendTextMessage(guess.first.getAccountID(), guess.second, message);
            } else {
                mConversationFacade.sendTextMessage(conference, message);
            }
        }
    }

    public void clickOnGoingPane() {
        getView().goToCallActivity(mConversation.getCurrentCall().getId());
    }

    public void callWithVideo(boolean video, Uri number) {
        if (number == null) {
            number = mPreferredNumber;
        }

        Conference conf = mConversation.getCurrentCall();
        if (conf != null) {
            getView().goToCallActivity(conf.getId());
        } else {
            Tuple<Account, Uri> guess = guess(number);
            if (guess != null && guess.first != null) {
                getView().goToCallActivityWithResult(guess, video);
            }
        }
    }

    public void deleteConversation() {
        mHistoryService.clearHistoryForConversation(mConversation);
    }

    public void loadConversation() {
        mConversation = mConversationFacade.getConversationById(mConversationId);

        long contactId = CallContact.contactIdFromId(mConversationId);
        CallContact contact = null;
        if (contactId >= 0) {
            contact = mContactService.findContactById(contactId);
        }
        if (contact == null) {
            Uri convUri = new Uri(mConversationId);
            if (!mPreferredNumber.isEmpty()) {
                contact = mContactService.findContactByNumber(mPreferredNumber.getRawUriString());
                if (contact == null) {
                    contact = CallContact.buildUnknown(convUri);
                }
            } else {
                contact = mContactService.findContactByNumber(convUri.getRawUriString());
                if (contact == null) {
                    contact = CallContact.buildUnknown(convUri);
                    mPreferredNumber = contact.getPhones().get(0).getNumber();
                } else {
                    mPreferredNumber = convUri;
                }
            }
        }
        mConversation = mConversationFacade.startConversation(contact);

        if (!mConversation.getContact().getPhones().isEmpty()) {
            contact = mContactService.getContact(mConversation.getContact().getPhones().get(0).getNumber());
            if (contact != null) {
                mConversation.setContact(contact);
            }
            getView().displayContactName(mConversation.getContact().getDisplayName());
        }

        getView().displayOnGoingCallPane(mConversation.getCurrentCall() == null);

        if (mConversation.getContact().getPhones().size() > 1) {
            for (Phone phone : mConversation.getContact().getPhones()) {
                if (phone.getNumber() != null && phone.getNumber().isRingId()) {
                    mAccountService.lookupAddress("", "", phone.getNumber().getRawUriString());
                }
            }
            getView().displayNumberSpinner(mConversation, mPreferredNumber);
        } else {
            getView().hideNumberSpinner();
            mPreferredNumber = mConversation.getContact().getPhones().get(0).getNumber();
        }

        getView().refreshView(mConversation, mPreferredNumber);
    }

    /**
     * Guess account and number to use to initiate a call
     */
    private Tuple<Account, Uri> guess(Uri number) {
        Account account = mAccountService.getAccount(mConversation.getLastAccountUsed());

        // Guess account from number
        if (account == null && number != null) {
            account = mAccountService.guessAccount(number);
        }

        // Guess number from account/call history
        if (account != null && number == null) {
            number = new Uri(mConversation.getLastNumberUsed(account.getAccountID()));
        }

        // If no account found, use first active
        if (account == null) {
            List<Account> accounts = mAccountService.getAccounts();
            if (accounts.isEmpty()) {
                return null;
            } else
                account = accounts.get(0);
        }

        // If no number found, use first from contact
        if (number == null || number.isEmpty()) {
            number = mConversation.getContact().getPhones().get(0).getNumber();
        }

        return new Tuple<>(account, number);
    }


    @Override
    public void update(Observable observable, ServiceEvent arg) {
        if (observable instanceof AccountService && arg != null) {
            if (arg.getEventType() == ServiceEvent.EventType.REGISTERED_NAME_FOUND) {
                final String name = arg.getEventInput(ServiceEvent.EventInput.NAME, String.class);
                final String address = arg.getEventInput(ServiceEvent.EventInput.ADDRESS, String.class);
                final int state = arg.getEventInput(ServiceEvent.EventInput.STATE, Integer.class);

                getView().updateView(address, name, state);
            }
        } else if (observable instanceof ConversationFacade && arg != null) {
            switch (arg.getEventType()) {
                case INCOMING_MESSAGE:
                case HISTORY_LOADED:
                case CALL_STATE_CHANGED:
                case CONVERSATIONS_CHANGED:
                    loadConversation();
                    break;
            }
        }
    }
}