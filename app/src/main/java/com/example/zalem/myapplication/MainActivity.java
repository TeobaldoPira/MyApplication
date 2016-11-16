package com.example.zalem.myapplication;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gun0912.tedpicker.ImagePickerActivity;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.theartofdev.edmodo.cropper.CropImageView.CropShape;
import com.theartofdev.edmodo.cropper.CropImageView.OnGetCroppedImageCompleteListener;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCrop.Options;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity {

    private static final int INTENT_REQUEST_GET_IMAGES = 99;
    private TextView textView;

    private CircularImageView circularImageView;
    private CropImageView cropImageView;
    private ImageView normalImageView;

    //Testando uma alteração para o github...
    //Testando outra alteração!
    //Alteração feita no próprio github!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);

        ActivityCompat.requestPermissions(this, new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.CAMERA}, 0);

        circularImageView = (CircularImageView) findViewById(R.id.circularImageView);
        normalImageView = (ImageView) findViewById(R.id.normalImageView);

        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        cropImageView.setCropShape(CropShape.OVAL);
        cropImageView.setAspectRatio(1, 1);
        cropImageView.setFixedAspectRatio(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == INTENT_REQUEST_GET_IMAGES && resultCode == Activity.RESULT_OK) {
            imageUris = intent.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);

            textView.setText(TextUtils.join("\n", imageUris));
            return;
        }


        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(intent);

            Bitmap bitmap = BitmapFactory.decodeFile(new File(resultUri.getPath()).getPath());
            System.out.println("TESTE BITMAP SIZE = " + bitmap.getWidth() + " - " + bitmap.getHeight());

//            circularImageView.setImageBitmap(bitmap);

            //TODO REMOVER! APENAS TESTE!
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 0, out);
            System.out.println("TESTE PNG: " + out.toByteArray().length);

            out = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 100, out);
            System.out.println("TESTE JPEG 100: " + out.toByteArray().length);

            out = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 75, out);
            System.out.println("TESTE JPEG 75: " + out.toByteArray().length);

            out = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.WEBP, 100, out);
            System.out.println("TESTE WEBP 100: " + out.toByteArray().length);

            out = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.WEBP, 90, out);
            System.out.println("TESTE WEBP 90: " + out.toByteArray().length);






            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(), new ByteArrayInputStream(out.toByteArray()));
//            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            normalImageView.setImageDrawable(roundedBitmapDrawable);


            return;
        }

        EasyImage.handleActivityResult(requestCode, resultCode, intent, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                textView.setText("ERRO!");
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                if (type == 0) {
                    textView.setText(imageFile.getAbsolutePath());

                    cropImageView.setImageUriAsync(Uri.fromFile(imageFile));
                } else if (type == 1) {


                    try {
                        File tempFile = File.createTempFile("img", "png", MainActivity.this.getCacheDir());

                        Options options = new Options();
                        options.setOvalDimmedLayer(true);
                        options.setShowCropFrame(false);
                        options.setShowCropGrid(false);
                        UCrop.of(Uri.fromFile(imageFile), Uri.fromFile(tempFile))
                                .withOptions(options)
                                .withAspectRatio(1, 1)
                                .withMaxResultSize(196, 196)
                                .start(MainActivity.this);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
    }


    /*
     * Ted Picker
     */

    private ArrayList<Uri> imageUris;

    public void openTedPicker(View v) {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        if (imageUris != null) {
            intent.putParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS, imageUris);
        }

        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
    }

    public void openTedPickerNew(View v) {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
    }


    /*
     * Easy Image
     */

    public void openEasyImagePicker(View v) {
        EasyImage.openChooserWithGallery(this, null, 0);
    }


    /*
     * Image Cropper
     */

    public void cropImage(View v) {
        cropImageView.setOnGetCroppedImageCompleteListener(new OnGetCroppedImageCompleteListener() {
            @Override
            public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {
                System.out.println("TESTE BITMAP SIZE = " + bitmap.getWidth() + " - " + bitmap.getHeight());
                cropImageView.setImageBitmap(bitmap);
            }
        });

        cropImageView.getCroppedImageAsync(CropShape.OVAL, 50, 50);
    }


    /*
     *
     */
    public void openUCropPicker(View v) throws IOException {
        EasyImage.openChooserWithGallery(this, "Escolher imagem usando", 1);
    }
}
