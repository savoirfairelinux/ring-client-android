/*
 *  Copyright (C) 2004-2020 Savoir-faire Linux Inc.
 *
 *  Authors:    AmirHossein Naghshzan <amirhossein.naghshzan@savoirfairelinux.com>
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
package cx.ring.tv.conversation;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cx.ring.R;
import cx.ring.application.JamiApplication;
import cx.ring.contacts.AvatarFactory;
import cx.ring.dependencyinjection.JamiInjectionComponent;
import cx.ring.model.CallContact;
import cx.ring.model.Error;
import cx.ring.model.Interaction;
import cx.ring.model.Uri;
import cx.ring.mvp.BaseSupportFragment;
import cx.ring.tv.model.TVListViewModel;
import cx.ring.utils.AndroidFileUtils;
import cx.ring.utils.ConversationPath;
import cx.ring.views.AvatarDrawable;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class ConversationFragment extends BaseSupportFragment<ConversationPresenter> implements ConversationView {

    private static final String ARG_MODEL = "model";

    private static final int SPEECH_REQUEST_CODE = 43600;

    private TVListViewModel mTvListViewModel;

    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mTextAudio;
    private TextView mTextMessage;
    private TextView mTextVideo;
    private RecyclerView mRecyclerView;
    private ImageButton mAudioButton;

    private int mSelectedPosition;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static File fileName = null;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    private ConversationPath path;

    private ConversationAdapter mAdapter = null;
    private AvatarDrawable mConversationAvatar;
    private Map<String, AvatarDrawable> mParticipantAvatars = new HashMap();

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public static ConversationFragment newInstance(TVListViewModel param) {
        ConversationFragment fragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MODEL, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((JamiApplication) getActivity().getApplication()).getRingInjectionComponent().inject(this);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTvListViewModel = getArguments().getParcelable(ARG_MODEL);
        }

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something...");
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }


    @Override
    public int getLayout() {
        return R.layout.frag_conversation_tv;
    }

    @Override
    public void injectFragment(JamiInjectionComponent component) {
        component.inject(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        path = ConversationPath.fromIntent(getActivity().getIntent());

        presenter.init(path);

        Uri contactUri = new Uri(path.getContactId());
        String accountId = path.getAccountId();
        mAdapter = new ConversationAdapter(this, presenter);

        ViewGroup textContainer = view.findViewById(R.id.text_container);
        ViewGroup audioContainer = view.findViewById(R.id.audio_container);
//        ViewGroup videoContainer = view.findViewById(R.id.video_container);
        mTextAudio = view.findViewById(R.id.text_audio);
        mTextMessage = view.findViewById(R.id.text_text);
//        mTextVideo = view.findViewById(R.id.text_video);

        ImageButton text = view.findViewById(R.id.button_text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpeechRecognizer();
            }
        });

        mAudioButton = view.findViewById(R.id.button_audio);
        mAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });

//        ImageButton video = view.findViewById(R.id.button_video);
//        video.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("*/*");
//
//                startActivityForResult(intent, REQUEST_CODE_FILE_PICKER);
//            }
//        });

        mAudioButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TransitionManager.beginDelayedTransition(textContainer);
                mTextAudio.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            }
        });

        text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TransitionManager.beginDelayedTransition(audioContainer);
                mTextMessage.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            }
        });

