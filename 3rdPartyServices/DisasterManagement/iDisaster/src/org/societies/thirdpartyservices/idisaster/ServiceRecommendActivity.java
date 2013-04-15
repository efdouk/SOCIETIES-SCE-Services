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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.societies.android.api.cis.SocialContract;
import org.societies.android.api.cis.SupportedAccountTypes;
import org.societies.thirdpartyservices.idisaster.R;
import org.societies.thirdpartyservices.idisaster.data.SocialActivity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Activity for recommending services in the selected disaster team (community).
 * 
 * NB: Currently a service is only recommended once - see method serviceInTeam
 * 
 * @author Jacqueline.Floch@sintef.no
 *
 */
public class ServiceRecommendActivity extends ListActivity implements OnClickListener {
//implements OnMultiChoiceClickListener {

	ContentResolver resolver;
	Cursor serviceCursor;			// used for the services in the global registry
	int service;					// keep track of number of services
	
	ArrayAdapter<String> serviceAdapter;
	ListView listView;
	
	ArrayList <Integer> serviceMap = new ArrayList <Integer> ();	// Maps service position in list on UI with service cursor position
																	// (Services already recommended are not displayed)

	Button serviceRecommendButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
    	setContentView (R.layout.service_recommend_layout);
    	listView = getListView();
    	resolver = getContentResolver();

    	// Add listener for long click on an item in the list
    	listView.setOnItemLongClickListener (new OnItemLongClickListener() {
    		public boolean onItemLongClick (AdapterView<?> parent, View view,
    			int position, long id) {
    			
    			if ((position) < service) {			// Retrieve information from selected service
    				serviceCursor.moveToPosition(serviceMap.get(position));
    				String description = serviceCursor.getString(serviceCursor.getColumnIndex(SocialContract.Services.DESCRIPTION));

    				Toast.makeText (getApplicationContext(), description, Toast.LENGTH_LONG).show();
    			} else {
//    					// Should never happen..
    			}
    			
    			return true;

    		}

    	});

	    // Add click listener to button
    	serviceRecommendButton = (Button) findViewById(R.id.serviceRecommendButton);
    	serviceRecommendButton.setOnClickListener(this);

    	
    	//TODO:  Add listener for long click
    	// listView.setOnItemLongClickListener(new DrawPopup());
		// TODO: Start the MemberDetails Activity
		// TODO: Add parameters to Intent
		// startActivity (new Intent(MemberListActivity.this, MemberDetailsActivity.class));

    }

    
    
    
/**
 * onResume is called at start of the active lifetime.
 * The list of people is retrieved from SocialProvider and assigned to 
 * view.
 * The data are fetched each time the activity becomes visible as these data
 * may be changed by other users (info fetched from the Cloud)
 */
                
    @Override
    protected void onResume() {
    	super.onResume();
    	
        if (serviceAdapter!= null) serviceAdapter.clear();
    	if (getServices ()					// Retrieve services from the global registry
    				.equals(iDisasterApplication.getInstance().QUERY_EXCEPTION)) {
    		showQueryExceptionDialog ();	// Exception: Display dialog and terminates activity
   		}
    	assignAdapter ();
    }

/**
 * onPause releases all data.
 * Called when resuming a previous activity (for instance using back button)
 */		       
	@Override
    protected void onPause() {
    		
    	super.onPause ();
    			
    	// TODO: check the following:
    	// When using managedQuery(), the activity keeps a reference to the cursor and close it
    	// whenever needed (in onDestroy() for instance.) 
        // When using a contentResolver's query(), the developer has to manage the cursor as a sensitive
        // resource. If you forget, for instance, to close() it in onDestroy(), you will leak 
        // underlying resources (logcat will warn you about it.)
        //

//    	serviceCursor.close();
    }

/**
 * onClick is called when button is clicked because
 * the OnClickListener is assigned to the button
 */

	public void onClick(View view) {

    	if(view == serviceRecommendButton){
    		// check if any service is selected
    		
			if (recommendNewServices()						// Insert services for the selected team
				.equals(iDisasterApplication.getInstance().INSERT_EXCEPTION)) {
				showQueryExceptionDialog ();	// Exception: Display dialog (and terminates activity)
				// Go back to the previous activity
			} else {
				// The services will be added to the adapter (and adapter to view) on resume of MemberListActivity 
				finish ();
			}
 		
    	} else { 				// Unknown button was clicked: should never happen
			iDisasterApplication.getInstance().debug (2, "Button click error");
    		finish();
    		return;
    	}
    }


