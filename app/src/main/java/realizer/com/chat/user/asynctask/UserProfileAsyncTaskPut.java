package realizer.com.chat.user.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.chat.registration.model.RegistrationModel;
import realizer.com.chat.utils.Config;
import realizer.com.chat.utils.OnTaskCompleted;
import realizer.com.chat.utils.Utility;

/**
 * Created by Win on 08/12/2016.
 */
public class UserProfileAsyncTaskPut extends AsyncTask<Void,Void,StringBuilder> {
    StringBuilder resultbuilder;
    String userID;
    String thumbnail;
    Context mycontext;
    private OnTaskCompleted callback;

    public UserProfileAsyncTaskPut(String userID, String thumbnail, Context mycontext, OnTaskCompleted callback) {
        this.userID = userID;
        this.thumbnail = thumbnail;
        this.mycontext = mycontext;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        resultbuilder =new StringBuilder();
        HttpClient httpClient=new DefaultHttpClient();
        String url= Config.URL+"udpdateThumbanil";
        HttpPut httpPut=new HttpPut(url);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            jsonObject.put("userId",userID);
            String newThumbnail= Utility.getURLImage(thumbnail);
            jsonObject.put("thumbanil", thumbnail.replaceAll("\n",""));
            json=jsonObject.toString();

            se=new StringEntity(json);
            httpPut.setEntity(se);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");

            HttpResponse httpResponse=httpClient.execute(httpPut);
            StatusLine statusLine=httpResponse.getStatusLine();
            int statuscode=statusLine.getStatusCode();
            if (statuscode==200)
            {
                HttpEntity entity=httpResponse.getEntity();
                InputStream content=entity.getContent();
                BufferedReader reader=new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line=reader.readLine())!=null)
                {
                    resultbuilder.append(line);
                }
            }
            else
            {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return resultbuilder;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);
        stringBuilder.append("@@@ProfilePic");
        callback.onTaskCompleted(stringBuilder.toString());
    }
}
