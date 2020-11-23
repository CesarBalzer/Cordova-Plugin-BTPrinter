package br.com.cordova.printer.bluetooth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.text.TextUtils;
import android.util.Xml.Encoding;
import android.util.Base64;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class BluetoothPrinter extends CordovaPlugin {

    private static final String LOG_TAG = "BluetoothPrinter";
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    Bitmap bitmap;
    String encoding = "iso-8859-1";

    public static final byte LINE_FEED = 0x0A;
    public static final byte[] CODIFICATION = new byte[] { 0x1b, 0x4D, 0x01 };

    public static final byte[] ESC_ALIGN_LEFT = { 0x1B, 0x61, 0x00 };
    public static final byte[] ESC_ALIGN_RIGHT = { 0x1B, 0x61, 0x02 };
    public static final byte[] ESC_ALIGN_CENTER = { 0x1B, 0x61, 0x01 };

    public static final byte[] CHAR_SIZE_00 = { 0x1B, 0x21, 0x00 };// Normal size
    public static final byte[] CHAR_SIZE_01 = { 0x1B, 0x21, 0x01 };// Reduzided width
    public static final byte[] CHAR_SIZE_08 = { 0x1B, 0x21, 0x08 };// bold normal size
    public static final byte[] CHAR_SIZE_10 = { 0x1B, 0x21, 0x10 };// Double height size
    public static final byte[] CHAR_SIZE_11 = { 0x1B, 0x21, 0x11 };// Reduzided Double height size
    public static final byte[] CHAR_SIZE_20 = { 0x1B, 0x21, 0x20 };// Double width size
    public static final byte[] CHAR_SIZE_30 = { 0x1B, 0x21, 0x30 };
    public static final byte[] CHAR_SIZE_31 = { 0x1B, 0x21, 0x31 };
    public static final byte[] CHAR_SIZE_51 = { 0x1B, 0x21, 0x51 };
    public static final byte[] CHAR_SIZE_61 = { 0x1B, 0x21, 0x61 };

    public static final byte[] UNDERL_OFF = { 0x1b, 0x2d, 0x00 }; // Underline font OFF
    public static final byte[] UNDERL_ON = { 0x1b, 0x2d, 0x01 }; // Underline font 1-dot ON
    public static final byte[] UNDERL2_ON = { 0x1b, 0x2d, 0x02 }; // Underline font 2-dot ON
    public static final byte[] BOLD_OFF = { 0x1b, 0x45, 0x00 }; // Bold font OFF
    public static final byte[] BOLD_ON = { 0x1b, 0x45, 0x01 }; // Bold font ON
    public static final byte[] FONT_A = { 0x1b, 0x4d, 0x00 }; // Font type A
    public static final byte[] FONT_B = { 0x1b, 0x4d, 0x01 }; // Font type B

	public static final byte[] BARCODE_UPC_A = { 0x1D, 0x6B, 0x00 };
	public static final byte[] BARCODE_UPC_E = { 0x1D, 0x6B, 0x01 };
	public static final byte[] BARCODE_EAN13 = { 0x1D, 0x6B, 0x02 };
	public static final byte[] BARCODE_EAN8 = { 0x1D, 0x6B, 0x03 };
	public static final byte[] BARCODE_CODE39 = { 0x1D, 0x6B, 0x04 };
	public static final byte[] BARCODE_ITF = { 0x1D, 0x6B, 0x05 };
	public static final byte[] BARCODE_CODABAR = { 0x1D, 0x6B, 0x06 };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("status")) {
            checkBTStatus(callbackContext);
            return true;
        } else if (action.equals("list")) {
            listBT(callbackContext);
            return true;
        } else if (action.equals("connect")) {
            String name = args.getString(0);
            if (findBT(callbackContext, name)) {
                try {
                    connectBT(callbackContext);
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                }
            } else {
                callbackContext.error("BLUETOOTH DEVICE NOT FOUND: " + name);
            }
            return true;
        } else if (action.equals("connected")) {
            connected(callbackContext);
            return true;
        } else if (action.equals("disconnect")) {
            try {
                disconnectBT(callbackContext);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("setEncoding")) {
            encoding = args.getString(0);
            return true;
        } else if (action.equals("printText")) {
            try {
                String msg = args.getString(0);
                printText(callbackContext, msg);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("printTextSizeAlign")) {
            try {
                String msg = args.getString(0);
                Integer size = Integer.parseInt(args.getString(1));
                Integer align = Integer.parseInt(args.getString(2));
                printTextSizeAlign(callbackContext, msg, size, align);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("printBase64")) {
            try {
                String msg = args.getString(0);
                Integer align = Integer.parseInt(args.getString(1));
                printBase64(callbackContext, msg, align);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("printImageUrl")) {
            try {
                String msg = args.getString(0);
                Integer align = Integer.parseInt(args.getString(1));
                printImageUrl(callbackContext, msg, align);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("printTitle")) {
            try {
                String msg = args.getString(0);
                Integer size = Integer.parseInt(args.getString(1));
                Integer align = Integer.parseInt(args.getString(2));
                printTitle(callbackContext, msg, size, align);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("printPOSCommand")) {
            try {
                String msg = args.getString(0);
                printPOSCommand(callbackContext, hexStringToBytes(msg));
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("printQRCode")) {
            try {
                String data = args.getString(0);
				Integer align = Integer.parseInt(args.getString(1));
				Integer model = Integer.parseInt(args.getString(2));
				Integer size = Integer.parseInt(args.getString(3));
				Integer eclevel = Integer.parseInt(args.getString(4));
				printQRCode(callbackContext, data, align, model, size, eclevel);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("printBarcode")) {
            try {
                Integer system = Integer.parseInt(args.getString(0));
				String data = args.getString(1);
				Integer align = Integer.parseInt(args.getString(2));
                Integer position = Integer.parseInt(args.getString(3));
                Integer font = Integer.parseInt(args.getString(4));
                Integer height = Integer.parseInt(args.getString(5));
				printBarcode(callbackContext, system, data, align, position, font, height);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
		}
        return false;
    }

    // This will return the status of BT adapter: true or false
    boolean checkBTStatus(CallbackContext callbackContext) {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter.isEnabled()) {
                callbackContext.success("true");
                return true;
            } else {
                callbackContext.success("false");
                return false;
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // This will return the array list of paired bluetooth printers
    void listBT(CallbackContext callbackContext) {
        BluetoothAdapter mBluetoothAdapter = null;
        String errMsg = null;
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                errMsg = "NO BLUETOOTH ADAPTER AVAILABLE";
                Log.e(LOG_TAG, errMsg);
                callbackContext.error(errMsg);
                return;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                JSONArray json = new JSONArray();
                for (BluetoothDevice device : pairedDevices) {
                    /*
                     * Hashtable map = new Hashtable(); map.put("type", device.getType());
                     * map.put("address", device.getAddress()); map.put("name", device.getName());
                     * JSONObject jObj = new JSONObject(map);
                     */
                    Log.v(LOG_TAG, "DEVICE getName-> " + device.getName());
                    Log.v(LOG_TAG, "DEVICE getAddress-> " + device.getAddress());
                    Log.v(LOG_TAG, "DEVICE getType-> " + device.getType());
                    json.put(device.getName());
                    json.put(device.getAddress());
                    json.put(device.getType());
                }
                callbackContext.success(json);
            } else {
                callbackContext.error("NO BLUETOOTH DEVICE FOUND");
            }
            // Log.d(LOG_TAG, "Bluetooth Device Found: " + mmDevice.getName());
        } catch (Exception e) {
            errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    // This will find a bluetooth printer device
    boolean findBT(CallbackContext callbackContext, String name) {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(LOG_TAG, "NO BLUETOOTH ADAPTER AVAILABLE");
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equalsIgnoreCase(name)) {
                        mmDevice = device;
                        return true;
                    }
                }
            }
            Log.d(LOG_TAG, "BLUETOOTH DEVICE FOUND: " + mmDevice.getName());
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // Tries to open a connection to the bluetooth printer device
    boolean connectBT(CallbackContext callbackContext) throws IOException {
        try {
            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            beginListenForData();
            Log.d(LOG_TAG, "BLUETOOTH OPENED: " + mmDevice.getName());
            callbackContext.success("BLUETOOTH OPENED: " + mmDevice.getName());
            return true;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // Check if printer is already connected
    boolean connected(CallbackContext callbackContext) {
        try {
            if (mmSocket == null) {
                callbackContext.success("false");
                return false;
            } else if (mmSocket.isConnected()) {
                callbackContext.success("true");
                return true;
            } else {
                callbackContext.success("false");
                return false;
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // After opening a connection to bluetooth printer device,
    // we have to listen and check if a data were sent to be printed.
    void beginListenForData() {
        try {
            final Handler handler = new Handler();
            // This is the ASCII code for a newline character
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        /*
                                         * final String data = new String(encodedBytes, "US-ASCII"); readBufferPosition
                                         * = 0; handler.post(new Runnable() { public void run() { myLabel.setText(data);
                                         * } });
                                         */
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                    }
                }
            });
            workerThread.start();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Print title formatted
    boolean printTitle(CallbackContext callbackContext, String msg, Integer size, Integer align) throws IOException {
        try {
            byte[] new_size = selFontSize(size);
            byte[] new_align = selAlignTitle(align);
            mmOutputStream.write(new_size);
            mmOutputStream.write(new_align);
            mmOutputStream.write(msg.getBytes(encoding));
            resetDefaultFontAlign();
            Log.d(LOG_TAG, "PRINT TITLE SEND " + msg);
            callbackContext.success("PRINT TITLE SEND" + msg);
            return true;

        } catch (IOException e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // size text
    private static byte[] selFontSize(int size) {
        byte[] char_size = CHAR_SIZE_00;
        switch (size) {
        case 1:
            char_size = CHAR_SIZE_01;
            break;
        case 8:
            char_size = CHAR_SIZE_08;
            break;
        case 10:
            char_size = CHAR_SIZE_10;
            break;
        case 11:
            char_size = CHAR_SIZE_11;
            break;
        case 20:
            char_size = CHAR_SIZE_20;
            break;
        case 30:
            char_size = CHAR_SIZE_30;
            break;
        case 31:
            char_size = CHAR_SIZE_31;
            break;
        case 51:
            char_size = CHAR_SIZE_51;
            break;
        case 61:
            char_size = CHAR_SIZE_61;
            break;
        }
        return char_size;
    }

    // align text
    private static byte[] selAlignTitle(int align) {
        byte[] char_align = ESC_ALIGN_LEFT;
        switch (align) {
        case 0:
            char_align = ESC_ALIGN_LEFT;
            break;
        case 1:
            char_align = ESC_ALIGN_CENTER;
            break;
        case 2:
            char_align = ESC_ALIGN_RIGHT;
            break;
        }
        return char_align;
    }

    private void resetDefaultFontAlign() {
        try {
            byte[] linefeed = new byte[] { 0x0A };
            byte[] char_padrao = new byte[] { 0x1B, 0x21, 0x00 };
            byte[] align_padrao = new byte[] { 0x1B, 0x61, 0x00 };

            mmOutputStream.write(linefeed);
            mmOutputStream.write(char_padrao);
            mmOutputStream.write(align_padrao);
            mmOutputStream.flush();
            Log.d(LOG_TAG, "RESET FONTS");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    boolean printText(CallbackContext callbackContext, String msg) throws IOException {
        try {
            // CANCEL CHINESE CHARACTER
            // mmOutputStream.write(0x1C);
            // mmOutputStream.write(0x2E);
            //
            // mmOutputStream.write(0x1B);
            // mmOutputStream.write(0x74);
            // mmOutputStream.write(0x10);
            // -------------------------
            // Select character code table (ESC t n) - n = 16(0x10) for WPC1252
            mmOutputStream.write(msg.getBytes(encoding));
            // tell the user data were sent
            Log.d(LOG_TAG, "PRINT TEXT SEND -> " + msg);
            callbackContext.success("PRINT TEXT SEND");
            return true;

        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // This will send data to bluetooth printer
    boolean printTextSizeAlign(CallbackContext callbackContext, String msg, Integer size, Integer align)
            throws IOException {
        try {
            // set unicode
            byte[] new_size = selFontSize(size);
            byte[] new_align = selAlignTitle(align);
            mmOutputStream.write(new_size);
            mmOutputStream.write(new_align);
            mmOutputStream.write(msg.getBytes(encoding));
            resetDefaultFontAlign();
            Log.d(LOG_TAG, "PRINT TEXT SENT " + msg);
            callbackContext.success("PRINT TEXT SENT");
            return true;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    boolean printPOSCommand(CallbackContext callbackContext, byte[] buffer) throws IOException {
        try {
            mmOutputStream.write(buffer);
            // tell the user data were sent
            Log.d(LOG_TAG, "PRINT POS COMMAND SENT");
            callbackContext.success("Data Sent");
            return true;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // disconnect bluetooth printer.
    boolean disconnectBT(CallbackContext callbackContext) throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            callbackContext.success("BLUETOOTH DISCONNECT");
            return true;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // send url from image
    boolean printImageUrl(CallbackContext callbackContext, String msg, Integer align) throws IOException {
        try {
            Log.d(LOG_TAG, "Preparar para impressao, passando o caminho da imagem -> " + msg);
            Log.d(LOG_TAG, "ALIGN -> " + align);
            Bitmap bmp = BitmapFactory.decodeFile(msg);
            if (bmp != null) {
                byte[] command = decodeBitmapUrl(bmp);
                Log.d(LOG_TAG, "SWITCH ALIGN -> " + align);
                switch (align) {
                case 0:
                    printLeftImage(command);
                    break;
                case 1:
                    printCenterImage(command);
                    break;
                case 2:
                    printRightImage(command);
                    break;
                }
            } else {
                Log.d(LOG_TAG, "PRINT PHOTO ERROR THE FILE ISN'T EXISTS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "PRINT TOOLS THE FILE ISN'T EXISTS");
        }
        return false;
    }

    // print image align left
    public void printLeftImage(byte[] msg) {
        try {
            Log.d(LOG_TAG, "PRINT LEFT IMAGE");
            mmOutputStream.write(ESC_ALIGN_LEFT);
            mmOutputStream.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "ERRO PRINT LEFT IMAGE");
        }
    }

    // print image align center
    public void printCenterImage(byte[] msg) {
        try {
            Log.d(LOG_TAG, "PRINT CENTER IMAGE");
            mmOutputStream.write(ESC_ALIGN_CENTER);
            mmOutputStream.write(msg);
            // return to left position
            mmOutputStream.write(ESC_ALIGN_LEFT);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "ERRO PRINT CENTER IMAGE");
        }
    }

    // print image align right
    public void printRightImage(byte[] msg) {
        try {
            Log.d(LOG_TAG, "PRINT RIGHT IMAGE");
            mmOutputStream.write(ESC_ALIGN_RIGHT);
            mmOutputStream.write(msg);
            // return to left position
            mmOutputStream.write(ESC_ALIGN_LEFT);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "ERRO PRINT RIGHT IMAGE");
        }
    }

    // This will send data to bluetooth printer
    boolean printBase64(CallbackContext callbackContext, String msg, Integer align) throws IOException {
        try {

            final String encodedString = msg;
            final String pureBase64Encoded = encodedString.substring(encodedString.indexOf(",") + 1);
            final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);

            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            bitmap = decodedBitmap;
            int mWidth = bitmap.getWidth();
            int mHeight = bitmap.getHeight();

            bitmap = resizeImage(bitmap, 48 * 8, mHeight);

            byte[] bt = decodeBitmapBase64(bitmap);

            // not work
            Log.d(LOG_TAG, "SWITCH ALIGN BASE64 -> " + align);
            switch (align) {
            case 0:
                mmOutputStream.write(ESC_ALIGN_LEFT);
                mmOutputStream.write(bt);
                break;
            case 1:
                mmOutputStream.write(ESC_ALIGN_CENTER);
                mmOutputStream.write(bt);
                break;
            case 2:
                mmOutputStream.write(ESC_ALIGN_RIGHT);
                mmOutputStream.write(bt);
                break;
            }
            // tell the user data were sent
            Log.d(LOG_TAG, "PRINT BASE64 SEND");
            callbackContext.success("PRINT BASE64 SEND");
            return true;

        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    boolean printQRCode(CallbackContext callbackContext, String data, int align, int model, int size, int eclevel) throws IOException {
        try {
            // Print QR code
			final String qr_pL = Character.toString((char)((data.length() + 3) % 256));
			final String qr_pH = Character.toString((char)((data.length() + 3) / 256));

			// Set alignment
			byte[] new_align = selAlignTitle(align);
            mmOutputStream.write(new_align);

			// Set QR model - https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=140
			byte qr_model = Integer.toHexString(model).getBytes()[0];
			mmOutputStream.write(new byte[]{ 0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, qr_model, 0x00 });

			// Set QR size - https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=141
			byte qr_size = Integer.toHexString(size).getBytes()[0];
			mmOutputStream.write(new byte[]{ 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, qr_size });

			// Set error correction level - https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=142
			byte qr_eclevel = Integer.toHexString(eclevel).getBytes()[0];
			mmOutputStream.write(new byte[]{ 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, qr_eclevel });

			// Store the data in the symbol storage area - https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=143
			mmOutputStream.write(new byte[]{ 0x1D, 0x28, 0x6B });
			mmOutputStream.write(qr_pL.getBytes());
			mmOutputStream.write(qr_pH.getBytes());
			mmOutputStream.write(new byte[]{ 0x31, 0x50, 0x30 });
			mmOutputStream.write(data.getBytes());

			// Print the symbol data in the symbol storage area
			mmOutputStream.write(new byte[]{ 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30 });

			// Reset align
			mmOutputStream.write(ESC_ALIGN_LEFT);

            Log.d(LOG_TAG, "PRINT QRCODE SENT");
            callbackContext.success("PRINT QRCODE SENT");
            return true;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    boolean printBarcode(CallbackContext callbackContext, int system, String data, int align, int position, int font, int height) throws IOException {
        try {
			// Validate barcode data
			byte[] bc_system = null;
			switch (system) {
				case 0:
					Integer[] valid_sizes_0 = { 11, 12 };
					if (!TextUtils.isDigitsOnly(data)) {
						callbackContext.error("Invalid data type: UPC-A expects numbers 0-9");
						return false;
					} else if (!Arrays.asList(valid_sizes_0).contains(data.length())) {
						callbackContext.error("Invalid data length: UPC-A requires 11-12 characters");
						return false;
					}else{
						bc_system = BARCODE_UPC_A;
					}
					break;
				case 1:
					Integer[] valid_sizes_1 = { 6, 7, 8, 11, 12 };
					if (!TextUtils.isDigitsOnly(data)) {
						callbackContext.error("Invalid data type: UPC-E expects numbers 0-9");
						return false;
					} else if (!Arrays.asList(valid_sizes_1).contains(data.length())) {
						callbackContext.error("Invalid data length: UPC-E requires 6-8 or 11-12 characters");
						return false;
					}else{
						bc_system = BARCODE_UPC_E;
					}
					break;
				case 2:
					Integer[] valid_sizes_2 = { 12, 13 };
					if (!TextUtils.isDigitsOnly(data)) {
						callbackContext.error("Invalid data type: EAN13 expects numbers 0-9");
						return false;
					} else if (!Arrays.asList(valid_sizes_2).contains(data.length())) {
						callbackContext.error("Invalid data length: EAN13 requires 12-13 characters");
						return false;
					}else{
						bc_system = BARCODE_EAN13;
					}
					break;
				case 3:
					Integer[] valid_sizes_3 = { 7, 8 };
					if (!TextUtils.isDigitsOnly(data)) {
						callbackContext.error("Invalid data type: EAN8 expects numbers 0-9");
						return false;
					} else if (!Arrays.asList(valid_sizes_3).contains(data.length())) {
						callbackContext.error("Invalid data length: EAN8 requires 7-8 characters");
						return false;
					}else{
						bc_system = BARCODE_EAN8;
					}
					break;
				case 4:
					if (data.length() > 255) {
						callbackContext.error("Data length too long for CODE39");
						return false;
					}else{
						bc_system = BARCODE_CODE39;
					}
					break;
				case 5:
					if (!TextUtils.isDigitsOnly(data)) {
						callbackContext.error("Invalid data type: ITF expects numbers 0-9");
						return false;
					} else if (data.length() < 2) {
						callbackContext.error("Invalid data length: ITF requires 2+ characters");
						return false;
					}else{
						bc_system = BARCODE_ITF;
					}
					break;
				case 6:
					if (data.length() < 2) {
						callbackContext.error("Invalid data length: CODABAR requires 2+ characters");
						return false;
					}else{
						bc_system = BARCODE_CODABAR;
					}
					break;
			}

			// Set alignment
			byte[] new_align = selAlignTitle(align);
            mmOutputStream.write(new_align);

			// Select print position of HRI characters
			byte bc_position = Integer.toHexString(position).getBytes()[0];
			mmOutputStream.write(new byte[]{ 0x1D, 0x48, bc_position });

			// Select font for HRI characters
			byte bc_font = Integer.toHexString(font).getBytes()[0];
			mmOutputStream.write(new byte[]{ 0x1D, 0x66, bc_font });

			// Set barcode height
			byte bc_height = Integer.toHexString(height).getBytes()[0];
			mmOutputStream.write(new byte[]{ 0x1D, 0x68, bc_height });

			// Set barcode system and print
			mmOutputStream.write(bc_system);
			mmOutputStream.write(data.getBytes());
			mmOutputStream.write(0x00);

			// Reset align
			mmOutputStream.write(ESC_ALIGN_LEFT);

            Log.d(LOG_TAG, "PRINT BARCODE SENT");
            callbackContext.success("PRINT BARCODE SENT");
            return true;
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }

    // New implementation
    private static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();

        if (width > w) {
            float scaleWidth = ((float) w) / width;
            float scaleHeight = ((float) h) / height + 24;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleWidth);
            Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
            return resizedBitmap;
        } else {
            Bitmap resizedBitmap = Bitmap.createBitmap(w, height + 24, Config.RGB_565);
            Canvas canvas = new Canvas(resizedBitmap);
            Paint paint = new Paint();
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, (w - width) / 2, 0, paint);
            return resizedBitmap;
        }
    }

    public static byte[] decodeBitmapBase64(Bitmap bmp) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        List<String> list = new ArrayList<String>(); // binaryString list
        StringBuffer sb;
        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;
        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }

        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i);

                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160) {
                    sb.append("0");
                } else {
                    sb.append("1");
                }
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }

        List<String> bmpHexList = binaryListToHexStringList(list);
        String commandHexString = "1D763000";

        // construct xL and xH
        // there are 8 pixels per byte. In case of modulo: add 1 to compensate.
        bmpWidth = bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1);
        int xL = bmpWidth % 256;
        int xH = (bmpWidth - xL) / 256;

        String xLHex = Integer.toHexString(xL);
        String xHHex = Integer.toHexString(xH);
        if (xLHex.length() == 1) {
            xLHex = "0" + xLHex;
        }
        if (xHHex.length() == 1) {
            xHHex = "0" + xHHex;
        }
        String widthHexString = xLHex + xHHex;

        // construct yL and yH
        int yL = bmpHeight % 256;
        int yH = (bmpHeight - yL) / 256;

        String yLHex = Integer.toHexString(yL);
        String yHHex = Integer.toHexString(yH);
        if (yLHex.length() == 1) {
            yLHex = "0" + yLHex;
        }
        if (yHHex.length() == 1) {
            yHHex = "0" + yHHex;
        }
        String heightHexString = yLHex + yHHex;

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString + widthHexString + heightHexString);
        commandList.addAll(bmpHexList);

        return hexList2Byte(commandList);
    }

    public static byte[] decodeBitmapUrl(Bitmap bmp) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> list = new ArrayList<String>(); // binaryString list
        StringBuffer sb;

        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;

        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }

        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i);

                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160) {
                    sb.append("0");
                } else {
                    sb.append("1");
                }
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }

        List<String> bmpHexList = binaryListToHexStringList(list);
        String commandHexString = "1D763000";
        String widthHexString = Integer.toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", " width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 2) {
            Log.e("decodeBitmap error", " height is too large");
            return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString + widthHexString + heightHexString);
        commandList.addAll(bmpHexList);

        return hexList2Byte(commandList);
    }

    public static List<String> binaryListToHexStringList(List<String> list) {
        List<String> hexList = new ArrayList<String>();
        for (String binaryStr : list) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);

                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            hexList.add(sb.toString());
        }
        return hexList;

    }

    public static String myBinaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i])) {
                hex += hexStr.substring(i, i + 1);
            }
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i])) {
                hex += hexStr.substring(i, i + 1);
            }
        }

        return hex;
    }

    private static String hexStr = "0123456789ABCDEF";

    private static String[] binaryArray = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000",
            "1001", "1010", "1011", "1100", "1101", "1110", "1111" };

    public static byte[] hexList2Byte(List<String> list) {
        List<byte[]> commandList = new ArrayList<byte[]>();

        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        byte[] bytes = sysCopy(commandList);
        return bytes;
    }

    // New implementation, change old
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }
}
