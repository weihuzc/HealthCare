package com.example.healthcare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class informationActivity extends Activity {
	private static final String targetNameSpace = MainActivity.targetNameSpace;
	private static final String WSDL = MainActivity.WSDL;
	private static final String methodName = "selectUserRegisterInfo ";
	private List<Map<String, String>> listItems;
	private ListView informationListView;
	private String[] information = new String[6];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);
		listItems = new ArrayList<Map<String, String>>();
		informationListView = (ListView) findViewById(R.id.informationListView);
		SimpleAdapter simpleAdapter = new SimpleAdapter(informationActivity.this, listItems,
				R.layout.information_item, new String[] { "title", "content" },
				new int[] { R.id.title, R.id.content});
		informationListView.setAdapter(simpleAdapter);
		
		new NetAsyncTask().execute();
	}

	class NetAsyncTask extends AsyncTask<Object, Object, String> {
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				Map<String, String> map= new HashMap<String, String>();
				map.put("title", getResources().getString(R.string.loginName));
				map.put("content", information[0]);
				listItems.add(map);
				map= new HashMap<String, String>();
				map.put("title", getResources().getString(R.string.realName));
				map.put("content", information[2]);
				listItems.add(map);
				map= new HashMap<String, String>();
				map.put("title", getResources().getString(R.string.sex));
				map.put("content", information[3]);
				listItems.add(map);
				map= new HashMap<String, String>();
				map.put("title", getResources().getString(R.string.birthday));
				int l = information[4].length();
				if (l < 8) {
					l = 0;
				} else {
					l -= 8;
				}
				map.put("content", information[4].substring(0, l));
				listItems.add(map);
				map= new HashMap<String, String>();
				map.put("title", "UserID");
				map.put("content", information[5]);
				listItems.add(map);							
			}
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Object... params) {
			SoapObject soapObject = new SoapObject(targetNameSpace, methodName);
			soapObject.addProperty("LoginName", MainActivity.loginName);
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(SoapEnvelope.VER11);

			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			try {
				httpSE.call(targetNameSpace + methodName, envelop);
				SoapObject resultObj = (SoapObject) envelop.getResponse();
				int count = resultObj.getPropertyCount();
				for (int i = 0; i < count; i++) {
					information[i] = resultObj.getProperty(i).toString();
					if (information[i].equals("anyType{}")) {
						information[i] = "";
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
				return "IOException";
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return "XmlPullParserException";
			}
			return "success";
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
