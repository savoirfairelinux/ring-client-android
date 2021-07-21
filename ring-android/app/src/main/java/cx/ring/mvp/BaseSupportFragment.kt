/*
 *  Copyright (C) 2004-2021 Savoir-faire Linux Inc.
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
package cx.ring.mvp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import cx.ring.R
import net.jami.model.Error
import net.jami.mvp.BaseView
import net.jami.mvp.RootPresenter
import javax.inject.Inject

abstract class BaseSupportFragment<T : RootPresenter<V>, V> : Fragment(), BaseView {
    @Inject lateinit
    var presenter: T

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Be sure to do the injection in onCreateView method
        presenter.bindView(this as V)
        initPresenter(presenter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.unbindView()
    }

    override fun displayErrorToast(error: Error) {
        val errorString: String = when (error) {
            Error.NO_INPUT -> getString(R.string.call_error_no_camera_no_microphone)
            Error.INVALID_FILE -> getString(R.string.invalid_file)
            Error.NOT_ABLE_TO_WRITE_FILE -> getString(R.string.not_able_to_write_file)
            Error.NO_SPACE_LEFT -> getString(R.string.no_space_left_on_device)
            else -> getString(R.string.generic_error)
        }
        Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show()
    }

    protected open fun initPresenter(presenter: T) {}

    protected fun replaceFragmentWithSlide(fragment: Fragment?, @IdRes content: Int) {
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(content, fragment!!, TAG)
            .addToBackStack(TAG)
            .commit()
    }

    protected fun replaceFragment(fragment: Fragment?, @IdRes content: Int) {
        parentFragmentManager
            .beginTransaction()
            .replace(content, fragment!!, TAG)
            .addToBackStack(TAG)
            .commit()
    }

    companion object {
        protected val TAG = BaseSupportFragment::class.simpleName
    }
}