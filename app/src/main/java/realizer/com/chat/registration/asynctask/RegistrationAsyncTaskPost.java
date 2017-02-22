package realizer.com.chat.registration.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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


/**
 * Created by Win on 07/09/2016.
 */
public class RegistrationAsyncTaskPost extends AsyncTask<Void,Void,StringBuilder> {

    StringBuilder resultbuilder;
    RegistrationModel rgm;
    ProgressDialog dialog;
    Context mycontext;
    private OnTaskCompleted callback;

    public RegistrationAsyncTaskPost(RegistrationModel rgm, Context mycontext, OnTaskCompleted cb) {
        this.rgm = rgm;
        this.mycontext=mycontext;
        this.callback=cb;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
       // dialog=ProgressDialog.show(mycontext,"","Inserting Data...!");
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        resultbuilder =new StringBuilder();
        HttpClient httpClient=new DefaultHttpClient();
        String url= Config.URL+"registerUser";
        HttpPost httpPost=new HttpPost(url);
        String json="";
        StringEntity se=null;
        JSONObject jsonObject=new JSONObject();
        try
        {
            jsonObject.put("firstName",rgm.getFname());
            jsonObject.put("lastName",rgm.getLname());
            jsonObject.put("emailId",rgm.getEmailid());
            jsonObject.put("phoneNo",rgm.getContactNo());
            jsonObject.put("loginId",rgm.getUserId());
            jsonObject.put("password",rgm.getPassword());
            jsonObject.put("magicWord", rgm.getMagicWord());
            jsonObject.put("gender", rgm.getGender());
            jsonObject.put("thumbnailUrl",rgm.getThumbnilUrl());
            String resdate = Config.getserverDate(rgm.getDob());
            jsonObject.put("dateofBirth",resdate);

            json=jsonObject.toString();

            se=new StringEntity(json);
            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse=httpClient.execute(httpPost);
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
                resultbuilder.append("@@@Register");
            }
            else if (statuscode==406)
            {
                HttpEntity entity=httpResponse.getEntity();
                InputStream content=entity.getContent();
                BufferedReader reader=new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line=reader.readLine())!=null)
                {
                    resultbuilder.append(line);
                }
                resultbuilder.append("@@@SameUserRegister");
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

        callback.onTaskCompleted(stringBuilder.toString());
    }

}
