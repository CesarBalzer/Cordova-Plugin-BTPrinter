var exec = require('cordova/exec');

var BTPrinter = {
    status: function (fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "status", []);
    },
    list: function (fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "list", []);
    },
    connect: function (fnSuccess, fnError, name) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "connect", [name]);
    },
    connected: function (fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "connected", []);
    },
    disconnect: function (fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "disconnect", []);
    },
    setEncoding: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "setEncoding", [str]);
    },
    printText: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printText", [str]);
    },
    printTextSizeAlign: function (fnSuccess, fnError, str, size, align) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printTextSizeAlign", [str, size, align]);
    },
    printTitle: function (fnSuccess, fnError, str, size, align) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printTitle", [str, size, align]);
    },
    printImageUrl: function (fnSuccess, fnError, str, align) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printImageUrl", [str, align]);
    },
    printBase64: function (fnSuccess, fnError, str, align) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printBase64", [str, align]);
    },
    printPOSCommand: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printPOSCommand", [str]);
    },
    printQRCode: function (fnSuccess, fnError, data, align, model, size, eclevel) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printQRCode", [data, align, model, size, eclevel]);
    },
    printBarcode: function (fnSuccess, fnError, system, data, align, position, font, height) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printBarcode", [system, data, align, position, font, height]);
    }
};

module.exports = BTPrinter;
