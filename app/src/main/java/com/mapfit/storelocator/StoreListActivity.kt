package com.mapfit.storelocator

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.core.widget.toast
import com.mapfit.android.Mapfit
import com.mapfit.android.MapfitMap
import com.mapfit.android.OnMapReadyCallback
import com.mapfit.android.annotations.Marker
import com.mapfit.android.annotations.MarkerOptions
import com.mapfit.android.annotations.callback.OnMarkerAddedCallback
import com.mapfit.android.annotations.callback.OnMarkerClickListener
import com.mapfit.android.geometry.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch


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

    private fun setupMap(mapfitMap: MapfitMap) {
        this.mapfitMap = mapfitMap

        addCoffeeShops()

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
     * Setup RecyclerView and insert list of stores.
     */
    private fun setupStoreList() {
        storeAdapter = StoreAdapter()
        storeAdapter.addStores(*stores)

        layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )

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
            148.asPx
        )
        attributionContainer.layoutParams = newParams

    }

    /**
     * Adds coffee shops to the map with Mapfit Geocoder.
     */
    private fun addCoffeeShops() {
        var index = 0
        stores.forEach {
            addGeocodedMarker(index++, it.address)
        }
    }

    /**
     * Adds marker with building polygons with geocoding the given address.
     */
    private fun addGeocodedMarker(index: Int, address: String) {

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

        val markerOptions = MarkerOptions()
            .streetAddress(address)
            .addBuildingPolygon(true)
            .icon(markerIcon)

        mapfitMap.addMarker(
            markerOptions,
            object : OnMarkerAddedCallback {
                override fun onMarkerAdded(marker: Marker) {

                    // address is added as marker title by default when geocoded. To not to display
                    // Place Info, clearing the title.
                    marker.title = ""

                    storeMarkerHash[index] = marker

                    // make the list visible when all addresses are displayed
                    if (storeMarkerHash.size == stores.size) {
                        storeRecycler.visibility = View.VISIBLE
                    }
                }

                override fun onError(exception: Exception) {
                    this@StoreListActivity.toast(exception.message.toString())
                }
            }
        )
    }

    private fun selectStore(position: Int) {
        if (!::selectionJob.isInitialized || selectionJob.isCompleted) {

            // zoom and center to selected store
            selectionJob = launch {
                mapfitMap.setZoom(17f, 200)
                storeMarkerHash[position]?.let { mapfitMap.setCenter(it.position, 200) }
            }
        }
    }

    /**
     * Returns an array of [Store] objects.
     */
    private fun getStoreArray(): Array<Store> {
        return arrayOf(
            Store("Chelsea", "450 W 15th Street,\nNew York, NY 10014", 1),
            Store("Bryant Park", "54 W 40th Street,\nNew York, NY 10018", 2),
            Store("Grand Central Palace", "60 E 42nd Street,\nNew York, NY 10165", 3),
            Store("Rockefeller Center", "1 Rockefeller Plaza, Suite D,\nNew York,NY 10020", 4),
            Store("Midtown East", "10 E 53rd Street,\nNew York, NY 10022", 5),
            Store("Clinton Street", "71 Clinton Street,\nNew York, NY 10002", 6),
            Store("Dean Street", "85 Dean Street Brooklyn,\nNY 11201", 7),
            Store("Williamsburg", "76 N. 4th Street, Store A Brooklyn,\nNY 11249", 8),
            Store("World Trade Center", "150 Greenwich St\nNew York, NY 10007", 9),
            Store("University Place", "101 University Place New York, NY 10003", 10)
        )
    }

    /**
     * Changes map options to initial settings.
     */
    private fun initialMapSettings(mapfitMap: MapfitMap) = launch {
        mapfitMap.apply {
            setZoom(12.684684f, 500)
            setRotation(2.0572796f, 500)
            setTilt(0.8590987f, 500)
            setCenter(LatLng(lat = 40.73748242049333, lng = -73.95733284034074), 500)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (::mapfitMap.isInitialized) initialMapSettings(mapfitMap)
            R.id.action_github -> {
                val url = "https://github.com/mapfit/store-locator-android-sample"
                val urlIntent = Intent(Intent.ACTION_VIEW)
                urlIntent.data = Uri.parse(url)
                startActivity(urlIntent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private val Int.asPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

}
