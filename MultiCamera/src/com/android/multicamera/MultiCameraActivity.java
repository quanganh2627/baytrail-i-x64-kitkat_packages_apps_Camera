/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.multicamera;

import java.io.IOException;

import android.view.Menu;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.annotation.SuppressLint;
import android.view.View;
import android.hardware.Camera;
import android.hardware.CameraRecord;
import java.lang.ref.WeakReference;
import android.os.Environment;
import android.os.StatFs;
import java.io.File;

import android.media.CamcorderProfile;


public class MultiCameraActivity extends Activity 
        implements CameraRecord.OnErrorListener, CameraRecord.OnInfoListener {

    public final static String TAG="MultiCamera";
	
    private static final int menu_setting = 1;
    private static final int PREVIEW_COUNT = 2;
    public CameraRecord mMultiCamera = null;
    public int mPreviewCount = 0;
    public boolean[] mRecordingFlag = new boolean[PREVIEW_COUNT];
    public boolean mPreviewFlag = false;
    
    private  long maxFileSize = 1024*1024*1024;//recording video File Size ,default 1G Byte
    //private  long mMinFileSize = 1024*1024;//1M Byte
    
    //settings
    String[] mSizeWH = new String[2];
    String mStrFPS;
    
    CameraPreview[] mCameraPreview = new CameraPreview[PREVIEW_COUNT];
    FrameLayout[] mFrameLayout = new FrameLayout[PREVIEW_COUNT];

    Button[] mBtnCamera = new Button[4];
    String[] mCameraStartText = new String[4];
    String[] mCameraStopText = new String[4];

    public boolean isDBG = true;//false;
    public boolean isSupportConfigureCamcorderProfile = false;
    private CamcorderProfile[] mProfileList = {null};
    private CamcorderProfile mCommonProfile ;
    /*quality list
    QUALITY_QVGA(320x240)7
    QUALITY_480P(640x480) 4
    QUALITY_720P(1080x720) 5// not supported according parameter camera now
   */
    private int mVideoEncodeQuality = CamcorderProfile.QUALITY_480P;//(640x480),framerate:30

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, " onCreate~~~");
        setContentView(R.layout.activity_multi_camera);
    	
        mMultiCamera = new CameraRecord(PREVIEW_COUNT);
        
        //set listener
        mMultiCamera.setOnErrorListener(this);
        mMultiCamera.setOnInfoListener(this);
        
        mFrameLayout[0] = (FrameLayout) findViewById(R.id.frame_layout1);
        mFrameLayout[1] = (FrameLayout) findViewById(R.id.frame_layout2);

        mBtnCamera[0]=(Button)findViewById(R.id.cameraRec1);
        mBtnCamera[1]=(Button)findViewById(R.id.cameraRec2);
        mBtnCamera[2]=(Button)findViewById(R.id.cameraRec3);
        mBtnCamera[3]=(Button)findViewById(R.id.cameraRec4);

        mCameraStopText[0] = getResources().getString(R.string.cameraStop1);
        mCameraStopText[1] = getResources().getString(R.string.cameraStop2);
        mCameraStopText[2] = getResources().getString(R.string.cameraStop3);
        mCameraStopText[3] = getResources().getString(R.string.cameraStop4);

        mCameraStartText[0] = getResources().getString(R.string.cameraRec1);
        mCameraStartText[1] = getResources().getString(R.string.cameraRec2);
        mCameraStartText[2] = getResources().getString(R.string.cameraRec3);
        mCameraStartText[3] = getResources().getString(R.string.cameraRec4);

        for (int i = 0; i < PREVIEW_COUNT; i++) {
            mRecordingFlag[i] = false;
            mCameraPreview[i] = new CameraPreview(this,i);
            mFrameLayout[i].addView(mCameraPreview[i], 0);
            mCameraPreview[i].setVisibility(SurfaceView.VISIBLE);
        }
        Button btnstartRec=(Button)findViewById(R.id.startRecAll);
        btnstartRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	int ret = -1;
            	for (int i = 0; i < PREVIEW_COUNT; i++) {
            	    if (!mRecordingFlag[i]) {
                        ret = safeStartRecordingById(i);
                        if (ret == 0) {
                    	    mBtnCamera[i].setText(mCameraStopText[i]);
                    	    mRecordingFlag[i] = true;
                        }
            	    } else {
                        Toast.makeText(getApplicationContext(), "camera"+i+"started already",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Button btnstopRec=(Button)findViewById(R.id.stopRecAll);
        btnstopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	int ret = -1;
            	
            	for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (mRecordingFlag[i]) {
                        ret = mMultiCamera.stopRecordingById(i);
                        if (ret == 0) { 
                    	    mBtnCamera[i].setText(mCameraStartText[i]);
                    	    mRecordingFlag[i] = false;
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "camera"+i+"stoped already",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
	});

        mBtnCamera[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	int ret = -1;
            	
                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 0) {
                        if (!mRecordingFlag[i]) {  
                            ret = safeStartRecordingById(i);
                            if (ret == 0) {
                                mBtnCamera[i].setText(mCameraStopText[i]);
                                mRecordingFlag[i] = true;
    		            }    
                        } else {
                            ret = mMultiCamera.stopRecordingById(i);
                            if (ret == 0) {
                                mBtnCamera[i].setText(mCameraStartText[i]);
                                mRecordingFlag[i] = false;
    		            }
                        }
                    }
                }
            }
        });

        mBtnCamera[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	int ret = -1;

                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 1) {
                        if (!mRecordingFlag[i]) {  
                            ret = safeStartRecordingById(i);
                    	    if (ret == 0) {
                                mBtnCamera[i].setText(mCameraStopText[i]);
                                mRecordingFlag[i] = true;
    	                    }    
                        } else {
                            ret = mMultiCamera.stopRecordingById(i);
                            if (ret == 0) {
                                mBtnCamera[i].setText(mCameraStartText[i]);
                                mRecordingFlag[i] = false;
    		            }
                        }
                    }
                }
            }
        });

        mBtnCamera[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	int ret = -1;

                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 2) {
                        if (!mRecordingFlag[i]) {  
                            ret = safeStartRecordingById(i);
                            if (ret == 0) {
                                mBtnCamera[i].setText(mCameraStopText[i]);
                                mRecordingFlag[i] = true;
    		            }    
                    	} else {
                            ret = mMultiCamera.stopRecordingById(i);
                            if (ret == 0) {
                                mBtnCamera[i].setText(mCameraStartText[i]);
                                mRecordingFlag[i] = false;
    		            }
                        }
                   }
               }
           }
        });

        mBtnCamera[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	int ret = -1;

                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 3) {
                        if (!mRecordingFlag[i]) {  
                            ret = safeStartRecordingById(i);
                            if (ret == 0) {
                                mBtnCamera[i].setText(mCameraStopText[i]);
                                mRecordingFlag[i] = true;
    		            }    
                        } else {
                            ret = mMultiCamera.stopRecordingById(i);
                            if (ret == 0) {                    			
                                mBtnCamera[i].setText(mCameraStartText[i]);
                                mRecordingFlag[i] = false;
    		            }
                        }
                    }	
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, " onDestroy~~~");
        mMultiCamera.Destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, " onStart~~~");
        SharedPreferences share = getSharedPreferences("com.android.multicamera_preferences",0);
        mStrFPS = share.getString("list_framerate","30");
        String strSize = share.getString("list_videosize","640x480");
        maxFileSize = Integer.parseInt(share.getString("edittext_maxFileSize","1024")) * 1024 * 1024;
        mSizeWH = strSize.split("x");

        if(isSupportConfigureCamcorderProfile){
            //default codec profile quality value
            String strDefultProfileQuality = share.getString("list_codecprofile_quality_key","4");//CamcorderProfile.QUALITY_480P = 4
            mVideoEncodeQuality = Integer.parseInt(strDefultProfileQuality);//CamcorderProfile.QUALITY_480P;
            if(isDBG){
                Log.d(TAG, "strDefultProfileQuality = "+ strDefultProfileQuality);
                Log.d(TAG,"mVideoEncodeQuality= "+mVideoEncodeQuality);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, " onStop~~~");
    }

    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        // TODO Auto-generated method stub  
        menu.add(0,menu_setting,1,"settings").setIcon(android.R.drawable.ic_menu_preferences);  
        return super.onCreateOptionsMenu(menu);  
    }  
      
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        // TODO Auto-generated method stub  
        super.onOptionsItemSelected(item);  
        Intent intent = new Intent(this, FragmentPreferences.class);
        CharSequence[] videoSizeSupported = mMultiCamera.getSupportedVideoSizes().split(",");
        CharSequence[] frameRateSupported = mMultiCamera.getSupportedFrameRate().split(",");
        intent.putExtra("videoSizeSupported", videoSizeSupported);
        intent.putExtra("frameRateSupported", frameRateSupported);
        startActivity(intent);  
        return false;  
    }  

    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        public SurfaceHolder gHolder;
        public int mCameraPreviewId;

        public CameraPreview(Context context, int cameraId) {
        	super(context);
            gHolder=this.getHolder();
            gHolder.addCallback(this);
            gHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mCameraPreviewId = cameraId;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                     int height) {
            Log.e(TAG, "CameraView surfaceChanged");

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.e(TAG, "CameraView surfaceCreated" + mCameraPreviewId);
            int ret = -1;

            mMultiCamera.setPreviewSurface(mCameraPreviewId, gHolder.getSurface());
            mPreviewCount++;
            if (mPreviewCount == PREVIEW_COUNT) {
                mMultiCamera.Init();

                int width = Integer.parseInt(mSizeWH[0]);
                int height = Integer.parseInt(mSizeWH[1]);
                int frameRate = Integer.parseInt(mStrFPS);

                if(isSupportConfigureCamcorderProfile){
                    //TODO: quality should be support setted by user ,others such as QUALITY_720P, QUALITY_1080P, QUALITY_QVGA(320x240)
                    //int quality = CamcorderProfile.QUALITY_480P;//QUALITY_480P;//default quality,
                    Log.d(TAG,"mVideoEncodeQuality = " + mVideoEncodeQuality );
                    mCommonProfile =  getVideoProfileByCameraId(0,mVideoEncodeQuality );//TODO temptly set as common profile

                    //set width,height, framerate base on profile
                    width = (width == mCommonProfile.videoFrameWidth)?width:mCommonProfile.videoFrameWidth;
                    height = (height == mCommonProfile.videoFrameHeight)?height:mCommonProfile.videoFrameHeight;
                    frameRate = (frameRate == mCommonProfile.videoFrameRate)?frameRate:mCommonProfile.videoFrameRate;
                    //set other profile params
                     mMultiCamera.setCommonVideoProfile(mCommonProfile);
                    //current recording control strategy is using maxFileSize instead of the max-duration 
                    mMultiCamera.setCommonParameters("max-duration="+mCommonProfile.duration*1000);//duration:ms
                }

                if(isDBG) { 
                    Log.d(TAG,"width = " + width);
                    Log.d(TAG,"height = " + height);
                    Log.d(TAG,"frameRate = " + frameRate);
                }
                mMultiCamera.setVideoSize(width,height);
                mMultiCamera.setVideoFrameRate(frameRate);
                mMultiCamera.setMaxFileSizeBytes(maxFileSize);

                if (!mPreviewFlag) {
                    ret = mMultiCamera.startPreview();
                    if (ret == 0) {
                        mPreviewFlag = true;
                    }
                }
            }
        }

	@Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.e(TAG, "surfaceDestroyed~~~");
            int ret = -1;

            mPreviewCount--;
            if (mPreviewCount == 0) {
                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (mRecordingFlag[i]) {
                        ret = mMultiCamera.stopRecordingById(i);
                        if (ret == 0) {
                            mBtnCamera[i].setText(mCameraStartText[i]);
                            mRecordingFlag[i] = false;
                        }
                     }
                 }
                 if (mPreviewFlag) {
                     ret = mMultiCamera.stopPreview();
                     if (ret == 0) {
                         mPreviewFlag = false;
                     }
                 }
            }
        }
    }

    public int safeStartRecordingById(int cameraId) {
        int ret = -1;
        if(isSpaceAvailableRecording()) {
            if(mMultiCamera != null) {
                ret = mMultiCamera.startRecordingById(cameraId);
            }
        } else {
            Toast.makeText(getApplicationContext(), "no avaliable space left!",
                                Toast.LENGTH_SHORT).show();
            Log.e(TAG, "space not avialibe for recording cameraId: " + cameraId);
        }
        return ret;
    }
    
    //check availabe space to recording 
    boolean isSpaceAvailableRecording() {
        String state = Environment.getExternalStorageState();  
        if(Environment.MEDIA_MOUNTED.equals(state)) {  
            File sdcardDir = Environment.getExternalStorageDirectory(); //dir is /sdcard/ 
            StatFs sf = new StatFs(sdcardDir.getPath());  
            long blockSize = sf.getBlockSize();  
            long blockCount = sf.getBlockCount();  
            long availCount = sf.getAvailableBlocks(); 
            long availableSpaceBytes = availCount*blockSize;//availableSpaceBytes
            //Log.d(TAG, "blockSize :"+ blockSize+",blockCount:"+ blockCount+",summary size:"+blockSize*blockCount/1024+"KB");  
            //Log.d(TAG, "availCount:"+ availCount+", avail space Size "+ availCount*blockSize/1024+"KB");

            Log.d(TAG, "PREVIEW_COUNT * maxFileSize = " + (PREVIEW_COUNT*maxFileSize)/1024 + "KB");
            Log.d(TAG, "availableSpaceBytes = " + availableSpaceBytes/1024 + "KB");
            if(availableSpaceBytes > PREVIEW_COUNT*maxFileSize) { 
                Log.d(TAG, sdcardDir + ": availableSpace = " + availableSpaceBytes/1024 + "KB");
                return true;
            } else {
                Log.e(TAG, sdcardDir + ": availableSpace " + availableSpaceBytes/1024 
                         + "KB too samll, should be more than PREVIEW_COUNT * video File size(KB) = "
                         + PREVIEW_COUNT + "*"+ maxFileSize/1024 +"KB");
                return false;
            }
        } else {
            Log.e(TAG, "media umounted");
            return false;
        }         
    }  
    
    /* add listener callback begin */
    // from CameraRecord.OnErrorListener
    @Override
    public void onError(CameraRecord mr, int what, int extra) {
        int cameraId = extra;
        Log.e(TAG, "CameraRecord error. what=" + what + ", cameraId =" + cameraId);
        if (what == CameraRecord.CAMERA_RECORD_ERROR_UNKNOWN) {
            // We may have run out of space on the sdcard.
            if (mRecordingFlag[cameraId]) {
                if (mMultiCamera != null) {
                    mMultiCamera.stopRecordingById(cameraId);
                    mRecordingFlag[cameraId] = false;
                }
            }
        }
    }

    // from CameraRecord.OnInfoListener
    @Override
    public void onInfo(CameraRecord mr, int what, int extra) {
        int cameraId = extra;
        Log.d(TAG, "CameraRecord info. what=" + what + ", cameraId =" + cameraId);
        if (what == CameraRecord.CAMERA_RECORD_INFO_MAX_FILESIZE_REACHED) {
            Log.d(TAG, "CAMERA_RECORD_INFO_MAX_FILESIZE_REACHED, cameraId =" + cameraId);
            if (mRecordingFlag[cameraId]) {
                if (mMultiCamera != null) {
                    mMultiCamera.stopRecordingById(cameraId);
                    if(isSpaceAvailableRecording()) {
                        Log.d(TAG, " ready to restart recording cameraId = " + cameraId);
                        mMultiCamera.startRecordingById(cameraId);
                    } else {
                        Log.w(TAG, "cameraId = " + cameraId + " can not restart recording, isSpaceAvailableRecording = false ");
                        mRecordingFlag[cameraId] = false;
                        mBtnCamera[cameraId].setText(mCameraStartText[cameraId]);//update recording button
                        Toast.makeText(getApplicationContext(), "no avaliable space left!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private CamcorderProfile getVideoProfileByCameraId(int cameraId, int quality) {
        mProfileList[cameraId]= CamcorderProfile.get(cameraId, quality);
        if(isDBG) { 
            Log.d(TAG, "mProfileList[" + cameraId +"] = "+ mProfileList[cameraId]);
            Log.d(TAG, "mProfileList[" + cameraId +"].quality = "+ mProfileList[cameraId].quality);
            Log.d(TAG, "mProfileList[" + cameraId +"].fileFormat = "+ mProfileList[cameraId].fileFormat);
            Log.d(TAG, "mProfileList[" + cameraId +"].duration = "+ mProfileList[cameraId].duration);
            Log.d(TAG, "mProfileList[" + cameraId +"].videoCodec = "+ mProfileList[cameraId].videoCodec);
            Log.d(TAG, "mProfileList[" + cameraId +"].videoBitRate = "+ mProfileList[cameraId].videoBitRate);
            Log.d(TAG, "mProfileList[" + cameraId +"].videoFrameWidth = "+ mProfileList[cameraId].videoFrameWidth);
            Log.d(TAG, "mProfileList[" + cameraId +"].videoFrameHeight = "+ mProfileList[cameraId].videoFrameHeight);
            Log.d(TAG, "mProfileList[" + cameraId +"].videoFrameRate = "+ mProfileList[cameraId].videoFrameRate);
		}

        return mProfileList[cameraId];
    }
    /* add listenrer callback end */
}
