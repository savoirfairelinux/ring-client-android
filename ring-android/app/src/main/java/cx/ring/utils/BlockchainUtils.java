/*
 *  Copyright (C) 2016 Savoir-faire Linux Inc.
 *
 *  Author: Thibault Wittemberg <thibault.wittemberg@savoirfairelinux.com>
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
package cx.ring.utils;

import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.EditText;

import cx.ring.service.LocalService;

public class BlockchainUtils {

    public static TextWatcher attachUsernameTextWatcher(final LocalService.Callbacks callbacks, final TextInputLayout inputLayout, final EditText inputText) {
        TextWatcher textWatcher = new BlockchainTextWatcher(callbacks, inputLayout, inputText);
        inputText.addTextChangedListener(textWatcher);
        return textWatcher;
    }

    public static void attachUsernameTextFilter(final EditText inputText) {
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char charToTest = source.charAt(i);

                    if (!Character.isLowerCase(charToTest)) {
                        return "";
                    }

                    if (!Character.isLetterOrDigit(charToTest) && charToTest != '-' && charToTest != '_') {
                        return "";
                    }
                }
                return null;
            }
        };

        inputText.setFilters(new InputFilter[]{filter});
    }
}
