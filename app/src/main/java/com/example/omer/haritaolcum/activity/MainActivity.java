package com.example.omer.haritaolcum.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.omer.haritaolcum.adapters.CustomInfoWindowAdapter;
import com.example.omer.haritaolcum.sqlite.MarkerBilgileri;
import com.example.omer.haritaolcum.adapters.MyMarkerLongClickListener;
import com.example.omer.haritaolcum.adapters.PlaceAutocompleteAdapter;
import com.example.omer.haritaolcum.R;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private final static String TAG = "Main Activity";
    private GoogleMap mMap;
    private final static int REQUEST_lOCATION = 90;
    Marker mMarker;
    List<Marker> markerList;
    List<MarkerBilgileri> markerBilgileriList;
    SupportMapFragment mapFragment;
    private LatLngBounds TR_BOUNDS = new LatLngBounds(
            new LatLng(35.9025, 25.90902), new LatLng(42.02683, 44.5742));
    AutoCompleteTextView etLocation;
    private PlaceAutocompleteAdapter mAdapter;
    GeoDataClient mGeoDataClient;
    View mapView;
    PopupWindow mPopupWindow;
    RelativeLayout positionofPopup;
    Button btnZoomin;
    Button btnZoomout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        positionofPopup = findViewById(R.id.popup);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        markerList = new ArrayList<>();
        markerBilgileriList = new ArrayList<>();
        //data client
        mGeoDataClient = Places.getGeoDataClient(this, null);
        etLocation = findViewById(R.id.et_location);
        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .setCountry("TR")
                .build();
        mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, TR_BOUNDS,
                autocompleteFilter);
        etLocation.setAdapter(mAdapter);
        etLocation.setOnItemClickListener(mAutocompleteClickListener);

        //harita oluştur
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        btnZoomin = findViewById(R.id.btnZoomin);
        btnZoomout = findViewById(R.id.btnZoomout);

        btnZoomin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        btnZoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });


        //map type button
        final Button btn_MapType = (Button) findViewById(R.id.btn_Sat);
        btn_MapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    btn_MapType.setText("NORMAL");
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    btn_MapType.setText("UYDU");
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Map Ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng ankara = new LatLng(39.925533, 32.866287);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ankara, 5));
        mMap.setLatLngBoundsForCameraTarget(TR_BOUNDS);
        //izin kontrolü ve yer belirleme tuşu
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_lOCATION);
            }
        }

        if (markerBilgileriList.size() > 0) {
            for (MarkerBilgileri markerBilgileri : markerBilgileriList) {
                Marker marker =
                        mMap.addMarker(new MarkerOptions()
                                .title(markerBilgileri.getIsim())
                                .snippet(markerBilgileri.getIcerik())
                                .position(new LatLng(markerBilgileri.getX(), markerBilgileri.getY()))
                                .draggable(true));
                markerList.add(marker);
            }
        }
        markerList.add(mMap.addMarker(new MarkerOptions()
                .title("Ankara")
                .snippet("Başkent")
                .position(new LatLng(39.9334,32.8597))
                .draggable(true)));



        mMap.setMinZoomPreference(5);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MainActivity.this));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
//                if (mPopupWindow.isShowing()) {
//                    mPopupWindow.dismiss();
//                }
            }
        });


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
//                try {
//                    List<Address> adressList = null;
//                    Geocoder geocoder = new Geocoder(MapsActivity.this);
//                    adressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//                    Address address = adressList.get(0);
//                    MarkerOptions mMarkerOp = new MarkerOptions()
//                            .title(address.getCountryName())
//                            .snippet(address.getAddressLine(0))
//                            .position(new LatLng(latLng.latitude, latLng.longitude
//                            ));
//                    Log.d(TAG, mMarkerOp.getSnippet());
//                    mMarker = mMap.addMarker(mMarkerOp);
//                } catch (IOException e) {
//                    Log.d(TAG, e.toString());
                //-----------------//
//            }
            }
        });

        //for İmplement markerClick listener
        mMap.setOnMarkerClickListener(this);


        mMap.setOnMarkerDragListener(new MyMarkerLongClickListener(markerList) {
            @Override
            public void onLongClickListener(Marker marker) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(marker.getTitle());
                builder.setMessage(marker.getSnippet());
                builder.show();
            }
        });


    }

    @Override
    //izinler
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_lOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Kullanıcı konum iznini vermedi", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //onSaveInstanceState
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mMarker != null) {
            savedInstanceState.putDouble("markerLtng", mMarker.getPosition().latitude);
            savedInstanceState.putDouble("markerLong", mMarker.getPosition().longitude);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    //Marker Click
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("!!!!!", "Marker");

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View customViev = layoutInflater.inflate(R.layout.custom_info_window, null);


        mPopupWindow = new PopupWindow(customViev, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        Button button = customViev.findViewById(R.id.btnReferansOlcumNok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });


//        mPopupWindow.showAtLocation(positionofPopup, Gravity.CENTER,0,0);


        return false;
    }


    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            hideSoftKeyboard();
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);
            Log.i(TAG, "Autocomplete item selected: " + primaryText);
            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */

            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data Client query that shows the first place result in
     * the details view on screen.
     */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);
                if (mMarker != null) {
                    mMarker.remove();
                }
                List<Address> adressList = null;
                Geocoder geocoder = new Geocoder(MainActivity.this);


                try {
                    adressList = geocoder.getFromLocationName(place.getName().toString(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Address address = adressList.get(0);

                MarkerOptions mMarkerOp = new MarkerOptions()
                        .title(address.getCountryName())
                        .snippet(address.getAddressLine(0))
                        .position(new LatLng(address.getLatitude(), address.getLongitude()
                        ));
                Log.d(TAG, mMarkerOp.getSnippet());
                mMarker = mMap.addMarker(mMarkerOp);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 9));


                Log.i(TAG, "Place details received: " + place.getName());

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };


}
