package com.example.healthcare;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class registerActivity extends Activity {
	private static final String targetNameSpace = MainActivity.targetNameSpace;
	private static final String WSDL = MainActivity.WSDL;
	private static final String methodName = "insertUserRegisterInfo ";
	private Button registerButton;
	private EditText loginNameEditText, passwordEditText, realNameEditText;
	private RadioGroup group;
	private RadioButton rb;
	private String isRegisterOK = null;
	private String birthday;
	private String sex;
	private int radioButtonId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		registerButton = (Button) findViewById(R.id.registerButton);
		loginNameEditText = (EditText) findViewById(R.id.loginNameEditText);
		passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		realNameEditText = (EditText) findViewById(R.id.realNameEditText);
		group = (RadioGroup) registerActivity.this.findViewById(R.id.radioGroup);
		radioButtonId =group.getCheckedRadioButtonId();
		rb = (RadioButton) findViewById(radioButtonId);
		sex = rb.getText().toString();
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				radioButtonId = arg0.getCheckedRadioButtonId();
				rb = (RadioButton) findViewById(radioButtonId);
				sex = rb.getText().toString();
			}
		});

		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
		datePicker.init(year, month, day, new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day) {
				birthday = year + "-" + month + "-" + day;
			}
		});

		registerButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (loginNameEditText.getText().toString().trim().equals("")
						|| passwordEditText.getText().toString().trim().equals("")) {
					Toast.makeText(registerActivity.this,
							getResources().getString(R.string.cannotBeEmpty), Toast.LENGTH_SHORT)
							.show();
				} else {
					new NetAsyncTask().execute();
				}
			}
		});
	}

	class NetAsyncTask extends AsyncTask<Object, Object, String> {
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				if (isRegisterOK.equals("true")) {
					Toast.makeText(registerActivity.this,
							getResources().getString(R.string.registerSuccess), Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(registerActivity.this,
							getResources().getString(R.string.registerFailed), Toast.LENGTH_SHORT)
							.show();
				}
			}
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Object... params) {
			SoapObject soapObject = new SoapObject(targetNameSpace, methodName);
			soapObject.addProperty("LoginName", loginNameEditText.getText().toString());
			soapObject.addProperty("Password", passwordEditText.getText().toString());
			soapObject.addProperty("RealName", realNameEditText.getText().toString());
			soapObject.addProperty("Sex", sex);
			soapObject.addProperty("DateofBirth", birthday);
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(SoapEnvelope.VER11);

			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			try {
				httpSE.call(targetNameSpace + methodName, envelop);
				SoapObject resultObj = (SoapObject) envelop.bodyIn;
				isRegisterOK = resultObj.getProperty(0).toString();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
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
