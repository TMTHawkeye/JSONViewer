package json.file.viewer.opener.reader.Adaptor

import android.app.AlertDialog
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.json.file.opener.json.reader.utility.utils
import json.file.viewer.opener.reader.Activities.FileContentActivity
import json.file.viewer.opener.reader.ModelClasses.JsonModelClass
import json.file.viewer.opener.reader.R
import json.file.viewer.opener.reader.databinding.RowForJsonFileBinding
import io.paperdb.Paper
import java.io.File

class FileAdaptor  (val context: Context,
                    val arrayList: ArrayList<JsonModelClass>,
                    val type: String
): RecyclerView.Adapter<FileAdaptor.ViewHolder>() {
    var renamePdf = ""
    var exampleListFull = ArrayList(arrayList)
    class ViewHolder(var binding: RowForJsonFileBinding): RecyclerView.ViewHolder(binding.root)

    lateinit var binding:RowForJsonFileBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding= RowForJsonFileBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var file: File

        with(arrayList[position]) {
            with(holder) {
                file = File(filePath)
                binding.fileNameTV.text = file.name
                binding.fileSize.text = fileSize
                binding.fileDate.text = fileDate

                if (file.name.contains(".json")
                ) {
                    holder.binding.fileIcon.setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.json_viewer_svg)
                    )
                } else if (file.name.contains(".pdf")) {
                    holder.binding.fileIcon.setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.converted_files_svg)
                    )
                }
                binding.moreOptions.setOnClickListener {
                    val popupMenu = PopupMenu(context, holder.binding.moreOptions)
                    popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {

                            // Toast.makeText(context, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                            R.id.deleteOp -> {

                                val builder1: AlertDialog.Builder = AlertDialog.Builder(context)
                                builder1.setMessage("Are you sure you want to delete this item?")
                                builder1.setCancelable(true)

                                builder1.setPositiveButton(
                                    "Yes"
                                ) { dialog, _ ->
                                    if (type == "recent") {
                                        deleteRecentFile(context, position)
//                                     notifyItemRemoved(position)
//                                     notifyDataSetChanged()

                                    } else if (type == "pdf") {
                                        file.delete()
                                    } else if (type == "json") {
                                        deleteFileFromDevice(context, filePath)
                                    }
                                    arrayList.remove(arrayList[position])
                                    notifyDataSetChanged()
                                }

                                builder1.setNegativeButton(
                                    "No"
                                ) { dialog, id -> dialog.cancel() }

                                val alert11: AlertDialog = builder1.create()
                                alert11.show()
                            }
                            R.id.shareOp -> {
                                shareFileFromDevice(file, context)
                            }
                            R.id.addToFav->{
                                addfavdata(context, filePath)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                }


                holder.binding.fileBtn.setOnClickListener {
//                    val globalcontext=context as MainActivity
                    if (file.name.contains(".pdf")) {
                        utils.updateToShPr(context, file.absolutePath)
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                context.getPackageName() + ".provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        }catch (e: ActivityNotFoundException){
                            e.printStackTrace()
                        }
                    }
                    else {
                        utils.updateToShPr(context, file.absolutePath)
                        val intent = Intent(context, json.file.viewer.opener.reader.Activities.FileContentActivity::class.java)
                        intent.putExtra("path", arrayList[position].filePath)
                        context.startActivity(intent)
//                        globalcontext.furtherCall(intent)
                    }
                }
            }
        }

    }

    override fun getItemCount()=arrayList.size

    fun deleteRecentFile(context: Context, position: Int) {
        var preferences = context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val set: MutableSet<String>? = preferences.getStringSet("RECENT_Files", null)
        if (set != null) {
            val spList: MutableList<String> = ArrayList(set)
            spList.removeAt(position)
            val setNew: MutableSet<String> = HashSet()
            setNew.addAll(spList)
            preferences = context.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putStringSet("RECENT_Files", setNew)
            editor.apply()
            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "file not exist", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFileFromDevice(context: Context, uri: String) {
        val file = File(uri)
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = MediaStore.Files.FileColumns.DATA + " = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        val contentResolver = context.contentResolver
        val c: Cursor? = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )
        if (c!!.moveToFirst()) {
            val id: Long = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val deleteUri: Uri =
                ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
            contentResolver.delete(deleteUri, null, null)
            val nfile = File(uri)
            if (nfile.exists()) {
                nfile.delete()
            }
        } else {
            Toast.makeText(context, "file not found", Toast.LENGTH_SHORT).show()
        }
        c.close()
    }

    fun shareFileFromDevice(file: File, context: Context) {
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

    fun addfavdata(context: Context, path: String) {
        val firstFavlist: java.util.ArrayList<String>? = Paper.book().read("fav", ArrayList())
        val secondFavlist: ArrayList<String> = ArrayList()
        if (firstFavlist == null) {
            secondFavlist.add(path)
            Paper.book().write("fav", secondFavlist)

        } else {

            if (firstFavlist.contains(path)) {
                firstFavlist.remove(path)
            }
            firstFavlist.add(path)
            Paper.book().write("fav", firstFavlist)
        }

        Toast.makeText(context, "Added to favorites!!", Toast.LENGTH_SHORT).show()
    }

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


}