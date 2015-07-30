//package my.bigpackage.inebriatorapp;
//
//import android.app.Application;
//import android.bluetooth.BluetoothAdapter;
//import android.content.Context;
//
//public class BluetoothManager extends Application {
//	
//	private static BluetoothManager singleton;
//	private static Context MainContext;
//	
//	private BluetoothAdapter BA;
//	
//	public synchronized static BluetoothManager getInstance(Context context)
//	{
//		MainContext = context;
//		if (singleton == null)
//		{
//			singleton = new BluetoothManager();
//		}
//		return singleton;
//	}
//	
//	public synchronized boolean createAdapter()
//	{
//		BA = BluetoothAdapter.getDefaultAdapter();
//		if (BA == null) { return false; }
//		else { return true; }
//	}
//	
//	public synchronized boolean isAdapterEnabled()
//	{
//		return BA.isEnabled();
//	}
//	
//	
//}

package my.bigpackage.inebriatorapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;

public class BluetoothManager extends Application {
	
	private static final String INEBRIATOR_NAME = "HC-06";
	private static final String INEBRIATOR_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB";
	private static final String INEBRIATOR_PIN_STRING = "1234";
	public static final String MESSAGE_BREAK = "`";
	private static final String MESSAGE_END = "~";
	
	public static final int FOUND_INEBRIATOR = 60;
	public static final int MESSAGE_READ = 61;
	public static final int MESSAGE_WRITE = 62;
	
	public static final String REQUEST_MENU = "REQUEST" + MESSAGE_BREAK + "MENU" + MESSAGE_END;
	public static final String RESPONSE_MENU_START = "RESPONSE<>MENU";
	
	public static final String UPDATE_ADD_LIQUOR_START = "UPDATE<>ADD_LIQUOR";
	
	private static BluetoothManager singleton;
	private static Context MainContext;
	private static Handler MainHandler = new Handler();
	private static BluetoothSocket BtSocket;
	private static ConnectedThread BtConnectedThread;
	private static IBluetooth IBT;
	
	private BluetoothAdapter BA;
	
	public synchronized static BluetoothManager getInstance(Context context, Handler handler)
	{
		MainContext = context;
		MainHandler = handler;
		if (singleton == null)
		{
			singleton = new BluetoothManager();
		}
		return singleton;
	}
	
	public synchronized void setHandler(Handler handler)
	{
		MainHandler = handler;
	}
	
	public synchronized boolean createAdapter()
	{
		BA = BluetoothAdapter.getDefaultAdapter();
		if (BA == null) { return false; }
		else { return true; }
	}
	
	public synchronized boolean isAdapterEnabled()
	{
		return BA.isEnabled();
	}
	
