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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cx.ring.R;
import cx.ring.application.JamiApplication;
import cx.ring.databinding.FragAccJamiPasswordBinding;
import cx.ring.mvp.AccountCreationModel;
import cx.ring.mvp.BaseSupportFragment;

public class JamiAccountPasswordFragment extends BaseSupportFragment<JamiAccountCreationPresenter>
        implements JamiAccountCreationView {

    private AccountCreationModel model;
    private FragAccJamiPasswordBinding binding;

    private boolean mIsChecked = false;

    public static JamiAccountPasswordFragment newInstance(AccountCreationModelImpl ringAccountViewModel) {
        JamiAccountPasswordFragment fragment = new JamiAccountPasswordFragment();
        fragment.model = ringAccountViewModel;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragAccJamiPasswordBinding.inflate(inflater, container, false);
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

        binding.createAccount.setOnClickListener(v -> presenter.createAccount());
        binding.createAccount.setStateListAnimator(null);
        binding.ringPasswordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsChecked = isChecked;
                if (isChecked) {
                    binding.passwordTxtBox.setVisibility(View.VISIBLE);
                    binding.ringPasswordRepeatTxtBox.setVisibility(View.VISIBLE);
                    binding.placeholder.setVisibility(View.GONE);
                    binding.createAccount.setText(R.string.wizard_password_button);
                    presenter.passwordChanged(binding.ringPassword.getText().toString(), binding.ringPasswordRepeat.getText().toString());
                } else {
                    binding.passwordTxtBox.setVisibility(View.GONE);
                    binding.ringPasswordRepeatTxtBox.setVisibility(View.GONE);
                    binding.placeholder.setVisibility(View.VISIBLE);
                    binding.createAccount.setText(R.string.wizard_password_skip);
                    presenter.passwordUnset();
                }
            }
        });
        binding.ringPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.passwordChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.ringPasswordRepeat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.passwordConfirmChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.ringPasswordRepeat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.createAccount();
            }
            return false;
        });
        binding.ringPasswordRepeat.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && binding.createAccount.isEnabled()) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    presenter.createAccount();
                    return true;
                }
                return false;
            }
        });

        presenter.init(model);
    }

    @Override
    public void updateUsernameAvailability(UsernameAvailabilityStatus status) {
    }

    @Override
    public void showInvalidPasswordError(final boolean display) {
        if (display) {
            binding.passwordTxtBox.setError(getString(R.string.error_password_char_count));
        } else {
            binding.passwordTxtBox.setError(null);
        }
    }

    @Override
    public void showNonMatchingPasswordError(final boolean display) {
        if (display) {
            binding.ringPasswordRepeatTxtBox.setError(getString(R.string.error_passwords_not_equals));
        } else {
            binding.ringPasswordRepeatTxtBox.setError(null);
        }
    }

    @Override
    public void enableNextButton(final boolean enabled) {
        if (!mIsChecked) {
            binding.createAccount.setEnabled(true);
            return;
        }

        binding.createAccount.setEnabled(enabled);
    }

    @Override
    public void goToAccountCreation(AccountCreationModel accountCreationModel) {
        Activity wizardActivity = getActivity();
        if (wizardActivity instanceof AccountWizardActivity) {
            AccountWizardActivity wizard = (AccountWizardActivity) wizardActivity;
            wizard.createAccount(accountCreationModel);
            JamiAccountCreationFragment parent = (JamiAccountCreationFragment) getParentFragment();
            if (parent != null) {
                parent.scrollPagerFragment(accountCreationModel);
            }
        }
    }

    @Override
    public void cancel() {
        Activity wizardActivity = getActivity();
        if (wizardActivity != null) {
            wizardActivity.onBackPressed();
        }
    }

    public void setUsername(String username) {
        model.setUsername(username);
    }

}
