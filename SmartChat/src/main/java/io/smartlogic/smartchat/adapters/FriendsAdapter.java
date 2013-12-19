package io.smartlogic.smartchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.models.Friend;

public class FriendsAdapter extends ArrayAdapter<Friend> {
    private Context mContext;
    private List<Friend> mFriends;

    public FriendsAdapter(Context context, List<Friend> friends) {
        super(context, R.layout.adapter_friends);

        this.mContext = context;
        this.mFriends = friends;
    }

    @Override
    public int getCount() {
        return mFriends.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view;
        if (convertView == null) {
            view = inflater.inflate(R.layout.adapter_friends, null);
        } else {
            view = convertView;
        }

        TextView friendEmail = (TextView) view.findViewById(R.id.email);
        friendEmail.setText(mFriends.get(position).getEmail());

        return view;
    }
}
