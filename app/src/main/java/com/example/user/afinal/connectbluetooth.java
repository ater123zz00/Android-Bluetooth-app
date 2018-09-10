package com.example.user.afinal;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.text.SimpleDateFormat;

public class connectbluetooth extends AppCompatActivity {

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private EditText inputdata;
    private Button sendDevice;

    private Button timeBtn;
    private Button setBtn;
    private Button setOverBtn;
    private Button homeBtn;

    private TextView collect;
    private Handler mHandler;
    // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread;
    // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null;
    // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private final static int Record_Mode = 1;
    private final static int Setting_Mode = 2;

    private  String[] _recieveData;

    static private String[] item = new String[20];
    static private String[] tag = new String[20];

    private int number =0;
    private int Mode_number =Setting_Mode;

    private String Data_filename = "DataFile.txt";
    private String Record_filename = "RecordFile.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectbluetooth);
        //初始化元件
        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        collect = (TextView) findViewById(R.id.collect);

        mScanBtn = (Button) findViewById(R.id.scan);
        mOffBtn = (Button) findViewById(R.id.off);
        mDiscoverBtn = (Button) findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
        inputdata = (EditText)findViewById(R.id.editText2);
        sendDevice = (Button)findViewById(R.id.send);

        timeBtn = (Button) findViewById(R.id.buttontime);
        setBtn = (Button) findViewById(R.id.buttonset);
        setOverBtn = (Button) findViewById(R.id.buttonsetOver);
        homeBtn = (Button) findViewById(R.id.buttonRhome);

        mReadBuffer.setMaxLines(50);
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        number =0;
        Mode_number =Setting_Mode;


        // 詢問藍芽裝置權限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        //定義執行緒 當收到不同的指令做對應的內容
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {//收到MESSAGE_READ 開始接收資料

                    try {
                        String readMessage = null;
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                        _recieveData = readMessage.split("\r");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if(_recieveData[0].equals("check")){
                        if(mConnectedThread != null)  //First check to make sure thread created
                            mConnectedThread.write("y");
                    }
                    collect.setText(_recieveData[0]);
                    _recieveData[0] += "\r\n";
                    mReadBuffer.append(_recieveData[0]);


                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String) (msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(connectbluetooth.this,MainActivity.class);
                startActivity(intent);
                connectbluetooth.this.finish();
            }
        });

            sendDevice.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v){
                    item[number] = inputdata.getText().toString();
                    tag[number] = collect.getText().toString();
                    collect.setText("設定成功!");
                    number++;
                }
            });

            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn(v);
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOff(v);
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    discover(v);
                }
            });

            setBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Mode_number = Setting_Mode;
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("set");
                    mReadBuffer.append("set\n");
                }
            });

            setOverBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Mode_number = Record_Mode;
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("setOver");
                    mReadBuffer.append("setOver\n");
                    writeData();
                }
            });

            timeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                    String date = sDateFormat.format(new java.util.Date());
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write(date);
                    mReadBuffer.append(date+"\n");
                    if(readData()){
                        if(tag[0]==""){}
                        else {
                            mReadBuffer.append("載入物品清單\r\n");
                        }
                    };
                    Mode_number = Record_Mode;
                }
            });

            collect.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkRecord(s.toString());
                }
            });


    }

    private void bluetoothOn(View view) {
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            } else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void bluetoothOff(View view) {
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(), "Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(View view) {
        // Check if the device is already discovering
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view) {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread() {
                public void run() {
                    boolean fail = false;
                    //取得裝置MAC找到連接的藍芽裝置
                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        //建立藍芽socket
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        //建立藍芽連線
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close(); //關閉socket
                            //開啟執行緒 顯示訊息
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false) {
                        //開啟執行緒用於傳輸及接收資料
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                        //開啟新執行緒顯示連接裝置名稱
                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {

        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes > 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read

                        byte[] buffer2 = new byte[1024];
                        for(int i=0;i<bytes;i++)buffer2[i]=buffer[i];
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer2).sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean readData() {
        if (getFilesDir().exists()) {
            try {
                FileInputStream inputStream = openFileInput(Data_filename);
                byte[] readBytes = new byte[inputStream.available()];
                inputStream.read(readBytes);
                String readString = new String(readBytes);
                String[] content = readString.split("\r\n");
                for (int i = 0, j = 0; i < content.length; i++) {
                    if (content[i] == "") continue;
                    if (i % 2 == 0) item[j] = content[i];
                    else {
                        tag[j] = content[i];
                        j++;number++;
                    }
                }
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else return false;
    }
    private void writeData(){
        try {
            FileOutputStream outputStream = openFileOutput(Data_filename, Context.MODE_PRIVATE);
            for(int i = 0 ; i < number ; i++){
                if (item[i]==null)continue;
                //SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                //String date = sDateFormat.format(new java.util.Date());
                outputStream.write(item[i].getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.write(tag[i].getBytes());
                outputStream.write("\r\n".getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeRecord(int tag_number){
        try {
            FileOutputStream outputStream = openFileOutput(Record_filename, Context.MODE_APPEND);

                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                outputStream.write(date.getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.write(tag[tag_number].getBytes());
                outputStream.write("\r\n".getBytes());

            mBluetoothStatus.setText(tag_number);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkRecord(String s) {
        if (Mode_number == Record_Mode) {
            for(int i=0;i<number;i++){
                if(s.equals(tag[i]))writeRecord(i);
            }
        }
    }
}