	public synchronized boolean pairWithInebriator()
	{
		Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
		BluetoothDevice inebriator = null;
		if (pairedDevices.size() > 0)
		{
			for (BluetoothDevice device : pairedDevices)
			{
				if (device.getName().equals(INEBRIATOR_NAME)) { inebriator = device; }
			}
		}
		
		if (inebriator != null)
		{
			MainHandler.obtainMessage(FOUND_INEBRIATOR, -1, -1, null).sendToTarget();
		}
		else
		{
			// http://stackoverflow.com/questions/7337032/how-can-i-avoid-or-dismiss-androids-bluetooth-pairing-notification-when-i-am-do
			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
			filter.addAction(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			MainContext.registerReceiver(BtReceiver, filter);
			IBT = getIBluetooth();
			
			this.BA.startDiscovery();
		}
		
		return false;
	}
	
	/**
	 * COPIED THIS SHIT
	 * http://stackoverflow.com/questions/3462968/how-to-unpair-bluetooth-device-using-android-2-1-sdk
	 * @return
	 */
	private IBluetooth getIBluetooth()
	{
		IBluetooth ibt = null;
		
		try
		{
			Class c2 = Class.forName("android.os.ServiceManager");
			Method m2 = c2.getDeclaredMethod("getService", String.class);
			IBinder b = (IBinder)m2.invoke(null, "bluetooth");
			
			Class c3 = Class.forName("android.bluetooth.IBluetooth");

		    Class[] s2 = c3.getDeclaredClasses();

		    Class c = s2[0];
		    Method m = c.getDeclaredMethod("asInterface",IBinder.class);
		    m.setAccessible(true);
		    ibt = (IBluetooth) m.invoke(null, b);
		} catch (Exception e) {
			
		}
		
		return ibt;
	}
	
	private BroadcastReceiver BtReceiver = new BroadcastReceiver()
	{
		private boolean FoundInebriator = false;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
			{
				if (((BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getBondState() == BluetoothDevice.BOND_BONDED)
				{
					MainHandler.obtainMessage(FOUND_INEBRIATOR, -1, -1, null).sendToTarget();
				}
			}
			else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); 
				
				try {
					byte[] pin = (byte[]) BluetoothDevice.class.getMethod("convertPinToBytes", String.class).invoke(BluetoothDevice.class, INEBRIATOR_PIN_STRING);
				    BluetoothDevice.class.getMethod("setPin", byte[].class).invoke(device, pin);
				    
					Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
		            Method setPinMethod = class1.getMethod("setPin");  
		            Boolean returnValue = (Boolean) setPinMethod.invoke(device, pin);
		            
		            device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
					device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (action.equals(BluetoothDevice.ACTION_FOUND))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getName().equals(INEBRIATOR_NAME))
				{
					BA.cancelDiscovery();
					this.FoundInebriator = true;
					
					try {						
						Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
			            Method createBondMethod = class1.getMethod("createBond");  
			            Boolean returnValue = (Boolean) createBondMethod.invoke(device);  
						//IBT.createBond(device.getAddress());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
			{
				if (!this.FoundInebriator)
				{
					String error = "You're too far from the Inebriator. Stumble closer, then try again.";
					MainHandler.obtainMessage(MainActivity.BLUETOOTH_CONNECTION_FAILED, -1, -1, error).sendToTarget();
				}
			}
		}
	};
	
	public synchronized boolean connectToInebriator()
	{
		if (this.BtConnectedThread != null && this.BtConnectedThread.isAlive()) { return true; }
		
		Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();
		BluetoothDevice inebriator = null;
		if (pairedDevices.size() > 0)
		{
			for (BluetoothDevice device : pairedDevices)
			{
				if (device.getName().equals(INEBRIATOR_NAME)) { inebriator = device; }
			}
		}
		
		if (inebriator != null)
		{
			BluetoothSocket tmp = null;
			java.util.UUID btUUID = java.util.UUID.fromString(INEBRIATOR_UUID_STRING);
			try
			{
				tmp = inebriator.createRfcommSocketToServiceRecord(btUUID);
			} catch (IOException e) { return false; }
			
			try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
				tmp.connect();
				BtSocket = tmp;
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                tmp.close();
	            } catch (IOException closeException) { }
	            return false;
	        }
			
			BtConnectedThread = new ConnectedThread(BtSocket);
			BtConnectedThread.start();
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public synchronized void destroyConnection()
	{
		if (BtConnectedThread != null && BtConnectedThread.isAlive()) { BtConnectedThread.cancel(); } 
	}
	
	public synchronized void sendMessage(String message)
	{
		if (BtConnectedThread == null) { return; }
		
		BtConnectedThread.write((message + MESSAGE_END).getBytes());
	}
	
	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket Socket;
		private final InputStream InStream;
		private final OutputStream OutStream;
		private StringBuilder SB = new StringBuilder();
		private String message = "";
		
		public ConnectedThread(BluetoothSocket socket)
		{
			this.Socket = socket;
			InputStream tempIn = null;
			OutputStream tempOut = null;
			
			try
			{
				tempIn = socket.getInputStream();
				tempOut = socket.getOutputStream();
			}
			catch (IOException e) { }
			
			this.InStream = tempIn;
			this.OutStream = tempOut;
		}
		
		public void run()
		{
			byte[] buffer = new byte[1024];
			int bytes;
			
			while (true)
			{
				try
				{
					bytes = this.InStream.read(buffer);
					
            		String strIncom = new String(buffer, 0, bytes);              // create string from bytes array
            		
            		SB.append(strIncom);
            		int endOfLineIndex = SB.indexOf(MESSAGE_END);
            		if (endOfLineIndex > 0)
            		{
            			String sbprint = SB.substring(0, endOfLineIndex);
            			message += sbprint;
            			MainHandler.obtainMessage(MESSAGE_READ, -1, -1, message).sendToTarget();
            			message = "";
            		}
            		else
            		{
            			//message += strIncom;
            			Log.e("TAG", message);
            		}
				}
				catch (IOException e) { break; }
			}
		}
		
		public void write(byte[] buffer)
		{
			try
			{
				this.OutStream.write(buffer);
			} catch (IOException e) { }
		}
		
		public void cancel()
		{
			try
			{
				this.Socket.close();
			}
			catch (IOException e) { }
		}
	}
	
	public synchronized void requestLiquorList()
	{
		this.connectToInebriator();
		this.sendMessage(this.REQUEST_MENU);
	}
	
	public synchronized void addNewLiquor(String name, int nozzle)
	{
		this.connectToInebriator();
		this.sendMessage(this.UPDATE_ADD_LIQUOR_START + MESSAGE_BREAK + name + MESSAGE_BREAK + Integer.toString(nozzle));
	}
}