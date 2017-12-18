/*
 *  Copyright (C) 2017 Savoir-faire Linux Inc.
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
package cx.ring.tv.account;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import java.util.List;

import cx.ring.R;
import cx.ring.account.ProfileCreationFragment;
import cx.ring.adapters.ContactDetailsTask;
import cx.ring.application.RingApplication;
import cx.ring.client.HomeActivity;
import cx.ring.navigation.RingNavigationPresenter;
import cx.ring.navigation.RingNavigationView;
import cx.ring.navigation.RingNavigationViewModel;
import cx.ring.tv.camera.CustomCameraActivity;

public class TVProfileEditingFragment extends RingGuidedStepFragment<RingNavigationPresenter>
        implements RingNavigationView {

    public static final int REQUEST_CODE_PHOTO = 1;
    public static final int REQUEST_CODE_GALLERY = 2;
    public static final int REQUEST_PERMISSION_CAMERA = 3;
    public static final int REQUEST_PERMISSION_READ_STORAGE = 4;
    private static final int USER_NAME = 1;
    private static final int GALLERY = 2;
    private static final int CAMERA = 3;
    private static final int NEXT = 4;

    private Bitmap mSourcePhoto;

    public static GuidedStepFragment newInstance() {
        return new TVProfileEditingFragment();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ProfileCreationFragment.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Bundle extras = data.getExtras();
                    if (extras == null) {
                        Log.e(TAG, "onActivityResult: Not able to get picture from extra");
                        return;
                    }
                    byte[] input = extras.getByteArray("data");
                    if (input == null) {
                        Log.e(TAG, "onActivityResult: Not able to get byte[] from extra");
                        return;
                    }
                    Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
                    updatePhoto(original);
                }
                break;
            case ProfileCreationFragment.REQUEST_CODE_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    updatePhoto(data.getData());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((RingApplication) getActivity().getApplication()).getRingInjectionComponent().inject(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.profile);
        String breadcrumb = "";
        String description = getString(R.string.profile_message_warning);


        Drawable icon;


//        RingNavigationViewModel viewModel = new RingAccountViewModelImpl(mAccountService.getCurrentAccount(), accounts);


//        RingAccountViewModelImpl ringAccountViewModel = (RingAccountViewModelImpl) getArguments().get(TVProfileEditingFragment.KEY_RING_ACCOUNT);
//        if (ringAccountViewModel != null && ringAccountViewModel.getPhoto() != null) {
//            icon = new BitmapDrawable(getResources(), ringAccountViewModel.getPhoto());
//        } else {
            icon = getActivity().getResources().getDrawable(R.drawable.ic_contact_picture);
//        }

        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public int onProvideTheme() {
        return R.style.Theme_Ring_Leanback_GuidedStep_First;
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        String desc = getString(R.string.account_edit_profile);
        String editdesc = getString(R.string.profile_name_hint);
        addEditTextAction(actions, USER_NAME, desc, editdesc, "");
        addAction(actions, CAMERA, getActivity().getResources().getString(R.string.take_a_photo), "");
        addAction(actions, GALLERY, getActivity().getResources().getString(R.string.open_the_gallery), "");
    }

    public long onGuidedActionEditedAndProceed(GuidedAction action) {
        if (action.getId() == USER_NAME) {
            String username = action.getEditDescription().toString();
            presenter.saveVCardFormattedName(username);
        } else if (action.getId() == CAMERA) {
            presenter.cameraClicked();
        } else if (action.getId() == GALLERY) {
            presenter.galleryClicked();
        }
        return super.onGuidedActionEditedAndProceed(action);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == CAMERA) {
            presenter.cameraClicked();
        } else if (action.getId() == GALLERY) {
            presenter.galleryClicked();
        }
    }

    @Override
    public void showViewModel(RingNavigationViewModel viewModel) {
        // displays account available info
    }

    @Override
    public void gotToImageCapture() {
        Intent intent = new Intent(getActivity(), CustomCameraActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }

    @Override
    public void askCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CAMERA);
    }

    @Override
    public void askGalleryPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                HomeActivity.REQUEST_PERMISSION_READ_STORAGE);
    }

    @Override
    public void goToGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_GALLERY);
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(R.string.gallery_error_title)
                    .setMessage(R.string.gallery_error_message)
                    .show();
        }
    }

    public void updatePhoto(Uri uriImage) {
        updatePhoto(ContactDetailsTask.loadProfilePhotoFromUri(getActivity(), uriImage));
    }

    public void updatePhoto(Bitmap image) {

//        RingAccountViewModelImpl model = (RingAccountViewModelImpl) ringAccountViewModel;
//        model.setPhoto(mSourcePhoto);

        mSourcePhoto = image;
        getGuidanceStylist().getIconView().setImageBitmap(image);
    }
}