/**
 * getServices retrieves the list of services in the global registry.
 * - services of type "client" are omitted (since they make no sense without a "provider")
 */
    private String getServices () {
    	
		if (serviceCursor != null) {
			serviceCursor.close();		// "close" releases data but does not set to null
			serviceCursor = null;
		}
		
//		testServices();
		
    	Uri serviceUri = SocialContract.Services.CONTENT_URI;
    					
    	String[] serviceProjection = new String[] {
    				SocialContract.Services.GLOBAL_ID,
    				SocialContract.Services._ID,
    				SocialContract.Services.NAME,
//    				SocialContract.Services.APP_TYPE,
    				SocialContract.Services.TYPE,
    				SocialContract.Services.DESCRIPTION};		// Description is used later on when the user selects services 

//		String serviceSelection = SocialContract.Services.APP_TYPE + "<> ?";
//		String serviceSelection = null;		
//		String[] serviceSelectionArgs = null;	
    	
		String serviceSelection = SocialContract.Services.TYPE + "<> ?";
		
		String[] serviceSelectionArgs = new String[] {
					SocialContract.ServiceConstants.SERVICE_TYPE_CLIENT };	// Ignore services of type CLIENT

    	try {
    		serviceCursor = resolver.query(serviceUri, serviceProjection,
    				serviceSelection, serviceSelectionArgs, null /* sortOrder*/);			
    		} catch (Exception e) {
    			iDisasterApplication.getInstance().debug (2, "Query to "+ serviceUri + "causes an exception");
    			return iDisasterApplication.getInstance().QUERY_EXCEPTION;
    		}
    	
     	return iDisasterApplication.getInstance().QUERY_SUCCESS;

    	}

    /**
     * assignAdapter assigns data to display to adapter and adapter to view.
     */
    	private void assignAdapter () {
    				
    		service = 0;
    		if (serviceMap != null) {
    			serviceMap.clear();	// Reset mapping between UI list and people list
    			
    		} else {				// Should never happen...
    			serviceMap = new ArrayList <Integer> ();
    		}
    				
    		ArrayList<String> serviceList = new ArrayList<String> ();

//    			An empty List will be assigned to Adapter
//    			if (serviceCursor == null) {

//    			An empty List will be assigned to Adapter
//    				if (serviceCursor == null) {
//    					if (serviceCursor.getCount() == 0) {
    				
    		if (serviceCursor != null) {
    			if (serviceCursor.getCount() != 0) {
    				while (serviceCursor.moveToNext()) {
    					// Only display services that are not recommended in the selected team	
    					if (! serviceInTeam (serviceCursor.getString(serviceCursor
    							.getColumnIndex(SocialContract.Services._ID)))) {
	    						
    						String displayName = serviceCursor.getString(serviceCursor
    								.getColumnIndex(SocialContract.Services.NAME));
    						String type = serviceCursor.getString(serviceCursor
    								.getColumnIndex(SocialContract.Services.TYPE));
    						serviceList.add (displayName);
    						service++;
    						serviceMap.add (serviceCursor.getPosition()); // Maps service in UI list and cursor position 	
    					}
    				}
    			}
    		}

    	    listView.setAdapter(new ArrayAdapter<String>(this,
    	            R.layout.disaster_list_item_multiple_choice, serviceList));
    	    
    	    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    	}

    	
    	
/**
 * check whether or not a service is already recommended in a team.
 */
    private boolean serviceInTeam (String serviceId) {
    	
    	if (ServiceListActivity.recommendedServiceCursor == null) {
    		return false;    		
    	}
    	if (ServiceListActivity.recommendedServiceCursor.getCount() ==0) {
    		return false;
    	}
    	
    	ServiceListActivity.recommendedServiceCursor.moveToFirst();
    	if (serviceId.equals(ServiceListActivity.recommendedServiceCursor.getString(
    			ServiceListActivity.recommendedServiceCursor.getColumnIndex(SocialContract.Services._ID)))) {
    			return true;
    	}
    	
    	while (ServiceListActivity.recommendedServiceCursor.moveToNext()) {
    		if (serviceId.equals(ServiceListActivity.recommendedServiceCursor.getString(
    				ServiceListActivity.recommendedServiceCursor.getColumnIndex(SocialContract.Services._ID)))) {
    			return true;
    		}
    	}
    	return false;
     }



