package realizer.com.chat.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import realizer.com.chat.LoginActivity;
import realizer.com.chat.R;
import realizer.com.chat.user.asynctask.UserDetailsAsyncTaskGet;
import realizer.com.chat.user.asynctask.UserProfileAsyncTaskPut;
import realizer.com.chat.utils.Config;
import realizer.com.chat.utils.GetImages;
import realizer.com.chat.utils.ImageStorage;
import realizer.com.chat.utils.OnTaskCompleted;
import realizer.com.chat.utils.Utility;
import realizer.com.chat.view.FullImageViewActivity;
import realizer.com.chat.view.ProgressWheel;

/**
 * Created by Win on 07/12/2016.
 */
public class UserProfileActivity extends AppCompatActivity implements OnTaskCompleted
{
    ProgressWheel loading;
    private Uri fileUri;
    Bitmap bitmap;
    ImageView userimg;
    String localPath="";
    String UserID;
    String image64bit="";
    String ThumbnailUrl="";
    TextView userfullname,email,phno,dateofbirth,NewProfilePic;
    String firstName,lastname,emailId,PhoneNo,DateCreated,dob;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_setting_activity);
        getSupportActionBar().setTitle("Profile");

        Drawable upArrow = ContextCompat.getDrawable(UserProfileActivity.this, R.drawable.ic_arrow_back);
        upArrow.setColorFilter(ContextCompat.getColor(UserProfileActivity.this, R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ab.setLogo(R.drawable.setting_icon);
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/BRLNSR.TTF");

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(UserProfileActivity.this);
        UserID=sharedpreferences.getString("userId", "");
        loading = (ProgressWheel) findViewById(R.id.loading);

        userfullname = (TextView) findViewById(R.id.setting_user_fullname);
        email= (TextView) findViewById(R.id.setting_user_emailId);
        phno= (TextView) findViewById(R.id.setting_user_phoneno);
        dateofbirth= (TextView) findViewById(R.id.setting_user_dob);
        NewProfilePic= (TextView) findViewById(R.id.setting_new_profile);
        loading.setVisibility(View.VISIBLE);

        userfullname.setTypeface(face);
        email.setTypeface(face);
        phno.setTypeface(face);
        dateofbirth.setTypeface(face);
        userimg= (ImageView) findViewById(R.id.setting_user_icon);

        if (Config.isConnectingToInternet(UserProfileActivity.this)) {
            UserDetailsAsyncTaskGet getdetails = new UserDetailsAsyncTaskGet(UserProfileActivity.this, UserProfileActivity.this, UserID);
            getdetails.execute();
        }
        else
        {
            Config.alertDialog(UserProfileActivity.this,"Network Error","No Internet Connection..!");
        }

        NewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOption();
            }
        });

        userimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b1=new Bundle();
                b1.putString("ImgURLUSER",ThumbnailUrl);
                Intent i=new Intent(UserProfileActivity.this, FullImageViewActivity.class);
                i.putExtras(b1);
                startActivity(i);
            }
        });
    }

    public void getOption() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.putExtra("crop", "true");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose Action");

        Intent[] intentArray = {cameraIntent};

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooser, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    public Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }
    private static File getOutputMediaFile(int type) {

        //External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            File sdcard = Environment.getExternalStorageDirectory() ;

            File folder = new File(sdcard.getAbsoluteFile(), Config.IMAGE_DIRECTORY_NAME);//the dot makes this directory hidden to the user
            folder.mkdir();

        }

        // Create search_layout media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");


        return mediaFile;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                if (data == null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    // down sizing image as it throws OutOfMemory Exception for larger
                    // images
                    options.inSampleSize = 8;
                    final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
                    Log.d("PATH", fileUri.getPath());
                    setPhoto(bitmap);
                    userimg.setImageBitmap(bitmap);
                    String path = encodephoto(bitmap);
                    image64bit=path;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ProfilePicPath", path);
                    editor.commit();
                    launchUploadActivity(data);
                } else
                {launchUploadActivity(data);}

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }

        }
    }

    //Encode image to Base64 to send to server
    private void setPhoto(Bitmap bitmapm) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                /*Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");*/

            }
        }
        else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmapm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            //4
            File file = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpeg");
            try {
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                //5
                fo.write(bytes.toByteArray());
                fo.close();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
    }
    private void launchUploadActivity(Intent data) {

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userid = sharedpreferences.getString("UidName", "");
        if (data.getData() != null) {
            try {
                if (bitmap != null) {
                    //bitmap.recycle();
                }

                InputStream stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                /*localPath = ImageStorage.saveEventToSdCard(bitmap, "userImages", UserProfileActivity.this);*/
                userimg.setImageBitmap(bitmap);
                String path = encodephoto(bitmap);
                image64bit=path;


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = (Bitmap) data.getExtras().get("data");
            localPath = ImageStorage.saveEventToSdCard(bitmap, "userImages",UserProfileActivity.this);
            userimg.setImageBitmap(bitmap);
            String path = encodephoto(bitmap);
            image64bit=path;
        }
        UploadThumbnail();
    }
    //Encode image to Base64 to send to server
    private String encodephoto(Bitmap bitmapm) {
        String imagebase64string = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] byteArrayImage = baos.toByteArray();
            imagebase64string = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagebase64string;
    }

    public void UploadThumbnail()
    {
        if (Config.isConnectingToInternet(UserProfileActivity.this)) {
            loading.setVisibility(View.VISIBLE);
            userimg.setEnabled(false);
            NewProfilePic.setEnabled(false);
            UserProfileAsyncTaskPut thumbnailPut = new UserProfileAsyncTaskPut(UserID, image64bit, UserProfileActivity.this, UserProfileActivity.this);
            thumbnailPut.execute();
        }
        else {
            Config.alertDialog(UserProfileActivity.this,"Network Error","No Internet Connection");
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onTaskCompleted(String s)
    {
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loading.setVisibility(View.GONE);
        userimg.setEnabled(true);
        NewProfilePic.setEnabled(true);
        String[] onTask=s.split("@@@");
        if (onTask[1].equals("ProfilePic"))
        {
            JSONObject rootObj = null;
            Log.d("String", onTask[0]);
            try {

                rootObj = new JSONObject(onTask[0]);
                String success=rootObj.getString("Success");
                String thumbnailUrl = rootObj.getString("ThumbanilUrl");
                if (success.equals("true"))
                {
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("userThumbnail", thumbnailUrl);
                    edit.commit();
                    ThumbnailUrl=thumbnailUrl;
                    Config.alertDialog(UserProfileActivity.this,"Success","Profile pic successfully updated.");
                }

            } catch (JSONException e) {
                e.printStackTrace();

                Log.e("JSON", e.toString());
                Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                Log.e("Login(JCause)", e.getCause().toString());
                Log.wtf("Login(JMsg)", e.getMessage());
            }
            SetThumbnail();
        }
        else if (onTask[1].equals("userDetails"))
        {
            JSONArray rootarray=null;
            JSONObject rootObj = null;
            Log.d("String", onTask[0]);
            try {
                rootarray=new JSONArray(onTask[0]);
                rootObj = rootarray.getJSONObject(0);
                firstName=rootObj.getString("firstName");
                lastname=rootObj.getString("lastName");
                emailId=rootObj.getString("emailId");
                PhoneNo=rootObj.getString("phoneNo");
                DateCreated=rootObj.getString("createTS");
                dob=rootObj.getString("dateofBirth");
                ThumbnailUrl=rootObj.getString("thumbnailUrl");

                userfullname.setText(firstName+" "+lastname);
                email.setText(emailId);
                phno.setText(PhoneNo);
                dateofbirth.setText(DateFormat(dob));
                //datecreate.setText(DateFormat("Date Created : "+DateFormat(DateCreated)));


            } catch (JSONException e) {
                e.printStackTrace();

                Log.e("JSON", e.toString());
                Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                Log.e("Login(JCause)", e.getCause().toString());
                Log.wtf("Login(JMsg)", e.getMessage());
            }

            SetThumbnail();
        }

    }
    public String DateFormat(String dateinput)
    {
        String[] setnttime=dateinput.split("T");
        String[] date=setnttime[0].split("-");
        String month=Config.getMonth(Integer.valueOf(date[1]));
        String newdate=date[2]+"/"+ month+"/"+date[0];
        return newdate;
    }
    public void SetThumbnail()
    {
        if (ThumbnailUrl.equals("")||ThumbnailUrl.equals("null")||ThumbnailUrl.equals(null))
        {

        }
        else{
            String newURL= Utility.getURLImage(ThumbnailUrl);
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,userimg,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length-1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                userimg.setImageBitmap(bitmap);
            }
        }
    }
}
