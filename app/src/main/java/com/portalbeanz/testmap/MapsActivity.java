package com.portalbeanz.testmap;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amalbit.trail.RouteOverlayView;
import com.amalbit.trail.TrailSupportMapFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @BindView(R.id.edtOrginal)
    EditText edtOrginal;
    @BindView(R.id.edtDestination)
    EditText edtDestination;
    @BindView(R.id.btn)
    Button btn;
    private GoogleMap mMap;
    private Marker marker;
    LatLng sydney;
    List<LatLng> listRoutes;
    private LatLngBounds.Builder bounds;
    int index = -1;
    int next = 1;
    private Handler handler = new Handler();
    public final static int CODE_ORGINAL = 1;
    LatLng orgLatlng;
    public final static int CODE_DESTIANTION = 2;
    LatLng desLatlng;
    private TrailSupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (TrailSupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        listRoutes = new ArrayList<>();
    }

    public void openPlaceAutoCompleteView(int code) {
        mMap.clear();
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, code);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_ORGINAL) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                orgLatlng = place.getLatLng();
                edtOrginal.setText(place.getAddress());
                mMap.addMarker(new MarkerOptions().position(orgLatlng));
             //   getRoute();
            }
            return;
        }
        if (requestCode == CODE_DESTIANTION) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                desLatlng = place.getLatLng();
                edtDestination.setText(place.getAddress());
                mMap.addMarker(new MarkerOptions().position(desLatlng));
             //   getRoute();
            }
            return;
        }
    }

    private void getRoute() {
        if(orgLatlng == null || desLatlng == null) return;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GetRoute polyline = retrofit.create(GetRoute.class);
        polyline.getPolylineData(orgLatlng.latitude+","+orgLatlng.longitude,desLatlng.latitude+","+desLatlng.longitude).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject gson = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                try {
                    startRoute(  parse(new JSONObject(gson.toString())));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception ignored) {
        }

        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
//    private RouteOverlayView.AnimType getCurrentAnimType() {
//        if(mSwitchCompat.isChecked()) {
//            return RouteOverlayView.AnimType.PATH;
//        } else {
//            return RouteOverlayView.AnimType.ARC;
//        }
//    }
    void startRoute(List<List<HashMap<String, String>>> result) {
        this.listRoutes.clear();
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            this.listRoutes.addAll(points);
            for (LatLng latlng :this.listRoutes) {
                bounds.include(latlng);
            }
          //  mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
            startAnimation();
        }
//
//        lineOptions.width(10);
//        lineOptions.color(Color.BLACK);
//        lineOptions.startCap(new SquareCap());
//        lineOptions.endCap(new SquareCap());
//        lineOptions.jointType(ROUND);
//        blackPolyLine = mMap.addPolyline(lineOptions);
//
//        PolylineOptions greyOptions = new PolylineOptions();
//        greyOptions.width(10);
//        greyOptions.color(Color.GRAY);
//        greyOptions.startCap(new SquareCap());
//        greyOptions.endCap(new SquareCap());
//        greyOptions.jointType(ROUND);
//        greyPolyLine = mMap.addPolyline(greyOptions);
//
//        animatePolyLine();
    }
    private void startAnim(List<LatLng> route){
        if(mMap != null) {
            mapFragment.setUpPath(route, mMap, RouteOverlayView.AnimType.PATH);
        } else {
            Toast.makeText(getApplicationContext(), "Map not ready", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style));
        // Add a marker in Sydney and move the camera
        sydney = new LatLng(21.047296, 105.790183);
        bounds = new LatLngBounds.Builder();



    }
    Runnable runnable =new Runnable() {
        @Override
        public void run() {
            if (index < listRoutes.size() - 1) {
                index++;
                // next = index +1;
            }
            updateMarker(marker, listRoutes.get(index));
            handler.postDelayed(this, MarkerAnimation.TIME_DELAY);
        }
    };
    private void startAnimation() {
        handler.removeCallbacks(runnable);
        marker = mMap.addMarker(new MarkerOptions().position(listRoutes.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_marker_15)));
        marker.setAnchor(0.5f, 0.5f);
        marker.setFlat(true);
        MarkerAnimation.rotateMarker(marker, new Random().nextFloat() * 360.0f);
        MarkerAnimation.fadeInMarker(this, marker, true);
        index = -1;
        startAnim(listRoutes);
        handler.postDelayed(runnable,1000);
    }
    public void updateMarker(Marker marker, LatLng newLocaiton) {
        if (marker != null) {
            Location prevLoc = new Location("gps");
            prevLoc.setLatitude(marker.getPosition().latitude);
            prevLoc.setLongitude(marker.getPosition().longitude);
            Location newLoc = new Location("gps");
            newLoc.setLatitude(newLocaiton.latitude);
            newLoc.setLongitude(newLocaiton.longitude);
            MarkerAnimation.animateMarker(newLoc, marker);
            double bearing = MarkerAnimation.getBearing(marker.getPosition(), new LatLng(newLoc.getLatitude(), newLoc.getLongitude()));
            if (bearing > 0.0d) {
                MarkerAnimation.rotateMarker(marker, (float) bearing);
            }
        }
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
        if (mMap == null) return;
       getRoute();
    }
    @OnClick(R.id.edtDestination)
    public void onViewDesClicked() {
       openPlaceAutoCompleteView(CODE_DESTIANTION);
    }
    @OnClick(R.id.edtOrginal)
    public void onViewOriginClicked() {
        openPlaceAutoCompleteView(CODE_ORGINAL);

    }
}
