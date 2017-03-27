/*
 *  Copyright (C) 2017 Savoir-faire Linux Inc.
 *
 *  Author: Aline Bonnet <aline.bonnet@savoirfairelinux.com>
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

package cx.ring.contactrequests;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cx.ring.R;

public class BlackListViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.unblock)
    protected ImageButton mButtonUnblock;

    @BindView(R.id.photo)
    protected ImageView mPhoto;

    @BindView(R.id.display_name)
    protected TextView mDisplayname;


    public BlackListViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(final BlackListListeners clickListener, final BlackListViewModel viewModel) {
        mButtonUnblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onUnblockClick(viewModel);
            }
        });
    }

    public interface BlackListListeners {
        void onUnblockClick(BlackListViewModel viewModel);
    }
}
