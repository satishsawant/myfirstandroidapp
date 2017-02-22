package realizer.com.chat.utils;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import java.util.ArrayList;

import realizer.com.chat.chat.model.AddedContactModel;
import realizer.com.chat.view.ProgressWheel;


/**
 * Created by Bhagyashri on 4/2/2016.
 */
public class Singleton {

    public static boolean isShowMap=false;
    public static Intent manualserviceIntent = null;
    public static Intent autoserviceIntent = null;
    private static Singleton _instance;
    public static ResultReceiver resultReceiver;
    public static Context context;
    public static Fragment fragment;
    public static Fragment mainFragment;
    public static ProgressWheel messageCenter = null;
    public static ArrayList<String> imageList = new ArrayList<>();
    public static ArrayList<AddedContactModel> selectedStudentList = new ArrayList<>();
    private Singleton()
    {

    }

    public static Intent getManualserviceIntent() {
        return manualserviceIntent;
    }

    public static void setManualserviceIntent(Intent manualserviceIntent) {
        Singleton.manualserviceIntent = manualserviceIntent;
    }

    public static Intent getAutoserviceIntent() {
        return autoserviceIntent;
    }

    public static void setAutoserviceIntent(Intent autoserviceIntent) {
        Singleton.autoserviceIntent = autoserviceIntent;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        Singleton.context = context;
    }


    public static Singleton getInstance()
    {
        if (_instance == null)
        {
            _instance = new Singleton();
        }
        return _instance;
    }

    public static ResultReceiver getResultReceiver() {
        return resultReceiver;
    }

    public static ProgressWheel getMessageCenter() {
        return messageCenter;
    }

    public static void setMessageCenter(ProgressWheel messageCenter) {
        Singleton.messageCenter = messageCenter;
    }

    public static void setResultReceiver(ResultReceiver resultReceiver) {
        Singleton.resultReceiver = resultReceiver;
    }

    public static Fragment getSelectedFragment() {
        return fragment;
    }

    public static void setSelectedFragment(Fragment fragment) {
        Singleton.fragment = fragment;
    }

    public static Fragment getMainFragment() {
        return mainFragment;
    }

    public static void setMainFragment(Fragment mainFragment) {
        Singleton.mainFragment = mainFragment;
    }

    public static boolean isIsShowMap() {
        return isShowMap;
    }

    public static void setIsShowMap(boolean isShowMap) {
        Singleton.isShowMap = isShowMap;
    }

    public static ArrayList<String> getImageList() {
        return imageList;
    }

    public static void setImageList(ArrayList<String> imageList) {
        Singleton.imageList = imageList;
    }
    public static ArrayList<AddedContactModel> getSelectedStudentList() {
        return selectedStudentList;
    }
    public static ArrayList<AddedContactModel> selectedStudeonBackKeyPress = new ArrayList<>();
    public static void setSelectedStudentList(ArrayList<AddedContactModel> selectedStudentList) {
        Singleton.selectedStudentList = selectedStudentList;
    }
    public static ArrayList<AddedContactModel> getSelectedStudeonBackKeyPress() {
        return selectedStudeonBackKeyPress;
    }

    public static void setSelectedStudeonBackKeyPress(ArrayList<AddedContactModel> selectedStudeonBackKeyPress) {
        Singleton.selectedStudeonBackKeyPress = selectedStudeonBackKeyPress;
    }

    public static boolean isDonclick() {
        return isDonclick;
    }

    public static void setIsDonclick(boolean isDonclick) {
        Singleton.isDonclick = isDonclick;
    }

    public static boolean isDonclick = Boolean.FALSE;

    public static String OpenThread="";

    public static String getOpenThread() {
        return OpenThread;
    }

    public static void setOpenThread(String openThread) {
        OpenThread = openThread;
    }
}

