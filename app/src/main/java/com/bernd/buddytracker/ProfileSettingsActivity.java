package com.bernd.buddytracker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ProfileSettingsActivity extends ActionBarActivity {

    private static String root = null;
    private static String imageFolderPath = null;
    private String imageName = "own.png";
    private static Uri fileUri = null;
    private static final int CAMERA_IMAGE_REQUEST = 1;

    public final static String propNickname = "nick";

    private static String nickName;
    private String FILENAME = "buddySettings.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        if (savedInstanceState != null) {
            String uri = savedInstanceState.getString("file-uri");
            if (!fileUri.equals("")) fileUri = Uri.parse(uri);
        }

        Button btn_save = (Button) findViewById(R.id.btn_save);
        nickName = getAttributes();
        EditText edit_name = (EditText) findViewById(R.id.edit_name);
        edit_name.setText(nickName);
        //Eingegebenen Namen speichern
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit_name = (EditText) findViewById(R.id.edit_name);
                ProfileSettingsActivity.nickName = edit_name.getText().toString();
                Toast.makeText(ProfileSettingsActivity.this, getString(R.string.nameSaved), Toast.LENGTH_SHORT).show();
                saveAttributes(nickName);
            }
        });
        //Bearbeiten abbrechen
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileSettingsActivity.this, MainActivity.class));
            }
        });
        Drawable profilePicture = getResources().getDrawable(R.drawable.no_image);
        ImageView profilePictureView = (ImageView) findViewById(R.id.profile_picture);
        profilePictureView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(ProfileSettingsActivity.this, "Test", Toast.LENGTH_LONG).show();
                captureImage();
                return false;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (fileUri == null) {
            outState.putString("file-uri", "");
        } else {
            outState.putString("file-uri", fileUri.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String getNickName() {
        return nickName;
    }

    private void saveAttributes(String nick) {
        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(nick.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getAttributes() {
        try {
            StringBuilder builder = new StringBuilder();
            FileInputStream fis = openFileInput(FILENAME);
            int ch;
            while ((ch = fis.read()) != -1) {
                builder.append((char) ch);
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;

                GetImageThumbnail getImageThumbnail = new GetImageThumbnail();
                try {
                    bitmap = getImageThumbnail.getThumbnail(fileUri, this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ImageView profilePictureView = (ImageView) this.findViewById(R.id.profile_picture);

                profilePictureView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, getString(R.string.sorry), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void captureImage() {
        Context ctx = this.getApplicationContext();
        ImageView profilePictureView = (ImageView) findViewById(R.id.profile_picture);
        File path;
        //fetching root directory
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
           path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else {
           path = ctx.getCacheDir();
        }

        //if (!image.exists())
        //    image.mkdirs();

        //Creating folders for Image
        /*imageFolderPath = root + "/profilePictures";
        File imagesFolder = new File(imageFolderPath);
        imagesFolder.mkdirs();*/

        //Creating Image here
        File image = new File(path, imageName);
        fileUri = Uri.fromFile(image);
        //profilePictureView.setTag(Environment.DIRECTORY_DCIM + File.separator + imageName);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(takePictureIntent, CAMERA_IMAGE_REQUEST);
    }

    public static Drawable getExampleProfilePicture(Context con) {
        return con.getResources().getDrawable(R.drawable.example);
    }

}
