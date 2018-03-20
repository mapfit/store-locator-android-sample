package com.mapfit.storelocator

import android.content.res.Resources
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.mapfit.android.Mapfit
import com.mapfit.android.MapfitMap
import com.mapfit.android.OnMapReadyCallback
import com.mapfit.android.annotations.Marker
import com.mapfit.android.annotations.callback.OnMarkerAddedCallback
import com.mapfit.android.annotations.callback.OnMarkerClickListener
import com.mapfit.android.geometry.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg

/**
 * Displays Mapfit Map and store locations with Mapfit Geocoder.
 */
class StoreListActivity : AppCompatActivity() {

    private lateinit var storeAdapter: StoreAdapter
    private lateinit var mapfitMap: MapfitMap
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var selectionJob: Job

    private val stores = getStoreArray()
    private val storeMarkerHash = HashMap<Int, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapfit.getInstance(this, getString(R.string.mapfit_api_key))

        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_home
            )
        )

        init()
    }

    private fun init() {

        // this call instantiates the map asynchronously
        mapView.getMapAsync(onMapReadyCallback = object : OnMapReadyCallback {
            override fun onMapReady(mapfitMap: MapfitMap) {
                setupMap(mapfitMap)
            }
        })

        rePositionAttributionLogo()

        setupStoreList()
    }

    /**
     * Setup RecyclerView and insert list of stores.
     */
    private fun setupStoreList() {
        storeAdapter = StoreAdapter()
        storeAdapter.addStores(*stores)
        storeRecycler.layoutManager = layoutManager

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(storeRecycler)

        storeRecycler.adapter = storeAdapter
        storeRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val view = snapHelper.findSnapView(layoutManager)
                    val position = layoutManager.getPosition(view)
                    selectStore(position)
                }
            }
        })
    }

    /**
     * Changes the bottom margin of Mapfit attribution logo so it won't be buried behind the store
     * list.
     */
    private fun rePositionAttributionLogo() {
        val attributionContainer: View =
            mapView.findViewById<View>(R.id.imgAttribution).parent as View
        val params = attributionContainer.layoutParams

        val newParams = RelativeLayout.LayoutParams(params.width, params.height)
        newParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        newParams.setMargins(
            16.asPx,
            16.asPx,
            0,
            136.asPx
        )
        attributionContainer.layoutParams = newParams

        layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )
    }

    private fun setupMap(mapfitMap: MapfitMap) {
        this.mapfitMap = mapfitMap

        addStores()

        mapfitMap.setOnMarkerClickListener(object : OnMarkerClickListener {
            override fun onMarkerClicked(marker: Marker) {
                for ((position, markerValue) in storeMarkerHash) {
                    if (markerValue.id == marker.id) {
                        selectStore(position)

                        layoutManager.smoothScrollToPosition(
                            storeRecycler,
                            RecyclerView.State(),
                            position
                        )
                    }
                }
            }
        })

        initialMapSettings(mapfitMap)
    }

    /**
     * Add venues to the map.
     */
    private fun addStores() {
        var index = 0
        stores.forEach {
            addGeocodedMarker(index++, it.address)
        }
    }

    /**
     * Adds marker with building polygons with geocoding the given address.
     */
    private fun addGeocodedMarker(index: Int, address: String) {
        mapfitMap.addMarker(
            address,
            true,
            object : OnMarkerAddedCallback {
                override fun onMarkerAdded(marker: Marker) {

                    // address is added as marker title by default when geocoded. To not to display
                    // Place Info, clearing the title.
                    marker.setTitle("")

                    storeMarkerHash[index] = marker

                    // set marker icon according to it's index
                    val markerIcon = when (index) {
                        0 -> R.drawable.m1
                        1 -> R.drawable.m2
                        2 -> R.drawable.m3
                        3 -> R.drawable.m4
                        4 -> R.drawable.m5
                        5 -> R.drawable.m6
                        6 -> R.drawable.m7
                        7 -> R.drawable.m8
                        8 -> R.drawable.m9
                        else -> R.drawable.m10
                    }

                    marker.setIcon(markerIcon)

                    // make the list visible when all addresses are displayed
                    if (storeMarkerHash.size == stores.size) {
                        storeRecycler.visibility = View.VISIBLE
                    }
                }

                override fun onError(exception: Exception) {
                    Toast.makeText(mapView.context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun selectStore(position: Int) {
        if (!::selectionJob.isInitialized || selectionJob.isCompleted) {

            // zoom and center to selected store
            selectionJob = launch {
                mapfitMap.setZoom(17f, 200)
                storeMarkerHash[position]?.let { mapfitMap.setCenter(it.getPosition(), 200) }
            }
        }
    }

    private fun getStoreArray(): Array<Store> {
        return arrayOf(
            Store("1", "525 w 26th St, Manhattan, NY, 10001", " 202-555-0164\n202-555-0115"),
            Store("2", "205 w 34th St, Manhattan, NY, 10001", " 202-555-0127"),
            Store("3", "494 8 Avenue, Manhattan, NY, 10001", "202-555-0138\n512-555-0116"),
            Store("4", "875 6 Avenue, Manhattan, NY, 10001", "202-555-0175"),
            Store("5", "122 Greenwich Avenue, Manhattan, NY, 10011", " 202-555-0142"),
            Store("6", "177 8 Avenue, Manhattan, NY, 10011", "512-555-0171"),
            Store("7", "227 w 27 St, Manhattan, NY, 10001", " 512-555-0160"),
            Store("8", "776 Avenue of the Americas, Manhattan, NY, 10001", " 512-555-0196"),
            Store("9", "124 8 Avenue, Manhattan ,NY ,10011", " 512-555-0116"),
            Store("10", "74 7th Ave, Manhattan, NY, 10011", "512-555-0190")
        )
    }

    private fun initialMapSettings(mapfitMap: MapfitMap) {
        bg {
            mapfitMap.setZoom(14.939252f, 500)
            mapfitMap.setTilt(1.0471976f, 500)
            mapfitMap.setRotation(0.15686037f, 500)
            mapfitMap.setCenter(
                LatLng(
                    lat = 40.743075076735416,
                    lng = -73.99652806346154
                ), 500
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (::mapfitMap.isInitialized) initialMapSettings(mapfitMap)
        }
        return super.onOptionsItemSelected(item)
    }

    private val Int.asPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

}
