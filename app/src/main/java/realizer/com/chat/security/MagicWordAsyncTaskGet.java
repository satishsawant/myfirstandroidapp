package realizer.com.chat.security;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.chat.utils.Config;
import realizer.com.chat.utils.OnTaskCompleted;

/**
 * Created by Win on 09/12/2016.
 */
public class MagicWordAsyncTaskGet extends AsyncTask<Void,Void,StringBuilder>
{
    StringBuilder resultLogin;
    Context myContext;
    private OnTaskCompleted callback;
    String MagicWord,LoginId;

    public MagicWordAsyncTaskGet(Context myContext, OnTaskCompleted callback, String magicWord, String loginId) {
        this.myContext = myContext;
        this.callback = callback;
        MagicWord = magicWord;
        LoginId = loginId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        resultLogin = new StringBuilder();

        String my= Config.URLSECURITY+"Security/validateMagicWord/"+LoginId+"/"+MagicWord;
        Log.d("URL", my);
        HttpGet httpGet = new HttpGet(my);
        HttpClient client = new DefaultHttpClient();
        try
        {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();

            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200)
            {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line=reader.readLine()) != null)
                {
                    resultLogin.append(line);
                }
            }
            else
            {
                // Log.e("Error", "Failed to Login");
            }
        }
        catch(ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            client.getConnectionManager().closeExpiredConnections();
            client.getConnectionManager().shutdown();
        }
        return resultLogin;
    }

    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);

        stringBuilder.append("@@@GetMagicWord");
        callback.onTaskCompleted(stringBuilder.toString());
    }

}
