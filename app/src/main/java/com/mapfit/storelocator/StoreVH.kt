package com.mapfit.storelocator

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class StoreVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtTitle by lazy { itemView.findViewById<TextView>(R.id.txtTitle) }
    private val txtAddress by lazy { itemView.findViewById<TextView>(R.id.txtAddress) }
    private val txtIndex by lazy { itemView.findViewById<TextView>(R.id.txtIndex) }

    fun bind(store: Store) {
        txtTitle.text = store.title
        txtAddress.text = store.address
        txtIndex.text = store.index.toString()
    }

}
