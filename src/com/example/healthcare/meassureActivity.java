package com.example.healthcare;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class meassureActivity extends Activity {
	private static final String targetNameSpace = MainActivity.targetNameSpace;
	private static final String WSDL = MainActivity.WSDL;
	private static final String methodName = "insertUserMeassureResult ";

	private ProgressBar mProBar;
	private Button startButton, stopButton;
	private TextView sBPTextView, dBPTextView, hRTextView;
	Context mContext;
	private readThread mReadThread = null; // 读取数据
	private BluetoothSocket socket = null;
	private int cBP, dBP, sBP, hR;// 当前血压，舒张压，收缩压，心率
	private boolean isConnect = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_meassure);
		mContext = this;
		init();
	}

	private void init() {
		mProBar = (ProgressBar) findViewById(R.id.progressBar1);
		mProBar.setVisibility(View.INVISIBLE);
		// mProBar.setProgress(50);
		sBPTextView = (TextView) findViewById(R.id.sBPTextView);
		dBPTextView = (TextView) findViewById(R.id.dBPTextView);
		hRTextView = (TextView) findViewById(R.id.hRTextView);

		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String msgText = "55aa06000001d200d9";
				string_to_hex_send(msgText);
			}
		});

		stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// mProBar.setVisibility(View.INVISIBLE);
				String msgText = "55aa06000002d300db";
				string_to_hex_send(msgText);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (isConnect == true || MainActivity.socket == null) {
			return;
		}
		socket = MainActivity.socket;
		mReadThread = new readThread();
		mReadThread.start();
		isConnect = true;
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdownClient();
	}

	protected void string_to_hex_send(String msgText) {
		// TODO Auto-generated method stub
		char msg_text_array[] = msgText.toCharArray();
		byte msg_byte_array[] = new byte[msg_text_array.length];
		byte msg_byte_array_helf[] = new byte[(msg_text_array.length + 1) / 2];

		for (int i = 0; i < msg_byte_array.length; i++) {
			msg_byte_array[i] = (byte) msg_text_array[i];
			if (msg_byte_array[i] >= '0' && msg_byte_array[i] <= '9') {
				msg_byte_array[i] -= '0';
			} else if (msg_byte_array[i] >= 'a' && msg_byte_array[i] <= 'f') {
				msg_byte_array[i] -= ('a' - 10);
			} else if (msg_byte_array[i] >= 'A' && msg_byte_array[i] <= 'F') {
				msg_byte_array[i] -= ('A' - 10);
			}
			if (i % 2 == 1) {
				msg_byte_array_helf[(i - 1) / 2] = (byte) (16 * msg_byte_array[i - 1] + msg_byte_array[i]);
			}
		}

		if (msgText.length() > 0) {
			sendMessageHandle(msg_byte_array_helf);
		}
	}

	// 发送数据
	private void sendMessageHandle(byte msg[])// (String msg)
	{
		if (socket == null) {
			Toast.makeText(mContext, "没有连接设备！", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			OutputStream os = socket.getOutputStream();
			os.write(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler LinkDetectedHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				mProBar.setVisibility(View.VISIBLE);
				// resultTextView.setText("当前压力为：" + String.valueOf(cBP));
				if (cBP < 0 || cBP > 120) {
					mProBar.setProgress(120);
				} else {
					mProBar.setProgress(cBP);
				}
			} else if (msg.what == 1) {
				sBPTextView.setText(String.valueOf(sBP));
				dBPTextView.setText(String.valueOf(dBP));
				hRTextView.setText(String.valueOf(hR));
				mProBar.setVisibility(View.INVISIBLE);
				if (!MainActivity.loginName.equals("none")) {
					new webserviceThread().start();
				}
			}
		}
	};

	/* 停止客户端连接 */
	private void shutdownClient() {
		new Thread() {
			public void run() {
				if (mReadThread != null) {
					mReadThread.interrupt();
					mReadThread = null;
					isConnect = false;
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

	// 读取数据
	private class readThread extends Thread {
		public void run() {
			InputStream mmInStream = null;
			try {
				mmInStream = socket.getInputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (true) {
				try {
					// Read from the InputStream
					int readCount = 0;
					int count = 3;
					byte[] b = new byte[1024];
					while (readCount < count) {
						readCount += mmInStream.read(b, readCount, count - readCount);
						if (b[2] != 0) {
							count = b[2] + 3;
						}
					}

					if (b[2] == 0x0e) {
						int x = b[10];
						if (x < 0) {
							x += 256;
						}
						int y = b[11];
						cBP = 16 * y + x / 16;
						String s = String.valueOf(cBP);
						Message msg0 = new Message();
						msg0.obj = s;
						msg0.what = 0;
						LinkDetectedHandler.sendMessage(msg0);
					} else if (b[2] == 0x14) {
						sBP = b[14];
						if (sBP < 0) {
							sBP += 256;
						}
						dBP = b[16];
						hR = b[20];
						Message msg1 = new Message();
						msg1.obj = "测量数据";
						msg1.what = 1;
						LinkDetectedHandler.sendMessage(msg1);
					}
				} catch (IOException e) {
					try {
						mmInStream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}

	private class webserviceThread extends Thread {
		public void run() {
			SoapObject soapObject = new SoapObject(targetNameSpace, methodName);
			soapObject.addProperty("LoginName", MainActivity.loginName);
			soapObject.addProperty("SystolicPressure", String.valueOf(sBP));
			soapObject.addProperty("DiastolicPressure", String.valueOf(dBP));
			soapObject.addProperty("PulseFrequency", String.valueOf(hR));
			SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(SoapEnvelope.VER11);

			envelop.dotNet = true;
			envelop.setOutputSoapObject(soapObject);
			HttpTransportSE httpSE = new HttpTransportSE(WSDL);
			try {
				httpSE.call(targetNameSpace + methodName, envelop);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
