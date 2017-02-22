package realizer.com.chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.chat.utils.Config;
import realizer.com.chat.utils.OnTaskCompleted;


public class LoginAsyncTaskPost extends AsyncTask<Void, Void,StringBuilder>
{
    ProgressDialog dialog;
    StringBuilder resultLogin;
    String uName, password;
    String deviceID;
    Context myContext;
    StringBuilder resultbuilder;
    private OnTaskCompleted callback;

    public LoginAsyncTaskPost(String uName, String password, String deviceID, Context myContext, OnTaskCompleted cb)
    {
        this.uName = uName;
        this.password = password;
        this.myContext = myContext;
        this.callback = cb;
        this.deviceID = deviceID;
    }

    @Override
    protected void onPreExecute() {
       // super.onPreExecute();
     // dialog= ProgressDialog.show(myContext, "", "Authenticating credentials...");

    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        resultLogin = new StringBuilder();
        resultbuilder =new StringBuilder();
        String my= Config.URL+"login";
       // String my= Config.URL+"StudentLogin/"+ uName + "/" +password;
        Log.d("URL", my);
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost=new HttpPost(my);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            jsonObject.put("LoginId",uName);
            jsonObject.put("Password",password);
            jsonObject.put("DeviceId",deviceID);

            json=jsonObject.toString();

            se=new StringEntity(json);
            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse=client.execute(httpPost);
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
      //  dialog.dismiss();
        //Pass here result of async task
         stringBuilder.append("@@@LoginIN");
         callback.onTaskCompleted(stringBuilder.toString());
    }

}
