/*
 *  Copyright (C) 2004-2021 Savoir-faire Linux Inc.
 *
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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cx.ring.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import cx.ring.R;
import cx.ring.adapters.SmartListAdapter;
import cx.ring.application.JamiApplication;
import net.jami.facades.ConversationFacade;
import cx.ring.fragments.CallFragment;

import net.jami.model.Contact;
import net.jami.model.Conference;
import net.jami.model.Call;
import net.jami.services.CallService;
import net.jami.smartlist.SmartListViewModel;
import cx.ring.utils.ConversationPath;
import cx.ring.viewholders.SmartListViewHolder;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ConversationSelectionActivity extends AppCompatActivity {
    private final static String TAG = ConversationSelectionActivity.class.getSimpleName();

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Inject
    @Singleton
    ConversationFacade mConversationFacade;

    @Inject
    @Singleton
    CallService mCallService;

    private SmartListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JamiApplication.getInstance().getInjectionComponent().inject(this);
        setContentView(R.layout.frag_selectconv);
        RecyclerView list = findViewById(R.id.conversationList);

        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);*/

        adapter = new SmartListAdapter(null, new SmartListViewHolder.SmartListListeners() {
            @Override
            public void onItemClick(SmartListViewModel smartListViewModel) {
                Intent intent = new Intent();
                intent.setData(ConversationPath.toUri(smartListViewModel.getAccountId(), smartListViewModel.getUri()));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onItemLongClick(SmartListViewModel smartListViewModel) {
            }
        }, mDisposable);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        Conference conference = null;
        Intent intent = getIntent();
        if (intent != null) {
            String confId = intent.getStringExtra(CallFragment.KEY_CONF_ID);
            if (!TextUtils.isEmpty(confId)) {
                conference = mCallService.getConference(confId);
            }
        }

        final Conference conf = conference;
        mDisposable.add(mConversationFacade
                .getCurrentAccountSubject()
                .switchMap(a -> a.getConversationsViewModels(false))
                .map(vm -> {
                    if (conf == null)
                        return vm;
                    List<SmartListViewModel> filteredVms = new ArrayList<>(vm.size());
                    models: for (SmartListViewModel v : vm) {
                        List<Contact> contacts = v.getContacts();
                        if (contacts.size() != 1)
                            continue;
                        for (Call call : conf.getParticipants()) {
                            if (call.getContact() == v.getContacts().get(0)) {
                                continue models;
                            }
                        }
                        filteredVms.add(v);
                    }
                    return filteredVms;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (adapter != null)
                        adapter.update(list);
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        mDisposable.clear();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter = null;
    }
}
