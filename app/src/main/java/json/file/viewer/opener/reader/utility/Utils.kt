package com.json.file.opener.json.reader.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class utils {
    companion object{
        fun updateToShPr(context: Context, file: String) {
            var preferences = context.getSharedPreferences(
                "PREFERENCE_NAME",
                AppCompatActivity.MODE_PRIVATE
            )
            val set: MutableSet<String>? = preferences.getStringSet("RECENT_Files", null)
            if (set != null) {
                val uriList: MutableList<String> = ArrayList(set)
                if (uriList.size!=0){
                    for (i in uriList.indices) {
                        if (file != uriList[i]) {
                            uriList.add(file)
                            val setNew: MutableSet<String> = HashSet()
                            setNew.addAll(uriList)
                            preferences = context.getSharedPreferences(
                                "PREFERENCE_NAME",
                                AppCompatActivity.MODE_PRIVATE
                            )
                            val editor: SharedPreferences.Editor = preferences.edit()
                            editor.putStringSet("RECENT_Files", setNew)
                            editor.apply()
                        }
                    }
                }else{
                    saveToShPr(context,file)
                }

            } else {
                saveToShPr(context, file)
            }
        }

        fun saveToShPr(context: Context, file: String) {
            val uriList: MutableList<String> = ArrayList()
            uriList.add(file)
            val set: MutableSet<String> = HashSet()
            set.addAll(uriList)
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                "PREFERENCE_NAME",
                AppCompatActivity.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putStringSet("RECENT_Files", set)
            editor.apply()
        }
    }
}