/**
 * add selected services for recommendation to SocialProvider
 */
	private String recommendNewServices () {
    
		SparseBooleanArray checkedRows = listView.getCheckedItemPositions();
		
		String recommendedServices = "New recommended services: ";
		Boolean recommendedFlag = false;

		for (int i=0; i<service; ++i) {
			if (checkedRows.get(i)) {
				recommendedFlag = true;
				serviceCursor.moveToPosition(serviceMap.get(i));
				recommendedServices = recommendedServices + " " + serviceCursor.
						getString(serviceCursor.getColumnIndex(SocialContract.Services.NAME));

				// Set the values related to the activity to store in SocialProvider
				ContentValues sharingValues = new ContentValues ();

				sharingValues.put(SocialContract.Sharing._ID_SERVICE,		// Id of the service to be recommended
						serviceCursor.getLong(serviceCursor.getColumnIndex(SocialContract.Services._ID)));
				sharingValues.put(SocialContract.Sharing._ID_OWNER,			// Member recommending the service
						iDisasterApplication.getInstance().me.peopleId);		
				sharingValues.put(SocialContract.Sharing._ID_COMMUNITY,	// Recommend service in the selected team
									iDisasterApplication.getInstance().selectedTeam.id);
				sharingValues.put(SocialContract.Sharing.TYPE, SocialContract.ServiceConstants.SERVICE_RECOMMENDED);
				sharingValues.put(SocialContract.Sharing.DESCRIPTION, "");

// Fields for synchronization with box.com
				sharingValues.put(SocialContract.Sharing.ACCOUNT_NAME, iDisasterApplication.getInstance().me.userName);
				sharingValues.put(SocialContract.Sharing.ACCOUNT_TYPE, SupportedAccountTypes.COM_BOX);
				sharingValues.put(SocialContract.Sharing.DIRTY, 1);
				
				try {
// The Uri value returned is not used.
//					Uri activityNewUri = getContentResolver().insert( SocialContract.CommunityActivity.CONTENT_URI,
//							sharingValues);
					getContentResolver().insert( SocialContract.Sharing.CONTENT_URI, 
							sharingValues);
				} catch (Exception e) {
					iDisasterApplication.getInstance().debug (2, "Insert to "+ 
										SocialContract.Sharing.CONTENT_URI + "causes an exception");
			    	return iDisasterApplication.getInstance().INSERT_EXCEPTION;
				}
			}
		}
		
		
		if (recommendedFlag) {			// New recommended services: add an activity to the feed
			
			SocialActivity socialActivity = 
					new SocialActivity (iDisasterApplication.getInstance().me.userName); // account for synchronization
			ContentResolver activityResolver = getContentResolver();
			
// The return code from addActivity is not exploited here		
			socialActivity.addActivity (activityResolver,			// Insert activity to the activity feed
// TODO: what should be set here? local or global id?
					iDisasterApplication.getInstance().selectedTeam.id,		// Feed of the the selected team
//TODO: what should be set here? global or local id? - local id seems to not work		
					iDisasterApplication.getInstance().me.userName,			// Me
					iDisasterApplication.getInstance().VERB_TEXT,			// Activity intent: Simple text
					recommendedServices,									// List of new recommended services
					iDisasterApplication.getInstance().TARGET_ALL);			// Recipient for Activity
		}


		
		
		return iDisasterApplication.getInstance().INSERT_SUCCESS;

	}

/**
 * showQueryExceptionDialog displays a dialog to the user and
 * terminates the activity.
 */
        			
	private void showQueryExceptionDialog () {
    	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(getString(R.string.dialogServiceQueryException))
        			.setCancelable(false)
          			.setPositiveButton (getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
          				public void onClick(DialogInterface dialog, int id) {
          					finish ();
          					return;
          				}
          			});
        AlertDialog alert = alertBuilder.create();
        alert.show();	
	}

/**
 * testServices retrieves the list of services in the global registry.
 */
    private void testServices () {
    	
		Cursor testCursor; 
    		
    	Uri serviceUri = SocialContract.Services.CONTENT_URI;
    					
    	String[] serviceProjection = new String[] {
    				SocialContract.Services._ID,
    				SocialContract.Services.GLOBAL_ID,
					SocialContract.Services._ID_OWNER,
	    			SocialContract.Services.APP_TYPE,
	    			SocialContract.Services.AVAILABLE,
	    			SocialContract.Services.CONFIG,
	    			SocialContract.Services.DEPENDENCY,
    				SocialContract.Services.DESCRIPTION,
    			    SocialContract.Services.NAME,
    				SocialContract.Services.TYPE,
	    			SocialContract.Services.URL,
    				SocialContract.Services.ACCOUNT_NAME,
    				SocialContract.Services.ACCOUNT_TYPE,
    				SocialContract.Services.CREATION_DATE,				
    				SocialContract.Services.DELETED,
    				SocialContract.Services.DIRTY,
    				SocialContract.Services.LAST_MODIFIED_DATE
    	
    	};		// Description is used later on when the user selects services 

				
    	try {
    		testCursor = resolver.query(serviceUri, serviceProjection, null, null, null /* sortOrder*/);			
    		} catch (Exception e) {
    			return ;
    		}

		int myCount = testCursor.getCount();
		
		if (testCursor.getCount() == 0) {
			return;
		}		
		
		int i =0;
		
		while (testCursor.moveToNext()) {
			
			long s01Id = testCursor.getLong(testCursor
					.getColumnIndex(SocialContract.Services._ID));
			String s02GlobalId = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.GLOBAL_ID));
			long s03IdOwner = testCursor.getLong(testCursor
					.getColumnIndex(SocialContract.Services._ID_OWNER));
			String s04IdAppType = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.APP_TYPE));
			String s05IdAvailable = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.AVAILABLE));
			String s06IdConfig = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.CONFIG));
			String s07IdDeependencyAvailable = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.DEPENDENCY));
			String s08Desc = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.DESCRIPTION));
			String s09NAme = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.NAME));
			
			String s10Type = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.TYPE));
			String s11URL = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.URL));			
			String s12AccountName = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.ACCOUNT_NAME));
			String s13AccountType = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.ACCOUNT_TYPE));
			String s14CreationDate = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.CREATION_DATE));
			String s15Deleted = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.DELETED));
			String s16Dirty = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.DIRTY));
			String s17Last = testCursor.getString(testCursor
					.getColumnIndex(SocialContract.Services.LAST_MODIFIED_DATE));
		
			i ++;			
		}
		return;
    	}
}
