package xposed.ayberkbaytok.sms_handler.app;

import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import xposed.ayberkbaytok.sms_handler.R;
import xposed.ayberkbaytok.sms_handler.data.SmsMessageData;
import xposed.ayberkbaytok.sms_handler.loader.ReceivedMessageLoader;
import xposed.ayberkbaytok.sms_handler.widget.RecyclerCursorAdapter;

/* package */ class ReceivedMessageAdapter extends RecyclerCursorAdapter<ReceivedMessageAdapter.ReceivedMessagesItemHolder> {
    public class ReceivedMessagesItemHolder extends RecyclerView.ViewHolder {
        public final TextView mSenderTextView;
        public final TextView mTimeSentTextView;
        public final TextView mBodyTextView;
        public SmsMessageData mMessageData;

        public ReceivedMessagesItemHolder(View itemView) {
            super(itemView);

            mSenderTextView = (TextView)itemView.findViewById(R.id.received_message_sender_textview);
            mTimeSentTextView = (TextView)itemView.findViewById(R.id.received_message_time_sent_textview);
            mBodyTextView = (TextView)itemView.findViewById(R.id.received_message_body_textview);
        }
    }

    private final ReceivedMessagesFragment mFragment;

    public ReceivedMessageAdapter(ReceivedMessagesFragment fragment) {
        mFragment = fragment;
    }

    @Override
    public ReceivedMessagesItemHolder onCreateViewHolder(ViewGroup group, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(mFragment.getContext());
        View view = layoutInflater.inflate(R.layout.listitem_received_messages, group, false);
        return new ReceivedMessagesItemHolder(view);
    }

    @Override
    protected int[] onBindColumns(Cursor cursor) {
        return ReceivedMessageLoader.get().getColumns(cursor);
    }

    @Override
    public void onBindViewHolder(ReceivedMessagesItemHolder holder, Cursor cursor) {
        final SmsMessageData messageData = ReceivedMessageLoader.get().getData(cursor, getColumns(), holder.mMessageData);
        holder.mMessageData = messageData;

        String sender = messageData.getSender();
        long timeSent = messageData.getTimeSent();
        String body = messageData.getBody();
        CharSequence timeSentString = DateUtils.getRelativeTimeSpanString(mFragment.getContext(), timeSent);

        holder.mSenderTextView.setText(sender);
        holder.mTimeSentTextView.setText(timeSentString);
        holder.mBodyTextView.setText(body);
        if (messageData.isRead()) {
            holder.mSenderTextView.setTypeface(null, Typeface.NORMAL);
            holder.mTimeSentTextView.setTypeface(null, Typeface.NORMAL);
            holder.mBodyTextView.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.mSenderTextView.setTypeface(null, Typeface.BOLD);
            holder.mTimeSentTextView.setTypeface(null, Typeface.BOLD);
            holder.mBodyTextView.setTypeface(null, Typeface.BOLD);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.showMessageDetailsDialog(messageData);
            }
        });
    }
}

