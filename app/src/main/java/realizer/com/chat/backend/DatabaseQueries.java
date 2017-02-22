package realizer.com.chat.backend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



import java.util.ArrayList;
import java.util.Date;

import realizer.com.chat.chat.model.ChatUserIdDetailsListModel;
import realizer.com.chat.chat.model.ChatThreadListModel;
import realizer.com.chat.chat.model.ChatMessageSendModel;

/**
 * Created by Win on 12/21/2015.
 */
public class DatabaseQueries
{
    SQLiteDatabase db;
    Context context;
    String UserD[];
    String scode;

    public DatabaseQueries(Context context) {
        this.context = context;
        SQLiteOpenHelper myHelper = SqliteHelper.getInstance(context);
        this.db = myHelper.getWritableDatabase();

       /* DALMyPupilInfo dal = new DALMyPupilInfo(context);
        UserD= dal.GetSTDDIVData();
        this.scode=UserD[2];*/
    }

    // Insert Userlist
    public long insertUserLIst(String userid,String fname,String lname,String thumbnailUrl)
    {
        ContentValues conV = new ContentValues();
        conV.put("userId", userid);
        conV.put("firstName", fname);
        conV.put("lastName", lname);
        conV.put("thumbnailUrl", thumbnailUrl);
        long newRowInserted = db.insert("UserList", null, conV);
        return newRowInserted;
    }

    // Insert UserInfo
    public long insertUserInfo(String userid,String fullname,String initial,String thumbnailUrl)
    {
        ContentValues conV = new ContentValues();
        conV.put("userId", userid);
        conV.put("firstFullName", fullname);
        conV.put("userInitial", initial);
        conV.put("thumbnailUrl", thumbnailUrl);
        long newRowInserted = db.insert("UserInfo", null, conV);
        return newRowInserted;
    }

    // Check User in List Details
    public boolean ChekUserInList(String userId)
    {
        boolean chek=false;
        Cursor c = db.rawQuery("SELECT * FROM UserList", null);
        String listUserid="";
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    listUserid = c.getString(c.getColumnIndex("userId"));
                    if (listUserid.equals(userId))
                    {
                        chek=true;
                    }
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return chek;
    }



    public ArrayList<ChatUserIdDetailsListModel> GetQueryTableData() {
        Cursor c = db.rawQuery("SELECT * FROM UserList", null);
        ArrayList<ChatUserIdDetailsListModel> result = new ArrayList<>();
        Log.d("LENGTH CURSOR", "" + c.getCount());
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    ChatUserIdDetailsListModel o = new ChatUserIdDetailsListModel();
                    o.setFname(c.getString(c.getColumnIndex("firstName")));
                    o.setUserid(c.getString(c.getColumnIndex("userId")));
                    o.setLname(c.getString(c.getColumnIndex("lastName")));
                    o.setThumbnail(c.getString(c.getColumnIndex("thumbnailUrl")));
                    result.add(o);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return result;
    }

    //Insert Query Information
    public long insertQuery(String fromUserid,String fromName,String sendName,String senduserId,String text,String dtime,String flag,Date sentDate)
    {
        ContentValues conV = new ContentValues();
        conV.put("fromUserId", fromUserid);
        conV.put("fromName", fromName);
        conV.put("sendName", sendName);
        conV.put("sendUserId",senduserId);
        conV.put("msg", text);
        conV.put("sentTime", dtime);
        conV.put("HasSyncedUp", flag);
        conV.put("sentDate", sentDate.getTime());
        long newRowInserted = db.insert("Query", null, conV);
        return newRowInserted;
    }

    // get ID
    public int getQueryId() {
        Cursor c = db.rawQuery("SELECT QueryId FROM Query ", null);
        int cnt = 1;
        int att=0;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {

                    att = c.getInt(0);
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return att;
    }


    //Insert Queue Infromation
    public long insertQueue(int id,String type,String priority,String time) {
        ContentValues conV = new ContentValues();
        conV.put("Id", id);
        conV.put("Type", type);
        conV.put("SyncPriority", priority);
        conV.put("Time", time);
        long newRowInserted = db.insert("SyncUPQueue", null, conV);
        return newRowInserted;
    }


    //select Query
    public ChatMessageSendModel GetQuery(int id) {
        Cursor c = db.rawQuery("SELECT * FROM Query WHERE QueryId="+id, null);
        ChatMessageSendModel o = new ChatMessageSendModel();
        int cnt = 1;
        if (c != null) {
            if (c.moveToFirst()) {
                System.out.print("while moving  - C != null");
                do {
                    o.setConversationId(c.getInt(c.getColumnIndex("QueryId")));
                    o.setFromName(c.getString(c.getColumnIndex("fromName")));
                    o.setSentTime(c.getString(c.getColumnIndex("sentTime")));
                    o.setFromUserId(c.getString(c.getColumnIndex("fromUserId")));
                    o.setSendtoName(c.getString(c.getColumnIndex("sendName")));
                    o.setSendtouserId(c.getString(c.getColumnIndex("sendUserId")));
                    o.setText(c.getString(c.getColumnIndex("msg")));
                    o.setHassync(c.getString(c.getColumnIndex("HasSyncedUp")));
                    cnt = cnt+1;
                }
                while (c.moveToNext());
            }
        }
        c.close();
        return o;
    }

    public void deleteAllData()
    {
        long deleterow =0;
        deleterow = db.delete("UserList",null, null);
        deleterow = db.delete("UserInfo",null, null);
        deleterow = db.delete("InitiatedChat",null, null);
        deleterow = db.delete("Query",null, null);
        deleterow = db.delete("SyncUPQueue",null, null);
    }
}
