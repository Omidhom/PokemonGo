package com.example.kotlinapp4pokemango

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val USER_LOCATION_REQUEST_CODE = 1000

    private var playerLocation: Location? = null
    private var oldLocationOfPlayer : Location? = null

    private var locationManager : LocationManager? = null
    private var locationListener: PlayerLocationListener? = null

    private var pokemonCharacters: ArrayList<PokemonCharacter> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = PlayerLocationListener()

        requestLocationPermission()
        initializePokemonCharacters()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

        // 36.691588, 48.513847

    }

    // Ask Permission
    private fun requestLocationPermission() {

        if(Build.VERSION.SDK_INT >=23) {

            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                USER_LOCATION_REQUEST_CODE)

                return
            }
        }

        accessUserLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {

        if (requestCode == USER_LOCATION_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                accessUserLocation()
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    inner class PlayerLocationListener: LocationListener {

        constructor() {

            playerLocation = Location("MyProvider")
            playerLocation?.latitude = 0.0
            playerLocation?.longitude = 0.0
        }

        override fun onLocationChanged(updatedLocation: Location?) {

            playerLocation = updatedLocation
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }


    }

    private fun initializePokemonCharacters() {

        pokemonCharacters.add(PokemonCharacter("Hi, this is C1.",
                "I'm powerful." ,
                R.drawable.c1 , 1.651729, 31.996134))
        pokemonCharacters.add(PokemonCharacter("Hi, this is C2.",
                "I'm powerful." ,
                R.drawable.c2 , 27.404523, 29.647654))
        pokemonCharacters.add(PokemonCharacter("Hi, this is C3.",
                "I'm powerful." ,
                R.drawable.c3 , 10.492703, 10.709112))
        pokemonCharacters.add(PokemonCharacter("Hi, this is C4.",
                "I'm powerful." ,
                R.drawable.c4 , 28.220750, 1.898764))
    }

    private fun accessUserLocation() {

        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
                2f, locationListener!!)

        var newThread = NewThread()
        newThread.start()

    }

    inner class NewThread: Thread{

        constructor(): super() {

            oldLocationOfPlayer = Location("MyProvider")
            oldLocationOfPlayer?.latitude = 0.0
            oldLocationOfPlayer?.longitude = 0.0

        }

        override fun run() {
            super.run()

            while (true) {

                if (oldLocationOfPlayer?.distanceTo(playerLocation) == 0f) {

                    continue
                }

                oldLocationOfPlayer = playerLocation
                try {

                    runOnUiThread {

                        mMap.clear()
                        val pLocation = LatLng(playerLocation!!.latitude, playerLocation!!.longitude)
                        mMap.addMarker(MarkerOptions().position(pLocation).title("Hi, I am the player!")
                                .snippet("Let's go!").icon(BitmapDescriptorFactory.fromResource(R.drawable.player)))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pLocation))

                        for (pokemonCharacterIndex in 0.until(pokemonCharacters.size)) {

                            val pc = pokemonCharacters[pokemonCharacterIndex]
                            if (pc.isKilled == false) {

                                val pcLocation = LatLng(pc.location!!.latitude , pc.location!!.longitude)
                                mMap.addMarker(MarkerOptions()
                                        .position(pcLocation)
                                        .title(pc.titleOfPokemon)
                                        .snippet(pc.message)
                                        .icon(BitmapDescriptorFactory.fromResource(pc.iconOfPokemon!!)))

                                if (playerLocation!!.distanceTo(pc.location) < 1) {

                                    Toast.makeText(this@MapsActivity,
                                            "${pc.titleOfPokemon} is eliminated!" ,
                                            Toast.LENGTH_SHORT).show()
                                    pc.isKilled = true
                                    pokemonCharacters[pokemonCharacterIndex] = pc
                                }
                            }

                        }

                    }
                    Thread.sleep(1000)


                } catch (exception: Exception) {

                    exception.printStackTrace()
                }

            }


        }
    }
}