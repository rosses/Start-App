/**
	com.lampa.startapp
	https://github.com/lampaa/com.lampa.startapp
	
	Phonegap plugin for check or launch other application in android device (iOS support).
	bug tracker: https://github.com/lampaa/com.lampa.startapp/issues
*/
package com.lampa.startapp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import java.util.Iterator;
import android.net.Uri;
import java.lang.reflect.Field;
import android.content.ActivityNotFoundException;
import android.util.Log;
import android.os.Bundle;

public class startApp extends CordovaPlugin {

	public static final String TAG = "startApp";
    public startApp() { }

	private boolean NO_PARSE_INTENT_VALS = false;
    public CallbackContext callbackContext;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    	this.callbackContext = callbackContext;

        if (action.equals("start")) {
            this.start(args, callbackContext);
        }
		else if(action.equals("check")) {
			this.check(args, callbackContext);
		}
		else if(action.equals("getExtras")) {
			this.getExtras(callbackContext);
		}
		else if(action.equals("getExtra")) {
			this.getExtra(args, callbackContext);
		}
		
		return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------
    /**
     * startApp
     */
    public void start(JSONArray args, CallbackContext callback) {
		Intent LaunchIntent;
		JSONObject params;
		JSONArray flags;
		JSONArray component;
		
		JSONObject extra;
		JSONObject key_value;
		String key;
		String value;
		
		int i;
		
		try {
			
			
			if (args.get(0) instanceof JSONObject) {
				params = args.getJSONObject(0);
				
        		LaunchIntent = new Intent();
				LaunchIntent.setAction(params.getString("action"));	

				if(params.has("RUT")) {
					LaunchIntent.putExtra("RUT",params.getInt("RUT"));	
				}
				if(params.has("DV")) {
					LaunchIntent.putExtra("DV", params.getString("DV").charAt(0));	
				}

				LaunchIntent.putExtra("INTENTOS", 3);
				LaunchIntent.putExtra("COLOR_PRIMARY", "#FF9900");
				LaunchIntent.putExtra("COLOR_PRIMARY_DARK", "#FF0000");
				LaunchIntent.putExtra("TITLE", "ABASTIBLE HUELLA CHECK");
				LaunchIntent.putExtra("SUBTITLE", "");
				LaunchIntent.putExtra("SKIP_TERMS", false);
				LaunchIntent.putExtra("PREVIRED", false);

				/**
				 * launch intent
				 */
				//if(params.has("intentstart") && "startActivityForResult".equals(params.getString("intentstart"))) {
					cordova.setActivityResultCallback(this);
					cordova.getActivity().startActivityForResult(LaunchIntent, 18);
				//}
				/*
				if(params.has("intentstart") && "sendBroadcast".equals(params.getString("intentstart"))) {
					cordova.getActivity().sendBroadcast(LaunchIntent);	
				}
				else {
					cordova.getActivity().startActivity(LaunchIntent);	
				}
				*/
				
				//callback.success();
			}
			else {
				callback.error("Incorrect params, array is not array object!");
			}
		} 
		catch (Exception e) {
			callback.error("Exception: " + e.getMessage());
			e.printStackTrace();
		}
    }

