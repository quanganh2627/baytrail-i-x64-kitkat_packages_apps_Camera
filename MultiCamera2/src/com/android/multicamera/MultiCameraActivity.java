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
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.SurfaceTexture;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.annotation.SuppressLint;
import android.view.View;
import android.hardware.Camera;
import android.hardware.CameraRecord;

public class MultiCameraActivity extends Activity implements SurfaceHolder.Callback{
    private static String TAG = "MultiCameraActivity";

    private static final int PERIOD = 60*60*1000;
    public CameraRecord mMultiCamera = null;
    public boolean mRecordingFlag = false;
    public boolean mPreviewFlag = false;
    public int showNo = 0;
    private static final int PREVIEW_COUNT = 2;
    public SurfaceHolder surfaceHolder;
    private RecordThread mRecordThread = null;

    private static final int RECORDING_START = 1;
    private static final int RECORDING_STOP = 2;
    private static final int RECORDING_CANCLED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multi_camera);
        
        SurfaceView surfaceView= (SurfaceView)findViewById(R.id.surfaceView1);
        surfaceHolder=surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFixedSize(640, 480);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        
        Button btnstartRec=(Button)findViewById(R.id.startRecAll);
        btnstartRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	if (!mRecordingFlag) {
                    mHandler.sendEmptyMessage(RECORDING_START);
                    mRecordingFlag = true;
            	} else {
            		Toast.makeText(getApplicationContext(), "started already",
            			     Toast.LENGTH_SHORT).show();
            	}
            }
        });

        Button btnstopRec=(Button)findViewById(R.id.stopRecAll);
        btnstopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {            	
                if (mRecordingFlag) {
                      mHandler.sendEmptyMessage(RECORDING_STOP);
                      mRecordingFlag = false;
                } else {
            		Toast.makeText(getApplicationContext(), "stoped already",
           			     Toast.LENGTH_SHORT).show();
                }
           }
	});

        Button cameraPreview1=(Button)findViewById(R.id.cameraPreview1);
        cameraPreview1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 0) {
                        mMultiCamera.surfaceShow(i);
                        showNo = i;
                    }
                }
            }
        });

        Button cameraPreview2=(Button)findViewById(R.id.cameraPreview2);
        cameraPreview2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 1) {
                       mMultiCamera.surfaceShow(i);
                       showNo = i;
                    }
                }
            }
        });

        Button cameraPreview3=(Button)findViewById(R.id.cameraPreview3);
        cameraPreview3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 2) {
                        mMultiCamera.surfaceShow(i);
                        showNo = i;
                    }
                }
            }
        });

        Button cameraPreview4=(Button)findViewById(R.id.cameraPreview4);
        cameraPreview4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < PREVIEW_COUNT; i++) {
                    if (i == 3) {
                        mMultiCamera.surfaceShow(i);
                        showNo = i;
                    }	
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("wangr", " onDestroy~~~");
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int[] previewLocation = new int[2];
        int surfaceWidth = 0;
        int surfaceHeight = 0;
        int ret = -1;
        
        Log.e("wangr", "surfaceCreated~~~" + showNo);
        SurfaceView surfaceView= (SurfaceView)findViewById(R.id.surfaceView1);
        surfaceView.getLocationOnScreen(previewLocation);
        surfaceWidth = surfaceView.getMeasuredWidth();
        surfaceHeight = surfaceView.getMeasuredHeight();

        mMultiCamera = new CameraRecord(PREVIEW_COUNT
                 , previewLocation[0]
       	         , previewLocation[1]
                 , surfaceWidth
                 , surfaceHeight);
        mMultiCamera.setVideoSize(640, 480);
        mMultiCamera.setVideoFrameRate(30);

        if (!mPreviewFlag) {
            ret = mMultiCamera.startPreview();
            if (ret == 0) {
                mPreviewFlag = true;
            }
        }
        mMultiCamera.surfaceShow(showNo);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.e("wangr", "surfaceDestroyed~~~");
    	int ret = -1;
    	
        mMultiCamera.surfaceShow(PREVIEW_COUNT);
        
        if (mRecordingFlag) {
            ret = mMultiCamera.stopRecording();
            if (ret == 0) {
            	mRecordingFlag = false;
            }    
        }

        if (mPreviewFlag) { 
            ret = mMultiCamera.stopPreview();
            if (ret == 0) {
            	mPreviewFlag = false;
            }
        }
        mMultiCamera.UInit();
    }
    

     public void startRecording(){
    	if(mMultiCamera != null){
    	   mMultiCamera.startRecording();
    	}
    }
    
    public void stopRecording(){
    	if(mMultiCamera!=null){
    	   mMultiCamera.stopRecording();
    	}
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORDING_START:
                    mRecordThread = new RecordThread();
                    mRecordThread.enableRecord();
                    mRecordThread.start();
                    Toast.makeText(MultiCameraActivity.this,
                            " Recording started "  , Toast.LENGTH_LONG).show();
                    break;
                case RECORDING_STOP:
                    stopRecording();
                    mRecordThread.stopRecord();            	
                    mRecordThread = null;
                    Toast.makeText(MultiCameraActivity.this,
                            " Recording stopped "  , Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        };
    };
  
    public class RecordThread extends Thread {
        private volatile boolean mIsRunning = false;

        public RecordThread() {
            mIsRunning = false;
        }

        public void enableRecord() {
            mIsRunning = true;
        }
	
        public void stopRecord() {
            mIsRunning = false;
        }

        @Override
        public void run() {
            while(mIsRunning) {
                try {
                    Log.i(TAG, "Recording for 30 minutes... ");
                    startRecording();
                    Thread.sleep(PERIOD);
                    stopRecording();
                } catch (InterruptedException e) {
                }
            }
        }
    }	    
}
