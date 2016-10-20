package com.example.healthcare;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

//程序入口
public class MainActivity extends Activity {
	static final String targetNameSpace = "http://192.168.1.109/";
	static final String WSDL = "http://192.168.1.109/HealthCare.asmx?WSDL";

	private static final String methodName = "verifyUserLoginInfo ";
	private SharedPreferences mSharedPreferencesData;
	static TabHost tabHost;
	private MenuItem mItem;
	private ActionBar actionBar;
	private BluetoothAdapter mBtAdapter;
	private EditText loginNameEditText, passwordEditText;
	private ArrayAdapter<String> mDevicesArrayAdapter;
	private ListView deviceListView;
	private Button searchButton, recordButton;
	private clientThread mClientThread = null;
	static BluetoothSocket socket = null;
	private BluetoothDevice device = null;
	private ProgressDialog connectDialog;
	private ProgressBar searchProgressBar;
	static String BlueToothAddress = null;
	private String address;
	static String loginName;
	private String isLoginOK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		if (!mBtAdapter.isEnabled()) {
			mBtAdapter.enable();
		}
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}
		mBtAdapter.startDiscovery();
		mDevicesArrayAdapter.clear();
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().toString().equals("TKBPH01")) {
					mDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
			}
		}

		actionBar = getActionBar();
		actionBar.show();
		mSharedPreferencesData = getSharedPreferences("MSHAREDPREFERENCES", 0);
		loginName = mSharedPreferencesData.getString("LOGINNAME", "none");

		searchProgressBar = (ProgressBar) findViewById(R.id.searchProgressBar);
		searchProgressBar.setVisibility(View.INVISIBLE);

		searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!mBtAdapter.isEnabled()) {
					mBtAdapter.enable();
				}
				mDevicesArrayAdapter.clear();
				Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device : pairedDevices) {
						mDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
					}
				}
				if (mBtAdapter.isDiscovering()) {
					mBtAdapter.cancelDiscovery();
				}
				mBtAdapter.startDiscovery();
			}
		});

		recordButton = (Button) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, mTabActivity.class);
				startActivity(intent);
			}
		});

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		this.registerReceiver(mReceiver, intentFilter);

		deviceListView = (ListView) findViewById(R.id.deviceListView);
		deviceListView.setAdapter(mDevicesArrayAdapter);
		deviceListView.setFastScrollEnabled(true);
		deviceListView.setOnItemClickListener(mDeviceClickListener);
	}

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			String info = ((TextView) v).getText().toString();
			if (mBtAdapter.isDiscovering()) {
				mBtAdapter.cancelDiscovery();
			}
			shutdownClient();
			connectDialog = ProgressDialog.show(MainActivity.this,
					getResources().getString(R.string.alert),
					getResources().getString(R.string.connecting));
			connectDialog.setCancelable(true);
			connectDialog.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface arg0) {
					shutdownClient();
				}
			});

			address = info.substring(info.length() - 17);
			MainActivity.BlueToothAddress = address;
			device = mBtAdapter.getRemoteDevice(address);
			mClientThread = new clientThread();
			mClientThread.start();
			// MainActivity.tabHost.setCurrentTab(1);
		}
	};

	private class clientThread extends Thread {
		public void run() {
			try {
				socket = device.createRfcommSocketToServiceRecord(UUID
						.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				socket.connect(); // 蓝牙连接

				Message msg = new Message();
				msg.what = 0;
				msg.obj = getResources().getString(R.string.connected);
				LinkDetectedHandler.sendMessage(msg);
			} catch (IOException e) {
				shutdownClient(); // 关闭客户端
				Log.e("connect", "", e);
				Message msg = new Message();
				msg.obj = getResources().getString(R.string.connectionFailed);
				msg.what = 1;
				LinkDetectedHandler.sendMessage(msg);
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler LinkDetectedHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			connectDialog.dismiss();
			Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			if (msg.what == 0) {
				Intent intent = new Intent(MainActivity.this, meassureActivity.class);
				startActivity(intent);
			}
		}
	};

	private void shutdownClient() {
		new Thread() {
			public void run() {
				if (mClientThread != null) {
					mClientThread.interrupt();
					mClientThread = null;
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					socket = null;
				}
			};
		}.start();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				if (state == BluetoothAdapter.STATE_ON) {
					Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
					if (pairedDevices.size() > 0) {
						for (BluetoothDevice device : pairedDevices) {
							if (device.getName().toString().equals("TKBPH01")) {
								mDevicesArrayAdapter.add(device.getName() + "\n"
										+ device.getAddress());
							}
						}
					}
					if (mBtAdapter.isDiscovering()) {
						mBtAdapter.cancelDiscovery();
					}
					mBtAdapter.startDiscovery();
				} else if (state == BluetoothAdapter.STATE_OFF) {
					mDevicesArrayAdapter.clear();
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				searchProgressBar.setVisibility(View.VISIBLE);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				searchProgressBar.setVisibility(View.INVISIBLE);
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED
						&& device.getName().toString().equals("TKBPH01")) {
					mDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
			} else if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				try {
					ClsUtils.setPin(btDevice.getClass(), btDevice, "919290"); // 手机和蓝牙采集器配对
					ClsUtils.createBond(btDevice.getClass(), btDevice);
					ClsUtils.cancelPairingUserInput(btDevice.getClass(), btDevice);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.sureToQuit))
				.setPositiveButton(getResources().getString(R.string.quit),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								MainActivity.this.finish();
							}
						})
				.setNegativeButton(getResources().getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
	}

	@Override
	protected void onDestroy() {
		/* unbind from the service */
		super.onDestroy();
		if (mBtAdapter.isEnabled()) {
			mBtAdapter.disable();
		}
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}
		mDevicesArrayAdapter.clear();
		this.unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.optionsmenu, menu);
		mItem = menu.getItem(0);
		if (loginName != "none") {
			mItem.setTitle(loginName);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.loginOrRegister:
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			LayoutInflater inflater = MainActivity.this.getLayoutInflater();
			AlertDialog dialog = builder
					.setView(inflater.inflate(R.layout.dlg_login, null))
					.setTitle(getResources().getString(R.string.login))
					// 标题
					// Add action buttons
					.setPositiveButton(getResources().getString(R.string.login),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									if (loginNameEditText.getText().toString().trim().equals("")
											|| passwordEditText.getText().toString().trim()
													.equals("")) {
										Toast.makeText(MainActivity.this,
												getResources().getString(R.string.cannotBeEmpty),
												Toast.LENGTH_SHORT).show();
									} else {
										new NetAsyncTask().execute();
									}
								}
							})
					.setNegativeButton(getResources().getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
								}
							}).create();

			dialog.show();

			loginNameEditText = (EditText) dialog.findViewById(R.id.loginNameEditText);
			passwordEditText = (EditText) dialog.findViewById(R.id.passwordEditText);
			TextView registerTextView = (TextView) dialog.findViewById(R.id.goToRegisterTextView);
			registerTextView.setClickable(true);
			registerTextView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, registerActivity.class);
					startActivity(intent);
				}
			});

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class NetAsyncTask extends AsyncTask<Object, Object, String> {
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				if (isLoginOK.equals("true")) {
					Toast.makeText(MainActivity.this,
							getResources().getString(R.string.loginSuccess), Toast.LENGTH_SHORT)
							.show();
					loginName = loginNameEditText.getText().toString().trim();
					mItem.setTitle(loginName);
					mSharedPreferencesData.edit()
							.putString("LOGINNAME", loginNameEditText.getText().toString().trim())
							.commit();
				} else {
					Toast.makeText(MainActivity.this,
							getResources().getString(R.string.loginNameorPasswordWrong),
							Toast.LENGTH_SHORT).show();
				}
			}
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(Object... params) {
			SoapObject soapObject = new SoapObject(targetNameSpace, methodName);
			soapObject.addProperty("LoginName", loginNameEditText.getText().toString().trim());
			soapObject.addProperty("Password", passwordEditText.getText().toString().trim());
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(SoapEnvelope.VER11);

			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			try {
				httpSE.call(targetNameSpace + methodName, envelop);
				SoapObject resultObj = (SoapObject) envelop.bodyIn;
				isLoginOK = resultObj.getProperty(0).toString().trim();

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

}
