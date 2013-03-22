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
package org.societies.webapp.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.jfree.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.cis.attributes.MembershipCriteria;
import org.societies.api.cis.management.ICis;
import org.societies.api.cis.management.ICisManager;
import org.societies.api.comm.xmpp.interfaces.ICommManager;
import org.societies.api.internal.css.management.ICSSLocalManager;
import org.societies.api.schema.cssmanagement.CssInterfaceResult;
import org.societies.api.schema.cssmanagement.CssRecord;
import org.societies.webapp.model.MessageForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.FileBody;


/**
 * 
 * This is the context aware wall proxy service
 *
 * @author guy feigenblat IBM Haifa research lab
 *
 */
@Controller
public class VGProxy {
	
	private static final String LOG_PREFIX = "CONTEXT_AWARE_WALL:\t"; 
	
	/** The logging facility. */
	private static final Logger LOG = LoggerFactory.getLogger(VGProxy.class);
	
	//String SERVER_URL = "http://ta-proj02:9082/VG4SWeb/vg/message";
	String SERVER_URL = "http://societies.local.macs.hw.ac.uk:9080/VG4SWeb/vg/message";
	String MOCK_ENTITY = "";
	boolean MOCK_ENTITY_ACTIVE = false;
	
	static byte[] mBackgroundFile;
	static String mBackgroundFileName=""; 
	static String mCisOfImage="";
	static boolean download = false;
	static String mCurrentCis="";
	static String mCurrentZone="";
	static Map<String,String> mImagesInZonePool = new HashMap<String,String>();
	
	/*
	 * {"userId" : "unknown7",  "cis": "111", "msg": "GuyGuyGuy", "style" : "", "zoneId":"1","messageId":"5","ts":null}
	 * 
	 * {"userId" : "unknown7",  "cis": "111", "msg": "GuyGuyGuy", "style" : "", "zoneId":null,"messageId":null,"ts":null}
	 * 
	 * 
	 * 
	 * 
	 * http://ta-proj02.haifa.ibm.com:9082/QueriesGatewayREST/RT/allActiveEntitiesIds
	 * 
	 */
	
	@Autowired private ICisManager cisManager;
	@Autowired private ICSSLocalManager cssManager;
	@Autowired private ICommManager commManager;
	@Autowired private SocietiesRegistryBean societiesRegistryBean;
	
	private Timer getImgTimerTask;
    
	public VGProxy(){
		LOG.info(LOG_PREFIX + "Ctor");
		
		//getImgTimerTask = new Timer();
        //getImgTimerTask.schedule(new GetImageNameTask(), 30*1000,10*1000);
	}
	
