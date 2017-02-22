package realizer.com.chat.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

import realizer.com.chat.R;
import realizer.com.chat.utils.GetImages;
import realizer.com.chat.utils.ImageStorage;
import realizer.com.chat.utils.Utility;

/**
 * Created by Win on 10/12/2016.
 */
public class FullImageViewActivity extends Activity
{
    ImageView imageView;

    boolean isImageFitToScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fullimageview_activity);

        imageView = (ImageView) findViewById(R.id.user_fullimageView);
        Bundle b=getIntent().getExtras();
        String userUrl=b.getString("ImgURLUSER");
        if (userUrl.equals("")||userUrl.equals(null))
        {
            imageView.setImageResource(R.drawable.user_icon);
        }
        else
        {
            SetThumbnail(userUrl);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (isImageFitToScreen) {
                    isImageFitToScreen = false;
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    imageView.setAdjustViewBounds(true);
                } else {
                    isImageFitToScreen = true;
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }*/
            }
        });
    }
    public void SetThumbnail(String ThumbnailUrl)
    {
        if (ThumbnailUrl.equals("")||ThumbnailUrl.equals("null")||ThumbnailUrl.equals(null))
        {

        }
        else{
            String newURL= Utility.getURLImage(ThumbnailUrl);
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,imageView,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length-1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
