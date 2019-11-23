package ir.eqtech.fasstrack;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.EngineInstantiationException;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapviewlite.LoadSceneCallback;
import com.here.sdk.mapviewlite.MapImage;
import com.here.sdk.mapviewlite.MapImageFactory;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapMarkerImageStyle;
import com.here.sdk.mapviewlite.MapPolyline;
import com.here.sdk.mapviewlite.MapPolylineStyle;
import com.here.sdk.mapviewlite.MapStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.mapviewlite.PixelFormat;
import com.here.sdk.mapviewlite.SceneError;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Waypoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MapViewLite mapView;
    private RoutingEngine routingEngine;
    private GeoPolyline routeGeoPolyline;

    private GeoCoordinates truck = new GeoCoordinates(53.557187,  9.966282);
    private GeoCoordinates truck_2 = new GeoCoordinates(53.557187,  9.966282);
    private GeoCoordinates destination = new GeoCoordinates(53.544795,  9.996712);

    private TextView ETA_textview;
    private TextView Distance_textview;
    private TextView Slot_number_text_view;

    private int estimatedTravelTimeInSeconds;
    private int lengthInMeters ;

    private Button start_btn;
    private Button reset_btn;

    private MapPolyline routeMapPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ETA_textview = (TextView) findViewById(R.id.ETA_text_view);
        Distance_textview = (TextView) findViewById(R.id.distance_text_view);
        Slot_number_text_view = (TextView) findViewById(R.id.truck_slot_number);

        start_btn = (Button) findViewById(R.id.start_btn);
        reset_btn = (Button) findViewById(R.id.reset_btn);

        // Get a MapViewLite instance from the layout.
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        loadMapScene();

        //-----------now we want to add routing----------
        try {
            routingEngine = new RoutingEngine();
        } catch (EngineInstantiationException e) {
            new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }


        Waypoint startWaypoint = new Waypoint(truck);
        Waypoint destinationWaypoint = new Waypoint(destination);


        MapImage mapImage = MapImageFactory.fromResource(this.getResources(), R.drawable.lorry);
        MapImage mapImage_destination = MapImageFactory.fromResource(this.getResources(), R.drawable.locationpin);

        MapMarker mapMarker = new MapMarker(truck);
        mapMarker.addImage(mapImage, new MapMarkerImageStyle());
        mapView.getMapScene().addMapMarker(mapMarker);

        MapMarker mapMarker_destination = new MapMarker(destination);
        mapMarker_destination.addImage(mapImage_destination, new MapMarkerImageStyle());
        mapView.getMapScene().addMapMarker(mapMarker_destination);



        List<Waypoint> waypoints =
                new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));

        routingEngine.calculateRoute(
                waypoints,
                new CarOptions(),
                new CalculateRouteCallback() {
                    @Override
                    public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                        if (routingError == null) {
                            Route route = routes.get(0);
                            //showRouteDetails(route);
                            //showRouteOnMap(route);
                            Log.d(TAG, "onRouteCalculated: "+ routes.get(0).toString());
                            Log.d(TAG, "onRouteCalculated: "+routes.toString());
                            Log.d(TAG, "onRouteCalculated: "+ route.getShape());
                            truck_2 = new GeoCoordinates(route.getShape().get(20).latitude , route.getShape().get(20).longitude);
                            estimatedTravelTimeInSeconds = route.getTravelTimeInSeconds();
                            lengthInMeters = route.getLengthInMeters();
                            Log.d(TAG, "onRouteCalculated: " + estimatedTravelTimeInSeconds);
                            Log.d(TAG, "onRouteCalculated: "+ lengthInMeters);
                            ETA_textview.setText("ETA : "+estimatedTravelTimeInSeconds/60 +" min");
                            Distance_textview.setText("Distance left : "+ lengthInMeters/1000 +" KM");

                            try {
                                routeGeoPolyline = new GeoPolyline(route.getShape());
                            } catch (InstantiationErrorException e) {
                                // It should never happen that the route shape contains less than two vertices.
                                return;
                            }
                            MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
                            mapPolylineStyle.setColor(0x00908AA0, PixelFormat.RGBA_8888);
                            mapPolylineStyle.setWidth(10);
                            routeMapPolyline = new MapPolyline(routeGeoPolyline, mapPolylineStyle);
                            mapView.getMapScene().addMapPolyline(routeMapPolyline);

                        } else {
                            Log.d(TAG, "onRouteCalculated: Error while calculating a route:");
                        }
                    }
                });

        //--------POST METHOD--------

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://34.254.196.82:5000")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api_Interface service = retrofit.create(Api_Interface.class);

        Call<ReceivingData> call=service.getStringScalar(new SendingData("123", String.valueOf(estimatedTravelTimeInSeconds),String.valueOf(lengthInMeters)));
        call.enqueue(new Callback<ReceivingData>() {
            @Override
            public void onResponse(Call<ReceivingData> call, Response<ReceivingData> response) {
                //response.body() have your LoginResult fields and methods  (example you have to access error then try like this response.body().getError() )

                    //response.body() have your LoginResult fields and methods  (example you have to access error then try like this response.body().getError() )
                    String truck_slot = response.body().getTruck_slot_number();
                    Slot_number_text_view.setText("Your Slot Number: "+truck_slot);


            }

            @Override
            public void onFailure(Call<ReceivingData> call, Throwable t) {
                //for getting error in network put here Toast, so get the error on network
            }
        });


        //--------End of POST-------
        //--------Start Button----------

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Waypoint startWaypoint_2 = new Waypoint(truck_2);


                MapImage mapImage_2 = MapImageFactory.fromResource(MainActivity.this.getResources(), R.drawable.lorry);
               // MapImage mapImage_destination_2 = MapImageFactory.fromResource(MainActivity.this.getResources(), R.drawable.locationpin);

                MapMarker mapMarker_2 = new MapMarker(truck_2);
                mapMarker_2.addImage(mapImage_2, new MapMarkerImageStyle());
                mapView.getMapScene().removeMapMarker(mapMarker);
                mapView.getMapScene().addMapMarker(mapMarker_2);

                List<Waypoint> waypoints_2 =
                        new ArrayList<>(Arrays.asList(startWaypoint_2, destinationWaypoint));

                routingEngine.calculateRoute(
                        waypoints_2,
                        new CarOptions(),
                        new CalculateRouteCallback() {
                            @Override
                            public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                                if (routingError == null) {
                                    Route route = routes.get(0);
                                    //showRouteDetails(route);
                                    //showRouteOnMap(route);
                                    Log.d(TAG, "onRouteCalculated: "+ routes.get(0).toString());
                                    Log.d(TAG, "onRouteCalculated: "+routes.toString());
                                    Log.d(TAG, "onRouteCalculated: "+ route.getShape());
                                    estimatedTravelTimeInSeconds = route.getTravelTimeInSeconds();
                                    lengthInMeters = route.getLengthInMeters();
                                    Log.d(TAG, "onRouteCalculated: " + estimatedTravelTimeInSeconds);
                                    Log.d(TAG, "onRouteCalculated: "+ lengthInMeters);
                                    ETA_textview.setText("ETA : "+estimatedTravelTimeInSeconds/60 +" min");
                                    Distance_textview.setText("Distance left : "+ lengthInMeters/1000 +" KM");

                                    try {
                                        routeGeoPolyline = new GeoPolyline(route.getShape());
                                    } catch (InstantiationErrorException e) {
                                        // It should never happen that the route shape contains less than two vertices.
                                        return;
                                    }
                                    MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
                                    mapPolylineStyle.setColor(0x00908AA0, PixelFormat.RGBA_8888);
                                    mapPolylineStyle.setWidth(10);
                                    MapPolyline routeMapPolyline_2 = new MapPolyline(routeGeoPolyline, mapPolylineStyle);
                                    mapView.getMapScene().removeMapPolyline(routeMapPolyline);
                                    mapView.getMapScene().addMapPolyline(routeMapPolyline_2);

                                } else {
                                    Log.d(TAG, "onRouteCalculated: Error while calculating a route:");
                                }
                            }
                        });

                //--------POST METHOD--------


                Call<ReceivingData> call2=service.getStringScalar(new SendingData("123", String.valueOf(estimatedTravelTimeInSeconds),String.valueOf(lengthInMeters)));
                call2.enqueue(new Callback<ReceivingData>() {
                    @Override
                    public void onResponse(Call<ReceivingData> call, Response<ReceivingData> response) {
                        //response.body() have your LoginResult fields and methods  (example you have to access error then try like this response.body().getError() )

                        //response.body() have your LoginResult fields and methods  (example you have to access error then try like this response.body().getError() )
                        String truck_slot = response.body().getTruck_slot_number();
                        Slot_number_text_view.setText("Your Slot Number: "+truck_slot);


                    }

                    @Override
                    public void onFailure(Call<ReceivingData> call, Throwable t) {
                        //for getting error in network put here Toast, so get the error on network
                    }
                });




            }
        });


        //---------End of Start
    }

    private void loadMapScene() {
        // Load a scene from the SDK to render the map with a map style.
        mapView.getMapScene().loadScene(MapStyle.NORMAL_DAY, new LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable SceneError sceneError) {
                if (sceneError == null) {
                    mapView.getCamera().setTarget(new GeoCoordinates(53.544795,  9.996712));
                    mapView.getCamera().setZoomLevel(14);
                } else {
                    Log.d(TAG, "onLoadScene failed: " + sceneError.toString());
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    /*private Call<GetDataFromWeb> callGET_ADS_sApi() {
        return cargo_service.GET_DATA_FROM_WEB_CALL(
                searched_word,
                currentPage );
    }*/
}
