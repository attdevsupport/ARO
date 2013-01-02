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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

@TargetApi(14)
public final class MultiRes extends Activity {

    private static final String TAG = null;

	private int mCurrentPhotoIndex = 0;

    
	private final String[] mPhotoIds = {
			"http://www.sillarsfamily.com/England2004/cwdata/Image_0010.jpg",
			"http://www.sillarsfamily.com/England2004/cwdata/Image_0022.jpg",
			"http://www.sillarsfamily.com/England2004/cwdata/Image_0033.jpg",
	        "http://www.sillarsfamily.com/England2004/cwdata/Image_0043.jpg",
	        "http://www.sillarsfamily.com/etagkidphotos/Image_0010.jpg",
			"http://www.sillarsfamily.com/etagkidphotos/Image_0022.jpg",
			"http://www.sillarsfamily.com/etagkidphotos/Image_0033.jpg",
	        "http://www.sillarsfamily.com/etagkidphotos/Image_0043.jpg",
	        
	       // "http://www.sillarsfamily.com/England2004/cwdata/Image_0069.jpg",
	       // "http://www.sillarsfamily.com/England2004/cwdata/Image_0074.jpg",
	      //  "http://www.sillarsfamily.com/England2004/cwdata/Image_0077a.jpg", 
	      //  "http://www.sillarsfamily.com/England2004/cwdata/Image_0092.jpg",
	     //   "http://www.sillarsfamily.com/England2004/cwdata/Image_0102.jpg", 
	      //  "http://www.sillarsfamily.com/England2004/cwdata/Image_0134.jpg",
	        };
    

    /** Called when the activity is first created. */
    @TargetApi(14)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //establish a cache
        try {
            File httpCacheDir = new File(getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
            //empty cache on startup - only suseful for testing.
            HttpResponseCache cache = HttpResponseCache.getInstalled();
 
        }
         catch (IOException e) {
            Log.i(TAG, "HTTP response cache installation failed:" + e);
        }
    
        
        
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
    @TargetApi(14)
	@Override
    protected void onStop() {
       
    	//empty cache after every use
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
        	//cache.flush will write these files to the card.  I dont really want this for testing purposes.
        //    cache.flush();
        	
        }
    }
    
    
    
    private void showPhoto(int photoIndex) {
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        
        String caching = "yes";
       
        
        //measure how long it takes to get an image
        
        Long timeStart=System.currentTimeMillis();
        final Bitmap bmp = getBitmap(caching, mPhotoIds[photoIndex]);
        imageView.setImageBitmap(bmp);
        Long timeEnddownload=System.currentTimeMillis()-timeStart;
      
        TextView statusText = (TextView) findViewById(R.id.status_text);
        statusText.setText(String.format("%d/%d", photoIndex + 1,
                mPhotoIds.length));
    	if (mPhotoIds[photoIndex].contains("England")){
    		//all of these files have a Cachecontrol header
        	if (mPhotoIds[photoIndex].contains("0010")){
	        	//cache time of 0
	        	statusText.append(" Max-age = 1437313. Render time=" + timeEnddownload);
	        }
	        if ( mPhotoIds[photoIndex].contains("0022")){
	        	statusText.append(" Max-age = 60. Render time=" + timeEnddownload);
	        }
	        if ( mPhotoIds[photoIndex].contains("0033")){
	        	statusText.append(" Max-age = NO_CACHE. Render time=" + timeEnddownload);
	        }
	        if ( mPhotoIds[photoIndex].contains("0043")){
	        	statusText.append(" Max-age = 0. Render time=" + timeEnddownload);
	        }   
    	}
    	else {
    		//i am using etags and immediately expiring the files
    		statusText.append(" Using Etags for cache. Render time=" + timeEnddownload);
    	}
    }

	Bitmap getBitmap(String caching, String url) {
		
		 
		 
		 
		 
		//load file
				try {
					Bitmap bitmap;
					//lets always use the cache - but my files will have various expire times.
				//	if (caching == "yes"){
						URL urln = new URL(url);
						HttpURLConnection getimagecloseconn = (HttpURLConnection) urln.openConnection();
						
						getimagecloseconn.setRequestProperty("connection", "close");
						
						getimagecloseconn.connect();
						String cachecontrol = getimagecloseconn.getHeaderField("Cache-Control");
						InputStream isclose = getimagecloseconn.getInputStream();
				        bitmap = BitmapFactory.decodeStream(isclose);
				        getimagecloseconn.disconnect();
				        
				        
				        
				//	}
				/*	else {
							URL urln = new URL(url);
							HttpURLConnection getimageopenconn = (HttpURLConnection) urln.openConnection();
							//this line skips the cache
							getimageopenconn.addRequestProperty("Cache-Control", "no-cache");
							getimageopenconn.setRequestProperty("connection", "close");
							getimageopenconn.connect();
							String cachecontrol = getimageopenconn.getHeaderField("Cache-Control");
							InputStream isopen = getimageopenconn.getInputStream();
					        bitmap = BitmapFactory.decodeStream(isopen);
					        getimageopenconn.disconnect();
						}
				*/
	
						return bitmap;
						} catch (Exception ex) {
						ex.printStackTrace();
						
						}
				
			
			return null;
			
				
	 }
}