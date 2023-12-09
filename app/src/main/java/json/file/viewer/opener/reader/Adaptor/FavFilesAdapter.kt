package json.file.viewer.opener.reader.Adaptor

import android.app.AlertDialog
import android.content.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import json.file.viewer.opener.reader.Activities.FileContentActivity
import json.file.viewer.opener.reader.BuildConfig
import json.file.viewer.opener.reader.ModelClasses.JsonModelClass
import json.file.viewer.opener.reader.R
import json.file.viewer.opener.reader.databinding.RowForJsonFileBinding
import io.paperdb.Paper
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class FavFilesAdapter(
    val context: Context,
    val arrayList: ArrayList<JsonModelClass>,
    val key: String
) : RecyclerView.Adapter<FavFilesAdapter.ViewHolder>() {
    lateinit var binding:RowForJsonFileBinding
    var exampleListFull = ArrayList(arrayList)
    var arrayList2:ArrayList<String> = ArrayList()
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler: Handler = Handler(Looper.getMainLooper())
    class ViewHolder(var binding: RowForJsonFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       binding= RowForJsonFileBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = File(arrayList[position].filePath)
        val path = arrayList[position].filePath
        holder.binding.fileNameTV.text = arrayList[position].fileName
        holder.binding.fileSize.text = arrayList[position].fileSize
        holder.binding.fileDate.text = arrayList[position].fileDate
        holder.binding.moreOptions.setOnClickListener {
            var renameFilee = ""
            lateinit var renameFile: File
            val popupMenu: PopupMenu = PopupMenu(context, holder.binding.moreOptions)
            if (file.name.contains("pdf")) {
                popupMenu.menuInflater.inflate(R.menu.fav_popup_menu, popupMenu.menu)
            } else {
                popupMenu.menuInflater.inflate(R.menu.fav_popup_menu, popupMenu.menu)
            }
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.deleteOp -> {
                        val builder1: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder1.setMessage("Are you sure you want to delete this item?")
                        builder1.setCancelable(true)
                        builder1.setPositiveButton(
                            "Yes",
                            DialogInterface.OnClickListener { dialog, _ ->
                                deleteFav(context,path)
                                arrayList.remove(arrayList[position])
                                notifyDataSetChanged()
//                                if (key == "fav") {
//                                    deleteFav(context, path)
//                                    arrayList.remove(arrayList[position])
//                                    notifyDataSetChanged()
//                                }
////                                else {
////                                    file.delete()
////                                    arrayList.remove(arrayList[position])
////                                    notifyItemRemoved(position)
////                                }
                            })
                        builder1.setNegativeButton(
                            "No",
                            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
                        val alert11: AlertDialog = builder1.create()
                        alert11.show()
                    }
                    R.id.shareOp -> {
                        shareFile(file, context)
                    }
                }
                true
            })
            popupMenu.show()
        }
        if (file.name.contains("json")) {
            holder.binding.fileIcon.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.json_viewer_svg)
            )
        } else if (file.name.contains(".pdf")) {
            holder.binding.fileIcon.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.converted_files_svg)
            )
        }
        if (file.name.contains("json")) {
            holder.itemView.setOnClickListener {
                getRecentData(context, path)
                val intent = Intent(context, FileContentActivity::class.java)
                intent.putExtra("path", path)
                context.startActivity(intent)
            }
        } else if (file.name.contains("pdf")) {
            holder.itemView.setOnClickListener {
                getRecentData(context, path)
                val intent = Intent(Intent.ACTION_VIEW)
                val data = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                intent.setDataAndType(data, "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = arrayList.size

    private val SearchFilter: Filter = object : Filter() {
        protected override fun performFiltering(constraint: CharSequence?): FilterResults? {
            val filteredList: ArrayList<JsonModelClass> = ArrayList()
            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(exampleListFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                for (item in exampleListFull) {
                    if(item.fileName.toLowerCase().contains(filterPattern)||item.fileName.toUpperCase().contains(filterPattern)){
                        filteredList.add(item)
                    }
//                    if (utils.GetAppName(item)!!.toLowerCase().contains(filterPattern)) {
//                        filteredList.add(item.toString())
//                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            arrayList.clear()
            arrayList.addAll(results.values as ArrayList<JsonModelClass> )
            notifyDataSetChanged()
        }
    }

    fun getFilter(): Filter {
        return SearchFilter
    }

    fun shareFile(file: File, context: Context) {
        val photoURI = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName
                .toString() + ".provider",
            file
        )
        val intentShareFile = Intent(Intent.ACTION_SEND)
        intentShareFile.type = "*/*"
        intentShareFile.putExtra(Intent.EXTRA_STREAM, photoURI)

        intentShareFile.putExtra(
            Intent.EXTRA_SUBJECT,
            "Sharing File from" + context.getString(R.string.app_name)
        )

        context.startActivity(
            Intent.createChooser(
                intentShareFile,
                context.getString(R.string.app_name)
            )
        )
    }

    fun deleteFav(context: Context, path: String) {
        Paper.init(context)
        val firstFavlist: ArrayList<String>? = Paper.book().read("fav", ArrayList())

        if (firstFavlist!!.contains(path)) {
            firstFavlist.remove(path)
        }
        Paper.book().write("fav", firstFavlist)
    }

    fun getRecentData(context: Context, path: String) {
        Paper.init(context)
        val firstlist: ArrayList<String>? = Paper.book().read("recent", ArrayList())
        val secondlist: ArrayList<String> = ArrayList()
        if (firstlist == null) {
            secondlist.add(path)
            Paper.book().write("recent", secondlist)
        } else {
            if (firstlist.contains(path)) {
                firstlist.remove(path)
            }
            firstlist.add(path)
            Paper.book().write("recent", firstlist)
        }
    }
    fun createDir(context: Context, dirName: String?): String? {
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
    fun createPdf(fileName:File){
        val doc = Document()
        val fOut = FileOutputStream(fileName)
        PdfWriter.getInstance(doc, fOut)
        //open the document
        doc.open()

        for (i in arrayList2) {
            val p1 = Paragraph(i)
            p1.setAlignment(Paragraph.ALIGN_LEFT)
            doc.add(p1)
        }
        doc.close()
    }
}