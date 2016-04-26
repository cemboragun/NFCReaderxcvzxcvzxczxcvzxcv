package com.cpiss.genunie;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

public class LoadingBar {

	static public PopupWindow showLoadingBar(final Activity activity,String text) {
		PopupWindow pwindo = null;
		TextView title; 
		  
		try { 
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.loading_bar, (ViewGroup) activity.findViewById(R.id.popup_element));
   
			pwindo = new PopupWindow(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
			pwindo.setAnimationStyle(R.style.AnimationPopup);
        
			pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);   
			   
			title = (TextView) layout.findViewById(R.id.lbText);  
			title.setText(text);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return pwindo;
	} 
	
	 
}