	private List<ICis> fillCisObjectforTesting(){
		LOG.debug(LOG_PREFIX+ " Start method 'fillCisObjectforTesting'");
		
		List<ICis> cisList = new ArrayList<ICis>();
		try {
			Future<CssInterfaceResult> cssInterfaceResultFuture = cssManager.getCssRecord();
			CssInterfaceResult cssInterfaceResult2 = cssInterfaceResultFuture.get();
			CssRecord cssRecord = cssInterfaceResult2.getProfile();
			
			try{
				LOG.debug(LOG_PREFIX+ " creating CIS for testing (in case no CIS if found):  CSS ID \t\t" + cssRecord.getCssIdentity());
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			Hashtable<String, MembershipCriteria> cisCriteria = new Hashtable<String, MembershipCriteria>();
			/*
			MembershipCriteria m = new MembershipCriteria();
			
		
				Rule r = new Rule("equals",new ArrayList<String>(Arrays.asList("married")));
				m.setRule(r);
				cisCriteria.put(CtxAttributeTypes.STATUS, m);
				r = new Rule("equals",new ArrayList<String>(Arrays.asList("Brazil")));
				m.setRule(r);
				cisCriteria.put(CtxAttributeTypes.ADDRESS_HOME_COUNTRY, m);
			*/
			
			
			
			cisManager.createCis("Guy CIS_"+generateRand(),"",cisCriteria, "description: test guy1");
			cisManager.createCis("Guy CIS_"+generateRand(),"",cisCriteria, "description: test guy2");
			cisList = cisManager.getCisList();
			if (cisList.size() > 0){
				LOG.debug(LOG_PREFIX+ " there are "+cisList.size() +" CIS in the system - first one is : "+(cisList.get(0)).getName() + " \t id " + (cisList.get(0)).getCisId());
			}else{
				LOG.debug(LOG_PREFIX+ "  Error! although just been created for testing, there are no CIS in the system");
			}
			
			
		} catch (InterruptedException e) {
			LOG.error(LOG_PREFIX +"exception caught: \t"+e.getMessage() + " \t get cause: "+e.getCause(),e);
		} catch (ExecutionException e) {
			LOG.error(LOG_PREFIX +"exception caught: \t"+e.getMessage() + " \t get cause: "+e.getCause(),e);		
		}catch (Exception e) {
			LOG.error(LOG_PREFIX +"exception caught: \t"+e.getMessage() + " \t get cause: "+e.getCause(),e);
		}
		LOG.debug("Start method 'fillCisObjectforTesting'");
		
		return cisList;
	}
	
	private int generateRand(){
		double rand = Math.random()*1000;
		rand = Math.round(rand);
		return (int)rand;
	}
	
	
	
	
	@RequestMapping(value="/test/createCIS.html",method = RequestMethod.GET)
	public void testCreateCIS(@RequestParam String cisName,@RequestParam String description) {
		Future<CssInterfaceResult> cssInterfaceResultFuture = cssManager.getCssRecord();
		CssInterfaceResult cssInterfaceResult2;
		CssRecord cssRecord=null;
		try {
			cssInterfaceResult2 = cssInterfaceResultFuture.get();
			cssRecord = cssInterfaceResult2.getProfile();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Hashtable<String, MembershipCriteria> cisCriteria = new Hashtable<String, MembershipCriteria>();
		cisManager.createCis(cisName,"",cisCriteria, description);	
	}
	
	
	
	@RequestMapping(value="/contextAwareWall.html",method = RequestMethod.GET)
	public ModelAndView pageLoad() {
		//model is nothing but a standard Map object
		Map<String, Object> model = new HashMap<String, Object>();
		return new ModelAndView("mobile", model) ;
	}
	
	
	
	@RequestMapping(value = "/upload.html", method = RequestMethod.POST)
	public @ResponseBody String upload(HttpServletRequest request1) {
		 
		DefaultMultipartHttpServletRequest request = (DefaultMultipartHttpServletRequest)request1;
		MultipartFile multipartFile = request.getFile("uploadedfile");
		String cisParam = request.getParameter("cis");
		
		if (cisParam == null || cisParam.length() ==0){
			LOG.error(LOG_PREFIX+ "  couldn't upload file; cis parameter is empty or null");
			return "";
		}
		
		String tempBackgroundImageName="";
		try {
		
			tempBackgroundImageName = multipartFile.getOriginalFilename();
			byte[] backgroundFile = multipartFile.getBytes();
			UploadTask uploadTask=null;
			
			String currentImageName = "";
			String warningMsg = "";
			synchronized (VGProxy.class) {
				if (mBackgroundFile != null && mBackgroundFile.length > 0 && mCisOfImage != null && mCisOfImage.length() > 0 &&
					mCurrentZone != null && mCurrentZone.length() > 0 && tempBackgroundImageName != null && tempBackgroundImageName.length() > 0){
				
						mBackgroundFile = backgroundFile ;
						mCisOfImage = cisParam;
						currentImageName = setBackgroundImageName(tempBackgroundImageName);
						uploadTask = new UploadTask(mBackgroundFile, currentImageName, mCisOfImage, mCurrentZone,"");
						
						mImagesInZonePool.put(mCisOfImage+"_"+mCurrentZone, currentImageName);
					
				}else{
					
						warningMsg+= "Couldn't upload file to WAS; Parametes: file name: "+tempBackgroundImageName+", cis "+
									mCisOfImage+", zone "+mCurrentZone+ ", file size ";
						warningMsg+= (mBackgroundFile != null)?mBackgroundFile.length: " null";
									 
				}
			}
			
			if (warningMsg.length() == 0){
				LOG.info(LOG_PREFIX+ "  file - "+currentImageName+" was sent to upload task;");	
			}else{
				LOG.warn(LOG_PREFIX+ " " + warningMsg);
			}
						
			if (uploadTask != null){
				uploadTask.start();
			}
			
		} catch (Exception e) {
			LOG.error(LOG_PREFIX+ "  Error writing file to byte[] stream. file name : "+tempBackgroundImageName,e);
		}
		
		return "";
	}
	
	
	private String setBackgroundImageName(String originalName){
		//first remove the file type (e.g. jpg) 
		mBackgroundFileName = originalName.split("\\.")[0];
		//then remove the unique android suffix (if exists)
		mBackgroundFileName = mBackgroundFileName.split("\\-")[0];
		return mBackgroundFileName;
	}
	
	
	@RequestMapping(value = "/download.html", method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response) {
		String fileName=null;
		try {
				fileName = request.getParameter("fileName");
				// copy it to response's OutputStream
				
				synchronized (VGProxy.class) {
					LOG.info(LOG_PREFIX+ "  request to download file "+ fileName + " sent to the server; file in server is '"+mBackgroundFileName+"'" );
					if (mBackgroundFile != null && mBackgroundFile.length > 0 && mBackgroundFileName.equals(fileName)){
						IOUtils.write(mBackgroundFile, response.getOutputStream());
						LOG.info(LOG_PREFIX+ " '"+fileName +"' file was written to output stream; file size "+mBackgroundFile.length + " bytes" );
					
					}else if (mBackgroundFileName.equals(fileName) &&  (mBackgroundFile == null || mBackgroundFile.length == 0)){
						LOG.warn(LOG_PREFIX+ " request to download image '"+fileName +"' accepted in server but image object is empty");
					}else if (mBackgroundFileName.equals(fileName)){
						LOG.info(LOG_PREFIX+ " request to download image '"+fileName +"' accepted in server but active image is '"+mBackgroundFileName+"'");
					}
				}
				
				LOG.info(LOG_PREFIX+ " begin file flush");
				response.flushBuffer();
				LOG.info(LOG_PREFIX+ "  end file flush");
		    } catch (IOException ex) {
		    	LOG.error(LOG_PREFIX+ " Error writing file to output stream. Filename was "+fileName,ex);
		    	throw new RuntimeException("IOError writing file to output stream");
		    } catch (Exception ex) {
		    	LOG.error(LOG_PREFIX+ " Error writing file to output stream. Filename was "+fileName,ex);
		    	throw new RuntimeException("Exception writing file to output stream");
		    }
	}
	
	
	/**
	 * Posting user message to the centralized context aware wall server
	 * 
	 * @param form
	 * @param result
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/postMsg1.html",method = RequestMethod.POST)
	public @ResponseBody String page(@Valid MessageForm form ,BindingResult result, Map model) {
		
		LOG.debug(LOG_PREFIX + " enter postMsg1.html");
		
		try{
			if (validateForm(form)){
				String jsonMsg="";
				try{
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("cis", form.getCisBox());
					jsonObject.put("userId", form.getUserId());
					jsonObject.put("style", form.getStyle());
					jsonObject.put("msgDest", form.getMsgDest());
					
					String postedMsg = form.getMsg();
					postedMsg= postedMsg.replace("\n", " ");
					jsonObject.put("msg", postedMsg);
					jsonMsg = jsonObject.toString();
					
					LOG.info(LOG_PREFIX + " created JSON msg object :\t" +jsonMsg);
					
					postMessageInternal(jsonMsg);
				}catch (Exception e) {
					LOG.error(LOG_PREFIX +"exception caught while posting the following msg: \n"+jsonMsg+"\n "+e.getMessage(),e);
				}
			}
			LOG.debug(LOG_PREFIX + " finish postMsg1.html");
			
		}catch (Exception e) {
			LOG.error(LOG_PREFIX +"exception caught: \t"+e.getMessage() + " \t get cause: "+e.getCause(),e);
		}
		
		return "";
	}
	
	
	
	private boolean validateForm(MessageForm form){
		boolean flag = true;
		if (form.getCisBox() == null || form.getCisBox().length() == 0){
			LOG.warn(LOG_PREFIX+ " Selected CIs can't be null or Empty \t form.getCisBox()='"+form.getCisBox()+"'");
			flag = false;
		}
		if (form.getUserId() == null || form.getUserId().length() == 0){
			LOG.warn(LOG_PREFIX+ " UserId can't be null or Empty \t form.getUserId()='"+form.getUserId()+"'");
			flag = false;
		}
		if (form.getMsg() == null || form.getMsg().trim().length() == 0){
			LOG.warn(LOG_PREFIX+" UserId can't be null or Empty \t form.getMsg()='"+form.getMsg()+"'");
			flag = false;
		}
		if (form.getStyle() == null ||  form.getStyle().length() == 0){
			LOG.warn(LOG_PREFIX+ " UserId can't be null or Empty \t form.getStyle()='"+form.getStyle()+"'");
			flag = false;
		}
		return flag;
		
	}
	
	@RequestMapping(value="/initialUserDetails.html",method = RequestMethod.GET)
	public @ResponseBody String getCIS() {
		List<ICis> cisList = new ArrayList<ICis>();
		JSONObject userDetailsObject = new JSONObject();
		try{
			cisList = cisManager.getCisList();
			if (cisList.isEmpty()){
				cisList = fillCisObjectforTesting();
			}
			
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObject; 
			
			for (ICis cis : cisList){
				jsonObject = new JSONObject();
				try {
					jsonObject.put("name", cis.getName());
					jsonObject.put("id", cis.getCisId());
				} catch (JSONException e) {
					LOG.error(LOG_PREFIX + "\t Json exception while iterating over CIS list \t msg: "+e.getMessage() +" cause: "+e.getCause(),e);
				}
				jsonArray.put(jsonObject);
			}
			/*
			if (cisList.isEmpty()){
				try {
					for (int i=1; i <= 4; i++){
						jsonObject = new JSONObject();
						jsonObject.put("name", "name"+Integer.toString(i));
						jsonObject.put("id", Integer.toString(i));
						jsonArray.put(jsonObject);
					}
				} catch (JSONException e) {
					LOG.error(LOG_PREFIX +"exception caught while getting user details",e);
				}
			}*/
			
			try{
				String jid = commManager.getIdManager().getThisNetworkNode().getJid();
				
				userDetailsObject.put("messages", jsonArray);
				
				if (societiesRegistryBean.isMockEntityActive()){
					userDetailsObject.put("userId",societiesRegistryBean.getMockEntity());
				}else{
					userDetailsObject.put("userId",jid);
				}
				
				LOG.debug(LOG_PREFIX+ " \t User details Json object "+userDetailsObject.toString());
			}catch (JSONException e) {
				LOG.error(LOG_PREFIX + "\t Json exception msg: "+e.getMessage() +" cause: "+e.getCause(),e);
			}
			
		}catch (Exception e) {
			LOG.error(LOG_PREFIX + "\t general exception msg: "+e.getMessage() +" cause: "+e.getCause(),e);
		}
		return userDetailsObject.toString();
	}
	
