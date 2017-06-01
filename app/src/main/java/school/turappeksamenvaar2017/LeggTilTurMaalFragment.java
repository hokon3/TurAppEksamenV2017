package school.turappeksamenvaar2017;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeggTilTurMaalFragment extends Fragment {

    final static String DBORDRE = "?aksjon=legg_til_turmaal";
    final static int AKSJON_TA_BILDE = 1;
    final static int AKSJON_VELG_BILDE = 2;

    String bruker, bildeSti;
    EditText eTNavn, eTType, eTBeskrivelse;
    ImageView iVBilde;
    TextView tVLatitude, tVLongitude, tVHoyde, tVBruker;
    Button kTaBilde, kLeggTilBilde, kLeggTilTurmaal;

    public LeggTilTurMaalFragment() {
        // Nødvending tom konstruktør
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences foretrekninger = PreferenceManager.getDefaultSharedPreferences(getContext());
        bruker = foretrekninger.getString("bruker","anonym");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View syn = inflater.inflate(R.layout.fragment_legg_til_tur_maal, container, false);

        eTNavn = (EditText)syn.findViewById(R.id.leggTilNavn);
        eTType = (EditText)syn.findViewById(R.id.leggTilType);
        eTBeskrivelse = (EditText)syn.findViewById(R.id.leggTilBeskrivelse);
        iVBilde = (ImageView)syn.findViewById(R.id.leggTilBilde);
        tVLatitude = (TextView)syn.findViewById(R.id.leggTilLatitude);
        tVLongitude = (TextView)syn.findViewById(R.id.leggTilLongitude);
        tVHoyde = (TextView)syn.findViewById(R.id.leggTilHoyde);
        kTaBilde = (Button)syn.findViewById(R.id.knappTaBilde);
        kLeggTilBilde = (Button)syn.findViewById(R.id.knappLeggTilBilde);
        kLeggTilTurmaal = (Button)syn.findViewById(R.id.knappLeggTilTurmaal);

        Location lokasjon = hentLokasjon();
        if (lokasjon != null){
            tVLatitude.setText(lokasjon.getLatitude()+"");
            tVLongitude.setText(lokasjon.getLongitude()+"");
            tVHoyde.setText(lokasjon.getAltitude()+"");
        }

        kTaBilde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taBilde();
            }
        });

        kLeggTilBilde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leggTilBilde();
            }
        });

        kLeggTilTurmaal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leggTilTurmaal();
            }
        });

        return syn;
    }

    public void taBilde(){
        File bildeFil = null;
        Intent taBildeIntensjon = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (taBildeIntensjon.resolveActivity(getActivity().getPackageManager()) != null){
            try {
                bildeFil = lagBildeFil();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (bildeFil != null){
                Uri bildeUri = Uri.fromFile(bildeFil);
                taBildeIntensjon.putExtra(MediaStore.EXTRA_OUTPUT,bildeUri);
                startActivityForResult(taBildeIntensjon, AKSJON_TA_BILDE);
            }
        }
    }

    private File lagBildeFil() throws IOException{
        String tid = new SimpleDateFormat("yyyymmdd_HHmmss").format(new Date());
        File bildeFil = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),tid);
        bildeSti = bildeFil.getAbsolutePath();
        return bildeFil;
    }

    public void leggTilBilde(){
        Intent velgBildeIntensjon = new Intent();
        velgBildeIntensjon.setType("image/*");
        velgBildeIntensjon.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(velgBildeIntensjon,AKSJON_VELG_BILDE);
    }

    public void leggTilTurmaal(){

    }

    public Location hentLokasjon(){
        Location lokasjon = null;
        LocationManager lokasjonsStyrer = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        String lokasjonGiver = LocationManager.GPS_PROVIDER;
        if (lokasjonsStyrer.isProviderEnabled(lokasjonGiver)){
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getActivity(),"Ikke tilatelse til å hente posisjon", Toast.LENGTH_LONG);
            }
            else {
                lokasjon = lokasjonsStyrer.getLastKnownLocation(lokasjonGiver);
            }
        }
        return lokasjon;
    }

    @Override
    public void onActivityResult(int henteKode, int resultatKode, Intent data) {
        if (henteKode == AKSJON_TA_BILDE && resultatKode == Activity.RESULT_OK){
            visBilde(bildeSti);
        }
        else if(henteKode == AKSJON_VELG_BILDE && resultatKode == Activity.RESULT_OK){
            visBilde(data.getData().getPath());
        }
    }

    private void visBilde(String sti){
        int maalW = iVBilde.getWidth();
        int maalH = iVBilde.getHeight();

        BitmapFactory.Options bmOpsjoner = new BitmapFactory.Options();
        bmOpsjoner.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sti,bmOpsjoner);
        int bildeW = bmOpsjoner.outWidth;
        int bildeH = bmOpsjoner.outHeight;

        int scaleFactor = Math.min(bildeW/maalW,bildeH/maalH);

        bmOpsjoner.inSampleSize = scaleFactor;
        bmOpsjoner.inJustDecodeBounds = false;
        Bitmap bitKart = BitmapFactory.decodeFile(sti, bmOpsjoner);
        iVBilde.setImageBitmap(bitKart);
        iVBilde.setVisibility(View.VISIBLE);
    }

    private class TurMaalOpplaster extends AsyncTask<String, Void, Long>{

        @Override
        protected Long doInBackground(String... params) {
            return null;
        }
    }
}
