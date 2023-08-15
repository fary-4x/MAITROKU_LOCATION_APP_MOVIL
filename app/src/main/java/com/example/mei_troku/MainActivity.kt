package com.example.mei_troku

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.location.GpsStatus
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mei_troku.Adapters.SearchViewAdapter
import com.example.mei_troku.DB.Conn
import com.example.mei_troku.DataClass.Places
import com.example.mei_troku.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity(), MapListener, GpsStatus.Listener {

    lateinit var mMap: MapView
    lateinit var controller: IMapController
    lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var binding: ActivityMainBinding
    private lateinit var searchAdapter: SearchViewAdapter
    private lateinit var snack : Snackbar
    private val PICK_IMAGE_REQUEST = 1

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            configureMapAndLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        searchAdapter = SearchViewAdapter {
            selectedLocation(it)
        }
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nah
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getAllSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                //nah
            }
        })
        binding.rvPlaceSearch.setHasFixedSize(true)
        binding.rvPlaceSearch.layoutManager = LinearLayoutManager(baseContext)
        binding.rvPlaceSearch.adapter = searchAdapter
        snack = Snackbar.make(binding.root, "Agregado a la base de datos con éxito", Snackbar.LENGTH_SHORT)
        binding.floatingActionButton.setOnClickListener {
            controller.setCenter(mMyLocationOverlay.myLocation);
            controller.animateTo(mMyLocationOverlay.myLocation);
        }

        mMap.setOnTouchListener { v, event ->
            val projection = mMap.projection
            val myLocation = mMyLocationOverlay.myLocation

            if (event != null && myLocation != null) {
                val myLocationPosition = projection.toPixels(myLocation, null)
                val markerSize = 48

                val x = event.x.toInt()
                val y = event.y.toInt()
                val isInsideMarker = (x >= myLocationPosition.x - markerSize / 2) &&
                        (x <= myLocationPosition.x + markerSize / 2) &&
                        (y >= myLocationPosition.y - markerSize) &&
                        (y <= myLocationPosition.y)

                if (isInsideMarker) {
                    val message = "¡Haz hecho clic en tu ubicación!"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    val listData = listOf("Tomar Foto", "Ver Fotos")
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listData)
                    val builder = AlertDialog.Builder(this)
                        .setTitle("Pagina base")
                        .setAdapter(adapter) { dialog: DialogInterface, which: Int ->
                            CoroutineScope(Dispatchers.IO).launch {
                                runOnUiThread{
                                    if(which == 0){
                                        TakePhotos(mMyLocationOverlay.myLocation)
                                    }else{
                                        getPhotos(mMyLocationOverlay.myLocation)
                                    }
                                }
                            }
                        }
                        .setNegativeButton("Cancel") { dialog: DialogInterface, _ ->
                            dialog.dismiss()
                        }
                    builder.create().show()

                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        getSavesPlaces(Conn(baseContext).get())
    }
    private fun getSavesPlaces(get: MutableList<Places>) {
        val poiMarkers = FolderOverlay(applicationContext)
        mMap.overlays.add(poiMarkers)

        for (place in get) {
            val poiMarker = Marker(mMap)
            val poiIcon = ContextCompat.getDrawable(this, R.drawable.starta)

            poiMarker.id = "place"
            poiMarker.title = "Lugar guardado"
            poiMarker.position = GeoPoint(place.lat, place.lon)

            poiIcon?.let {
                poiMarker.icon = BitmapDrawable(resources, it.toBitmap())
            }

            poiMarker.setOnMarkerClickListener { marker, mapView ->
                getPhotos(poiMarker.position)
                true
            }

            poiMarkers.add(poiMarker)
        }
    }


    private fun getPhotos(myLocation: GeoPoint){
        var places = Conn(baseContext).getByLatLon(myLocation.latitude, myLocation.longitude)
        val listData = List(places.size) { index -> "Foto ${index + 1}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listData)
        val builder = AlertDialog.Builder(this)
            .setTitle("Pagina base")
            .setAdapter(adapter) { dialog: DialogInterface, which: Int ->
                CoroutineScope(Dispatchers.IO).launch {
                    runOnUiThread{
                        if (which >= 0 && which < places.size) {
                            val selectedPlace = places[which]
                            val imgByteArray = selectedPlace.img
                            val bitmap = BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.size)
                            showImageInAlertDialog(bitmap)
                        }
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun showImageInAlertDialog(bitmap: Bitmap) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(bitmap)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun TakePhotos(myLocation: GeoPoint) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data ?: return

            val inputStream = contentResolver.openInputStream(selectedImageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream(20480)
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
            val imgByteArray = outputStream.toByteArray()

            val conn = Conn(baseContext)
            conn.create(mMyLocationOverlay.myLocation.latitude, mMyLocationOverlay.myLocation.longitude, imgByteArray)

            snack.setText("Agregado a la base de datos con éxito").show()
        }
    }

    private fun selectedLocation(id: Double) {
        val itemList = searchAdapter.places
        val poiIcon = resources.getDrawable(R.drawable.starta)

        val roadManager: RoadManager = OSRMRoadManager(this, "MY_USER_AGENT")
        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(mMyLocationOverlay.myLocation)

        val poiMarkers = FolderOverlay(applicationContext)

        if (mMap.overlays.size >= Conn(baseContext).get().size) {
            for (overlay in mMap.overlays) {
                mMap.overlays.removeLast();
            }
            getSavesPlaces(Conn(baseContext).get());
            configureMapAndLocation();

        }

        mMap.overlays.add(poiMarkers)

        binding.searchView.hide()
        for (i in itemList) {
            if (i.latitude == id) {
                val location = GeoPoint(i.latitude, i.longitude)

                waypoints.add(location)

                mMap.controller.setCenter(location)
                val poiMarker = Marker(mMap)
                poiMarker.id = "place"
                poiMarker.title = "Lugar Seleccionado"
                poiMarker.position = location
                poiMarker.icon = poiIcon
                poiMarkers.add(poiMarker)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val road = roadManager.getRoad(waypoints)
                        runOnUiThread {
                            val roadOverlay = RoadManager.buildRoadOverlay(road)
                            roadOverlay.color = Color.RED
                            roadOverlay.width = 12.0f
                            mMap.overlays.add(roadOverlay);
                            mMap.invalidate();
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    private fun configureMapAndLocation() {
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        controller = mMap.controller
        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation);
                controller.animateTo(mMyLocationOverlay.myLocation);
                //onFilter();
            }
        }
        // val mapPoint = GeoPoint(latitude, longitude)

        controller.setZoom(20.0)

        Log.e("TAG", "onCreate:in ${controller.zoomIn()}")
        Log.e("TAG", "onCreate: out  ${controller.zoomOut()}")
        // controller.animateTo(mapPoint)
        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)

    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        // event?.source?.getMapCenter()
        Log.e("TAG", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
        Log.e("TAG", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
        //  Log.e("TAG", "onScroll   x: ${event?.x}  y: ${event?.y}", )
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        //  event?.zoomLevel?.let { controller.setZoom(it) }


        Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
        return false;
    }

    override fun onGpsStatusChanged(event: Int) {


        TODO("Not yet implemented")
    }

//    private fun onSearch(){
//
//    }

    fun getAllSearch(name: String) {
        val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
        //val mainCoroutineScope = CoroutineScope(Dispatchers.Main)
        ioCoroutineScope.launch {
            val info = PlaceSearchManager(applicationContext);
            val data = info.searchPlaces(name)
            CoroutineScope(Dispatchers.Main).launch {
                Log.i("alv", data.toString())
                if (data != null) {
                    searchAdapter.onUpdateList(data)
                }
            }
        }
        //ioCoroutineScope.cancel()

    }

}