	/**
	 * Executing rest call to post the given message (as Json object) 
	 * @param jsonMsg
	 */
	
	private void postMessageInternal(String jsonMsg){
		
		LOG.debug(LOG_PREFIX + " enter postMessageInternal");
		
		String responseString="";
		int statusCode;
		try {
			HttpPost httpPostRequest = new HttpPost(societiesRegistryBean.getServerURL()+"vg/message/");
			
			StringEntity entity = new StringEntity(jsonMsg, HTTP.UTF_8);
			entity.setContentType("application/json");
			httpPostRequest.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			
			LOG.debug(LOG_PREFIX +"before executing rest call to: "+httpPostRequest.getURI());
			HttpResponse response = client.execute(httpPostRequest);
			statusCode = response.getStatusLine().getStatusCode();
			
			if (response.getEntity() != null && response.getEntity().getContent() != null){
				BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				while (bf.ready()){
					responseString += bf.readLine();
				}
			}
			LOG.info(LOG_PREFIX +"after executing rest call to: "+httpPostRequest.getURI()+ "\t Json" +jsonMsg+"\t status code- "+statusCode+"\t response: "+responseString);
			
			
		} catch (UnsupportedEncodingException e) {
			LOG.error(LOG_PREFIX +"exception caught while posting the following msg: \n"+jsonMsg+"\n "+e.getMessage(),e);
		} catch (IOException e) {
			LOG.error(LOG_PREFIX +"exception caught while posting the following msg: \n"+jsonMsg+"\n "+e.getMessage(),e);
		}
		
		LOG.debug(LOG_PREFIX + " finish postMessageInternal");
	}
	
