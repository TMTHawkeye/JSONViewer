package json.file.viewer.opener.reader.Activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import json.file.viewer.opener.reader.Adaptor.FileAdaptor
import json.file.viewer.opener.reader.ModelClasses.JsonModelClass
import json.file.viewer.opener.reader.databinding.ActivityConvertedFilesBinding
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class ConvertedFilesActivity : AppCompatActivity() {
    lateinit var binding: ActivityConvertedFilesBinding
    lateinit var filesList: ArrayList<JsonModelClass>
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())

    lateinit var adapter: FileAdaptor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Inflate the layout for this fragment
        binding = ActivityConvertedFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.backBtn.setOnClickListener{
            finish()
        }

        val directory = File(getExternalFilesDir("PDF Files"), "")
        filesList = ArrayList()
        executor.execute {
            exploreForFiles(directory)
            handler.post {
                if (filesList.size > 0) {
                    binding.pdfFileRV.layoutManager = LinearLayoutManager(this)
                    binding.pdfFileRV.setHasFixedSize(true)
                    filesList.reverse()
                    adapter = FileAdaptor(this, filesList, "pdf")
                    binding.pdfFileRV.adapter = adapter
                    binding.nofileTV.visibility = View.GONE
                } else {
                    runOnUiThread {
                        binding.nofileTV.visibility = View.VISIBLE
                        binding.nofileIV.visibility = View.VISIBLE
                    }
                }
            }
        }


        binding.searchBoxContainer.clearSearchQueryBtn.setOnClickListener {
            val text=binding.searchBoxContainer.searchEditText.text.toString()
            Log.e("ffftxxxt", "onCreate: "+text )

            if (text.equals("")) {
                binding.searchBoxContainer.searchCardView.visibility = View.GONE
                binding.searchBtn.visibility = View.VISIBLE
            } else {
                binding.searchBoxContainer.searchEditText.text = null

            }
        }
        binding.searchBtn.setOnClickListener {
            binding.searchBoxContainer.searchCardView.visibility = View.VISIBLE
        }

        binding.searchBoxContainer.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                // filter your list from your input
                adapter.getFilter().filter(s)
                //  filter(s.toString())
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        })
    }

    private fun exploreForFiles(dir: File) {
        val fileList = dir.listFiles()
        if (fileList != null) {

            for (i in fileList.indices) {
                if (fileList[i].isDirectory) {
                    exploreForFiles(fileList[i])
                } else {
                    if (fileList[i].name.endsWith(".pdf")) {
                        val fileName = fileList[i].name
                        val fileSize: Long = (fileList[i].length())
                        val finalSize=getStringSizeLengthFile(fileSize)
                        val filePath = fileList[i].absolutePath
                        var longDate=getInstalledDate().toString()
                        filesList.add(JsonModelClass(fileName, finalSize.toString(), filePath,getDate(longDate)))
                    }
                }
            }
        }

    }

    fun getInstalledDate():Long{
        val installed: Long = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime
        return installed
    }

    fun getDate(datee: String): String {
        // Create a DateFormatter object for displaying date in specified format.
        val dateFormat = "dd-MM-yyyy"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        var dateee=datee.toLong()
        calendar.timeInMillis = dateee
        return formatter.format(calendar.time)
    }

    fun getStringSizeLengthFile(size: Long): String {
        val df = DecimalFormat("0")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb) return (df.format((size / sizeKb).toDouble()*1024) ).toString()+ " Kb"
        else if (size < sizeGb) return df.format(
            (size / sizeMb).toDouble()
        ) + " Mb" else if (size < sizeTerra) return df.format((size / sizeGb).toDouble()) + " Gb"
        return ""
    }


}