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
package cx.ring.mvp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;

import javax.inject.Inject;

import cx.ring.R;
import cx.ring.account.RingAccountCreationFragment;
import cx.ring.utils.Log;

public abstract class BaseFragment<T extends RootPresenter> extends Fragment {

    protected static final String TAG = BaseFragment.class.getSimpleName();

    @Inject
    protected T presenter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Be sure to do the injection in onCreateView method
        presenter.bindView(this);
        initPresenter(presenter);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
        presenter.unbindView();
    }

    protected void initPresenter(T presenter) {

    }

    protected void replaceFragmentWithSlide(Fragment fragment, @IdRes int content) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_left,
                        R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
                .replace(content, fragment, RingAccountCreationFragment.TAG)
                .addToBackStack(RingAccountCreationFragment.TAG)
                .commit();
    }

    protected void replaceFragment(Fragment fragment, @IdRes int content) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(content, fragment, RingAccountCreationFragment.TAG)
                .addToBackStack(RingAccountCreationFragment.TAG)
                .commit();
    }
}
