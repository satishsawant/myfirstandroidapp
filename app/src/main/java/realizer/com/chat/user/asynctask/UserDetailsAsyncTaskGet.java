package realizer.com.chat.user.asynctask;

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
 * Created by Win on 08/12/2016.
 */
public class UserDetailsAsyncTaskGet extends AsyncTask<Void,Void,StringBuilder> {
    StringBuilder result;
    Context myContext;
    String userId;
    private OnTaskCompleted callback;

    public UserDetailsAsyncTaskGet(Context myContext, OnTaskCompleted callback,String userId) {
        this.myContext = myContext;
        this.callback = callback;
        this.userId=userId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected StringBuilder doInBackground(Void... params) {
        result = new StringBuilder();

        String my= Config.URL+"userList?userId="+userId;
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
                    result.append(line);
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
        return result;
    }
    @Override
    protected void onPostExecute(StringBuilder stringBuilder) {
        super.onPostExecute(stringBuilder);

        stringBuilder.append("@@@userDetails");
        callback.onTaskCompleted(stringBuilder.toString());
    }

}
