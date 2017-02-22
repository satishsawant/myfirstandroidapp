package realizer.com.chat;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Win on 13/12/2016.
 */
public class AboutMyChat extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_snooz_chat_activity);
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/girolight001.otf");

        TextView title= (TextView) findViewById(R.id.app_title);
        title.setTypeface(face);
    }
}
