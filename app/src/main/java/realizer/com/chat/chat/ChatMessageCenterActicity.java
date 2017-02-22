package realizer.com.chat.chat;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import realizer.com.chat.R;
import realizer.com.chat.backend.DatabaseQueries;
import realizer.com.chat.chat.adapter.ChatMessageCenterListAdapter;
import realizer.com.chat.chat.asynctask.GetThreadMessageAsyncTaskPost;
import realizer.com.chat.chat.asynctask.SendMessgeAsyncTaskPost;
import realizer.com.chat.chat.model.ChatMessageViewListModel;
import realizer.com.chat.emoji.EmojiconEditText;
import realizer.com.chat.emoji.EmojiconGridView;
import realizer.com.chat.emoji.EmojiconsPopup;
import realizer.com.chat.emoji.emoji.Emojicon;
import realizer.com.chat.utils.Config;

import realizer.com.chat.utils.OnTaskCompleted;
import realizer.com.chat.utils.Singleton;
import realizer.com.chat.view.ProgressWheel;

/**
 * Created by Win on 30/11/2016.
 */
public class ChatMessageCenterActicity extends AppCompatActivity implements AbsListView.OnScrollListener,OnTaskCompleted
{

    TextView send;
    ProgressWheel loading;

    int mCurrentX ;
    int  mCurrentY;
    int currentPosition;
    int lstsize,num;
    MessageResultReceiver resultReceiver;
    String ThreadID,ReceiverID,UserID,InitiateID,SendtoMSGID,UserFullName,SenderThumbnail;
    ListView lsttname;

    ArrayList<ChatMessageViewListModel> chatMessages=new ArrayList<>();
    ChatMessageCenterListAdapter adapter;

