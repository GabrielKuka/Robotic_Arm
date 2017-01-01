package com.gabriel.roboticarmcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

 class BtInterface {

	//////// Required bluetooth objects////////
	private BluetoothDevice device = null;
	private BluetoothSocket socket = null;
	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private InputStream receiveStream = null;
	private BufferedReader receiveReader = null;
	private OutputStream sendStream = null; // no need to buffer it as we are
											// going to send 1 char at a time

	// this thread will listen to incoming messages. It will be killed when
	// connection is closed
	private ReceiverThread receiverThread;

	// these handlers corresponds to those in the main activity
	private Handler handlerStatus, handlerMessage;

	protected static int BLUETOOTH_NOT_ENABLED = -2;
	protected static int CONNECTED = 1;
	protected static int DISCONNECTED = 2;
	protected static final String TAG = "Chihuahua";
	protected boolean foundChihuahua = false;

	 BtInterface(Handler hstatus, Handler h) {
		handlerStatus = hstatus;
		handlerMessage = h;

	}

	public boolean isConnected() {

		return foundChihuahua;

	}

	// when called from the main activity, it sets the connection with the
	// remote device
	 void connect() {

		discover();

		Set<BluetoothDevice> setpairedDevices = btAdapter.getBondedDevices();
		BluetoothDevice[] pairedDevices = setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);

		foundChihuahua = false;

		receiverThread = new ReceiverThread(handlerMessage);



		for (BluetoothDevice pairedDevice : pairedDevices) {
			if (pairedDevice.getName().contains("HC-06")) {

				device = pairedDevice;
				try {

					socket = device
							.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					receiveStream = socket.getInputStream();
					receiveReader = new BufferedReader(new InputStreamReader(receiveStream));
					sendStream = socket.getOutputStream();



				} catch (IOException e) {
					e.printStackTrace();
				}
				foundChihuahua = true;

				 new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							socket.connect();
							Log.d("Status: ", "Connected");

							Message msg = handlerStatus.obtainMessage();
							msg.arg1 = CONNECTED;
							handlerStatus.sendMessage(msg);

							receiverThread.start();

						} catch (IOException e) {
							Log.v("N", "Connection Failed : " + e.getMessage());
							e.printStackTrace();
						}
					}
				}).start();



				break;

			}

		}
//		if (!foundChihuahua) {
//			Message msg = handlerStatus.obtainMessage();
//			msg.arg1 = BLUETOOTH_NOT_ENABLED;
//			handlerStatus.sendMessage(msg);
//		}


	}

	// properly closing the socket and updating the status
	 void close() {
		try {
			socket.close();
			receiverThread.interrupt();
			foundChihuahua = false;
			Message msg = handlerStatus.obtainMessage();
			msg.arg1 = DISCONNECTED;
			handlerStatus.sendMessage(msg);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// the main function of the app : sending character over the Serial
	// connection when the user presses a key on the screen
	 void sendData(String data) {
		try {
			sendStream.write(data.getBytes());	
			sendStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// this thread listens to replies from Arduino as it performs actions, then
	// update the log through the Handler
	private class ReceiverThread extends Thread {
		Handler handler;

		ReceiverThread(Handler h) {
			handler = h;
		}

		@Override
		public void run() {
			while (socket != null) {
				if (isInterrupted()) {
					try {
						join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					if (receiveStream.available() > 0) {
						String dataToSend; // when we hit a line break, we
												// send the data

						dataToSend = receiveReader.readLine();
						if (dataToSend != null) {
							Log.v(TAG, dataToSend);
							Message msg = handler.obtainMessage();
							Bundle b = new Bundle();
							b.putString("receivedData", dataToSend);
							msg.setData(b);
							handler.sendMessage(msg);
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	 private void discover() {

		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();

	}

}
