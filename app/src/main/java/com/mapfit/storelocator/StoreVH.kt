package com.mapfit.storelocator

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class StoreVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtTitle by lazy { itemView.findViewById<TextView>(R.id.txtTitle) }
    private val txtAddress by lazy { itemView.findViewById<TextView>(R.id.txtAddress) }
    private val txtPhone by lazy { itemView.findViewById<TextView>(R.id.txtPhone) }

    fun bind(store: Store) {
        txtTitle.text = store.title
        txtAddress.text = store.address
        txtPhone.text = store.phone
    }

}
