package io.smartlogic.smartchat.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.R;
import io.smartlogic.smartchat.views.DrawingView;

public class SmartChatPreviewActivity extends Activity {
    private DrawingView mDrawingView;
    private Button mUploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_chat_preview);

        File pictureFile = new File(getIntent().getExtras().getString(Constants.EXTRA_PHOTO_PATH));
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());

        ImageView preview = (ImageView) findViewById(R.id.smartchat);
        preview.setImageBitmap(bitmap);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.container);

        RelativeLayout.LayoutParams previewLayoutParams = (RelativeLayout.LayoutParams) preview.getLayoutParams();

        mDrawingView = new DrawingView(this);
        mDrawingView.setLayoutParams(previewLayoutParams);

        layout.addView(mDrawingView);

        mUploadButton = (Button) findViewById(R.id.upload);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SmartChatPreviewActivity.this, UploadActivity.class);

                if (mDrawingView.doesDrawingExist()) {
                    String drawingPhotoPath = saveDrawing();
                    intent.putExtra(Constants.EXTRA_DRAWING_PATH, drawingPhotoPath);
                }

                intent.putExtra(Constants.EXTRA_PHOTO_PATH, getIntent().getExtras().getString(Constants.EXTRA_PHOTO_PATH));
                startActivity(intent);
            }
        });
    }

    private String saveDrawing() {
        try {
            File pictureFile = File.createTempFile("drawing", ".png", getExternalCacheDir());

            OutputStream out = new FileOutputStream(pictureFile);
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
