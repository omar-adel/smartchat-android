package io.smartlogic.smartchat.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.activities.MainActivity;
import io.smartlogic.smartchat.activities.UploadActivity;
import io.smartlogic.smartchat.adapters.FriendSelectorAdapter;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.models.Friend;

public class PickFriendsFragment extends ListFragment implements FriendSelectorAdapter.OnFriendCheckedListener,
        UploadActivity.OnDoneSelectedListener {

    private FriendSelectorAdapter mAdapter;
    private HashSet<Integer> mCheckedFriendIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCheckedFriendIds = new HashSet<Integer>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListShown(false);

        new LoadFriendsTask().execute();
    }

    @Override
    public void onFriendChecked(Friend friend, boolean isChecked) {
        if (isChecked) {
            mCheckedFriendIds.add(friend.getId());
        } else {
            mCheckedFriendIds.remove(friend.getId());
        }
    }

    @Override
    public void onDoneSelected() {
        new UploadTask().execute();
    }

    private ApiClient getApiClient() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String email = prefs.getString(Constants.EXTRA_EMAIL, "");
        String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

        return new ApiClient(email, encodedPrivateKey);
    }

    private class LoadFriendsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ApiClient client = getApiClient();
            List<Friend> friends = client.getFriends();

            mAdapter = new FriendSelectorAdapter(getActivity(), friends, PickFriendsFragment.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setListAdapter(mAdapter);
            setListShown(true);
        }
    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ApiClient client = getApiClient();

            String photoPath = getArguments().getString(Constants.EXTRA_PHOTO_PATH);
            String drawingPath = getArguments().getString(Constants.EXTRA_DRAWING_PATH, "");

            List<Integer> friendIds = new ArrayList<Integer>();
            friendIds.addAll(mCheckedFriendIds);

            client.uploadMedia(friendIds, photoPath, drawingPath);

            File photoFile = new File(photoPath);
            photoFile.delete();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