    EditText msg;
    int qid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_center);
        send= (TextView) findViewById(R.id.btnSendText);
        lsttname = (ListView) findViewById(R.id.lstviewquery);
        msg = (EditText) findViewById(R.id.edtmsgtxt);
        loading = (ProgressWheel) findViewById(R.id.loading);

        // emojies
        final EmojiconEditText emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        final View rootView = findViewById(R.id.root_view);
        final ImageView emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        final ImageView submitButton = (ImageView) findViewById(R.id.submit_btn);


        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiconEditText == null || emojicon == null) {
                    return;
                }

                int start = emojiconEditText.getSelectionStart();
                int end = emojiconEditText.getSelectionEnd();
                if (start < 0) {
                    emojiconEditText.append(emojicon.getEmoji());
                } else {
                    emojiconEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiconEditText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if(!popup.isShowing()){

                    //If keyboard is visible, simply show the emoji popup
                    if(popup.isKeyBoardOpen()){
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else{
                        emojiconEditText.setFocusableInTouchMode(true);
                        emojiconEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else{
                    popup.dismiss();
                }
            }
        });

        qid=0;
        lstsize = chatMessages.size();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatMessageCenterActicity.this);
        UserID = sharedpreferences.getString("userId", "");
        UserFullName = sharedpreferences.getString("userFullName", "");
        SenderThumbnail = sharedpreferences.getString("userThumbnail", "");


        lsttname.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String CLIPBOARD_TEXT="";
                CLIPBOARD_TEXT=chatMessages.get(position).getMessage();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(CLIPBOARD_TEXT, CLIPBOARD_TEXT);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ChatMessageCenterActicity.this, "Message Copied", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        ThreadID = b.getString("THREADID");
        ReceiverID = b.getString("RECEIVERID");
        InitiateID=b.getString("InitiatedID");

        getSupportActionBar().setTitle(b.getString("ActionBarTitle"));
        getSupportActionBar().setIcon(R.drawable.chat_icon);


        if (UserID.equals(ReceiverID))
        {
            SendtoMSGID=InitiateID;
        }
        else if (UserID.equals(InitiateID))
        {
            SendtoMSGID=ReceiverID;
        }

        loading.setVisibility(View.VISIBLE);

        GetThreadMessages();

        NotificationManager notifManager= (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();

//On submit, add the edittext text to listview and clear the edittext
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String toServer = emojiconEditText.getText().toString();
                String toServerUnicodeEncoded = StringEscapeUtils.escapeJava(toServer);


                if(toServer.length()!=0)
                {
                    loading.setVisibility(View.VISIBLE);

                    emojiconEditText.setEnabled(false);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
                    String date = df.format(calendar.getTime());
                    /*Date sendDate =  new Date();
                    try {
                        sendDate = df.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }*/
                    if(isConnectingToInternet()) {
                        SendMessgeAsyncTaskPost sendMsg=new SendMessgeAsyncTaskPost(ChatMessageCenterActicity.this,ThreadID,UserID,date.toString(),toServerUnicodeEncoded,SendtoMSGID,ChatMessageCenterActicity.this);
                        sendMsg.execute();
                        ChatMessageViewListModel o=new ChatMessageViewListModel();
                        String[] datenew=date.split(" ");
                        String newdate=datenew[0];
                        String[] senddate=newdate.split("/");
                        o.setSendDate(senddate[2]+"-"+senddate[1]+"-"+senddate[0]);
                        o.setSendTime(GetMSGTime(date));
                        o.setMessage(toServer.toString());
                        o.setThreadId(ThreadID);
                        o.setSenderName(UserFullName);
                        o.setSenderThumbnail(SenderThumbnail);
                        chatMessages.add(o);
                        emojiconEditText.getText().clear();
                    }
                    else
                    {
                    }
                }
            }
        });

        /*send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(msg.getText().length()!=0)
                {
                    loading.setVisibility(View.VISIBLE);

                    msg.setEnabled(false);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
                    String date = df.format(calendar.getTime());
                    *//*Date sendDate =  new Date();
                    try {
                        sendDate = df.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }*//*
                    if(isConnectingToInternet()) {
                        SendMessgeAsyncTaskPost sendMsg=new SendMessgeAsyncTaskPost(ChatMessageCenterActicity.this,ThreadID,UserID,date.toString(),msg.getText().toString(),SendtoMSGID,ChatMessageCenterActicity.this);
                        sendMsg.execute();
                        ChatMessageViewListModel o=new ChatMessageViewListModel();
                        String[] datenew=date.split(" ");
                        String newdate=datenew[0];
                        String[] senddate=newdate.split("/");
                        o.setSendDate(senddate[2]+"-"+senddate[1]+"-"+senddate[0]);
                        o.setSendTime(GetMSGTime(date));
                        o.setMessage(msg.getText().toString());
                        o.setThreadId(ThreadID);
                        o.setSenderName(UserFullName);
                        o.setSenderThumbnail(SenderThumbnail);
                        chatMessages.add(o);
                        msg.setText("");
                    }
                    else
                    {
                    }
                }
            }
        });*/
    }
    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }
    public boolean isConnectingToInternet(){

        ConnectivityManager connectivity =
                (ConnectivityManager) this.getSystemService(
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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mCurrentX = view.getScrollX();
        mCurrentY = view.getScrollY();
        currentPosition = lsttname.getSelectedItemPosition();
        Log.d("Position", "" + currentPosition);
    }

    public void GetThreadMessages()
    {
        if (Config.isConnectingToInternet(ChatMessageCenterActicity.this)) {
            GetThreadMessageAsyncTaskPost getMessage = new GetThreadMessageAsyncTaskPost(ChatMessageCenterActicity.this, ThreadID, ChatMessageCenterActicity.this);
            getMessage.execute();
        }
        else
        {
            Config.alertDialog(ChatMessageCenterActicity.this,"Network Error","No Internet Connection..!");
        }
    }
    @Override
    public void onTaskCompleted(String s)
    {
        //msg.setEnabled(true);
        String tp;
        String[] onTask=s.split("@@@");
        if (onTask[1].equals("MessageList"))
        {
            loading.setVisibility(View.GONE);

            JSONArray rootObj = null;
            Log.d("String", onTask[0]);
            try {

                rootObj = new JSONArray(onTask[0]);
                int i=rootObj.length();

                for(int j=0;j<i;j++)
                {
                    ChatMessageViewListModel o=new ChatMessageViewListModel();
                    JSONObject obj = rootObj.getJSONObject(j);

                    o.setMessageId(obj.getString("messageId"));
                    o.setSenderId(obj.getString("senderId"));
                    //o.setTimeStamp(obj.getString("timeStamp"));
                    String datet[] = obj.getString("timeStamp").split("T");
                    o.setSendDate(datet[0]);
                    String time[] = datet[1].split(":");
                    int t1 = Integer.valueOf(time[0]);
                    if (t1==12)
                    {
                        tp = "PM";
                        o.setSendTime("" + t1 + ":" + time[1] + " " + tp);
                    } else if (t1 > 12) {
                        int t2 = t1-12;
                        tp = "PM";
                        o.setSendTime("" + t2 + ":" + time[1] + " " + tp);
                    }
                    else
                    {
                        tp = "AM";
                        o.setSendTime(time[0] + ":" + time[1] + " " + tp);
                    }

                    String serverResponse = obj.getString("message");
                    String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
                    o.setMessage(fromServerUnicodeDecoded);
                    o.setThreadId(obj.getString("threadId"));
                    o.setReceiverId(obj.getString("receiverId"));
                    o.setSenderName(obj.getString("SenderName"));
                    o.setSenderThumbnail(obj.getString("senderThumbnail"));

                    chatMessages.add(o);
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
        else if (onTask[1].equals("SendMsg"))
        {
            loading.setVisibility(View.GONE);
            if (onTask[0].equals(""))
            {}
            else {

                JSONObject rootObj = null;
                try {
                    rootObj = new JSONObject(onTask[0]);
                    String Success = rootObj.getString("Success");
                    if (Success.equals("true")) {
                   /* chatMessages.clear();
                    GetThreadMessageAsyncTaskPost getMessage=new GetThreadMessageAsyncTaskPost(ChatMessageCenterActicity.this,ThreadID,ChatMessageCenterActicity.this);
                    getMessage.execute();*/
                    } else {
                        Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Message Not Sent");
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
        }

        DisplayMessagesinList();

    }

    public void DisplayMessagesinList()
    {
        if(chatMessages.size()!=0)
        {
            adapter = new ChatMessageCenterListAdapter(this,chatMessages);
            lsttname.setAdapter(adapter);

            lsttname.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            //lsttname.setFastScrollEnabled(true);
            //lsttname.setScrollY(lsttname.getCount());
            lsttname.setSelection(lsttname.getCount() - 1);
            //lsttname.smoothScrollToPosition(lsttname.getCount());
            lsttname.setOnScrollListener(this);
            lstsize =  chatMessages.size();
        }
        else
        {
            //noData.setVisibility(View.VISIBLE);
            //lsttname.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("ActiveThread", "");
        edit.commit();
        finish();
    }

    class UpdateUI implements Runnable {
        String update;
        Bundle b;

        public UpdateUI(String update,Bundle data) {

            this.update = update;
            b=data;
        }

        public void run() {

            if(update.equals("RecieveMessage")) {

                String tp;
                String url=b.getString("ReceiverURL");
                String time1=b.getString("ReceiveTime");

                ChatMessageViewListModel o=new ChatMessageViewListModel();
                String datet[] = time1.split("T");
                o.setSendDate(datet[0]);
                String time[] = datet[1].split(":");
                int t1 = Integer.valueOf(time[0]);
                if (t1==12)
                {
                    tp = "PM";
                    o.setSendTime("" + t1 + ":" + time[1] + " " + tp);
                } else if (t1 > 12) {
                    int t2 = t1-12;
                    tp = "PM";
                    o.setSendTime("" + t2 + ":" + time[1] + " " + tp);
                }
                else
                {
                    tp = "AM";
                    o.setSendTime(time[0] + ":" + time[1] + " " + tp);
                }
                o.setMessage(b.getString("ReceiveMSG"));
                o.setThreadId(ThreadID);
                o.setSenderName(b.getString("ReceiverNAME"));
                o.setSenderThumbnail(url);
                chatMessages.add(o);
                DisplayMessagesinList();
                //GetThreadMessages();
            }

            else if(update.equals("SendMessageMessage")) {

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("ActiveThread", "");
                edit.commit();
                finish();
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //Recive the result when new Message Arrives
    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 100){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("RecieveMessage",resultData));
            }
            if(resultCode == 200){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("SendMessageMessage",resultData));
            }

        }
    }
    public String GetMSGTime(String time1)
    {
        String returntime;
        String tp;
        String datet[] = time1.split(" ");

        String time[] = datet[1].split(":");
        int t1 = Integer.valueOf(time[0]);
        if (t1==12)
        {
            tp = "PM";
            returntime=("" + t1 + ":" + time[1] + " " + tp);
        } else if (t1 > 12) {
            int t2 = t1-12;
            tp = "PM";
            returntime=("" + t2 + ":" + time[1] + " " + tp);
        }
        else
        {
            tp = "AM";
            returntime=(time[0] + ":" + time[1] + " " + tp);
        }
        return returntime;
    }
}
