package pt.ipbeja.aula5;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import pt.ipbeja.aula5.data.db.ChatDatabase;
import pt.ipbeja.aula5.data.entity.Contact;

public class ContactsMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public static void start(Context context) {
        Intent starter = new Intent(context, ContactsMapActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                List<Contact> contacts = ChatDatabase.getInstance(getApplicationContext()).contactDao().getAllContacts();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                boolean validBounds = false;
                for (Contact c : contacts) {
                    if(c.getCoordinates().isValid()) {
                        validBounds = true;
                        LatLng latLng = new LatLng(c.getCoordinates().getLatitude(), c.getCoordinates().getLongitude());
                        Marker marker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(latLng)
                                        .title(c.getName())
                                        .snippet(getString(R.string.contact_map_marker_snippet))
                        );

                        marker.setTag(c);

                        builder.include(latLng);
                    }
                }
                if(validBounds) googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));


            }
        });



        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Contact c = (Contact) marker.getTag();
                ChatActivity.start(ContactsMapActivity.this, c.getId());
            }
        });

    }
}
