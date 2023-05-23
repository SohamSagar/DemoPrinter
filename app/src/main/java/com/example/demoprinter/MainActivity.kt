package com.example.demoprinter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.epson.epos2.Epos2Exception
import com.epson.epos2.discovery.Discovery
import com.epson.epos2.discovery.DiscoveryListener
import com.epson.epos2.discovery.FilterOption
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener
import com.example.demoprinter.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.HashMap

class MainActivity : AppCompatActivity(), ReceiveListener {

    private var receiptTargetPrinter = ""
    private lateinit var binding:ActivityMainBinding

    private var mPrinterList: java.util.ArrayList<HashMap<String, String>>? = null
    lateinit var mFilterOption: FilterOption
    private var target: String = ""
    lateinit var mPrinter: Printer
    private val DISCONNECT_INTERVAL = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        printerSdk()

        binding.btnPrint.setOnClickListener {
            val t = object : Thread() {
                override fun run() {
                    initializeObject()
                    finalizeObject()
                    runPrintReceiptSequence()
                }
            }
            t.start()
        }
    }

    fun initializeObject(): Boolean {
            try {
                mPrinter = Printer(Printer.TM_U220, 0, this)
            } catch (e: java.lang.Exception) {
                //ShowMsg.showException(e, "Printer", mContext)
                return false
            }

        mPrinter.setReceiveEventListener(this)
        return true
    }

    private fun finalizeObject() {
        if (mPrinter == null) {
            return
        }
        mPrinter.setReceiveEventListener(null)
        //mPrinter = null
    }

    private fun runPrintReceiptSequence(): Boolean {

            if (!printCodeNew()) {
                return false
            }
            if (!printCodeNewKitchen()) {
                return false
            }

        return printData()
    }

    fun printCodeNewKitchen() : Boolean {
        var method = ""
        var textData = StringBuilder()

        if (mPrinter == null) {
            return false
        }
        try {

            mPrinter.addTextAlign(Printer.ALIGN_CENTER)
            mPrinter.addTextStyle(0, 0, 0, 1)
            mPrinter.addText(textData.toString())
            textData.delete(0, textData.length)
            mPrinter.addTextAlign(Printer.ALIGN_LEFT)
            mPrinter.addText(textData.toString())
            textData.delete(0, textData.length)
            mPrinter.addTextStyle(0, 0, 0, 1)

            Log.e("PrintData",mPrinter.toString())

            mPrinter.addTextAlign(Printer.ALIGN_LEFT)
            mPrinter.addTextStyle(0, 0, 0, 1)
            mPrinter.addText(textData.toString())
            textData.delete(0, textData.length)
            mPrinter.addTextAlign(Printer.ALIGN_CENTER)

            mPrinter.addText(textData.toString())
            textData.delete(0, textData.length)
            mPrinter.addTextStyle(0, 0, 0, 1)
            mPrinter.addCut(Printer.CUT_FEED)

        } catch (e: Exception) {
            Log.e("Error::", e.message.toString())
            mPrinter.clearCommandBuffer()
            return false
        }

        textData = StringBuilder()

        return true
    }

    fun printerSdk() {
        mPrinterList = java.util.ArrayList()
        mFilterOption = FilterOption()
        mFilterOption.deviceType = Discovery.TYPE_PRINTER
        mFilterOption.epsonFilter = Discovery.FILTER_NAME
        mFilterOption.usbDeviceName = Discovery.TRUE

        try {
            Discovery.start(this, mFilterOption, mDiscoveryListener)
        } catch (e: Exception) {
            Log.e("SettingError", e.message.toString())
            e.printStackTrace()
        }

    }

    val mDiscoveryListener = DiscoveryListener { deviceInfo ->
        this.runOnUiThread {
            val item = HashMap<String, String>()
            item["PrinterName"] = deviceInfo.deviceName
            item["Target"] = deviceInfo.target
            item["IP_Address"] = deviceInfo.ipAddress

            if (item["Target"]!!.contains("[local_printer]")) {
                target = deviceInfo.target.replace("[local_printer]", "")
            } else {
                target = deviceInfo.target
            }

            mPrinterList!!.add(item)
            Log.e("CheckTarget",target)
            Log.e("xcelteclist", mPrinterList.toString())
        }
    }

    private fun printData(): Boolean {
        if (mPrinter == null) {
            return false
        }

        if (!connectPrinterReceipt()) {
            mPrinter.clearCommandBuffer()
            Log.e("Here123", "Here123receipt")
            return false
        }

        try {
            Log.e("Here1234", "Here1234")
            mPrinter.sendData(Printer.PARAM_DEFAULT)
        } catch (e: java.lang.Exception) {
            mPrinter.clearCommandBuffer()
            //ShowMsg.showException(e, "sendData", mContext)
            Log.e("Erorr1", "error123")
            try {
                mPrinter.disconnect()
            } catch (ex: java.lang.Exception) {
                // Do nothing
                Log.e("error", e.localizedMessage!!)
            }
            return false
        }
        disconnectPrinter()
        return true
    }

    private fun connectPrinterReceipt(): Boolean {
        if (mPrinter == null) {
            return false
        }
        try {
            mPrinter.connect("TCP:F8:D0:27:2B:E1:3E", Printer.PARAM_DEFAULT)
        } catch (e: Exception) {
            Log.e("Here134", e.toString())
            //ShowMsg.showException(e, "connect", mContext)
            return false
        }
        return true
    }

    override fun onPtrReceive(p0: Printer?, p1: Int, p2: PrinterStatusInfo?, p3: String?) {

    }

    fun printCodeNew(): Boolean {
        var method = ""
        var textData = StringBuilder()

        if (mPrinter == null) {
            return false
        }
        try {
            mPrinter.addTextFont(Printer.FONT_B)
            mPrinter.addTextSize(1, 1)

            mPrinter.addTextAlign(Printer.ALIGN_CENTER)
            mPrinter.addTextStyle(0, 0, 0, 1)
            textData.append("My First Print")
            mPrinter.addText(textData.toString())
            textData.delete(0, textData.length)
            mPrinter.addText(textData.toString())
            textData.delete(0, textData.length)

            mPrinter.addCut(Printer.CUT_FEED)

        } catch (e: Exception) {
            Log.e("Error::", e.message.toString())
            mPrinter.clearCommandBuffer()
            return false
        }

        textData = StringBuilder()

        return true
    }

    private fun disconnectPrinter() {
        if (mPrinter == null) {
            return
        }
        while (true) {
            try {
                mPrinter.disconnect()
                break
            } catch (e: java.lang.Exception) {
                if (e is Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (e.errorStatus == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL.toLong())
                        } catch (ex: java.lang.Exception) {
                        }
                    } else {
                        runOnUiThread {

                        }
                        break
                    }
                } else {
                    runOnUiThread {
                    }
                    break
                }
            }
        }
        mPrinter.clearCommandBuffer()

    }
}