# Cordova-Plugin-BTPrinter

A cordova plugin for bluetooth printer for android platform.

This code is being adapted from a fork of [Cordova-Plugin-Bluetooth-Printer](https://github.com/srehanuddin/Cordova-Plugin-Bluetooth-Printer), of free use and modifications that will arise for the improvement of the plugin.

Suggestions, critiques are welcome, participate and send a commit helping to improve the plugin for the community.

Thanks!

## Support

- [ Text simple](#Print-simple-text)
- [ Print text with size and align](#Print-text-with-size-and-align)
- [ Print image from path with align](#Print-image-from-path-with-align)
- [ Print image from base64 with align](#Print-image-from-base64-with-align)
- [ Print title with size and align](#Print-title-with-size-and-align)
- [ POS printing](#POS-printing)
- [ Print QRCode ](#Print-QRCode)
- [ Print Barcode ](#Print-Barcode)

## Install

Using the Cordova CLI and NPM, run:

```
cordova plugin add https://github.com/CesarBalzer/Cordova-Plugin-BTPrinter.git
```

## Usage

Get list of paired bluetooth devices, including printers, if any:

```javascript
BTPrinter.list(function(data){
        console.log("Success");
        console.log(data); // paired bluetooth devices array
    },function(err){
        console.log("Error");
        console.log(err);
    });
```

Returns an array with the format:

```
data[0] = Device 1 name
data[1] = Device 1 MAC address
data[2] = Device 1 type
data[3] = Device 2 name
data[4] = Device 2 MAC address
data[5] = Device 2 type
...
```
Where device **name** is the required string to connect to the printer.

### Check Bluetooth status

```javascript
BTPrinter.status(function(data){
	console.log("Success");
	console.log(data) // bt status: true or false
},function(err){
	console.log("Error");
	console.log(err)
});
```

### Connect printer

```javascript
BTPrinter.connect(function(data){
	console.log("Success");
	console.log(data)
},function(err){
	console.log("Error");
	console.log(err)
}, "PrinterName");
```

### Check if printer is connected

```javascript
BTPrinter.connected(function(data){
	console.log("Success");
	console.log(data) // connected: true or false
},function(err){
	console.log("Error");
	console.log(err);
});
```

### Disconnect printer

```javascript
BTPrinter.disconnect(function(data){
	console.log("Success");
	console.log(data)
},function(err){
	console.log("Error");
	console.log(err)
}, "PrinterName");
```

#### Obs:

I thought it best to create the function within a timeout

```javascript
setTimeout(function(){
    BTPrinter.disconnect(function(data){
	console.log("Success");
	console.log(data)
    },function(err){
	console.log("Error");
	console.log(err)
    }, "PrinterName")
}, 1500);
```

### Set text encoding

```javascript
BTPrinter.setEncoding(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "ISO-8859-1")
```
Refer to printer's manual for supported encodings and codepages.

### Print simple text

```javascript
BTPrinter.printText(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "String to Print")
```

### Print text with size and align

```javascript
BTPrinter.printTextSizeAlign(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "String to Print",'0','0')//string, size, align
```

### Print image from path with align

```javascript
BTPrinter.printImageUrl(function(data){
    console.log("Success");
    console.log(data);
},function(err){
    console.log("Error");
    console.log(err);
}, "Path String",'0');//path image, align
```

In android tests with /storage/emulated/0/Pictures/myfolder/myimage.jpg - size max: 300x300px.

### Print image from base64 with align

- with align still in tests not work alignment)

```javascript
BTPrinter.printBase64(function(data){
    console.log("Success");
    console.log(data);
},function(err){
    console.log("Error");
    console.log(err);
}, "Image Base64 String",'0');//base64 string, align
```

### Print title with size and align

- with align still in tests not work alignment)

```javascript
BTPrinter.printTitle(function(data){
    console.log("Success");
    console.log(data);
},function(err){
    console.log("Error");
    console.log(err);
}, "String text",'0');//string, size, align
```

### POS printing

```javascript
BTPrinter.printPOSCommand(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "0C");//OC is a POS command for page feed
```

### Print QRCode

```javascript
var data = "https://github.com/CesarBalzer/Cordova-Plugin-BTPrinter";
var align = 1; /* 0, 1, 2 */
var model = 49; /* https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=140 */
var size = 32; /* https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=141 */
var eclevel = 50; /* https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=142 */
BTPrinter.printQRCode(function(data){
    console.log("Success");
    console.log(data);
},function(err){
    console.log("Error");
    console.log(err);
}, data, align, model, size, eclevel);
```

### Print Barcode

```javascript
var system = 0; /* Barcode system, defined as "m" at https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=128 */
var data = "012345678901"; /* Barcode data, according to barcode system */
var align = 1; /* 0, 1, 2 */
var position = 2; /* Text position: https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=125 */;
var font = 0; /* Font for HRI characters: https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=126 */
var height = 64; /* Set barcode height: https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=127*/
BTPrinter.printBarcode(function(data){
    console.log("Success");
    console.log(data);
},function(err){
    console.log("Error");
    console.log(err);
}, system, data, align, position, font, height);
```

Notice that UPC-A, UPC-E, EAN13 and ITF accepts only:
```
Numbers 0-9
```
Sending other characters will return a plugin error with the proper description.

CODE39 accepts:
```
0 – 9, A – Z, SP, $, %, *, +, -, ., /
```
CODABAR accepts:
```
0 – 9, A – D, a – d, $, +, −, ., /, :
```

## Size options

```javascript
     0 = CHAR_SIZE_00 // equivalent 0x1B, 0x21, 0x00 : Normal size
     1 = CHAR_SIZE_01 // equivalent 0x1B, 0x21, 0x01 : Reduzided width
     8 = CHAR_SIZE_08 // equivalent 0x1B, 0x21, 0x08 : bold normal size
    10 = CHAR_SIZE_10 // equivalent 0x1B, 0x21, 0x10 : Double height size
    11 = CHAR_SIZE_11 // equivalent 0x1B, 0x21, 0x11 : Reduzided Double height size
    20 = CHAR_SIZE_20 // equivalent 0x1B, 0x21, 0x20 : Double width size
    30 = CHAR_SIZE_30 // equivalent 0x1B, 0x21, 0x30
    31 = CHAR_SIZE_31 // equivalent 0x1B, 0x21, 0x31
    51 = CHAR_SIZE_51 // equivalent 0x1B, 0x21, 0x51
    61 = CHAR_SIZE_61 // equivalent 0x1B, 0x21, 0x61
```

## Align options

```javascript
    0 = ESC_ALIGN_LEFT // equivalent 0x1B, 0x61, 0x00
    1 = ESC_ALIGN_CENTER // equivalent 0x1B, 0x61, 0x01
    2 = ESC_ALIGN_RIGHT // equivalent 0x1B, 0x61, 0x02
```

## Plugin Demo App

To test most of the plugin's functions, you can use the free [BTPrinter Plugin Demo app](https://www.andreszsogon.com/cordova-bluetooth-printer-plugin-demo-app/) by Andrés Zsögön, and inspect the source code for more details.

[![DemoApp](https://www.andreszsogon.com/wp-content/uploads/printer-btplugin-demo-app-1.png)](https://www.andreszsogon.com/cordova-bluetooth-printer-plugin-demo-app/)
[![DemoApp](https://www.andreszsogon.com/wp-content/uploads/printer-btplugin-demo-app-2.png)](https://www.andreszsogon.com/cordova-bluetooth-printer-plugin-demo-app/)

## Sample receipt

The following sample receipt was printed with the plugin demo app using a generic portable bluetooth printer:

![Receipt](https://www.andreszsogon.com/wp-content/uploads/btplugin-demo-app.png)
