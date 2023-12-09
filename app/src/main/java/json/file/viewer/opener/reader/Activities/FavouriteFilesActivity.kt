package json.file.viewer.opener.reader.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import json.file.viewer.opener.reader.Adaptor.FavFilesAdapter
import json.file.viewer.opener.reader.ModelClasses.JsonModelClass
import json.file.viewer.opener.reader.R
import json.file.viewer.opener.reader.databinding.ActivityFavouriteFilesBinding
import io.paperdb.Paper
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class FavouriteFilesActivity : AppCompatActivity() {
    lateinit var view: ActivityFavouriteFilesBinding
    private lateinit var arrayList: ArrayList<JsonModelClass>
    lateinit var adapter: FavFilesAdapter
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view= ActivityFavouriteFilesBinding.inflate(layoutInflater)
        setContentView(view.root)
        arrayList = ArrayList()

        executor.execute {
            getData()
            handler.post{
                if (arrayList.size < 1) {
                    view.nofileTV.visibility = View.VISIBLE
                } else {
                    view.nofileTV.visibility = View.GONE
                    view.favRV.layoutManager = LinearLayoutManager(this@FavouriteFilesActivity)
                    view.favRV.setHasFixedSize(true)
                    arrayList.reverse()
                    adapter = FavFilesAdapter(this@FavouriteFilesActivity, arrayList, "fav")
                    view.favRV.adapter = adapter
                }
            }
        }

        view.searchBoxContainer.clearSearchQueryBtn.setOnClickListener {
            val text=view.searchBoxContainer.searchEditText.text.toString()
            Log.e("ffftxxxt", "onCreate: "+text )

            if (text.equals("")) {
                view.searchBoxContainer.searchCardView.visibility = View.GONE
                view.searchBtn.visibility = View.VISIBLE
            } else {
                view.searchBoxContainer.searchEditText.text = null

            }
        }
        view.searchBtn.setOnClickListener {
            view.searchBoxContainer.searchCardView.visibility = View.VISIBLE
        }

        view.searchBoxContainer.searchEditText.addTextChangedListener(object : TextWatcher {
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
        view.backBtn.setOnClickListener { onBackPressed() }

    }

    fun getData() {
        val getlist: ArrayList<String>? = Paper.book().read("fav", ArrayList())
        for (i in 0 until getlist!!.size) {
            val file = File(getlist[i])
            val file_Name = file.name
            val size = getStringSizeLengthFile(file.length())
            val file_Path = file.absolutePath
            val date = file.lastModified()
//            val fileDate = getDate(date)
            val filess = File(file_Path)
            val filePath = filess.exists()
            var fileDate=getDate(getInstalledDate())
            if (filePath) {
                arrayList.add(JsonModelClass(file_Name, size, file_Path.toString(),fileDate))
            }
        }
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

    fun getDate(dateee: Long): String {
        // Create a DateFormatter object for displaying date in specified format.
        val dateFormat = "dd-MM-yyyy"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = dateee
        return formatter.format(calendar.time)
    }

    fun getInstalledDate():Long{
        val installed: Long = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime
        return installed
    }


}