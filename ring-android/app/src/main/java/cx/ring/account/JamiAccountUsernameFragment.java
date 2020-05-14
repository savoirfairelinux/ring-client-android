/*
 * Copyright (C) 2004-2020 Savoir-faire Linux Inc.
 *
 * Authors:    AmirHossein Naghshzan <amirhossein.naghshzan@savoirfairelinux.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package cx.ring.account;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import cx.ring.R;
import cx.ring.application.JamiApplication;
import cx.ring.databinding.FragAccJamiUsernameBinding;
import cx.ring.mvp.AccountCreationModel;
import cx.ring.mvp.BaseSupportFragment;
import cx.ring.utils.RegisteredNameFilter;

public class JamiAccountUsernameFragment extends BaseSupportFragment<JamiAccountCreationPresenter>
        implements JamiAccountCreationView {

    private AccountCreationModel model;
    private FragAccJamiUsernameBinding binding;

    public static JamiAccountUsernameFragment newInstance(AccountCreationModelImpl ringAccountViewModel) {
        JamiAccountUsernameFragment fragment = new JamiAccountUsernameFragment();
        fragment.model = ringAccountViewModel;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragAccJamiUsernameBinding.inflate(inflater, container, false);
        ((JamiApplication) getActivity().getApplication()).getInjectionComponent().inject(this);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);
        binding.ringUsername.setFilters(new InputFilter[]{new RegisteredNameFilter()});
        binding.ringUsername.requestFocus();
        binding.createAccount.setOnClickListener(v -> presenter.createAccount());
//        binding.switchRingPush.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.setPush(isChecked));
        binding.skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.registerUsernameChanged(false);
                presenter.createAccount();
            }
        });
        binding.ringUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                presenter.userNameChanged(s.toString());
            }
        });

        presenter.init(model);
        presenter.setPush(true);
    }

    @Override
    public void updateUsernameAvailability(UsernameAvailabilityStatus status) {
        binding.ringUsernameAvailabilitySpinner.setVisibility(View.GONE);
        switch (status){
            case ERROR:
                binding.ringUsernameTxtBox.setErrorEnabled(true);
                binding.ringUsernameTxtBox.setError(getString(R.string.unknown_error));
                binding.ringUsernameTxtBox.setEndIconMode(TextInputLayout.END_ICON_NONE);
                break;
            case ERROR_USERNAME_INVALID:
                binding.ringUsernameTxtBox.setErrorEnabled(true);
                binding.ringUsernameTxtBox.setError(getString(R.string.invalid_username));
                binding.ringUsernameTxtBox.setEndIconMode(TextInputLayout.END_ICON_NONE);
                break;
            case ERROR_USERNAME_TAKEN:
                binding.ringUsernameTxtBox.setErrorEnabled(true);
                binding.ringUsernameTxtBox.setError(getString(R.string.username_already_taken));
                binding.ringUsernameTxtBox.setEndIconMode(TextInputLayout.END_ICON_NONE);
                break;
            case LOADING:
                binding.ringUsernameTxtBox.setErrorEnabled(false);
                binding.ringUsernameTxtBox.setEndIconMode(TextInputLayout.END_ICON_NONE);
                binding.ringUsernameAvailabilitySpinner.setVisibility(View.VISIBLE);
                break;
            case AVAILABLE:
                binding.ringUsernameTxtBox.setErrorEnabled(false);
                binding.ringUsernameTxtBox.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                binding.ringUsernameTxtBox.setEndIconDrawable(R.drawable.ic_good_green);
                break;
            case RESET:
                binding.ringUsernameTxtBox.setErrorEnabled(false);
                binding.ringUsername.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                enableNextButton(false);
            default:
                binding.ringUsernameAvailabilitySpinner.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void showInvalidPasswordError(final boolean display) {
    }

    @Override
    public void showNonMatchingPasswordError(final boolean display) {
    }

    @Override
    public void enableNextButton(final boolean enabled) {
        binding.createAccount.setEnabled(enabled);
    }

    @Override
    public void goToAccountCreation(AccountCreationModel accountCreationModel) {
        JamiAccountCreationFragment parent = (JamiAccountCreationFragment) getParentFragment();
        if (parent != null) {
            parent.scrollPagerFragment(accountCreationModel);
        }
    }

    @Override
    public void cancel() {
        Activity wizardActivity = getActivity();
        if (wizardActivity != null) {
            wizardActivity.onBackPressed();
        }
    }

}
