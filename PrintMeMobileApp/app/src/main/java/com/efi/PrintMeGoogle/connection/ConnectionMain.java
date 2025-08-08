package com.efi.PrintMeGoogle.connection;

import android.util.Log;

import java.util.HashMap;

public class ConnectionMain {

	
	public static Object SendRequest(HashMap<String,Object> params) throws Exception{
		//Log.i("ConnectionMain","sendReq");
		HashMap<String, Object> uploadFiles = params.get(Constants.UPLOAD_FILE) == null ? null : (HashMap<String, Object>) params.get(Constants.UPLOAD_FILE);
		IConnection connectionObject;
		if(uploadFiles != null){
//			boolean isShareFlow = uploadFiles.get(Constants.IS_SHARE_FLOW) == null ? true: Boolean.parseBoolean((String) uploadFiles.get(Constants.IS_SHARE_FLOW));
//			String fname = uploadFiles.get("fileName").toString();
//			String fileExtension = CommonUtils.getFileExtension(fname);
//			//Log.d("ConnectionMain",fileExtension);

//			if(isShareFlow){
//				connectionObject = new FileUploadUtility(params);
//			}
//			else {
//				return HandleNativePrintUpload(params);
//			}
			connectionObject = new FileUploadUtility(params);

		}
		else{
			connectionObject = new HttpsUrlConnection(params);
		}
		//connectionObject = new HttpsUrlConnection(params);
		if(connectionObject == null)
			return "";
		return connectionObject.SendRequest();
	}

	private static Object HandleNativePrintUpload(HashMap<String, Object> params)
	{
		IConnection connectionObject;
		try
		{
			connectionObject = new HttpsUrlConnection(params);
			Response response = (Response) connectionObject.SendRequest();
			if(response.statusCode == 201)
				return response;
			else if(response.statusCode == 500)
				{
					connectionObject = new FileUploadUtility(params);
					response = (Response) connectionObject.SendRequest();
					return response;
				}

		}catch (Exception e){
			e.printStackTrace();
		}
		return "";

	}

}
