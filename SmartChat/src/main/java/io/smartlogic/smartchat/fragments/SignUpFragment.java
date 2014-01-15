package io.smartlogic.smartchat.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.activities.ContactsActivity;
import io.smartlogic.smartchat.api.ApiClient;
import io.smartlogic.smartchat.api.GCMRegistration;
import io.smartlogic.smartchat.models.User;
import io.smartlogic.smartchat.sync.SyncClient;

public class SignUpFragment extends Fragment {
    private Button mSignUpButton;

    public SignUpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mSignUpButton = (Button) rootView.findViewById(R.id.sign_up);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });

        return rootView;
    }

    public void attemptSignUp() {
        EditText usernameView = (EditText) getView().findViewById(R.id.username);
        EditText emailView = (EditText) getView().findViewById(R.id.email);
        EditText passwordView = (EditText) getView().findViewById(R.id.password);

        usernameView.setError(null);
        emailView.setError(null);
        passwordView.setError(null);

        String username = usernameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.email_blank));
            focusView = emailView;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailView.setError(getString(R.string.email_invalid));
            focusView = emailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.password_blank));
            focusView = passwordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new RegistrationTask(username, email, password).execute();

            mSignUpButton.setOnClickListener(null);
        }
    }

    private class RegistrationTask extends AsyncTask<Void, Void, Void> {
        private String username;
        private String email;
        private String password;

        public RegistrationTask(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... params) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);

            ApiClient client = new ApiClient();
            String base64PrivateKey = client.registerUser(user);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.EXTRA_PRIVATE_KEY, base64PrivateKey);
            editor.putString(Constants.EXTRA_USERNAME, user.getUsername());
            editor.commit();

            new GCMRegistration(getActivity()).check();

            new SyncClient(getActivity()).sync();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(getActivity(), ContactsActivity.class);
            startActivity(intent);
        }
    }
}
