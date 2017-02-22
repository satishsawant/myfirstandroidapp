package realizer.com.chat.backend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shree on 12/30/2015.
 */
public class SqliteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Chating";
    private static final int DATABASE_VERSION =1;
    SQLiteDatabase db;
    static Context mycontext;
    private static SqliteHelper mInstance = null;
    private static final String UserList="CREATE TABLE UserList(userId TEXT,firstName TEXT,lastName TEXT,thumbnailUrl TEXT)";
    private static final String ChatUserInfo="CREATE TABLE UserInfo(userId TEXT,firstFullName TEXT,userInitial TEXT,thumbnailUrl TEXT)";
    private static final String InitiatedChat ="CREATE TABLE InitiatedChat(Id INTEGER,Initiated TEXT,threadName TEXT,lastSenderName TEXT,timeStamp TEXT,threadID TEXT,lastSenderByID TEXT,lastMessageId TEXT,lastMessageText TEXT,initiateId TEXT,initiateName TEXT,UnreadCount INTEGER,ThumbnailURL TEXT)";

    private static final String Query ="CREATE TABLE Query(QueryId INTEGER PRIMARY KEY   AUTOINCREMENT,fromUserId TEXT,fromName TEXT,sendName TEXT,sendUserId TEXT,msg TEXT,messageID Text,sentTime TEXT,sentDate INTEGER,HasSyncedUp TEXT)";
    private static final String SyncUPQueue ="CREATE TABLE SyncUPQueue(Id INTEGER,Type TEXT,SyncPriority TEXT,Time TEXT)";



    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mycontext = context;
    }

    public static SqliteHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new SqliteHelper(ctx.getApplicationContext());
        }
        mycontext = ctx;
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UserList);
        db.execSQL(ChatUserInfo);
        db.execSQL(InitiatedChat);
        db.execSQL(Query);
        db.execSQL(SyncUPQueue);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE if exists " + "UserList");
        db.execSQL("DROP TABLE if exists " + "UserInfo");
        db.execSQL("DROP TABLE if exists " + "InitiatedChat");
        db.execSQL("DROP TABLE if exists " + "Query");
        db.execSQL("DROP TABLE if exists " + "SyncUPQueue");
        onCreate(db);
    }
}