//        video.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                TransitionManager.beginDelayedTransition(videoContainer);
//                mTextVideo.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
//            }
//        });

        mTitle = view.findViewById(R.id.title);
        mSubTitle = view.findViewById(R.id.subtitle);
        mRecyclerView = view.findViewById(R.id.recycler_view);

        String id = mTvListViewModel.getContact().getRingUsername();
        String displayName = mTvListViewModel.getContact().getDisplayName();
        mTitle.setText(displayName);
        if (!displayName.equals(id))
            mSubTitle.setText(id);
        else
            mSubTitle.setVisibility(View.GONE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            createTextDialog(spokenText);
        }
    }

    private void createTextDialog(String spokenText) {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext(), R.style.Theme_MaterialComponents_Dialog)
                .setTitle(spokenText)
                .setMessage("")
                .setPositiveButton(R.string.tv_dialog_send, (dialog, whichButton) -> presenter.sendText(path, spokenText))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.getWindow().setLayout(900, 400);
        alertDialog.setOwnerActivity(getActivity());
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive= alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setFocusable(true);
                positive.setFocusableInTouchMode(true);
                positive.requestFocus();
            }
        });

        alertDialog.show();
    }

    private void createAudioDialog() {
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext(), R.style.Theme_MaterialComponents_Dialog)
                .setTitle(R.string.tv_send_audio_dialog_message)
                .setMessage("")
                .setPositiveButton(R.string.tv_dialog_send, (dialog, whichButton) -> sendAudio())
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.tv_audio_play, null)
                .create();
        alertDialog.getWindow().setLayout(900, 400);
        alertDialog.setOwnerActivity(getActivity());
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive= alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setFocusable(true);
                positive.setFocusableInTouchMode(true);
                positive.requestFocus();

                Button button = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPlay(mStartPlaying);
                        if (mStartPlaying) {
                            button.setText(R.string.tv_audio_pause);
                            if (player != null) {
                                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        button.setText(R.string.tv_audio_play);
                                        mStartPlaying = true;
                                    }
                                });
                            }
                        } else {
                            button.setText(R.string.tv_audio_play);
                        }
                        mStartPlaying = !mStartPlaying;
                    }
                });
            }
        });

        alertDialog.show();
    }

    @Override
    public void addElement(Interaction element) {
        mAdapter.add(element);
        scrollToTop();
    }

    @Override
    public void updateElement(Interaction element) {
        mAdapter.update(element);
    }

    @Override
    public void removeElement(Interaction element) {
        mAdapter.remove(element);
    }

    @Override
    public void refreshView(List<Interaction> interactions) {
        if (interactions == null) {
            return;
        }
        if (mAdapter != null) {
            mAdapter.updateDataset(interactions);
        }
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) getActivity().finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName.getAbsolutePath());
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        try {
            fileName = AndroidFileUtils.createAudioFile(getContext());
        } catch (IOException e) {
            return;
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName.getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();

        mAudioButton.setImageResource(R.drawable.lb_ic_stop);
        mTextAudio.setText(R.string.tv_audio_recording);
    }

    private void stopRecording() {
        if (recorder == null) {
            return;
        }
        recorder.stop();
        recorder.release();
        recorder = null;

        mAudioButton.setImageResource(R.drawable.baseline_mic_24);
        mTextAudio.setText(R.string.tv_send_audio);

        createAudioDialog();
    }

    private void sendAudio() {
        Log.w(TAG, "onActivityResult: fileName " + fileName.getAbsolutePath() + " " + fileName.exists() + " " + fileName.length());
        Single<File> file = null;
//        if (fileName == null || !fileName.exists() || fileName.length() == 0) {
//            android.net.Uri createdUri = resultData.getData();
//            if (createdUri != null) {
//                file = AndroidFileUtils.getCacheFile(requireContext(), createdUri);
//            }
//        } else {
            file = Single.just(fileName);
//        }
        fileName = null;
        if (file == null) {
            Toast.makeText(getActivity(), "Can't find picture", Toast.LENGTH_SHORT).show();
            return;
        }
        startFileSend(file.flatMapCompletable(this::sendFile));
    }

    private void startFileSend(Completable op) {
        op.observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {}, e -> {
                    Log.e(TAG, "startFileSend: not able to create cache file", e);
                    displayErrorToast(Error.INVALID_FILE);
                });
    }

    private Completable sendFile(File file) {
        return Completable.fromAction(() -> presenter.sendFile(path, file));
    }

    public void updatePosition(int position) {
        mSelectedPosition = position;
    }

    public void updateAdapterItem() {
        if (mSelectedPosition != -1) {
            mAdapter.notifyItemChanged(mSelectedPosition);
            mSelectedPosition = -1;
        }
    }

    @Override
    public void scrollToTop() {
        if (mAdapter.getItemCount() > 0) {
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void displayContact(CallContact contact) {
        mCompositeDisposable.clear();
        mCompositeDisposable.add(AvatarFactory.getAvatar(requireContext(), contact)
                .doOnSuccess(d -> {
                    mConversationAvatar = (AvatarDrawable) d;
                    mParticipantAvatars.put(contact.getPrimaryNumber(),
                            new AvatarDrawable((AvatarDrawable) d));
                })
                .flatMapObservable(d -> contact.getUpdatesSubject())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(c -> {
                    mConversationAvatar.update(c);
                    String uri = contact.getPrimaryNumber();
                    if (mParticipantAvatars.containsKey(uri)) {
                        mParticipantAvatars.get(uri).update(c);
                    }
                    mAdapter.setPhoto();
                }));
    }

    @Override
    public void switchToUnknownView(String name) {
        // todo
    }

    @Override
    public void switchToIncomingTrustRequestView(String message) {
        // todo
    }

    @Override
    public void switchToConversationView() {
        // todo
    }

    public AvatarDrawable getConversationAvatar(String uri) {
        return mParticipantAvatars.get(uri);
    }

    public void askWriteExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, JamiApplication.PERMISSIONS_REQUEST);
    }

}