	/**
	 * Getting all messages filtered with the given parameters
	 * @param userID - user id (JID)
	 * @param cis - CIS the user want to filter by
	 * @param number - message id
	 * @return
	 */
	
	
	
	@RequestMapping(value="/getMsg.html",method = RequestMethod.GET)
	public @ResponseBody String getMessages(@RequestParam String userID,@RequestParam String cis, @RequestParam String number){
		
		String result = getMessagesInternal(userID,cis,number);
		GetBackgroundImageTask imageTask = new GetBackgroundImageTask();
		imageTask.start();
		
		return result;
	}
	
	/**
	 * 
	 * @param zoneId
	 * @return
	 */
	@RequestMapping(value="/getZoneDetails.html",method = RequestMethod.GET)
	public @ResponseBody String getZoneDetails(@RequestParam String zoneId){
		LOG.info(LOG_PREFIX+ "  Start 'getZoneDetails'");
		if (zoneId == null || zoneId.trim().length() == 0){
			LOG.warn("in getZoneDetails method; zoneId is null or empty zoneId='"+zoneId+"'");
			return "";
		}
		
		String url =  societiesRegistryBean.getPzQueriesURL() + "/Admin/zone/"+zoneId;
		LOG.info(LOG_PREFIX+ " creating URL -->  "+url);
	
		String response  = perfromGetHttpRequest(url);
		
		if (response == null ||  response.length() == 0){
			LOG.warn(LOG_PREFIX+ "  no response for '"+url+"' !!" );
		}else{
			LOG.info(LOG_PREFIX+ "  response for '"+url+"' is : "+response);
		}
		
		
		
		LOG.info(LOG_PREFIX+ "  End 'getZoneDetails'");
		return "";
	}
	
	
	/**
	 * Getting messages from the centralized VG server
	 * @param userId
	 * @param cis
	 * @param number
	 * @return
	 */
	private String getMessagesInternal(String userId, String cis, String number){
 		LOG.debug(LOG_PREFIX + " enter getMessagesInternal");
		
		
		String retResponseString = "";
		
		String url = "";
		try {
			
			url =  societiesRegistryBean.getServerURL() + "vg/message/";
			String cisParam = URLEncoder.encode(cis, "UTF-8").replace("+", "%20");
			String userIdParam = URLEncoder.encode(userId, "UTF-8").replace("+", "%20");
			url = url + userIdParam+"/"+ cisParam+ "/"+number;
			
			String responseString="";
			responseString = perfromGetHttpRequest(url);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("data", responseString);
			
			String currentZoneId = extractZoneId(responseString);
			
			String logMsg = "";
			synchronized (VGProxy.class) {
				if ( !mCurrentCis.equals(cis) || !mCurrentZone.equals(currentZoneId) ){
					mCisOfImage = mCurrentCis;
					mBackgroundFile = null;
					
					String imageNameFromCache = mImagesInZonePool.get(cis+"_"+currentZoneId);
					mBackgroundFileName = imageNameFromCache != null ? imageNameFromCache : "";
					
					LOG.info(LOG_PREFIX + " going to reset mBackgroundFileName");
				}
				mCurrentCis = cis;
				mCurrentZone = currentZoneId;
				
				logMsg += "mBackgroundFileName = '"+mBackgroundFileName+"'\t mCurrentZone = '"+mCurrentZone +"'\t mCisOfImage = '"+mCisOfImage+"'"; 
				
				jsonObject.put("imgName",mBackgroundFileName);
				jsonObject.put("zoneId",currentZoneId);
				jsonObject.put("cis",cis);
			}
			LOG.info(LOG_PREFIX + " in 'getMessagesInternal' ; Members values: "+logMsg);
			
			retResponseString = jsonObject.toString();
			
	    }catch (Exception e) {
	    	LOG.error(LOG_PREFIX + " exception in method 'getMessagesInternal' ; generated URL is "+url,e);
		}
		
		LOG.debug(LOG_PREFIX + " finish 'getMessageInternal'; Return value: "+retResponseString);
	    return retResponseString;
	}
	
