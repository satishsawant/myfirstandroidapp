package realizer.com.chat.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import realizer.com.chat.AboutMyChat;
import realizer.com.chat.LoginActivity;
import realizer.com.chat.R;
import realizer.com.chat.backend.DatabaseQueries;
import realizer.com.chat.security.ChangePasswordAsyncTaskPost;
import realizer.com.chat.security.LogoutUserAsyncTaskPost;
import realizer.com.chat.user.UserProfileActivity;
import realizer.com.chat.chat.adapter.ChatThreadListModelAdapter;
import realizer.com.chat.chat.asynctask.ThreadListAsyncTaskGet;
import realizer.com.chat.chat.model.ChatThreadListModel;
import realizer.com.chat.utils.Config;
import realizer.com.chat.utils.OnTaskCompleted;
import realizer.com.chat.utils.Singleton;
import realizer.com.chat.view.ProgressWheel;

/**
 * Created by Win on 30/11/2016.
 */
public class ChatThreadListActivity extends AppCompatActivity implements OnTaskCompleted
{
    ProgressWheel loading;
    ListView threadList;
    private MenuItem done;
    TextView noData;
    String LoginId="";
    String UserFullName="";
    MessageResultReceiver resultReceiver;

    /*DatabaseQueries qr;*/

    ArrayList<ChatThreadListModel> temp=new ArrayList<>();
    int num;

    private static final String TAG = "ThreadList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_thread_list);
       /* qr=new DatabaseQueries(getApplicationContext());*/
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
        LoginId=sharedpreferences.getString("userId","");
        UserFullName=sharedpreferences.getString("userFullName","");
        getSupportActionBar().setTitle(UserFullName);
        getSupportActionBar().setIcon(R.drawable.chat_icon);

        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        TextView userfullname= (TextView) findViewById(R.id.thread_user_name);
        /*userfullname.setText(UserFullName);
        userfullname.setTypeface(face);*/


        loading = (ProgressWheel) findViewById(R.id.loading);
        threadList= (ListView) findViewById(R.id.lsttname);
        noData = (TextView) findViewById(R.id.tvNoDataMsg);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        GetThreadLIST();

        //ArrayList<TeacherQuery1model> chat =getThreadList(temp);

        threadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = threadList.getItemAtPosition(position);
                ChatThreadListModel homeworkObj = (ChatThreadListModel) o;
                String threadId = homeworkObj.getThreadId();
                String receiverID=homeworkObj.getParticipentID();
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("ActiveThread", threadId);
                edit.commit();
                Bundle bundle = new Bundle();
                bundle.putString("THREADID",threadId );
                bundle.putString("RECEIVERID",receiverID);
                bundle.putString("ActionBarTitle",homeworkObj.getCustomThreadName());
                bundle.putString("InitiatedID", homeworkObj.getInitiateId());
                Intent i = new Intent(ChatThreadListActivity.this, ChatMessageCenterActicity.class);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }
    public void newThread(View v)
    {
        //Config.FIreBaseCreateTopic("group");
        Intent i=new Intent(ChatThreadListActivity.this, InitateNewChatActivity.class);
        startActivity(i);
    }

    @Override
    public void onTaskCompleted(String s) {
        loading.setVisibility(View.GONE);
        String[] onTask=s.split("@@@");
        if (onTask[1].equals("ThreadList"))
        {
            JSONArray rootObj = null;
            Log.d("String", onTask[0]);
            try {

                rootObj = new JSONArray(onTask[0]);
                int i=rootObj.length();

                for(int j=0;j<i;j++)
                {
                    ChatThreadListModel o=new ChatThreadListModel();
                    JSONObject obj = rootObj.getJSONObject(j);
                    o.setThreadName(obj.getString("threadName"));
                    o.setProfileImg(obj.getString("thumbnailUrl"));
                    o.setLastSenderName(obj.getString("lastSenderName"));
                    o.setUnreadCount(obj.getInt("badgeCount"));
                    o.setDate(obj.getString("timeStamp"));
                    o.setThreadId(obj.getString("threadId"));
                    o.setLastSenderId(obj.getString("lastSenderById"));
                    o.setLastMessageId(obj.getString("lastMessageId"));
                    o.setLastMessage(obj.getString("lastMessageText"));
                    o.setInitiateId(obj.getString("initiateId"));
                    o.setInitiateName(obj.getString("initiateName"));
                    o.setParticipentID(obj.getString("participantList"));
                    o.setCustomThreadName(obj.getString("threadCustomName"));
                    temp.add(o);
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

            UpdateThreadList();
        }
        else if (onTask[1].equals("changePass"))
        {
            if (onTask[0].equals("true"))
            {
                Config.alertDialog(ChatThreadListActivity.this,"Success","Password Change Successfully.");
            }
            else if(onTask[0].equals("false"))
            {
                ChangePasswordbyUser();
                Config.alertDialog(ChatThreadListActivity.this, "Failed", "Old Password Invalid.");
            }
            UpdateThreadList();
        }
        else if (onTask[1].equals("LogOut"))
        {
            if (onTask[0].equals("true"))
            {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("Login", "false");
                edit.commit();
                String tokenID=sharedpreferences.getString("TockenID","");
                /*qr.deleteAllData();*/
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    Log.d("TokenID2:", tokenID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent1=new Intent(ChatThreadListActivity.this, LoginActivity.class);
                startActivity(intent1);
                finish();
                //Config.alertDialog(ChatThreadListActivity.this,"Success","Password Change Successfully.");
            }
            else if(onTask[0].equals("false"))
            {
                Config.alertDialog(ChatThreadListActivity.this, "Failed", "Logout Unsuccessful.");
            }
        }
    }

    public void UpdateThreadList()
    {
        if(temp.size()!=0)
        {
            threadList.setAdapter(new ChatThreadListModelAdapter(ChatThreadListActivity.this, temp));
            noData.setVisibility(View.GONE);
            threadList.setVisibility(View.VISIBLE);
        }
        else
        {
            noData.setVisibility(View.VISIBLE);
            threadList.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        done = menu.findItem(R.id.action_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.user_profile:
                Intent intent=new Intent(ChatThreadListActivity.this, UserProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.user_logout:

                if (Config.isConnectingToInternet(this))
                {
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
                    String tokenID=sharedpreferences.getString("TockenID","");
                    Log.d("TokenID:", tokenID);
                    LogoutUserAsyncTaskPost logout=new LogoutUserAsyncTaskPost(this,LoginId,tokenID,this);
                    logout.execute();
                }
                else
                {
                    Config.alertDialog(this,"Network Error","No Internet Connection..!");
                }

                return true;
            case R.id.user_change_pass:
                ChangePasswordbyUser();
                return true;
            case R.id.about_us:
                Intent i=new Intent(ChatThreadListActivity.this, AboutMyChat.class);
                startActivity(i);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void ChangePasswordbyUser()
    {
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.change_password_user, null);
        final Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        final Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText oldpass= (EditText) dialoglayout.findViewById(R.id.edt_change_oldpass);
        final EditText newpass= (EditText) dialoglayout.findViewById(R.id.edt_change_newpass);
        final EditText confnewpass= (EditText) dialoglayout.findViewById(R.id.edt_change_confmnewpass);
        submit.setTypeface(face);
        cancel.setTypeface(face);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChatThreadListActivity.this);
        builder.setView(dialoglayout);

        final AlertDialog alertDialog = builder.create();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldpass.getText().toString().equals(""))
                {
                    Config.alertDialog(ChatThreadListActivity.this,"Error","Please Enter Old Password");
                }
                else if (oldpass.getText().length()<6)
                {
                    Config.alertDialog(ChatThreadListActivity.this,"Error","Please Password should be 6 characters or digits");
                }
                else if (newpass.getText().toString().equals(""))
                {
                    Config.alertDialog(ChatThreadListActivity.this,"Error","Please Enter New Password");
                }
                else if (newpass.getText().length()<6)
                {
                    Config.alertDialog(ChatThreadListActivity.this,"Error","Please Password should be 6 characters or digits");
                }
                else if (confnewpass.getText().toString().equals(""))
                {
                    Config.alertDialog(ChatThreadListActivity.this,"Error","Please Enter Confirm Password");
                }
                else if (confnewpass.getText().length()<6)
                {
                    Config.alertDialog(ChatThreadListActivity.this,"Error","Please Password should be 6 characters or digits");
                }
                else if (!confnewpass.getText().toString().equals(newpass.getText().toString()))
                {
                    Config.alertDialog(ChatThreadListActivity.this,"Error","Password does not match!");
                }
                else
                {
                    if (Config.isConnectingToInternet(ChatThreadListActivity.this))
                    {
                        loading.setVisibility(View.VISIBLE);
                        ChangePasswordAsyncTaskPost changepassword=new ChangePasswordAsyncTaskPost(ChatThreadListActivity.this,LoginId,oldpass.getText().toString(),newpass.getText().toString(),ChatThreadListActivity.this);
                        changepassword.execute();
                        alertDialog.dismiss();
                    }
                    else
                    {
                        Config.alertDialog(ChatThreadListActivity.this,"Network Error","No Internet Connection..");
                    }
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

    public void GetThreadLIST()
    {
        if (Config.isConnectingToInternet(ChatThreadListActivity.this))
        {
            loading.setVisibility(View.VISIBLE);
            ThreadListAsyncTaskGet getthread=new ThreadListAsyncTaskGet(ChatThreadListActivity.this,ChatThreadListActivity.this,LoginId);
            getthread.execute();
        }
        else
        {
            Config.alertDialog(ChatThreadListActivity.this,"Network Error","No Internet Connection..!");
        }

    }

    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            if(update.equals("RecieveMessage")) {

            }

            else if(update.equals("RefreshThreadList")) {
                temp.clear();
                GetThreadLIST();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //GetThreadLIST();
        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);


    }

    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 100){
                ChatThreadListActivity.this.runOnUiThread(new UpdateUI("RecieveMessage"));
            }
            if(resultCode == 200){
                ChatThreadListActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }


}
