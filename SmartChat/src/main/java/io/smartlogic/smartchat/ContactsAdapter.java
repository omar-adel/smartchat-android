package io.smartlogic.smartchat;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ContactsAdapter extends CursorAdapter {
    public interface OnContactAddedListener {
        public void onContactAdded(int contactId);
    }

    private Context mContext;
    private OnContactAddedListener onContactAddedListener;

    public ContactsAdapter(Context context, OnContactAddedListener onContactAddedListener, Cursor cursor) {
        super(context, cursor, true);

        this.mContext = context;
        this.onContactAddedListener = onContactAddedListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return View.inflate(context, R.layout.adapter_contacts, null);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        final int idFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
        int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

        TextView contactName = (TextView) view.findViewById(R.id.contact_name);
        contactName.setText(cursor.getString(nameFieldColumnIndex));

        Button addContact = (Button) view.findViewById(R.id.add_contact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onContactAddedListener.onContactAdded(cursor.getInt(idFieldColumnIndex));
            }
        });
    }
}
