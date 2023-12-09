package com.yuyh.jsonviewer.library.adapter

import androidx.recyclerview.widget.RecyclerView

abstract class BaseJsonViewerAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH>() {
    companion object {
        @JvmField
        var KEY_COLOR = -0x6dd867
        @JvmField
        var TEXT_COLOR = -0xc54ab6
        @JvmField
        var NUMBER_COLOR = -0xda551e
        @JvmField
        var BOOLEAN_COLOR = -0x67d80
        @JvmField
        var URL_COLOR = -0x992d2b
        var NULL_COLOR = -0x10a6cb
        @JvmField
        var BRACES_COLOR = -0xb5aaa1
        @JvmField
        var TEXT_SIZE_DP = 12f
    }
}