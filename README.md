# Cordova-Plugin-BTPrinter

A cordova plugin for bluetooth printer for android platform.

This code is being adapted from a fork of [Cordova-Plugin-Bluetooth-Printer](https://github.com/srehanuddin/Cordova-Plugin-Bluetooth-Printer), of free use and modifications that will arise for the improvement of the plugin.

Suggestions, critiques are welcome, participate and send a commit helping to improve the plugin for the community.

Thank's!

## Support

- [ Text simple](#Print-simple-text)
- [ Print text with size and align](#Print-text-with-size-and-align)
- [ Print image from path with align](#Print-image-from-path-with-align)
- [ Print image from base64 with align](#Print-image-from-base64-with-align)
- [ Print title with size and align](#Print-title-with-size-and-align)
- [ POS printing](#POS-printing)
- [ Print QRCode ](#Print-QRCode)

## Install

Using the Cordova CLI and NPM, run:

```
cordova plugin add https://github.com/CesarBalzer/Cordova-Plugin-BTPrinter.git
```

## Usage

Get list of paired bluetooth printers

```
BTPrinter.list(function(data){
        console.log("Success");
        console.log(data); //list of printer in data array
    },function(err){
        console.log("Error");
        console.log(err);
    });
```

Return:

```
data[0] Printer name
data[1] Printer address
data[2] Printer type
```

### Check Bluetooth status

```
BTPrinter.status(function(data){
	console.log("Success");
	console.log(data) // bt status: true or false
},function(err){
	console.log("Error");
	console.log(err)
});
```

### Connect printer

```
BTPrinter.connect(function(data){
	console.log("Success");
	console.log(data)
},function(err){
	console.log("Error");
	console.log(err)
}, "PrinterName");
```

### Disconnect printer

```
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

```
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

### Print simple text

```
BTPrinter.printText(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "String to Print")
```

### Print text with size and align

```
BTPrinter.printTextSizeAlign(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "String to Print",'0','0')//string, size, align
```

### Print image from path with align

```
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

```
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

```
BTPrinter.printTitle(function(data){
    console.log("Success");
    console.log(data);
},function(err){
    console.log("Error");
    console.log(err);
}, "String text",'0');//string, size, align
```

### POS printing

```
BTPrinter.printPOSCommand(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "0C");//OC is a POS command for page feed
```

### Print QRCode

- not working

```
BTPrinter.printQRCode(function(data){
    console.log("Success");
    console.log(data)
},function(err){
    console.log("Error");
    console.log(err)
}, "https://github.com/CesarBalzer/Cordova-Plugin-BTPrinter");//string to qrcode
```

The best option I found was to use this [albertorcf](https://github.com/srehanuddin/Cordova-Plugin-Bluetooth-Printer/issues/24#issue-201362448):

```
    var justify_center = '\x1B\x61\x01';
    var justify_left   = '\x1B\x61\x00';
    var qr_model       = '\x32';          // 31 or 32
    var qr_size        = '\x08';          // size
    var qr_eclevel     = '\x33';          // error correction level (30, 31, 32, 33 - higher)
    var qr_data        = 'https://github.com/CesarBalzer/Cordova-Plugin-BTPrinter';
    var qr_pL          = String.fromCharCode((qr_data.length + 3) % 256);
    var qr_pH          = String.fromCharCode((qr_data.length + 3) / 256);
    //
    BTPrinter.printText(null,null, justify_center +
       '\x1D\x28\x6B\x04\x00\x31\x41' + qr_model + '\x00' +        // Select the model
       '\x1D\x28\x6B\x03\x00\x31\x43' + qr_size +                  // Size of the model
       '\x1D\x28\x6B\x03\x00\x31\x45' + qr_eclevel +               // Set n for error correction
       '\x1D\x28\x6B' + qr_pL + qr_pH + '\x31\x50\x30' + qr_data + // Store data
       '\x1D\x28\x6B\x03\x00\x31\x51\x30' +                        // Print
       '\n\n\n' +
       justify_left,'1','0');
```

## Size options

```
     0 = CHAR_SIZE_01 // equivalent 0x1B, 0x21, 0x00
     8 = CHAR_SIZE_08 // equivalent 0x1B, 0x21, 0x08
    10 = CHAR_SIZE_10 // equivalent 0x1B, 0x21, 0x10
    11 = CHAR_SIZE_11 // equivalent 0x1B, 0x21, 0x11
    20 = CHAR_SIZE_20 // equivalent 0x1B, 0x21, 0x20
    30 = CHAR_SIZE_30 // equivalent 0x1B, 0x21, 0x30
    31 = CHAR_SIZE_31 // equivalent 0x1B, 0x21, 0x31
    51 = CHAR_SIZE_51 // equivalent 0x1B, 0x21, 0x51
    61 = CHAR_SIZE_61 // equivalent 0x1B, 0x21, 0x61
```

## Align options

```
    0 = ESC_ALIGN_LEFT // equivalent 0x1B, 0x61, 0x00
    1 = ESC_ALIGN_CENTER // equivalent 0x1B, 0x61, 0x01
    2 = ESC_ALIGN_RIGHT // equivalent 0x1B, 0x61, 0x02
```
