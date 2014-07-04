package com.freakk.midibluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MidiKeyboardBluetooth extends Activity implements OnTouchListener {

	// Layout view
	private TextView mTitle;

	private static enum View {
		VIEW_PIANO, VIEW_PADS
	};

	private View activeView;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothCommandService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter

	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for Bluetooth Command Service
	private BluetoothCommandService mCommandService = null;

	PianoKeys piano;
	Pads pads;
	private int[] keyValues = { 0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21,
			23, 24 };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// setContentView(R.layout.main);

		piano = new PianoKeys(this);
		pads = new Pads(this);

		// default view
		setContentView(piano);
		piano.setOnTouchListener(this);
		activeView = View.VIEW_PIANO;

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		// Set up the custom title and View
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupCommand() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		// otherwise set up the command service
		else {
			if (mCommandService == null)
				setupCommand();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mCommandService != null) {
			if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
				mCommandService.start();
			}
		}

	}

	private void setupCommand() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		mCommandService = new BluetoothCommandService(this, mHandler);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mCommandService != null)
			mCommandService.stop();
	}

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothCommandService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					break;
				case BluetoothCommandService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothCommandService.STATE_LISTEN:
				case BluetoothCommandService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}

	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mCommandService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupCommand();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.piano:
			// Ensure this device is discoverable by others
			setContentView(piano);
			activeView = View.VIEW_PIANO;
			piano.setOnTouchListener(this);
			return true;
		case R.id.pads:
			// Ensure this device is discoverable by others
			setContentView(pads);
			activeView = View.VIEW_PADS;
			pads.setOnTouchListener(this);
			return true;
		}
		return false;
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
	// mCommandService.write(BluetoothCommandService.VOL_UP);
	// return true;
	// } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
	// mCommandService.write(BluetoothCommandService.VOL_DOWN);
	// return true;
	// }
	//
	// return super.onKeyDown(keyCode, event);
	// }

	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				// (byte)(value >>> 24),
				(byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}

	public static byte[] hex2ba(String sHex) {
		byte[] ba = new byte[sHex.length() / 2];
		for (int i = 0; i < sHex.length() / 2; i++) {
			ba[i] = (Integer.decode("0x" + sHex.substring(i * 2, (i + 1) * 2)))
					.byteValue();
		}
		return ba;
	}

	/*
	 * // public boolean onTouch(View v, MotionEvent e) { public boolean
	 * onTouch(android.view.View v, MotionEvent e) { // TODO Auto-generated
	 * method stub //int action = (e.getAction() & MotionEvent.ACTION_MASK); int
	 * pointCount = e.getPointerCount();
	 * 
	 * for (int i = 0; i < pointCount; i++) { int action = (e.getAction() &
	 * MotionEvent.ACTION_MASK); if (action == MotionEvent.ACTION_DOWN || action
	 * == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_UP ||
	 * action == MotionEvent.ACTION_POINTER_UP ) { float x = e.getX(i); float y
	 * = e.getY(i); int keyNum = -24; // Temp popup keyNum value int isBlackKey
	 * = 0;
	 * 
	 * // midi-msg[0]: StatusByte --> Chan 1 , noteOn = 91(HEX) or 145(DEC) //
	 * Chan 1 , noteOff = 81(HEX) or 129(DEC)
	 * 
	 * String status_byte = "81"; if (action == MotionEvent.ACTION_DOWN ||
	 * action == MotionEvent.ACTION_POINTER_DOWN) status_byte = "91"; String
	 * data_byte_1 = new String(); // fixed velocity String data_byte_2 = "7F";
	 * 
	 * byte[] midi_msg = new byte[3];
	 * 
	 * switch (activeView) { case VIEW_PIANO: keyNum = (int) (x /
	 * piano.getWhiteKeyWidth()); if (y < piano.getBlackKeyHeight()) { // check
	 * if it's a black key hit if (keyNum != 2 && keyNum != 6 && keyNum != 9 &&
	 * keyNum != 13) { if (keyNum == 0 || keyNum == 3 || keyNum == 7 || keyNum
	 * == 10 || keyNum == 14) { if (x > ((keyNum + 1) piano.getWhiteKeyWidth() -
	 * piano .getBlackKeyWidth() / 2)) { isBlackKey = 1; } } else if (x >
	 * ((keyNum + 1) piano.getWhiteKeyWidth() - piano .getBlackKeyWidth() / 2))
	 * {// upper right // side isBlackKey = 1;
	 * 
	 * } // else isBlackKey = " "; } else if (x < ((keyNum) *
	 * piano.getWhiteKeyWidth() + piano .getBlackKeyWidth() / 2)) { keyNum--;
	 * isBlackKey = 1; }// upper left side } if (action ==
	 * MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
	 * if (isBlackKey == 1) piano.setTouchedKey(keyNum, true); else
	 * piano.setTouchedKey(keyNum, false); } else{ if (isBlackKey == 1)
	 * piano.setReleasedKey(keyNum, true); else piano.setReleasedKey(keyNum,
	 * false); } break;
	 * 
	 * case VIEW_PADS: keyNum = (int) (x / pads.getPadWidth()); if (y >
	 * pads.getPadHeight()) keyNum += 6; if (action == MotionEvent.ACTION_DOWN
	 * || action == MotionEvent.ACTION_POINTER_DOWN) {
	 * pads.setTouchedKey(keyNum); } else if(action == MotionEvent.ACTION_UP ||
	 * action == MotionEvent.ACTION_POINTER_UP) { pads.setReleasedKey(keyNum); }
	 * break; }
	 * 
	 * int midiNote = 24 + keyValues[keyNum] + isBlackKey;
	 * 
	 * 
	 * isBlackKey = 0; data_byte_1 = "" + Integer.toHexString(midiNote);
	 * System.out.println(data_byte_1);
	 * 
	 * System.out.println("key " + midiNote); midi_msg = hex2ba(status_byte +
	 * data_byte_1 + data_byte_2); mCommandService.write(midi_msg); } } return
	 * true; }
	 */

	public boolean onTouch(android.view.View v, MotionEvent event) {

		float x, y;
		// get pointer index from the event object
		int pointerIndex = event.getActionIndex();

		// get pointer ID
		int pointerId = event.getPointerId(pointerIndex);

		// get masked (not specific to a pointer) action
		int maskedAction = event.getActionMasked();

		x = event.getX(pointerIndex);
		y = event.getY(pointerIndex);

		int keyNum = -24; // temp init value
		int isBlackKey = 0;

		// midi-msg[0]: StatusByte -->
		// Chan 1 , noteOn = 90(HEX) or 144(DEC)
		// Chan 1 , noteOff = 80(HEX) or 128(DEC)
		String status_byte = ""; // MIDI status byte

		//-------------------------------------------
		// DETECT PRESSED KEY
		//-------------------------------------------
		switch (activeView) {
		case VIEW_PIANO:
			keyNum = (int) (x / piano.getWhiteKeyWidth());
			if (y < piano.getBlackKeyHeight()) {
				// check if it's a black key hit
				if (keyNum != 2 && keyNum != 6 && keyNum != 9 && keyNum != 13) {
					if (keyNum == 0 || keyNum == 3 || keyNum == 7
							|| keyNum == 10 || keyNum == 14) {
						if (x > ((keyNum + 1) * piano.getWhiteKeyWidth() - piano
								.getBlackKeyWidth() / 2)) {
							isBlackKey = 1;
						}
					} else if (x > ((keyNum + 1) * piano.getWhiteKeyWidth() - piano
							.getBlackKeyWidth() / 2)) {// upper right side
						isBlackKey = 1;
					}
				} else if (x < ((keyNum) * piano.getWhiteKeyWidth() + piano
						.getBlackKeyWidth() / 2)) {
					keyNum--;
					isBlackKey = 1;
				}// upper left side
			}
			break;

		case VIEW_PADS:
			keyNum = (int) (x / pads.getPadWidth());
			if (y > pads.getPadHeight())
				keyNum += 6;
			break;
		}

		//-------------------------------------------
		// HANDLE MULTITOUCH EVENTS
		//-------------------------------------------
		switch (maskedAction) {

		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			status_byte = "90";
			switch (activeView) {
			case VIEW_PADS:
				pads.setTouchedKey(keyNum);
				break;
			case VIEW_PIANO:
				if (isBlackKey == 1)
					piano.setTouchedKey(keyNum, true);
				else
					piano.setTouchedKey(keyNum, false);
				break;
			}
			break;
		}
		case MotionEvent.ACTION_MOVE: { // a pointer was moved
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL: {
			status_byte = "80";
			switch (activeView) {
			case VIEW_PADS:
				pads.setReleasedKey(keyNum);
				break;
			case VIEW_PIANO:
				if (isBlackKey == 1)
					piano.setReleasedKey(keyNum, true);
				else
					piano.setReleasedKey(keyNum, false);
				break;
			}
			break;
		}
		}

		//-------------------------------------------
		// GENERATE MIDI MESSAGES
		//-------------------------------------------
		String data_byte_1 = new String();
		// fixed velocity
		String data_byte_2 = "7F";

		byte[] midi_msg = new byte[3];

		int midiNote = 24 + keyValues[keyNum] + isBlackKey;

		data_byte_1 = "" + Integer.toHexString(midiNote);
		System.out.println(data_byte_1);

		System.out.println("key " + midiNote);
		midi_msg = hex2ba(status_byte + data_byte_1 + data_byte_2);
		if(status_byte != "") mCommandService.write(midi_msg);

		return true;
	}
}
