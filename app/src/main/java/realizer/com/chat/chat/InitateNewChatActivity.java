package realizer.com.chat.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import realizer.com.chat.R;
import realizer.com.chat.backend.DatabaseQueries;
import realizer.com.chat.chat.adapter.ChatUserIdDetailsListModelAdapter;
import realizer.com.chat.chat.adapter.ChatUserIdAutoCompleteListAdapter;
import realizer.com.chat.chat.asynctask.ChatUserListAsyncTaskGet;
import realizer.com.chat.chat.asynctask.InitiateThreadAsyncTaskPost;
import realizer.com.chat.chat.model.AddedContactModel;
import realizer.com.chat.chat.model.ChatUserIdDetailsListModel;
import realizer.com.chat.chat.model.ChatMessageSendModel;
import realizer.com.chat.utils.ChatSectionIndexer;
import realizer.com.chat.utils.Config;
import realizer.com.chat.utils.OnTaskCompleted;
import realizer.com.chat.utils.Singleton;
import realizer.com.chat.view.ProgressWheel;

/**
 * Created by Win on 30/11/2016.
 */
public class InitateNewChatActivity extends AppCompatActivity implements OnTaskCompleted
{
    DatabaseQueries qr;
    int num,n;
    ChatUserIdDetailsListModelAdapter adapter;
    ChatUserIdAutoCompleteListAdapter autoCompleteAdapter;
    ListView addedStudent,nameList;
    EditText autocomplteTextView;
    EditText message;
    ImageView selectStudent;
    MenuItem search,done;
    TextView send;
    String UserId,UserLoginId,UserFullName;
    int LisTCount;
    int qid[];
    ProgressWheel loading;
    TextView nodata;
    ArrayList<AddedContactModel> userLIst,selectedList;
    ArrayList<String> sendUserIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiate_new_chat);
        initiateView();
        if (Config.isConnectingToInternet(InitateNewChatActivity.this)) {
            ChatUserListAsyncTaskGet getUserList = new ChatUserListAsyncTaskGet(InitateNewChatActivity.this, InitateNewChatActivity.this);
            getUserList.execute();
        }
        else
        {
            Config.alertDialog(InitateNewChatActivity.this, "Network Error", "No Internet Connection..!");
        }
        userLIst = new ArrayList<>();
        userLIst=GetTeacherList();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(InitateNewChatActivity.this);
        UserId=sharedpreferences.getString("userId","");
        UserLoginId=sharedpreferences.getString("userLoginId","");
        UserFullName=sharedpreferences.getString("userFullName","");
        sendUserIdList=new ArrayList<String>();
        selectedList = new ArrayList<AddedContactModel>();
        qr = new DatabaseQueries(getApplicationContext());
        Singleton.setSelectedStudentList(selectedList);

        Collections.sort(userLIst, new ChatNoCaseComparator());
        autoCompleteAdapter = new ChatUserIdAutoCompleteListAdapter(this,userLIst);
        Config.hideSoftKeyboardWithoutReq(this, autocomplteTextView);
        Config.hideSoftKeyboardWithoutReq(this, message);
        autocomplteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() > 0) {
                    ArrayList<AddedContactModel> listClone = new ArrayList<AddedContactModel>();
                    for (AddedContactModel d : userLIst) {
                        if (d.getFname() != null && d.getFname().toLowerCase().contains(s.toString()))
                            if (d.getFname().toLowerCase(Locale.getDefault()).contains(s.toString()))
                                //something here
                                listClone.add(d);
                    }

                    if (listClone.size() > 0) {
                        nameList.setVisibility(View.VISIBLE);
                        autoCompleteAdapter = new ChatUserIdAutoCompleteListAdapter(InitateNewChatActivity.this, listClone);
                        nameList.setAdapter(autoCompleteAdapter);
                    }
                    if (s.toString() != null)
                        new SelectContactFilter().getFilter().filter(s.toString());

                } else {
                    addedStudent.setVisibility(View.VISIBLE);
                    nameList.setVisibility(View.GONE);
                    // nodata.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        nameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = nameList.getItemAtPosition(position);

                AddedContactModel addedContactModel = (AddedContactModel) o;
                selectedList.add(addedContactModel);
                Singleton.setSelectedStudentList(selectedList);

                adapter = new ChatUserIdDetailsListModelAdapter(InitateNewChatActivity.this, selectedList);
                addedStudent.setAdapter(adapter);
                addedStudent.setVisibility(View.VISIBLE);
                nameList.setVisibility(View.GONE);
                Config.hideSoftKeyboardWithoutReq(InitateNewChatActivity.this, autocomplteTextView);
                autocomplteTextView.setText("");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.hideSoftKeyboardWithoutReq(InitateNewChatActivity.this, message);
                if (addedStudent.getCount() == 0) {
                    Toast.makeText(InitateNewChatActivity.this, "No Student Added", Toast.LENGTH_SHORT).show();
                } else if (message.getText().toString().trim().isEmpty()) {
                    Toast.makeText(InitateNewChatActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                } else {
                    // sendTo = getList();
                    if (selectedList.size() == 0 || message.getText().toString().trim().length() == 0) {

                    } else {
                        loading.setVisibility(View.VISIBLE);
                        Singleton.setMessageCenter(loading);
                        //String uidstud = "";
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
                        String date = df.format(calendar.getTime());
                        Date sendDate = new Date();
                        try {
                            sendDate = df.parse(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // String uidstudarr[] = new String[selectedList.size()];
                        int qidcount = 0;

                        try {

                            qid = new int[selectedList.size()];

                            long n = -1;
                            for (int k = 0; k < selectedList.size(); k++) {

                                n = qr.insertQuery(UserId,UserLoginId, selectedList.get(k).getFname(),selectedList.get(k).getUserId(), message.getText().toString(), date, "true", sendDate);
                                //n = qr.insertQuery("true", uidname, selectedList.get(k).getUserId(), message.getText().toString().trim(), date, "true", sendDate);
                                if (n > 0) {
                                    // Toast.makeText(getActivity(), "Query Inserted Successfully", Toast.LENGTH_SHORT).show();
                                    n = -1;

                                    qid[qidcount] = qr.getQueryId();
                                    n = qr.insertQueue(qid[qidcount], "Query", "2", date);

                                    qidcount = qidcount + 1;

                                }
                            }

                            if (n > 0) {
                                //Toast.makeText(getActivity(), "Queue Inserted Successfully", Toast.LENGTH_SHORT).show();
                                n = -1;
                                if (isConnectingToInternet()) {
                                    for (int i = 0; i < qid.length; i++) {
                                        ChatMessageSendModel obj = qr.GetQuery(qid[i]);
                                        InitiateThreadAsyncTaskPost asyncobj = new InitiateThreadAsyncTaskPost(obj,InitateNewChatActivity.this, InitateNewChatActivity.this);
                                        asyncobj.execute();
                                    }
                                } else
                                {
                                    Config.alertDialog(InitateNewChatActivity.this,"Network Error","No Internet Connection..!");
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        });

    }
    public void addNewThread(View v)
    {
        Intent intent = new Intent(InitateNewChatActivity.this, ChatSectionIndexer.class);
        startActivity(intent);
    }
    private class SelectContactFilter implements Filterable {
        @Override
        public Filter getFilter() {
            return new ListFilter();
        }
    }
    public class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            //string  method replaceAll(" +", " ") will replace the multiple space to single space between two string
            String constraintStr = constraint.toString().trim().replaceAll(" +", " ").toLowerCase(Locale.getDefault());
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {
                List<AddedContactModel> filterItems = new ArrayList<>();
                synchronized (this) {
                    for (AddedContactModel item : userLIst) {
                        if (item.getFname().toLowerCase(Locale.getDefault()).contains(constraintStr)) {
                            filterItems.add(item);
                        }
                    }
                    result.count = filterItems.size();
                    result.values = filterItems;
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<AddedContactModel> filtered = (ArrayList<AddedContactModel>) results.values;
            if (results.values != null) {
                if (filtered.isEmpty()) {
                    nodata.setVisibility(View.VISIBLE);
                    nameList.setVisibility(View.GONE);
                } else {
                    nodata.setVisibility(View.GONE);
                    nameList.setVisibility(View.VISIBLE);
                }

                autoCompleteAdapter = new ChatUserIdAutoCompleteListAdapter(InitateNewChatActivity.this, filtered);
                nameList.setAdapter(autoCompleteAdapter);
            } else {
                nodata.setVisibility(View.VISIBLE);
                nameList.setVisibility(View.GONE);
            }
        }
    }
    public class ChatNoCaseComparator implements Comparator<AddedContactModel> {
        public int compare(AddedContactModel s1, AddedContactModel s2) {
            return s1.getFname().compareToIgnoreCase(s2.getFname());
        }
    }
    @Override
    public void onTaskCompleted(String json) {
        String result="";
        String ontask[]=json.split("@@@");
        if (ontask[1].equals("UserList"))
        {

            JSONArray rootObj = null;
            Log.d("String", json);
            try {

                rootObj = new JSONArray(ontask[0]);
                String userId="",firstName="",lastName="",thumbnailurl="";
                int i=rootObj.length();

                for(int j=0;j<i;j++)
                {
                    JSONObject obj = rootObj.getJSONObject(j);
                    userId = obj.getString("userId");
                    firstName=obj.getString("firstName");
                    lastName=obj.getString("lastName");
                    thumbnailurl=obj.getString("thumbnailUrl");

                    if (userId.equals(UserId))
                    {
                    }
                    else {
                        boolean chk=qr.ChekUserInList(userId);
                        if (!chk)
                        {
                            long n1 = qr.insertUserLIst(userId, firstName,lastName,thumbnailurl);
                            if(n1>=0)
                            {
                                Log.d("UserList", " Done!!!");
                                result="User Inserted";
                            }else {
                                Log.d("UserList", " Not Done!!!");
                                result="User Not Inserted";
                            }
                        }
                    }
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
        else if (ontask[1].equals("QueryError"))
        {
            JSONObject obj = null;
            Log.d("String", json);
            try {
                obj = new JSONObject(ontask[0]);
                String message=obj.getString("Message");
                if (message.equals("An error has occurred."))
                {
                    loading.setVisibility(View.GONE);
                    Config.alertDialog(InitateNewChatActivity.this,"Error","Chat not Initialize..!");
                }
                else
                {

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
        else if (ontask[1].equals("InitThread"))
        {
            finish();
            Intent intent=new Intent(InitateNewChatActivity.this,ChatThreadListActivity.class);
            startActivity(intent);
        }
    }
    private ArrayList<AddedContactModel> GetTeacherList()
    {
        ArrayList<AddedContactModel> results = new ArrayList<>();
        DatabaseQueries qr = new DatabaseQueries(this);
        ArrayList<ChatUserIdDetailsListModel> tnamelst = qr.GetQueryTableData();
        boolean memberPresentInList = false;
        for(int i=0;i<tnamelst.size();i++)
        {
            if (!memberPresentInList) {
                AddedContactModel contactModel = new AddedContactModel();
                ChatUserIdDetailsListModel tDetails = tnamelst.get(i);
                contactModel.setFname(tDetails.getFname());
                contactModel.setUserId(tDetails.getUserid());
                contactModel.setLname(tDetails.getLname());
                contactModel.setProfileimage(tDetails.getThumbnail());
                results.add(contactModel);
            }
        }

        return results;
    }
    public void initiateView()
    {
        selectStudent = (ImageView) findViewById(R.id.imgbtnAddContact);
        autocomplteTextView = (EditText) findViewById(R.id.edt_select_contact);
        send = (TextView) findViewById(R.id.btnSendText);
        message = (EditText)findViewById(R.id.edtmsgtxt);
        addedStudent = (ListView) findViewById(R.id.ivaddedContact);
        nameList = (ListView)findViewById(R.id.lvstudentnamelist);
        nodata=(TextView) findViewById(R.id.noDataFound);
        loading = (ProgressWheel)findViewById(R.id.loading);
    }

    public void open(String s){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(InitateNewChatActivity.this);
        alertDialogBuilder.setTitle(s);
        final String temp = s;
        alertDialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                removStud(temp);
                LisTCount = 1;
                ArrayList<String> stud = getList();

                ArrayAdapter adapter = new ArrayAdapter(InitateNewChatActivity.this, android.R.layout.simple_list_item_1, stud);
                addedStudent.setAdapter(adapter);
                addedStudent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String temp = String.valueOf(addedStudent.getItemAtPosition(position));
                        open(temp);
                    }
                });

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public ArrayList<String> getList()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InitateNewChatActivity.this);
        Set<String> set = preferences.getStringSet("NameList", null);
        Set<String> set1 = preferences.getStringSet("SetList", null);
        Set<String> set2 = preferences.getStringSet("SearchList", null);
        ArrayList<String> result = new ArrayList<String>();

        if (set == null) {

        } else {
            ArrayList<String> sample = new ArrayList<String>(set);
            result.addAll(sample);
        }

        if (set1 == null) {

        } else {
            ArrayList<String> sample1 = new ArrayList<String>(set1);
            result.addAll(sample1);
        }

        if (set2 == null) {

        } else {
            ArrayList<String> sample2 = new ArrayList<String>(set2);
            result.addAll(sample2);
        }
        return result;
    }

    public void removStud(String s)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InitateNewChatActivity.this);
        Set<String> setall  = preferences.getStringSet("NameList", null);
        Set<String> setset = preferences.getStringSet("SetList", null);
        Set<String> setsearch = preferences.getStringSet("SearchList", null);
        SharedPreferences.Editor editor = preferences.edit();
        if(setall!=null && !setall.isEmpty()) {
            for (String temp : setall)
                if (s.equals(temp)) {
                    setall.remove(temp);
                    editor.putStringSet("NameList", setall);
                    break;
                }
        }

        if(setsearch!=null && !setsearch.isEmpty()) {
            for (String temp : setsearch)
                if (s.equals(temp)) {
                    setsearch.remove(temp);
                    editor.putStringSet("SearchList", setsearch);
                    break;
                }
        }
        if(setset!=null && !setset.isEmpty()) {
            for (String temp : setset)
                if (s.equals(temp)) {
                    setset.remove(temp);
                    editor.putStringSet("SetList", setset);
                    break;
                }
        }
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Singleton.isDonclick())
        {
            Singleton.setIsDonclick(Boolean.FALSE);
            if(Singleton.getSelectedStudentList() != null ) {
                selectedList = Singleton.getSelectedStudentList();
                adapter = new ChatUserIdDetailsListModelAdapter(this, selectedList);
                if(Singleton.getSelectedStudentList().size()>0)
                    addedStudent.setAdapter(adapter);
                else
                    addedStudent.setAdapter(null);
                addedStudent.setVisibility(View.VISIBLE);
                nameList.setVisibility(View.GONE);
                nodata.setVisibility(View.GONE);
            }
            else
            {
                nodata.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            selectedList = Singleton.getSelectedStudentList();
        }

        if(done != null)
            done.setVisible(false);
        if(search != null)
            search.setVisible(false);


        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
}
