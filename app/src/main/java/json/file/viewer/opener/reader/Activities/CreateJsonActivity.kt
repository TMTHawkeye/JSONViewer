package json.file.viewer.opener.reader.Activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.snackbar.Snackbar
import json.file.viewer.opener.reader.R
import json.file.viewer.opener.reader.databinding.ActivityCreateJsonBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class CreateJsonActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateJsonBinding
    lateinit var reqObj: JSONObject
    lateinit var req: JSONArray
    var fileText = ""
    var renameFile = ""
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateJsonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        req = JSONArray() // Move inside the loop

        binding.addBtn.setOnClickListener {
            if (!binding.editTextJsonKey.text!!.isEmpty() && !binding.editTextJsonValue.text!!.isEmpty()) {
                reqObj = JSONObject() // Move inside the loop
                reqObj.put(
                    binding.editTextJsonKey.text.toString(),
                    binding.editTextJsonValue.text.toString()
                )
                req.put(reqObj) // ADDED HERE
                binding.convertedText.text = req.toString()

                binding.editTextJsonKey.text = null
                binding.editTextJsonValue.text = null
            }
            else{
                if(binding.editTextJsonKey.text!!.isEmpty()){
                    Toast.makeText(this, "Key cannot be null", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Value cannot be null", Toast.LENGTH_SHORT).show()

                }
            }

        }

        binding.saveBtn.setOnClickListener {
            if (!binding.convertedText.text.isEmpty()) {
                showAlertDialog()
            }
            else{
                Toast.makeText(this, "Empty File cannot be saved!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backBtn.setOnClickListener{
            finish()
        }


    }

    fun generateDir(context: Context, dirName: String?): String? {
        val f: File = File(Environment.getExternalStorageDirectory(), dirName!!)
        val path = f.path
        val folder = File(path)
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e("DirectoryLog :: ", "Problem creating folder")
            }
        }
        Log.e("FT :: ", "creating folder" + folder.getPath())
        return folder.getPath()
    }

    fun showAlertDialog() {
        try {
            val pathh = generateDir(this, "/JSON Files")
            Log.d("TAG", "onCreate: " + pathh)
            runOnUiThread {
                val alert = Dialog(this)
                alert.setCancelable(false)
                alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alert.setContentView(R.layout.rename_alert_dialog)
                alert.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alert.getWindow()!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                );
                val fileName = alert.findViewById<EditText>(R.id.fileNameTV)
                val cancelBtn = alert.findViewById<TextView>(R.id.cancelBtn)
                val okBtn = alert.findViewById<TextView>(R.id.okBtn)
                val image_rename = alert.findViewById<AppCompatImageView>(R.id.image_rename)
                val title_rename = alert.findViewById<AppCompatTextView>(R.id.title_rename)

                image_rename.setImageDrawable(getDrawable(R.drawable.create_json_icon))
                title_rename.text = "Create Json"

//
                // fileName.setText(newString)
                okBtn.setOnClickListener {
                    executor.execute {
                        renameFile = fileName.text.toString()
                        val filee = File("$pathh/$renameFile.json")
                        val builder = null
                        appendData(filee)

                        MediaScannerConnection.scanFile(this,
                            arrayOf<String>(filee.toString()),
                            null,
                            object : MediaScannerConnection.OnScanCompletedListener {
                                override fun onScanCompleted(path: String, uri: Uri) {
                                    Log.i("ExternalStorage", "Scanned $path:")
                                    Log.i("ExternalStorage", "-> uri=$uri")
                                }
                            })
                        // createPdf(this, builder.toString(), filee.absolutePath)

                    }
                    handler.post {
                        alert.dismiss()

                    }
                }
                cancelBtn.setOnClickListener {
                    alert.dismiss()
                }
                alert.show()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun appendData(file: File) {
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }
        try {
            val fileOutputStream = FileOutputStream(file)
            val writer = OutputStreamWriter(fileOutputStream)
            writer.append(binding.convertedText.text.toString())
            writer.close()
            fileOutputStream.close()
            val snackbar = Snackbar.make(
                findViewById(R.id.mainLayout_create),
                "JSON File Created",
                Snackbar.LENGTH_LONG
            )
            snackbar.show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}

