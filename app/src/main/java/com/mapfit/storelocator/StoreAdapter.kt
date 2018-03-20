package com.mapfit.storelocator

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Recycler adapter to display stores.
 *
 * Created by dogangulcan on 3/13/18.
 */
class StoreAdapter : RecyclerView.Adapter<StoreVH>() {

    private val stores = mutableListOf<Store>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreVH =
        StoreVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_store,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: StoreVH, position: Int) {
        holder.bind(stores[position])
    }

    override fun getItemCount() = stores.size

    fun addStores(vararg store: Store) {
        stores.addAll(store)
    }

}

