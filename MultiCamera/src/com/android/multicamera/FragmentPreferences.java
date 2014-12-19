package com.android.multicamera;

import android.app.Activity;  
import android.content.Intent;
import android.os.Bundle;  
import android.preference.ListPreference;
import android.preference.PreferenceFragment;  
  
public class FragmentPreferences extends Activity {  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        // TODO Auto-generated method stub  
        super.onCreate(savedInstanceState);  
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();  
    }  
      
      
    public static class PrefsFragement extends PreferenceFragment{
    	@Override  
        public void onCreate(Bundle savedInstanceState) {  
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);  
            addPreferencesFromResource(R.xml.multicamera_setting);
            CharSequence lpFrameRateKey = "list_framerate";
            CharSequence lpvideoSizeKey = "list_videosize";
            ListPreference lpFrameRate = (ListPreference) this.findPreference(lpFrameRateKey);
            ListPreference lpVideoSize = (ListPreference) this.findPreference(lpvideoSizeKey);
            Intent intent= this.getActivity().getIntent();
            CharSequence[] videoSizeSupported =intent.getCharSequenceArrayExtra("videoSizeSupported");
            CharSequence[] frameRateSupported =intent.getCharSequenceArrayExtra("frameRateSupported");
            lpFrameRate.setEntries(frameRateSupported);
            lpFrameRate.setEntryValues(frameRateSupported);
            lpFrameRate.setDefaultValue("30");
            lpVideoSize.setEntries(videoSizeSupported);
            lpVideoSize.setEntryValues(videoSizeSupported);
            lpVideoSize.setDefaultValue("640x480");
            
        }
    }
}
