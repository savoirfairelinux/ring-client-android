package cx.ring.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import cx.ring.R;
import cx.ring.model.Conversation;
import cx.ring.model.TextMessage;
import cx.ring.views.ConversationViewHolder;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationViewHolder> {
    private final static String TAG = ConversationAdapter.class.getSimpleName();

    private static final double MINUTE = 60L * 1000L;
    private static final double HOUR = 3600L * 1000L;

    private final Context mContext;
    private final ArrayList<Conversation.ConversationElement> mTexts = new ArrayList<>();
    private final LruCache<Long, Bitmap> mMemoryCache;
    private final ExecutorService mInfosFetcher;
    private final HashMap<Long, WeakReference<ContactPictureTask>> mRunningTasks = new HashMap<>();

    public ConversationAdapter(Context ctx, LruCache<Long, Bitmap> cache, ExecutorService pool) {
        mContext = ctx;
        mMemoryCache = cache;
        mInfosFetcher = pool;
    }

    public void updateDataset(final ArrayList<Conversation.ConversationElement> list, long rid) {
        Log.d(TAG, "updateDataset, list size: " + list.size() + " - rid: " + rid);
        if (list.size() == mTexts.size()) {
            if (rid != 0) {
                notifyDataSetChanged();
            }
            return;
        }
        int lastPos = mTexts.size();
        int newItmes = list.size() - lastPos;
        if (lastPos == 0 || newItmes < 0) {
            mTexts.clear();
            mTexts.addAll(list);
            notifyDataSetChanged();
        } else {
            for (int i = lastPos; i < list.size(); i++)
                mTexts.add(list.get(i));
            notifyItemRangeInserted(lastPos, newItmes);
        }
    }

    @Override
    public int getItemCount() {
        return mTexts.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Conversation.ConversationElement txt = mTexts.get(position);
        if (txt.text != null) {
            if (txt.text.isIncoming())
                return Conversation.ConversationMessageType.INCOMING_TEXT_MESSAGE.getType();
            else
                return Conversation.ConversationMessageType.OUTGOING_TEXT_MESSAGE.getType();
        }
        return Conversation.ConversationMessageType.CALL_INFORMATION_TEXT_MESSAGE.getType();
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int res;
        if (viewType == Conversation.ConversationMessageType.INCOMING_TEXT_MESSAGE.getType()) {
            res = R.layout.item_conv_msg_peer;
        } else if (viewType == Conversation.ConversationMessageType.OUTGOING_TEXT_MESSAGE.getType()) {
            res = R.layout.item_conv_msg_me;
        } else {
            res = R.layout.item_conv_call;
        }
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
        return new ConversationViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder h, int position) {
        Conversation.ConversationElement txt = mTexts.get(position);

        if (txt.text != null) {
            h.mCid = txt.text.getContact().getId();
            h.mMsgTxt.setText(txt.text.getMessage());
            if (h.mPhoto != null) {
                h.mPhoto.setImageBitmap(null);
            }

            boolean shouldSeparateByDetails = this.shouldSeparateByDetails(txt, position);
            boolean isConfigSameAsPreviousMsg = this.isMessageConfigSameAsPrevious(txt, position);

            if (txt.text.isIncoming() && !isConfigSameAsPreviousMsg) {
                this.setImage(h, txt);
            }

            if (txt.text.getStatus() == TextMessage.Status.SENDING) {
                h.mMsgDetailTxt.setVisibility(View.VISIBLE);
                h.mMsgDetailTxt.setText(R.string.message_sending);
            } else if (shouldSeparateByDetails) {
                h.mMsgDetailTxt.setVisibility(View.VISIBLE);
                String timeSeparationString = computeTimeSeparationStringFromMsgTimeStamp(
                        txt.text.getTimestamp()
                );
                h.mMsgDetailTxt.setText(timeSeparationString);
            } else {
                h.mMsgDetailTxt.setVisibility(View.GONE);
            }
        } else {
            this.configureForCallInfoTextMessage(h, txt);
        }
    }

    private String computeTimeSeparationStringFromMsgTimeStamp(long timestamp) {
        long now = new Date().getTime();
        if (now - timestamp < MINUTE && mContext != null) {
            return mContext.getString(R.string.time_just_now);
        } else if (now - timestamp < HOUR) {
            return DateUtils.getRelativeTimeSpanString(timestamp, now, 0, 0).toString();
        } else {
            return DateUtils.formatSameDayTime(timestamp, now, DateFormat.SHORT, DateFormat.SHORT)
                    .toString();
        }
    }

    private TextMessage getPreviousMessageFromPosition(int position) {
        if (!mTexts.isEmpty() && position > 0) {
            return mTexts.get(position - 1).text;
        }
        return null;
    }

    private TextMessage getNextMessageFromPosition(int position) {
        if (!mTexts.isEmpty() && position < mTexts.size() - 1) {
            return mTexts.get(position + 1).text;
        }
        return null;
    }

    private boolean shouldSeparateByDetails(final Conversation.ConversationElement convElement,
                                            int position) {
        if (convElement == null || convElement.text == null) {
            return false;
        }

        boolean shouldSeparateMsg = false;
        TextMessage previousTextMessage = this.getPreviousMessageFromPosition(position);
        if (previousTextMessage != null) {
            shouldSeparateMsg = true;
            TextMessage nextTextMessage = this.getNextMessageFromPosition(position);
            if (nextTextMessage != null) {
                long diff = nextTextMessage.getTimestamp() - convElement.text.getTimestamp();
                if (diff < MINUTE) {
                    shouldSeparateMsg = false;
                }
            }
        }
        return shouldSeparateMsg;
    }

    private boolean isMessageConfigSameAsPrevious(final Conversation.ConversationElement convElement,
                                                  int position) {
        if (convElement == null || convElement.text == null) {
            return false;
        }

        boolean sameConfig = false;
        TextMessage previousMessage = this.getPreviousMessageFromPosition(position);
        if (previousMessage != null &&
                previousMessage.isIncoming() &&
                convElement.text.isIncoming() &&
                previousMessage.getNumber().equals(convElement.text.getNumber())) {
            sameConfig = true;
        }
        return sameConfig;
    }

    private void setImage(final ConversationViewHolder convViewHolder,
                          final Conversation.ConversationElement convElement) {
        final Long cid = convElement.text.getContact().getId();
        Bitmap bmp = mMemoryCache.get(cid);
        if (bmp != null) {
            convViewHolder.mPhoto.setImageBitmap(bmp);
        } else {
            convViewHolder.mPhoto.setImageBitmap(mMemoryCache.get(-1L));
            final WeakReference<ConversationViewHolder> wh = new WeakReference<>(convViewHolder);
            final ContactPictureTask.PictureLoadedCallback cb = new ContactPictureTask.PictureLoadedCallback() {
                @Override
                public void onPictureLoaded(final Bitmap bmp) {
                    final ConversationViewHolder fh = wh.get();
                    if (fh == null || fh.mPhoto.getParent() == null)
                        return;
                    if (fh.mCid == cid) {
                        fh.mPhoto.post(new Runnable() {
                            @Override
                            public void run() {
                                fh.mPhoto.setImageBitmap(bmp);
                                fh.mPhoto.startAnimation(AnimationUtils.loadAnimation(fh.mPhoto.getContext(), R.anim.contact_fadein));
                            }
                        });
                    }
                }
            };
            WeakReference<ContactPictureTask> wtask = mRunningTasks.get(cid);
            ContactPictureTask task = wtask == null ? null : wtask.get();
            if (task != null) {
                task.addCallback(cb);
            } else {
                task = new ContactPictureTask(mContext, convViewHolder.mPhoto, convElement.text.getContact(), new ContactPictureTask.PictureLoadedCallback() {
                    @Override
                    public void onPictureLoaded(Bitmap bmp) {
                        mMemoryCache.put(cid, bmp);
                        mRunningTasks.remove(cid);
                    }
                });
                task.addCallback(cb);
                mRunningTasks.put(cid, new WeakReference<>(task));
                mInfosFetcher.execute(task);
            }
        }
    }

    private void configureForCallInfoTextMessage(final ConversationViewHolder convViewHolder,
                                                 final Conversation.ConversationElement convElement) {
        if (convViewHolder == null || mContext == null ||
                convElement == null || convElement.call == null) {
            return;
        }

        int pictureResID;
        String histTxt;
        String callNumber = convElement.call.getNumber();
        if (convElement.call.isMissed()) {
            pictureResID = (convElement.call.isIncoming()) ?
                    R.drawable.ic_call_missed_black_24dp :
                    R.drawable.ic_call_missed_outgoing_black_24dp;
            histTxt = convElement.call.isIncoming() ?
                    mContext.getString(R.string.notif_missed_incoming_call, callNumber) :
                    mContext.getString(R.string.notif_missed_outgoing_call, callNumber);
        } else {
            pictureResID = (convElement.call.isIncoming()) ?
                    R.drawable.ic_call_received_black_24dp :
                    R.drawable.ic_call_made_black_24dp;
            histTxt = convElement.call.isIncoming() ?
                    mContext.getString(R.string.notif_incoming_call_title, callNumber) :
                    mContext.getString(R.string.notif_outgoing_call_title, callNumber);
        }

        convViewHolder.mCid = convElement.call.getContactID();
        convViewHolder.mPhoto.setImageResource(pictureResID);
        convViewHolder.mHistTxt.setText(histTxt);
        convViewHolder.mHistDetailTxt.setText(DateFormat.getDateTimeInstance()
                .format(convElement.call.getStartDate()));
    }
}