	private String extractZoneId(String json){
		String zoneId = "";
		try{
			JSONArray jsonArray=null;
			try{
				if (json.length() > 3){
					jsonArray = new JSONArray(json);
				}
			}catch(Exception e){
				LOG.error(LOG_PREFIX + " exception in method 'extractZoneId' ; couldn't parse json String to json array ; Json = "+json);
			}
			
			if (jsonArray != null && jsonArray.length() > 0){
				JSONArray zones = ((JSONArray)  ((JSONObject)jsonArray.get(0)).get("zoneId"));
				if (zones.length() > 0){
					zoneId = zones.getString(0);
				}
			}
		}catch(Exception e){
			LOG.error(LOG_PREFIX + " exception in method 'extractZoneId' ; couldn't extract zone Id from msg. Setting zone id to ''",e);
		}
		return zoneId;
	}

	
	private class UploadTask extends Thread{
		byte[] data;
		String fileName;
		String cis;
		String urlServer;
		String zoneId;
		
		public UploadTask(byte[] data, String fileName,String cis, String zoneId, String urlServer){
			this.data = data;
			this.fileName = fileName;
			this.cis = cis;
			this.urlServer = urlServer;
			this.zoneId = zoneId;
			
			LOG.info(LOG_PREFIX + " upload task was created with parameters; data length "+data.length + " ; file name = "+this.fileName+" ; cis = "+this.cis+" ; zoneId = "+zoneId);
		}
		
