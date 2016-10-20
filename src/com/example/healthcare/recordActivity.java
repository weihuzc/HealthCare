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

public class recordActivity extends Activity {
	private static final String targetNameSpace = MainActivity.targetNameSpace;
	private static final String WSDL = MainActivity.WSDL;
	private static final String methodName = "selectUserMeassureResult ";

	private List<Map<String, String>> listItems;
	private ListView recordListView;
	static int[] mSBP = new int[30];
	static int[] mDBP = new int[30];
	static int[] mHR = new int[30];
	static String[] mDateTime = new String[30];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		listItems = new ArrayList<Map<String, String>>();
		recordListView = (ListView) findViewById(R.id.recordListView);

		for (int i = 0; i < 30; i++) {
			mSBP[i] = mDBP[i] = mHR[i] = 0;
			mDateTime[i] = null;
		}

		Map<String, String> tempListItem = new HashMap<String, String>();
		// tempListItem.put("loginName", "µÇÂ¼Ãû");
		tempListItem.put("sBP", getResources().getString(R.string.SBP));
		tempListItem.put("dBP", getResources().getString(R.string.DBP));
		tempListItem.put("hR", getResources().getString(R.string.HR));
		tempListItem.put("dateTime", getResources().getString(R.string.time));
		listItems.add(tempListItem);

		new NetAsyncTask().execute();
	}

	class NetAsyncTask extends AsyncTask<Object, Object, String> {
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				SimpleAdapter simpleAdapter = new SimpleAdapter(recordActivity.this, listItems,
						R.layout.record_item, new String[] { "sBP", "dBP", "hR", "dateTime" },
						new int[] { R.id.sBP, R.id.dBP, R.id.hR, R.id.dateTime });
				recordListView.setAdapter(simpleAdapter);
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
				int j = 0;
				for (int i = 0; i < count; i += 5) {
					Map<String, String> listItem = new HashMap<String, String>();
					// listItem.put("loginName",
					// resultObj.getProperty(i).toString().trim());
					listItem.put("sBP", resultObj.getProperty(i + 2).toString());
					listItem.put("dBP", resultObj.getProperty(i + 3).toString());
					listItem.put("hR", resultObj.getProperty(i + 4).toString());
					listItem.put("dateTime", resultObj.getProperty(i + 1).toString());
					listItems.add(listItem);
					mSBP[j] = Integer.valueOf(resultObj.getProperty(i + 2).toString()).intValue();
					mDBP[j] = Integer.valueOf(resultObj.getProperty(i + 3).toString()).intValue();
					mHR[j] = Integer.valueOf(resultObj.getProperty(i + 4).toString()).intValue();
					mDateTime[j] = resultObj.getProperty(i + 1).toString();
					j++;
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
