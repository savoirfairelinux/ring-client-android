/*
 *  Copyright (C) 2017 Savoir-faire Linux Inc.
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
package cx.ring.tv.main;

import java.util.ArrayList;

import cx.ring.navigation.RingNavigationViewModel;
import cx.ring.tv.model.TVListViewModel;

public interface MainView {

    void showLoading(boolean show);

    void refreshContact(TVListViewModel contact);

    void showContacts(ArrayList<TVListViewModel> contacts);

    void callContact(String accountID, String ringID);

    void displayErrorToast(int error);

    void displayAccountInfos(String address, RingNavigationViewModel viewModel);

    void showExportDialog(String pAccountID);

    void showAccountManagement();
}
