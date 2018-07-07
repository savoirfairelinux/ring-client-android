/*
 *  Copyright (C) 2004-2018 Savoir-faire Linux Inc.
 *
 *  Author: Aline Bonnet <aline.bonnet@savoirfairelinux.com>
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
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package cx.ring.navigation;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.facades.ConversationFacade;
import cx.ring.model.Account;
import cx.ring.mvp.RootPresenter;
import cx.ring.services.AccountService;
import cx.ring.services.DeviceRuntimeService;
import cx.ring.utils.Log;
import cx.ring.utils.StringUtils;
import cx.ring.utils.VCardUtils;
import ezvcard.VCard;
import ezvcard.property.FormattedName;
import ezvcard.property.Photo;
import ezvcard.property.RawProperty;
import ezvcard.property.Uid;
import io.reactivex.Scheduler;

public class RingNavigationPresenter extends RootPresenter<RingNavigationView> {

    private static final String TAG = RingNavigationPresenter.class.getSimpleName();

    private AccountService mAccountService;
    private DeviceRuntimeService mDeviceRuntimeService;
    private ConversationFacade mConversationFacade;

    @Inject
    @Named("UiScheduler")
    protected Scheduler mUiScheduler;

    @Inject
    public RingNavigationPresenter(AccountService accountService,
                                   DeviceRuntimeService deviceRuntimeService,
                                   ConversationFacade conversationFacade) {
        this.mAccountService = accountService;
        this.mDeviceRuntimeService = deviceRuntimeService;
        this.mConversationFacade = conversationFacade;
    }

    @Override
    public void bindView(RingNavigationView view) {
        super.bindView(view);
        mCompositeDisposable.add(mAccountService.getObservableAccounts()
                .observeOn(mUiScheduler)
                .subscribe(r -> updateUser()));
        mCompositeDisposable.add(mAccountService.getCurrentAccountSubject()
                .observeOn(mUiScheduler)
                .subscribe(r -> updateUser()));
    }

    @Override
    public void unbindView() {
        super.unbindView();
    }

    public void updateUser() {
        if (getView() == null) {
            return;
        }

        Account currentAccount = mAccountService.getCurrentAccount();
        getView().showViewModel(new RingNavigationViewModel(currentAccount, mAccountService.getAccounts()));
    }

    public void setAccountOrder(Account selectedAccount) {
        if (getView() == null) {
            return;
        }
        mAccountService.setCurrentAccount(selectedAccount);
    }

    public void saveVCardPhoto(Photo photo) {
        String accountId = mAccountService.getCurrentAccount().getAccountID();
        String ringId = mAccountService.getCurrentAccount().getUsername();
        File filesDir = mDeviceRuntimeService.provideFilesDir();

        VCard vcard = VCardUtils.loadLocalProfileFromDisk(filesDir, accountId);
        vcard.setUid(new Uid(ringId));
        vcard.removeProperties(Photo.class);
        vcard.addPhoto(photo);
        vcard.removeProperties(RawProperty.class);
        VCardUtils.saveLocalProfileToDisk(vcard, accountId, filesDir);

        updateUser();
    }

    public void saveVCardFormattedName(String username) {
        String accountId = mAccountService.getCurrentAccount().getAccountID();
        File filesDir = mDeviceRuntimeService.provideFilesDir();

        VCard vcard = VCardUtils.loadLocalProfileFromDisk(filesDir, accountId);
        vcard.setFormattedName(username);
        vcard.removeProperties(RawProperty.class);
        VCardUtils.saveLocalProfileToDisk(vcard, accountId, filesDir);

        updateUser();
    }

    public void saveVCard(Account account, String username, Photo photo) {
        String accountId = account.getAccountID();
        String ringId = account.getUsername();
        File filesDir = mDeviceRuntimeService.provideFilesDir();

        VCard vcard = VCardUtils.loadLocalProfileFromDisk(filesDir, accountId);
        vcard.setUid(new Uid(ringId));

        if (photo != null) {
            vcard.removeProperties(Photo.class);
            vcard.addPhoto(photo);
        }

        if (!StringUtils.isEmpty(username)) {
            vcard.setFormattedName(username);
        }

        vcard.removeProperties(RawProperty.class);
        VCardUtils.saveLocalProfileToDisk(vcard, accountId, filesDir);

        updateUser();
    }

    public String getAlias(Account account) {
        if (account == null) {
            Log.e(TAG, "Not able to get alias");
            return null;
        }
        VCard vcard = VCardUtils.loadLocalProfileFromDisk(mDeviceRuntimeService.provideFilesDir(), account.getAccountID());
        FormattedName name = vcard.getFormattedName();
        if (name != null) {
            String name_value = name.getValue();
            if (name_value != null && !name_value.isEmpty()) {
                return name_value;
            }
        }
        return null;
    }

    public String getAccountAlias(Account account) {
        if (account == null) {
            Log.e(TAG, "Not able to get account alias");
            return null;
        }
        String alias = getAlias(account);
        return (alias == null) ? account.getAlias() : alias;
    }

    public String getUri(Account account, CharSequence defaultNameSip) {
        if (account.isIP2IP()) {
            return defaultNameSip.toString();
        }
        return account.getDisplayUri();
    }

    public void cameraClicked() {
        boolean hasPermission = mDeviceRuntimeService.hasVideoPermission() &&
                mDeviceRuntimeService.hasWriteExternalStoragePermission();
        if (hasPermission) {
            getView().gotToImageCapture();
        } else {
            getView().askCameraPermission();
        }
    }

    public void galleryClicked() {
        boolean hasPermission = mDeviceRuntimeService.hasGalleryPermission();
        if (hasPermission) {
            getView().goToGallery();
        } else {
            getView().askGalleryPermission();
        }
    }
}