		@Override
		public void run() {
			upload(data,fileName,cis, zoneId,urlServer);
		}
		
	}

	
	private void upload(byte[] data, String fileName,String cis, String zone,String urlServer){
    	HttpURLConnection connection = null;
    	DataOutputStream outputStream = null;
    	
    	
    	String lineEnd = "\r\n";
    	String twoHyphens = "--";
    	String boundary =  "*****";

    	int bytesRead, bytesAvailable, bufferSize;
    	byte[] buffer;
    	int maxBufferSize = 1*1024*1024;
    	FileOutputStream fos = null;
    	
    	if (fileName == null || fileName.length() == 0 || cis == null || cis.length() == 0 || zone == null || zone.length() == 0 ){
    		LOG.error(LOG_PREFIX+ " ERROR Can't upload image; Error in input;  fileName="+fileName + " \t cis="+cis+" \t zone = "+zone );
    		return;
    	}
    	
    	long ts = System.currentTimeMillis();
    	LOG.info(LOG_PREFIX + " START upload of '"+ fileName+"' to WAS; Cis '"+cis+"'  zone '"+zone+"'");
    	try {
	    	
			File file = new File(fileName);
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
	    	
			FileInputStream fileInputStream = new FileInputStream(file);
	    	
	    	
	    	try {
				cis = URLEncoder.encode(cis, "UTF-8").replace("+", "%20");
				zone = URLEncoder.encode(zone, "UTF-8").replace("+", "%20");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
	    	
	    	URL url = new URL(societiesRegistryBean.getServerURL()+"UploadServlet?cis="+cis+"&zoneId="+zone);
	    	connection = (HttpURLConnection) url.openConnection();
	    
	    	// Allow Inputs & Outputs
	    	connection.setDoInput(true);
	    	connection.setDoOutput(true);
	    	connection.setUseCaches(false);
	
	    	// Enable POST method
	    	connection.setRequestMethod("POST");
	
	    	connection.setRequestProperty("Connection", "Keep-Alive");
	    	connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	    
	    	outputStream = new DataOutputStream( connection.getOutputStream() );
	    
	    	outputStream.writeBytes(twoHyphens + boundary + lineEnd);
	    
	    	
	    	outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
	    	outputStream.writeBytes(lineEnd);
	    
	    	bytesAvailable = fileInputStream.available();
	    	bufferSize = Math.min(bytesAvailable, maxBufferSize);
	    	buffer = new byte[bufferSize];
	    
	    	// Read file
	    	bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	    	
	    	while (bytesRead > 0)  	{
		    	outputStream.write(buffer, 0, bufferSize);
		    	bytesAvailable = fileInputStream.available();
		    	bufferSize = Math.min(bytesAvailable, maxBufferSize);
		    	bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	    	}
	    
	    	outputStream.writeBytes(lineEnd);
	    	outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
	    	// Responses from the server (code and message)
	    	int serverResponseCode = connection.getResponseCode();
	    	String serverResponseMessage = connection.getResponseMessage();
	    	
	    	LOG.info(LOG_PREFIX + " ServerResponseMessage  "+ serverResponseCode + "  " + serverResponseMessage);
	    	
	    	fileInputStream.close();
	    	outputStream.flush();
	    	outputStream.close();
	    	fos.close();
    	}catch (Exception ex) {
    		LOG.error(LOG_PREFIX+ "  " + ex.getMessage(),ex);
    		
    	//Exception handling
    	}
    	LOG.info(LOG_PREFIX+ " END upload of '"+ fileName+"' to WAS; in "+ ((float)(System.currentTimeMillis()-ts)) /1000 + " seconds" );
	}
	
	
	private class GetBackgroundImageTask extends Thread{
		@Override
		public void run() {
			String cis= null;
			String zoneId = null;
			String backgroundImageName;
			byte[] currentImageObject;
			synchronized (VGProxy.class) {
				cis = mCurrentCis;
				zoneId = mCurrentZone;
				backgroundImageName = mBackgroundFileName;
				currentImageObject = mBackgroundFile;
			}
			
			if (cis == null || cis.length() ==0 || zoneId == null || zoneId.length() == 0 ){
				LOG.warn(LOG_PREFIX+ "  in GetImageNameTask , mCurrentCis='"+mCurrentCis + "'  mCurrentZone='"+mCurrentZone +"--> can't pefrom task");
				return;
			}
			
			String result = "", cisEncoded="",zoneIdEncoded="";
			
			try {
				cisEncoded = URLEncoder.encode(cis,"UTF-8").replace("+", "%20");
				zoneIdEncoded = URLEncoder.encode(zoneId,"UTF-8").replace("+", "%20");
				
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			
			result = perfromGetHttpRequest(societiesRegistryBean.getServerURL()+"vg/message/cis/"+cisEncoded+"/zoneId/"+zoneIdEncoded);
			
			String imageName = "";
			try{
				JSONObject json = new JSONObject(result);
				imageName = json.getString("imageName");
			}catch(Exception e){
				System.out.println(e);
			}
			
			if (imageName.length() == 0){
				LOG.info(LOG_PREFIX+ "  in 'GetBackgroundImageTask' ; image name from VG server is empty");
				return;
			}else if (backgroundImageName.equals(imageName) && currentImageObject != null && currentImageObject.length > 0){
				LOG.info(LOG_PREFIX+ "  in 'GetBackgroundImageTask' ; Image name in local virgo server equals image name on WAS - no need to update");
				
				synchronized (VGProxy.class) {
					mImagesInZonePool.put(cis+"_"+zoneId,backgroundImageName);
				}
				return;
			}
			
			
			byte[] imageBytesArray=null;
			try{
				String urlStr = societiesRegistryBean.getServerURL()+"upload/"+imageName;
				 URL url = new URL(urlStr); //you can write here any link
				 
				 LOG.info(LOG_PREFIX+ "  going to download image: "+urlStr);
				 
				 
	             /* Open a connection to that URL. */
	             URLConnection ucon = url.openConnection();
	
	             /*
	              * Define InputStreams to read from the URLConnection.
	              */
	             InputStream is = ucon.getInputStream();
	             BufferedInputStream bis = new BufferedInputStream(is);
	
	             /*
	              * Read bytes to the Buffer until there is nothing more to read(-1).
	              */
	             ByteArrayBuffer baf = new ByteArrayBuffer(5000);
	             int current = 0;
	             while ((current = bis.read()) != -1) {
	                baf.append((byte) current);
	             }
	             
	             imageBytesArray = baf.toByteArray();
	             LOG.info(LOG_PREFIX+ "  finished download image , size: "+ imageBytesArray.length  +"bytes;  URL " + urlStr);
			
			}catch(Exception e){
				LOG.error(LOG_PREFIX+ "  "+ e.getMessage(), e);
			}
             
            String newMembersVal = "";
			synchronized (VGProxy.class) {
				mBackgroundFileName = imageName;
				mBackgroundFile = imageBytesArray;
				mCisOfImage = cis;
				mImagesInZonePool.put(cis+"_"+zoneId,mBackgroundFileName);
				
				newMembersVal += "the image CIS "+mCisOfImage + "  fileName "+mBackgroundFileName;
			}
			
			LOG.info(LOG_PREFIX+ " after download new Members: "+newMembersVal);
		}
	}
	
	@PreDestroy
	public void cleanUp() throws Exception {
	  System.out.println("Spring Container is destroy! Customer clean up");
	  
	  
	  if(getImgTimerTask != null){
		  getImgTimerTask.cancel();
	  }
	}
	
	/**
	 * Internal Helper to perform HTTP request
	 * @param url
	 * @return
	 */
	private String perfromGetHttpRequest(String url){
		String responseString="";
		int statusCode;
		try{
			LOG.debug(LOG_PREFIX+ " before executing HTTP get- url:\t "+ url);
			HttpGet httpGetRequest = new HttpGet(url);
			httpGetRequest.addHeader("accept", "application/json");
	    	HttpClient httpclient = new DefaultHttpClient();
	    	HttpResponse response = httpclient.execute(httpGetRequest);
	    	statusCode = response.getStatusLine().getStatusCode();
	    	if (response.getEntity() != null && response.getEntity().getContent() != null){
				BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String temp;
				while ((temp = bf.readLine()) != null) {
					responseString+= temp;	
				}
				
			}
	    	LOG.info(LOG_PREFIX +"after executing rest call to: "+httpGetRequest.getURI()+ "\t status code- "+statusCode+"\t response: "+responseString);
		}catch (Exception e) {
			LOG.error(LOG_PREFIX + " exception in method 'perfromGetHttpRequest' ; generated URL is "+url,e);
		}
		
		return responseString;
	}
	
	public void setICisManager(ICisManager cisManager){
		this.cisManager = cisManager;
	}
	
	public void setServerURL(String serverURL) {
		this.SERVER_URL = serverURL;
		
	}
	
	public void setCommManager(ICommManager commManager){
		this.commManager = commManager;
	}


	public SocietiesRegistryBean getSocietiesRegistryBean() {
		return societiesRegistryBean;
	}

	@Autowired
	public void setSocietiesRegistryBean(SocietiesRegistryBean societiesRegistryBean) {
		this.societiesRegistryBean = societiesRegistryBean;
	}
	
	/*
	void action(final HttpServletRequest request) {
	    final String paramValue = request.getSession().getServletContext().getInitParameter("paramName");
	    System.out.println(paramValue);
	}*/

	
	
}
