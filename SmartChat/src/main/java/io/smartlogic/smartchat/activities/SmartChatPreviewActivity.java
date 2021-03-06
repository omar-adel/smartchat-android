package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.views.DrawingView;

@EActivity(R.layout.activity_smart_chat_preview)
public class SmartChatPreviewActivity extends Activity {
    private File mediaFile;
    private DrawingView mDrawingView;
    private int mExpireIn = Constants.DEFAULT_EXPIRE_IN;

    private boolean mMessageShowing = false;

    @ViewById(R.id.message_edit)
    EditText mMessageEdit;

    @ViewById(R.id.undo)
    Button mUndoButton;

    @ViewById(R.id.container)
    RelativeLayout mContainer;

    @ViewById(R.id.smartchat_photo)
    ImageView previewPhoto;

    @ViewById(R.id.smartchat_video)
    VideoView previewVideo;

    @ViewById(R.id.expire_in)
    Button mExpireInButton;

    @AfterViews
    protected void afterViews() {
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        String photoPath = null;
        String videoPath = null;
        if (getIntent() != null && getIntent().getExtras() != null) {
            photoPath = getIntent().getExtras().getString(Constants.EXTRA_PHOTO_PATH, null);
            videoPath = getIntent().getExtras().getString(Constants.EXTRA_VIDEO_PATH, null);
        }

        View preview = previewPhoto;

        if (photoPath != null) {
            mediaFile = new File(photoPath);
            Bitmap bitmap = BitmapFactory.decodeFile(mediaFile.getAbsolutePath());

            previewPhoto.setImageBitmap(bitmap);

            mContainer.removeView(previewVideo);
        } else if (videoPath != null) {
            mediaFile = new File(videoPath);

            previewVideo.setVideoPath(mediaFile.getPath());
            previewVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    previewVideo.start();
                }
            });
            previewVideo.start();

            preview = previewVideo;

            mContainer.removeView(previewPhoto);

            MediaPlayer mp = MediaPlayer.create(this, Uri.parse(videoPath));
            mExpireIn = (int) Math.ceil(((float) mp.getDuration()) / 1000);
            mp.release();
            mContainer.removeView(mExpireInButton);
        }

        RelativeLayout.LayoutParams previewLayoutParams = (RelativeLayout.LayoutParams) preview.getLayoutParams();

        mDrawingView = new DrawingView(this);
        mDrawingView.setLayoutParams(previewLayoutParams);

        int drawingViewIndex = mContainer.indexOfChild(preview) + 1;
        mContainer.addView(mDrawingView, drawingViewIndex);

        mMessageEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    toggleMessage();
                }
                return false;
            }
        });
        mMessageEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newStr = mDrawingView.textChanged(s.toString());

                if (!newStr.equals(s.toString())) {
                    mMessageEdit.setText(newStr);
                    mMessageEdit.setSelection(newStr.length());
                }
            }
        });
    }

    @Click(R.id.upload)
    void upload() {
        Intent intent = new Intent(SmartChatPreviewActivity.this, PickFriendsActivity.class);

        if (mDrawingView.doesDrawingExist()) {
            String drawingPhotoPath = saveDrawing();
            intent.putExtra(Constants.EXTRA_DRAWING_PATH, drawingPhotoPath);
        }

        intent.putExtra(Constants.EXTRA_FILE_PATH, mediaFile.getPath());
        intent.putExtra(Constants.EXTRA_EXPIRE_IN, mExpireIn);
        startActivity(intent);
    }

    @Click(R.id.expire_in)
    void expireIn() {
        final NumberPicker picker = new NumberPicker(SmartChatPreviewActivity.this);
        picker.setMinValue(3);
        picker.setMaxValue(20);
        picker.setWrapSelectorWheel(false);
        picker.setValue(mExpireIn);

        AlertDialog dialog = new AlertDialog.Builder(SmartChatPreviewActivity.this)
                .setTitle(R.string.expire_in)
                .setView(picker)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mExpireIn = picker.getValue();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        dialog.show();
    }

    @Click(R.id.undo)
    void undo() {
        mDrawingView.undoPath();
    }

    @Click(R.id.draw)
    void toggleDraw() {
        mUndoButton.setVisibility(mUndoButton.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

        mDrawingView.toggleDrawing();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(event);
        }

        toggleMessage();

        return true;
    }

    private boolean toggleMessage() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (mMessageShowing) {
            mMessageEdit.setVisibility(View.INVISIBLE);
            mDrawingView.setText(mMessageEdit.getText().toString());

            inputMethodManager.hideSoftInputFromWindow(mContainer.getWindowToken(), 0);
        } else {
            mMessageEdit.setVisibility(View.VISIBLE);
            mMessageEdit.requestFocus();

            inputMethodManager.toggleSoftInputFromWindow(mContainer.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        }

        mMessageShowing = !mMessageShowing;

        mDrawingView.setTextShowing(!mMessageShowing);

        return mMessageShowing;
    }

    @Override
    public void onBackPressed() {
        if (mMessageShowing) {
            toggleMessage();
            return;
        }

        if (mediaFile != null) {
            mediaFile.delete();
        }

        super.onBackPressed();
    }

    private String saveDrawing() {
        try {
            File pictureFile = File.createTempFile("drawing", ".png", getExternalCacheDir());

            OutputStream out = new FileOutputStream(pictureFile);
            mDrawingView.hideSwatch();
            mDrawingView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            return pictureFile.getAbsolutePath();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
