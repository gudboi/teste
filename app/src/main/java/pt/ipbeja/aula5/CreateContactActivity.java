package pt.ipbeja.aula5;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;

import pt.ipbeja.aula5.data.db.ChatDatabase;
import pt.ipbeja.aula5.data.entity.Contact;
import pt.ipbeja.aula5.data.entity.Coordinates;

public class CreateContactActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Activity request codes
    private static final int PHOTO_REQUEST_CODE = 123;

    // Instance State Bundle keys
    private static final String LAT_LNG_KEY = "latlng";
    private static final String PHOTO_BITMAP_KEY = "photoBytes";

    // Views
    private EditText contactNameInput;
    private ImageView contactPhoto;

    // Map related objects
    private Marker contactMarker = null;
    private LatLng currentLatLng = null;

    // Photo bitmap
    private Bitmap contactPhotoBitmap = null;


    public static void start(Context context) {
        Intent starter = new Intent(context, CreateContactActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);



        // Se a instance state não é null, terá alguma coisa lá guardada
        if(savedInstanceState != null) {
            // idêntico aos extras dos intents (de facto os Intents guardam um Bundle mas oferecem métodos de conveniência para acesso a estes campos)
            this.currentLatLng = savedInstanceState.getParcelable(LAT_LNG_KEY);
            this.contactPhotoBitmap = savedInstanceState.getParcelable(PHOTO_BITMAP_KEY);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        contactNameInput = findViewById(R.id.contact_name_input);
        contactPhoto = findViewById(R.id.contact_photo);


        contactPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                removePhoto();
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(contactPhotoBitmap != null) {
            contactPhoto.setImageBitmap(contactPhotoBitmap);
            contactPhoto.setVisibility(View.VISIBLE);
        }
        else {
            contactPhoto.setVisibility(View.GONE);
        }

    }

    private void removePhoto() {
        contactPhotoBitmap = null;
        contactPhoto.setImageBitmap(null);
        contactPhoto.setVisibility(View.GONE);
    }


    public void createContact(View view) {

        String name = contactNameInput.getText().toString();
        if(!name.isEmpty() && contactMarker != null) {
            Coordinates coordinates = new Coordinates(contactMarker.getPosition().latitude, contactMarker.getPosition().longitude);
            byte[] photoBytes = getBytesFromBitmap(contactPhotoBitmap);

            // Mesmo que não exista foto, não há problema em guardar null no campo dos bytes! Tratamos o caso de ser null quando utilizarmos a foto
            ChatDatabase.getInstance(this).contactDao().insert(new Contact(0, name, coordinates, photoBytes));
            finish();
        }
        else {
            if(name.isEmpty()) Snackbar.make(findViewById(android.R.id.content), R.string.create_contact_empty_name_alert, Toast.LENGTH_SHORT).show();
            else Snackbar.make(findViewById(android.R.id.content), R.string.create_contact_no_location_alert, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        // Rectangulo que delimita Portugal. Coordenadas dos cantos SW e canto NE, por esta ordem!
        LatLngBounds bounds = new LatLngBounds(new LatLng(36.568494, -10.448224),new LatLng(42.572301, -5.578860));

        // A camera pega nesse rectangulo e ajusta-se
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

        // Se a LatLng actual não é null (ver instance state), então podemos adicionar o Marker que já existia
        if(currentLatLng != null) {
            contactMarker = googleMap.addMarker(new MarkerOptions().position(currentLatLng).draggable(true));
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Se o Marker já existe, colocamos na nova posição em vez de criar um novo
                // Assim garantimos que só existe um Marker no mapa
                if(contactMarker != null) {
                    contactMarker.setPosition(latLng);
                }
                else {
                    contactMarker = googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                }
            }
        });

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {



        // Ler https://developer.android.com/reference/android/app/Activity#onSaveInstanceState(android.os.Bundle)

        // Aqui guardamos no Bundle as coisas que queremos guardar antes da Activity ser destruida
        // para quando ela for reconstruida as podermos recuperar (ver onCreate)

        if(contactMarker != null) outState.putParcelable(LAT_LNG_KEY, contactMarker.getPosition());

        // também podiamos ir buscar o bitmap directamente à imageview:
        // BitmapDrawable drawable = (BitmapDrawable) contactPhoto.getDrawable();
        // Bitmap bitmap = drawable.getBitmap();
        if(contactPhotoBitmap != null) outState.putParcelable(PHOTO_BITMAP_KEY, contactPhotoBitmap);

        // No final, chamamos o super já com o bundle composto
        super.onSaveInstanceState(outState);
    }

    public void takePhoto(View view) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Para verificar que de facto existe uma aplicação que dê conta do nosso pedido
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Se sim, lançamos o Intent
            startActivityForResult(intent, PHOTO_REQUEST_CODE);
        }
        else {
            // Se não existir, podemos mostrar uma mensagem de erro ao utilizador
            // TODO error
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            // Vamos buscar o thumbnail da foto
            Bitmap thumbnail = (Bitmap) extras.get("data");
            // Colocamos esse Bitmap na ImageView
            this.contactPhoto.setImageBitmap(thumbnail);
            // E podemos guardar o Bitmap para o caso de a Activity ser destruida (ver onSaveInstanceState)
            this.contactPhotoBitmap = thumbnail;
        }
    }

    /**
     * Decodes a Bitmap into an array of bytes
     * @param bmp The source Bitmap
     * @return An array of bytes or null if the Bitmap was null
     */
    private byte[] getBytesFromBitmap(Bitmap bmp) {
        if(bmp == null) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}
