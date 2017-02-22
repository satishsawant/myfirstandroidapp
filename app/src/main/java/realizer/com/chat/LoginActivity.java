package realizer.com.chat;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import realizer.com.chat.backend.DatabaseQueries;
import realizer.com.chat.chat.ChatThreadListActivity;
import realizer.com.chat.registration.RegistrationActivity;
import realizer.com.chat.security.MagicWordAsyncTaskGet;
import realizer.com.chat.security.PaswordtoEmailAsyncTaskPost;
import realizer.com.chat.security.ResetPasswordAsyncTaskPost;
import realizer.com.chat.user.UserProfileActivity;
import realizer.com.chat.utils.Config;
import realizer.com.chat.utils.ImageStorage;
import realizer.com.chat.utils.OnTaskCompleted;
import realizer.com.chat.utils.Singleton;
import realizer.com.chat.utils.StoreBitmapImages;
import realizer.com.chat.view.ProgressWheel;

/**
 * Created by Win on 30/11/2016.
 */
public class LoginActivity extends Activity implements OnTaskCompleted
{
    EditText userName, password;
    CheckBox checkBox;
    SharedPreferences sharedpreferences;
    AlertDialog.Builder adbdialog;
    DatabaseQueries dbqr;
    int num,n;
    ProgressWheel loading;
    String FirebaseTocken="";
    private static final String TAG = "Firebase";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        userName = (EditText) findViewById(R.id.edtEmpId);
        password = (EditText) findViewById((R.id.edtPassword));
        checkBox = (CheckBox) findViewById(R.id.checkBox1);
        dbqr=new DatabaseQueries(getApplicationContext());
        loading = (ProgressWheel) findViewById(R.id.loading);
        Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    userName.setText(result);
                    userName.setSelection(result.length());
                    // alert the user
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    password.setText(result);
                    password.setSelection(result.length());
                    // alert the user
                }
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = sharedpreferences.edit();
                if (checkBox.isChecked()) {

                    edit.putString("UserName", userName.getText().toString().trim());
                    edit.putString("Password", password.getText().toString().trim());
                    edit.putString("CHKSTATE", "true");
                    edit.commit();
                } else {
                    edit.putString("UserName", "");
                    edit.putString("Password", "");
                    edit.putString("CHKSTATE", "false");
                    edit.commit();
                }
            }
        });
        String chk = sharedpreferences.getString("CHKSTATE","");
        Log.d("CHECKED", chk);
        if(chk.equals("true"))
        {
            checkBox.setChecked(true);
            userName.setText(sharedpreferences.getString("UserName",""));
            password.setText(sharedpreferences.getString("Password", ""));
        }
        else
        {
            checkBox.setChecked(false);
            userName.setText("");
            password.setText("");
        }
    }
    public void LoginClick(View v)
    {

        boolean res = isConnectingToInternet();
        if (res == false) {
            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            //Utils.alertDialog(LoginActivity.this, "", Utils.actionBarTitle(getString(R.string.LoginNoInternate)).toString());
        } else if (userName.getText().toString().equals("") && password.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter Username/Password", Toast.LENGTH_LONG).show();
            //Utils.alertDialog(LoginActivity.this, "", Utils.actionBarTitle(getString(R.string.LoginEnterUserPswd)).toString());
        } else if (userName.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter Username", Toast.LENGTH_LONG).show();
            //Utils.alertDialog(LoginActivity.this, "", Utils.actionBarTitle(getString(R.string.LoginEnterUsername)).toString());
        } else if (password.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Please Enter Password", Toast.LENGTH_LONG).show();
            //Utils.alertDialog(LoginActivity.this, "", Utils.actionBarTitle(getString(R.string.LoginEnterPassword)).toString());
        } else {

            final SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            String logchk = sharedpreferences.getString("LogChk", "");
            String uname = sharedpreferences.getString("UserName","");
            if(logchk.equals("true") && !uname.equals(userName.getText().toString().trim()))
            {
                adbdialog = new AlertDialog.Builder(LoginActivity.this);
                adbdialog.setTitle("Login Alert");
                adbdialog.setMessage("All the Data of Previous User will be Deleted,\nDo You want to Proceed?");
                adbdialog.setIcon(android.R.drawable.ic_dialog_alert);
                adbdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //loading.setVisibility(View.VISIBLE);
                        new NewLoginAsync().execute();

                    } });


                adbdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        userName.setText("");
                        password.setText("");
                    } });
                adbdialog.show();

            }
            else {
                if(Config.isConnectingToInternet(LoginActivity.this)) {
                    loading.setVisibility(View.VISIBLE);
                    FireBaseInitialization();
                    LoginAsyncTaskPost obj = new LoginAsyncTaskPost(userName.getText().toString(), password.getText().toString(), FirebaseTocken, LoginActivity.this, LoginActivity.this);
                    obj.execute();
                }
                else
                {
                    Config.alertDialog(LoginActivity.this,"Network Error","Please check your internet connection");
                }
            }
        }




    }
    public void SignupClick(View v)
    {
        Intent i=new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(i);
    }
    public void forgotPass(View v)
    {
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.forgotpwd_recoveryoption, null);
        Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final RadioButton mail = (RadioButton)dialoglayout.findViewById(R.id.rb_option_mail);
        final RadioButton magicword = (RadioButton)dialoglayout.findViewById(R.id.rb_option_magic_word);
        submit.setTypeface(face);
        cancel.setTypeface(face);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(dialoglayout);

        final AlertDialog alertDialog = builder.create();
        mail.setChecked(true);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mail.isChecked()) {
                    alertDialog.dismiss();
                    recoverPasswordByEmail();
                }
                else if (magicword.isChecked()) {
                    alertDialog.dismiss();
                    recoverPasswordByMagicWord();
                }
                else {
                    Config.alertDialog(LoginActivity.this,"Error","Please select anyone option for recovery password.");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
    public void recoverPasswordByEmail()
    {
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.forgotpwd_rmailpassword, null);
        Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText userID = (EditText)dialoglayout.findViewById(R.id.edtuserid);
        final EditText email = (EditText)dialoglayout.findViewById(R.id.edtmailid);
        submit.setTypeface(face);
        cancel.setTypeface(face);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(dialoglayout);
        final AlertDialog alertDialog = builder.create();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userID.getText().toString().trim();
                String userEmail =  email.getText().toString().trim();

                if(userID.length()>0 && userEmail.length()>0)
                {
                    if (Config.isConnectingToInternet(LoginActivity.this)) {
                        PaswordtoEmailAsyncTaskPost emailpass = new PaswordtoEmailAsyncTaskPost(LoginActivity.this, userId, userEmail, LoginActivity.this);
                        emailpass.execute();
                        loading.setVisibility(View.VISIBLE);
                        alertDialog.dismiss();
                    }
                    else
                    {
                        Config.alertDialog(LoginActivity.this,"Network Error","Please check your internet connection");
                    }

                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter User ID/Email ID...", Toast.LENGTH_LONG).show();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void recoverPasswordByMagicWord()
    {
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.forgotpwd_mwordpassword, null);
        Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText userID = (EditText)dialoglayout.findViewById(R.id.edtuserid);
        userID.setText(userName.getText().toString());
        final EditText magicWord = (EditText)dialoglayout.findViewById(R.id.edtmagicword);

        final TextView titledialog = (TextView)dialoglayout.findViewById(R.id.dialogTitle);
        final TextView infodialog = (TextView)dialoglayout.findViewById(R.id.infodialog);

        submit.setTypeface(face);
        cancel.setTypeface(face);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(dialoglayout);

        final AlertDialog alertDialog = builder.create();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userID.getText().toString().trim();
                String wordMagic =  magicWord.getText().toString().trim();
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("MagicLoginID", userId);
                edit.commit();

                if (userId.toString().equals(""))
                {
                    Config.alertDialog(LoginActivity.this,"Error","User Id is Empty");
                }
                else if (wordMagic.toString().equals(""))
                {
                    Config.alertDialog(LoginActivity.this,"Error","Magic Word is Empty");
                }
                else
                {
                    if (Config.isConnectingToInternet(LoginActivity.this))
                    {
                        MagicWordAsyncTaskGet getmagicword=new MagicWordAsyncTaskGet(LoginActivity.this,LoginActivity.this,wordMagic,userId);
                        getmagicword.execute();
                        loading.setVisibility(View.VISIBLE);
                        alertDialog.dismiss();
                    }
                    else
                    {
                        Config.alertDialog(LoginActivity.this,"Network Error","No Internet Connection");
                    }
                }
                //resetPassword();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.INVISIBLE);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onTaskCompleted(String s) {
        String onTaskResult[]=s.split("@@@");
        loading.setVisibility(View.INVISIBLE);
        if (onTaskResult[1].equals("LoginIN"))
        {
            LoginOntask(onTaskResult[0]);
        }
        else if (onTaskResult[1].equals("GetMagicWord"))
        {
            GetMagicWordOntask(onTaskResult[0]);
        }
        else if (onTaskResult[1].equals("PasswordReset"))
        {
            loading.setVisibility(View.INVISIBLE);
            if (onTaskResult[0].equals("true"))
            {
                Config.alertDialog(LoginActivity.this,"Success","Password Reset Successfully");
            }
            else
            {
                Config.alertDialog(LoginActivity.this,"Alert","Password Reset Failed");
            }
        }
        else if (onTaskResult[1].equals("PasswordToEmail"))
        {
            loading.setVisibility(View.INVISIBLE);
            if (onTaskResult[0].equals("true"))
            {
                Config.alertDialog(LoginActivity.this,"Success","Password sent to your Email Address.");
            }
            else
            {
                Config.alertDialog(LoginActivity.this,"Error","Invalid Inputs.");
                recoverPasswordByEmail();
            }
        }

    }
    public void LoginOntask(String s)
    {
        String result="";
        JSONObject rootObj = null;
        Log.d("String", s);
        try {

            rootObj = new JSONObject(s);
            String userId="",fullname="",initial="",thumbnailurl="";
            String success="";
            success=rootObj.getString("Success");
            userId = rootObj.getString("UserId");
            fullname = rootObj.getString("UserFullName");
            initial = rootObj.getString("UserInitials");
            thumbnailurl = rootObj.getString("ThumbanilUrl");

            if (success.equals("true")) {
                long n1 = dbqr.insertUserInfo(userId, fullname, initial, thumbnailurl);
                if (n1 >= 0) {
                    Log.d("UserList", " Done!!!");
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("Login", "true");
                    edit.putString("userId",userId);
                    edit.putString("userFullName",fullname);
                    edit.putString("userThumbnail",thumbnailurl);
                    edit.putString("userLoginId",userName.getText().toString());
                    edit.putString("TockenID", FirebaseTocken);
                    edit.commit();

                    String newURL=thumbnailurl;
                    if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1])) {
                        new StoreBitmapImages(newURL, newURL.split("/")[newURL.split("/").length - 1]).execute(newURL);
                    }
                    Intent i=new Intent(LoginActivity.this, ChatThreadListActivity.class);
                    startActivity(i);
                    finish();
                    result = "User Inserted";
                } else {
                    Log.d("UserList", " Not Done!!!");
                    result = "User Not Inserted";
                }
            }
            else if (success.equals("false"))
            {
                Config.alertDialog(LoginActivity.this,"Authentication Failed..","Invalid Username/Password");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            num = 1;
            Log.e("JSON", e.toString());
            Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
            Log.e("Login(JStackTrace)", e.getStackTrace().toString());
            Log.e("Login(JCause)", e.getCause().toString());
            Log.wtf("Login(JMsg)", e.getMessage());
        }
    }
    public void GetMagicWordOntask(String s)
    {
        JSONObject rootObj = null;
        Log.d("String", s);
        try {

            rootObj = new JSONObject(s);
            String userId="";
            String success="";
            success=rootObj.getString("Success");
            userId = rootObj.getString("userId");

            if (success.equals("true")) {
                ResetPasswordByMagic(userId);
            }
            else if (success.equals("false"))
            {
                recoverPasswordByMagicWord();
                Config.alertDialog(LoginActivity.this, "Error", "Invalid Inputs.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            num = 1;
            Log.e("JSON", e.toString());
            Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
            Log.e("Login(JStackTrace)", e.getStackTrace().toString());
            Log.e("Login(JCause)", e.getCause().toString());
            Log.wtf("Login(JMsg)", e.getMessage());
        }
    }
    public class NewLoginAsync extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            dbqr.deleteAllData();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("LogChk", "false");
            edit.commit();
            LoginAsyncTaskPost obj = new LoginAsyncTaskPost(userName.getText().toString(), password.getText().toString(), sharedpreferences.getString("TockenID",""),LoginActivity.this, LoginActivity.this);
            obj.execute();
        }
    }
    public boolean isConnectingToInternet(){

        ConnectivityManager connectivity =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }
    public void FireBaseInitialization()
    {
        String token = FirebaseInstanceId.getInstance().getToken();
        //FirebaseInstanceId.getInstance().deleteToken();
        String msg2 = getString(R.string.msg_token_fmt, token);
        Log.d(TAG, msg2);
        //Toast.makeText(LoginActivity.this, msg2, Toast.LENGTH_SHORT).show();
        FirebaseTocken=token;

    }
    public void FIreBaseCreateTopic(String topicName)
    {
        FirebaseMessaging.getInstance().subscribeToTopic(topicName);
        String msg = getString(R.string.msg_subscribed);
        Log.d(TAG, msg);
        //Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void ResetPasswordByMagic(String magicuserId)
    {
        final String MaguserId=magicuserId;
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        String LoginIdnew=sharedpreferences.getString("MagicLoginID", "");
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.forgotpwd_resetpassword, null);
        Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText userID = (EditText)dialoglayout.findViewById(R.id.edtuserid);
        userID.setText(LoginIdnew);
        final EditText password = (EditText)dialoglayout.findViewById(R.id.edtpwd);
        final EditText confpassword = (EditText)dialoglayout.findViewById(R.id.edtconfirmpwd);
        submit.setTypeface(face);
        cancel.setTypeface(face);

        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(dialoglayout);

        final AlertDialog alertDialog = builder.create();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordstr = password.getText().toString().trim();
                String confpasswordstr=confpassword.getText().toString().trim();
                if (password.getText().toString().equals(""))
                {
                    Config.alertDialog(LoginActivity.this, "Alert", "Enter Password");
                }
                else if(passwordstr.trim().length()<6)
                {
                    Config.alertDialog(LoginActivity.this, "Alert", "Password length must be 6 to 10 digit/characters");
                }
                else if (confpassword.getText().toString().equals(""))
                {
                    Config.alertDialog(LoginActivity.this, "Alert", "Enter Confirm Password");
                }
                else if(confpasswordstr.trim().length()<6)
                {
                    Config.alertDialog(LoginActivity.this, "Alert", "Confirm Password length must be 6 to 10 digit/characters");
                }
                else if (password.getText().toString().equals(confpassword.getText().toString()))
                {
                    if (Config.isConnectingToInternet(LoginActivity.this)) {
                        ResetPasswordAsyncTaskPost resetPasswordAsyncTaskPost = new ResetPasswordAsyncTaskPost(LoginActivity.this, MaguserId, passwordstr, LoginActivity.this);
                        resetPasswordAsyncTaskPost.execute();
                        loading.setVisibility(View.VISIBLE);
                        alertDialog.dismiss();
                    }
                    else
                    {
                        Config.alertDialog(LoginActivity.this,"Network Error","Please check your internet connection");
                    }
                }
                else {
                        Config.alertDialog(LoginActivity.this, "Alert", "Password and Confirm password not Match");
                }
            }
        }

        );
        cancel.setOnClickListener(new View.OnClickListener()

                                  {
                                      @Override
                                      public void onClick(View v) {
                                          loading.setVisibility(View.INVISIBLE);
                                          alertDialog.dismiss();
                                      }
                                  }

        );

            alertDialog.show();

    }

}
