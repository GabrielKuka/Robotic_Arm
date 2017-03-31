package com.gabriel.roboticarmcontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@SuppressLint({"HandlerLeak", "ClickableViewAccessibility"})
public class Main extends Activity implements OnClickListener {

    //////// Others////////
    public BluetoothAdapter btAdapter;
    public boolean sts = false;

    //////// Layout////////
    private LinearLayout controls;

    ///////// Views////////
    private Button turn_On_or_Off;
    private TextView actual_Status;

    //////// Constants////////
    protected static final int REQUEST_ENABLE_BT = 2;


    public BtInterface bt = null;

    // Handler to check the state of the bluetooth
    final Handler handlerStatus = new Handler() {

        public void handleMessage(Message msg) {

            int status = msg.arg1;
            if (status == BtInterface.CONNECTED) {

                ifConnected();

            } else if (status == BtInterface.DISCONNECTED) {

                ifDisconnected();

            } else if (status == BtInterface.BLUETOOTH_NOT_ENABLED) {

                ifNotEnabled();
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        init();
        Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(in, REQUEST_ENABLE_BT);
        launchDialog();
    }

    private void launchDialog() {
        new LovelyStandardDialog(this)
                .setTopColorRes(R.color.blue2)
                .setButtonsColorRes(R.color.blue)
                .setIcon(R.drawable.appicon)
                .setTitle(R.string.dialogTitle)
                .setMessage(R.string.dialogMessage)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        launchDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    // function to initialize objects
    private void init() {


        turn_On_or_Off = (Button) findViewById(R.id.disconnect);
        turn_On_or_Off.setVisibility(View.INVISIBLE);

        actual_Status = (TextView) findViewById(R.id.tvPD);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        controls = (LinearLayout) findViewById(R.id.controls);
        controls.setVisibility(View.INVISIBLE);

        Button open = (Button) findViewById(R.id.openG);
        Button close = (Button) findViewById(R.id.closeG);

        Button left1 = (Button) findViewById(R.id.left1);
        Button left2 = (Button) findViewById(R.id.left2);

        Button up1 = (Button) findViewById(R.id.up1);
        Button up2 = (Button) findViewById(R.id.up2);

        Button right1 = (Button) findViewById(R.id.right1);
        Button right2 = (Button) findViewById(R.id.right2);

        Button down1 = (Button) findViewById(R.id.down1);
        Button down2 = (Button) findViewById(R.id.down2);

        left1.setOnClickListener(this);
        left2.setOnClickListener(this);
        right1.setOnClickListener(this);
        right2.setOnClickListener(this);
        up1.setOnClickListener(this);
        up2.setOnClickListener(this);
        down1.setOnClickListener(this);
        down2.setOnClickListener(this);
        open.setOnClickListener(this);
        close.setOnClickListener(this);

    }

    final Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            String string = msg.getData().getString("receivedData");

        }

    };


    // Actions to be taken after specific states of bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {

                bt = new BtInterface(handlerStatus, handler);
                bt.connect();

            } else if (resultCode == RESULT_CANCELED) {

                displayMessage("Bluetooth must be turned on!");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1300);
                            finish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        }
    }

    // connect or disconnect
    public void turnOnorOff(View v) {

        if (!sts) {

            actual_Status.setText(R.string.initCon);
            bt = new BtInterface(handlerStatus, handler);
            bt.connect();

        } else {

            bt.close();

        }

    }

    // Perform these actions if bluetooth connected
    public void ifConnected() {
        launchDialog();
        displayMessage("Connected!");
        controls.setVisibility(View.VISIBLE);
        actual_Status.setText(R.string.connected);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        turn_On_or_Off.setVisibility(View.VISIBLE);
        turn_On_or_Off.setText(R.string.disconnect);
        sts = true;

    }

    // Perform these actions if bluetooth disconnected
    public void ifDisconnected() {
        launchDialog();
        controls.setVisibility(View.INVISIBLE);
        displayMessage("Disconnected!");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        actual_Status.setText(R.string.disconnected);
        turn_On_or_Off.setText(R.string.connect);
        sts = false;
    }

    // Perform these actions if bluetooth is not available
    public void ifNotEnabled() {
        displayMessage("Turn on bluetooth!");
        Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(in, REQUEST_ENABLE_BT);
    }

    public void displayMessage(String text) {

        Crouton.makeText(this, text, Style.CONFIRM).show();

    }

    // Check command
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.left1:

                bt.sendData("a");

                break;

            case R.id.left2:

                bt.sendData("e");

                break;

            case R.id.right1:

                bt.sendData("b");

                break;

            case R.id.right2:

                bt.sendData("f");

                break;

            case R.id.up1:

                bt.sendData("c");

                break;

            case R.id.up2:

                bt.sendData("g");

                break;

            case R.id.down1:
                bt.sendData("d");
                break;

            case R.id.down2:
                bt.sendData("h");
                break;

            case R.id.openG:
                bt.sendData("x");
                break;

            case R.id.closeG:
                bt.sendData("z");
                break;
        }

    }


}
