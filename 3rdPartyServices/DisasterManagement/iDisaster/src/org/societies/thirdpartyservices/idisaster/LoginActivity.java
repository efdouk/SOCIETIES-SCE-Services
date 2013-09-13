/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.societies.thirdpartyservices.idisaster;

import org.societies.thirdpartyservices.idisaster.R;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.text.InputType;

import android.widget.Toast;

import android.widget.Button;
import android.view.View.OnClickListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.view.inputmethod.InputMethodManager;

import org.societies.android.api.cis.SocialContract;
//import org.societies.thirdpartyservices.idisaster.SocialContract;
import android.net.Uri;

import android.content.ContentValues;
import android.content.ContentResolver;
import android.database.Cursor;



/**
 * This activity is responsible for user login,
 * including handling wrong user name and password.
 * 
 * @author Jacqueline.Floch@sintef.no
 *
 */


// TODO: This ACtivity will be removed
// Login should be handled by CSS Management Light

public class LoginActivity extends Activity implements OnClickListener {

	private EditText userNameView;
	private EditText userEmailView;
	private EditText userPasswordView;
	private String userName;
	private String userEmail;
	private String userPassword;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView (R.layout.login_layout);

		// Get editable fields
		userNameView = (EditText) findViewById(R.id.editUserName);
		userEmailView = (EditText) findViewById(R.id.editEmail);
		userPasswordView = (EditText) findViewById(R.id.editPassword);
	    userPasswordView.setInputType (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    	// Add click listener to button
    	final Button button = (Button) findViewById(R.id.loginButton);
    	button.setOnClickListener(this);
    	
    	
    }
 		
/**
 * onClick is called when button is clicked because
 * the OnClickListener is assigned to the button
 */
	public void onClick (View view) {

// TODO: remove code
// The text will now be set to the Hint text
//    	if (userNameView.getText().length() == 0) {					// check input for user name
//
//    	// Hide the soft keyboard otherwise the toast message does appear more clearly.
//    	    InputMethodManager mgr = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
//    	    mgr.hideSoftInputFromWindow(userNameView.getWindowToken(), 0);
//	    
//    		Toast.makeText(this, getString(R.string.toastUserName), 
//    				Toast.LENGTH_LONG).show();
//    		return;
//
//    	} else if (userPasswordView.getText().length() == 0) {		// check input for password
//
//    		// Hide the soft keyboard otherwise the toast message does appear more clearly.
//    	    InputMethodManager mgr = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
//    	    mgr.hideSoftInputFromWindow(userPasswordView.getWindowToken(), 0);
//
//    	    Toast.makeText(this, getString(R.string.toastPassword), 
//	    			Toast.LENGTH_LONG).show();
//	    	return;
//
//    	} else {													// verify the password and store in preferences file
//
//    		userName = userNameView.getText().toString();
//    		userPassword = userPasswordView.getText().toString();
//		... go on as bealow

    	if (userNameView.getText().length() == 0) {					// check input for user name
    		userName = getString (R.string.loginUserNameHint);
    	} else {
    		userName = userNameView.getText().toString();
    	}

    	if (userEmailView.getText().length() == 0) {		// check input for password
    		userEmail = getString (R.string.loginEmailHint);
    	} else {
    		userEmail = userEmailView.getText().toString();
    	}

    	if (userPasswordView.getText().length() == 0) {		// check input for password
    		userPassword = getString (R.string.loginPasswordHint);
    	} else {
    		userPassword = userPasswordView.getText().toString();
    	}

     		
//TODO: the user name and password should be checked by the platform
        	// Instantiate the Societies platform => needs modification of platforLogin
//        iDisasterApplication.getInstance().platformLogIn ();

    		
    	boolean loginCode = false;	// TODO: replaced by code returned by Societes API
    			    		
    	// Create dialog for wrong password
    	if (loginCode) { 							
    		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
    		alertBuilder.setMessage(getString(R.string.loginDialog))
    			.setCancelable(false)
    			.setPositiveButton (getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
	    	           userNameView.setText(getString(R.string.emptyText));
	    	           userNameView.setHint(getString(R.string.loginUserNameHint));
	    	           userEmailView.setText(getString(R.string.emptyText));
	    	           userEmailView.setHint(getString(R.string.loginEmailHint));
	    	           userPasswordView.setText(getString(R.string.emptyText));
	    	           userPasswordView.setHint(getString(R.string.loginPasswordHint));
	    	           return;
    				}
    			});
	    	AlertDialog alert = alertBuilder.create();
	    	alert.show();
	    	return;
	   	}
    	
// TODO: store log information in the Social Provider - not in preferences.
// Temporary solution as long CSS Manager Light is not available
    	
    	// Store user name and password in preferences
//        iDisasterApplication.getInstance().setUserIdentity (userName, userEmail, userPassword);
      	
    	
    	
// Code for testing the correct setting of preferences 
//    	    String testName = iDisasterApplication.getInstance().preferences.
//    	    	getString ("pref.username","");
//    	    String testEmail = iDisasterApplication.getInstance().preferences.
//	    	getString ("pref.email","");
//    	    String testPassword = iDisasterApplication.getInstance().preferences.
//    	    	getString ("pref.password","");
//    	    Toast.makeText(this, "Debug: "  + testName + " " + testEmail + " " + testPassword, 
//    			Toast.LENGTH_LONG).show();

        // Add user to Social Provider
		if (!iDisasterApplication.testDataUsed) {			// Test data
			insertMe ();
		}        

        // Hide the soft keyboard:
		// - the soft keyboard will not appear on next activity window!
    	InputMethodManager mgr = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
    	mgr.hideSoftInputFromWindow(userPasswordView.getWindowToken(), 0);

//    	finish();	// noHistory=true in Manifest => the activity is removed from the activity stack and finished.

	    // Send intent to Disaster activity
	    startActivity(new Intent(LoginActivity.this, DisasterListActivity.class));
	}

	/**
	 * insertMe is used to add the user information in SocialProvider
	 */

	private void insertMe () {
		
		Uri uri = SocialContract.Me.CONTENT_URI;

		
		//What to get:
		String[] projection = new String [] {
				SocialContract.Me._ID,
				SocialContract.Me.NAME,
				SocialContract.Me.DISPLAY_NAME
			};
		
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		
		ContentResolver resolver  = getContentResolver();
		Cursor cursor = resolver.query(uri, projection, selection, selectionArgs,sortOrder);

//		Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs,
//				sortOrder);
		
		
		if (cursor == null) {
			iDisasterApplication.getInstance().showDialog (this, "No content provider found", getString(R.string.dialogOK));
			
		} else {

			while (cursor.moveToNext()) {

				String displayName = cursor.getString(cursor
						.getColumnIndex(SocialContract.Me.DISPLAY_NAME));
				
				iDisasterApplication.getInstance().showDialog (this, "name: " + displayName , getString(R.string.dialogOK));
				
			}

		}
		

		
//		ContentValues updateValues = new ContentValues();
//		updateValues.put(SocialContract.Me.GLOBAL_ID , userEmail);
//		updateValues.put(SocialContract.Me.NAME , userName);
//		updateValues.put(SocialContract.Me.DISPLAY_NAME , userName);
//		managedQuery(uri, projection, selection, selectionArgs,
//				sortOrder);
		
	
	}
}
