/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.multires;


import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public final class MultiRes extends Activity {

    private int mCurrentPhotoIndex = 0;
 //   private int[] mPhotoIds = new int[] { R.drawable.sample_0,
  //          R.drawable.sample_1, R.drawable.sample_2, R.drawable.sample_3,
  //          R.drawable.sample_4, R.drawable.sample_5, R.drawable.sample_6,
  //          R.drawable.sample_7 };
    
	private final String[] mPhotoIds = {
			"http://www.sillarsfamily.com/England2004/cwdata/Image_0010.jpg",
			"http://www.sillarsfamily.com/England2004/cwdata/Image_0022.jpg",
			"http://www.sillarsfamily.com/England2004/cwdata/Image_0033.jpg",
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0043.jpg",
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0069.jpg",
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0074.jpg",
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0077a.jpg", 
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0092.jpg",
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0102.jpg", 
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0134.jpg",
	        };
    

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        showPhoto(mCurrentPhotoIndex);

        // Handle clicks on the 'Next' button.
        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCurrentPhotoIndex = (mCurrentPhotoIndex + 1)
                        % mPhotoIds.length;
                showPhoto(mCurrentPhotoIndex);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("photo_index", mCurrentPhotoIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mCurrentPhotoIndex = savedInstanceState.getInt("photo_index");
        showPhoto(mCurrentPhotoIndex);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void showPhoto(int photoIndex) {
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        
        String closeconnection = "no";
        
        if (photoIndex%2==0){
        	//even number
        	//close connection right away
        	closeconnection = "yes";
        }
        
        final Bitmap bmp = getBitmap(closeconnection, mPhotoIds[photoIndex]);
        imageView.setImageBitmap(bmp);
      //  imageView.setImageResource(mPhotoIds[photoIndex]);

        TextView statusText = (TextView) findViewById(R.id.status_text);
        statusText.setText(String.format("%d/%d", photoIndex + 1,
                mPhotoIds.length));
        if (closeconnection == "yes"){
        	statusText.append(" connection closed");
        }
        else{
        	statusText.append(" connection not closed");
        }
        
    }

	Bitmap getBitmap(String closeconnection, String url) {
		//we know the cache situation of the file, so use that to determine how to display the file
		 
		 
		 
		 //pulling out all the caching logiic here.
		//samople app so makes no sense to have
				try {
					Bitmap bitmap;
					if (closeconnection == "yes"){
						URL urln = new URL(url);
						HttpURLConnection getimagecloseconn = (HttpURLConnection) urln.openConnection();
						
						getimagecloseconn.setRequestProperty("connection", "close");
						
						getimagecloseconn.connect();
						String cachecontrol = getimagecloseconn.getHeaderField("Cache-Control");
						InputStream isclose = getimagecloseconn.getInputStream();
				        bitmap = BitmapFactory.decodeStream(isclose);
				        getimagecloseconn.disconnect();
					}
					else {
							URL urln = new URL(url);
							HttpURLConnection getimageopenconn = (HttpURLConnection) urln.openConnection();
							getimageopenconn.connect();
							String cachecontrol = getimageopenconn.getHeaderField("Cache-Control");
							InputStream isopen = getimageopenconn.getInputStream();
					          bitmap = BitmapFactory.decodeStream(isopen);
						}
				
	
						return bitmap;
						} catch (Exception ex) {
						ex.printStackTrace();
						
						}
			
			return null;
			
				
	 }
}