    /**
     * checkApp
     */	 
	public void check(JSONArray args, CallbackContext callback) {
		JSONObject params;
		
		try {
			if (args.get(0) instanceof JSONObject) {
				params = args.getJSONObject(0);
		
		
				if(params.has("package")) {
					PackageManager pm = cordova.getActivity().getApplicationContext().getPackageManager();
					
					/**
					 * get package info
					 */
					PackageInfo PackInfo = pm.getPackageInfo(params.getString("package"), PackageManager.GET_ACTIVITIES);
						
					/**
					 * create json object
					 */
					JSONObject info = new JSONObject();
						
					info.put("versionName", PackInfo.versionName);
					info.put("packageName", PackInfo.packageName);
					info.put("versionCode", PackInfo.versionCode);
					info.put("applicationInfo", PackInfo.applicationInfo);
						
					callback.success(info);
				}
				else {
					callback.error("Value \"package\" in null!");
				}
			}
			else {
				callback.error("Incorrect params, array is not array object!");
			}
		} catch (JSONException e) {
			callback.error("json: " + e.toString());
			e.printStackTrace();
		}
		catch (NameNotFoundException e) {
			callback.error("NameNotFoundException: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * getExtras
	 */
	public void getExtras(CallbackContext callback) {
		try {
			Bundle extras = cordova.getActivity().getIntent().getExtras(); 
			JSONObject info = new JSONObject();

			if (extras != null) {
				for (String key : extras.keySet()) {
					info.put(key, extras.get(key).toString());
				}
			}
			
			callback.success(info);
		}
		catch(JSONException e) {
			callback.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * getExtra
	 */
	public void getExtra(JSONArray args, CallbackContext callback) {
		try {
			String extraName = parseExtraName(args.getString(0));
			Intent extraIntent = cordova.getActivity().getIntent();

			if(extraIntent.hasExtra(extraName)) {
				String extraValue = extraIntent.getStringExtra(extraName);
				
				if (extraValue == null) {
					extraValue = ((Uri) extraIntent.getParcelableExtra(extraName)).toString();
				}

				callback.success(extraValue);
			}
			else {
				callback.error("extra field not found");	
			}
		}
		catch(JSONException e) {
			callback.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * static functions
	 */
	static String parseExtraName(String extraName) {
		String parseIntentExtra = extraName;
		
		try {
			parseIntentExtra = getIntentValueString(extraName);
			Log.i(TAG, parseIntentExtra);
		}
		catch(NoSuchFieldException e) {
			parseIntentExtra = extraName;	
		}
		catch(IllegalAccessException e) {
			e.printStackTrace();
			return extraName;
		}
		
		Log.e(TAG, parseIntentExtra);
		
		return parseIntentExtra;
	}
	
	static String getIntentValueString(String flag) throws NoSuchFieldException, IllegalAccessException {
		Field field = Intent.class.getDeclaredField(flag);
		field.setAccessible(true);

		return (String) field.get(null);
	}
	
	static int getIntentValue(String flag) throws NoSuchFieldException, IllegalAccessException {
		Field field = Intent.class.getDeclaredField(flag);
		field.setAccessible(true);
		
		return field.getInt(null);
	}

   /**
     * For start to be able to return results to our app from the intent we need to wait 
     * for the result to return. Then only call the callback with our result data.
     */	 
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
		try {
        
	        JSONObject info = new JSONObject();
	        info.put("resultado", "UNKNOWN");
	        info.put("requestCode", requestCode);
	        info.put("resultCode", resultCode);
	        
            if (resultCode == Activity.RESULT_OK) {
            	info.put("resultado", "OK");
                Bundle extras = data.getExtras();

                if (extras != null) {
                	info.put("cdv", "extras is not null");
                	String msg = "";
	                for (Object obj : data.getExtras().keySet()) {
	                    Object value = extras.get(obj.toString());
	                    info.put(obj.toString(), value);
	                }
	                info.put("msg",msg);
                }
                this.callbackContext.success(info);
            } 
            else {
            	info.put("resultado", "NO OK");
                this.callbackContext.error(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	/*

	protected override void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "requestCode");
		Log.i(TAG, requestCode);
		Log.i(TAG, "resultCode");
		Log.i(TAG, resultCode);

		Bundle result = data.getExtras();
		String msg = "";
        for (Object obj : data.getExtras().keySet()) {
            Object value = result.get(obj.toString());
            msg = msg + String.format("%s : %s\n\n", new Object[]{key, value});
            Log.d(TAG, String.format("%s : %s", new Object[]{key, value}));
        }

        return;

	}
	*/
	
}
