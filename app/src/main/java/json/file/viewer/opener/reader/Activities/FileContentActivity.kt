package json.file.viewer.opener.reader.Activities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import json.file.viewer.opener.reader.R
import json.file.viewer.opener.reader.databinding.ActivityFileContentBinding
import org.json.JSONException
import java.io.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class FileContentActivity : AppCompatActivity() {
    lateinit var binding:ActivityFileContentBinding
    var fileText=""
    var renamePdf = ""
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityFileContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras
        val message = bundle!!.getString("path")
        val file= File(message)
        binding.jsonViewer.setKeyColor(Color.BLUE)
        binding.jsonViewer.setValueTextColor(Color.BLACK)
        binding.jsonViewer.setValueNumberColor(R.color.blue)
        binding.jsonViewer.setValueUrlColor(Color.BLUE)
        binding.jsonViewer.setValueNullColor(Color.RED)
        binding.jsonViewer.setBracesColor(R.color.blue)
        binding.jsonViewer.setTextSize(12f)
        executor.execute {
            fileText= readTextFromFile(file).toString()
            runOnUiThread {
                try {
                    binding.jsonViewer.bindJson(fileText,this)
                }catch (e: JSONException)
                {
                    Toast.makeText(this, "Error! File may have invalid format.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
        handler.post {
            binding.progressBar.visibility= View.GONE
            //binding.jsonView.expandAll();
        }
        binding.pdfConvertBtn.setOnClickListener {
            showAlertDialog()
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    fun readTextFromFile(file: File): String? {
        var textFromXmlFile: String? = null
        if (file.exists()) {
            val text = StringBuilder()
            try {
                val bufferReader = BufferedReader(FileReader(file))
                var lines = bufferReader.readLine()
                while (lines != null) {
                    text.append(lines)
                    text.append('\n')
                    lines = bufferReader.readLine()
                }
                bufferReader.close()
                textFromXmlFile = text.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                //You'll need to add proper error handling here
            }
        } else {
        }
        return textFromXmlFile
    }

    fun generateDir(context: Context, dirName: String?): String? {
        val path = context.getExternalFilesDir(dirName).toString()
        val folder = File(path)
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e("DirectoryLog :: ", "Problem creating folder")
            }
        }
        Log.e("FT :: ", "creating folder" + folder.getPath())
        return folder.getPath()
    }
    fun generatePDF(context: Context, text: String, mDestFile: String) {
        val doc = Document()
        var outputfile: File?
        try {
            outputfile = File(mDestFile)
            val fOut = FileOutputStream(outputfile)
            PdfWriter.getInstance(doc, fOut)
            //open the document
            doc.open()
            val p1 = Paragraph(fileText)
            p1.alignment = Paragraph.ALIGN_JUSTIFIED
            doc.add(p1)
            val snackbar = Snackbar.make(
                findViewById(R.id.mylayout),
                "Converted",
                Snackbar.LENGTH_LONG
            )
            snackbar.show()
        } catch (de: DocumentException) {
            Log.e("PDFCreator", "DocumentException:$de")
        } catch (e: IOException) {
            Log.e("PDFCreator", "ioException:$e")
        } finally {
            doc.close()
            Log.e("PDFCreator", "ioException:$")
        }

    }
    fun showAlertDialog() {
        try {
            val pathh = generateDir(this, "/PDF Files")

            runOnUiThread {
                val alert = Dialog(this)
                alert.setCancelable(false)
                alert.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alert.setContentView(R.layout.rename_alert_dialog)
                alert.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alert.getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                val fileName = alert.findViewById<EditText>(R.id.fileNameTV)
                val cancelBtn = alert.findViewById<TextView>(R.id.cancelBtn)
                val okBtn = alert.findViewById<TextView>(R.id.okBtn)
//
                // fileName.setText(newString)
                okBtn.setOnClickListener {
                    executor.execute {
                        renamePdf = fileName.text.toString()
                        val filee = File("$pathh/$renamePdf.pdf")
                        val builder = null
                        generatePDF(this, fileText, filee.absolutePath)
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


}