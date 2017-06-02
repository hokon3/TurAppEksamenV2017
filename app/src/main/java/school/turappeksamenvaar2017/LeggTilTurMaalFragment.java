package school.turappeksamenvaar2017;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

    SQLiteAdapter sqLiteAdapter;

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
        tVBruker = (TextView)syn.findViewById(R.id.leggTilBruker);
        kTaBilde = (Button)syn.findViewById(R.id.knappTaBilde);
        kLeggTilBilde = (Button)syn.findViewById(R.id.knappLeggTilBilde);
        kLeggTilTurmaal = (Button)syn.findViewById(R.id.knappLeggTilTurmaal);

        Location lokasjon = hentLokasjon();
        if (lokasjon != null){
            tVLatitude.setText(lokasjon.getLatitude()+"");
            tVLongitude.setText(lokasjon.getLongitude()+"");
            tVHoyde.setText(lokasjon.getAltitude()+"");
        }

        tVBruker.setText(bruker);

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
        String navn = eTNavn.getText()+"";
        String type = eTType.getText()+"";
        String beskrivelse = eTBeskrivelse.getText()+"";
        String latitude = tVLatitude.getText()+"";
        String longitude = tVLongitude.getText()+"";
        String hoyde = tVHoyde.getText()+"";
        if (navn.equals("") || type.equals("") || beskrivelse.equals("") || latitude.equals("") || longitude.equals("") || hoyde.equals("")){
            Toast.makeText(getContext(),"Tomme felt er ikke tillat",Toast.LENGTH_SHORT).show();
        }
        else {
            if (bildeSti == null){
                bildeSti = "";
            }
            sqLiteAdapter = new SQLiteAdapter(getActivity());
            sqLiteAdapter.aapne();
            double dLatitude = Double.parseDouble(latitude);
            double dLongitude = Double.parseDouble(longitude);
            int iHoyde = Integer.parseInt(hoyde);
            ContentValues nyeVerdier = new ContentValues();
            nyeVerdier.put(SQLiteAdapter.NAVN, navn);
            nyeVerdier.put(SQLiteAdapter.TYPE, type);
            nyeVerdier.put(SQLiteAdapter.BESKRIVELSE, beskrivelse);
            nyeVerdier.put(SQLiteAdapter.BILDE, bildeSti);
            nyeVerdier.put(SQLiteAdapter.LATITUDE, dLatitude);
            nyeVerdier.put(SQLiteAdapter.LONGITUDE, dLongitude);
            nyeVerdier.put(SQLiteAdapter.HOYDE, iHoyde);
            nyeVerdier.put(SQLiteAdapter.BRUKER, bruker);
            sqLiteAdapter.settInnTurMaal(nyeVerdier);

            if (harNett()) {
                TurMaalOpplaster turMaalOpplaster = new TurMaalOpplaster();
                turMaalOpplaster.execute(navn,type,beskrivelse,bildeSti,latitude,longitude,hoyde,bruker);
            }
            else {
                nyeVerdier = new ContentValues();
                nyeVerdier.put(SQLiteAdapter.NAVN, navn);
                sqLiteAdapter.settInnNye(nyeVerdier);
            }
            sqLiteAdapter.steng();
        }
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
            visBilde();
        }
        else if(henteKode == AKSJON_VELG_BILDE && resultatKode == Activity.RESULT_OK){
            bildeSti = data.getData().getPath();
            visBilde();
        }
    }

    private void visBilde(){
        int maalW = iVBilde.getWidth();
        int maalH = iVBilde.getHeight();

        BitmapFactory.Options bmOpsjoner = new BitmapFactory.Options();
        bmOpsjoner.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bildeSti,bmOpsjoner);
        int bildeW = bmOpsjoner.outWidth;
        int bildeH = bmOpsjoner.outHeight;

        int scaleFactor = Math.min(bildeW/maalW,bildeH/maalH);

        bmOpsjoner.inSampleSize = scaleFactor;
        bmOpsjoner.inJustDecodeBounds = false;
        Bitmap bitKart = BitmapFactory.decodeFile(bildeSti, bmOpsjoner);
        iVBilde.setImageBitmap(bitKart);
        iVBilde.setVisibility(View.VISIBLE);
    }

    // Sjekker om nettverkstilgang
    public boolean harNett()
    {
        ConnectivityManager tilkoblingsStyrer = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nettverkInfo = tilkoblingsStyrer.getActiveNetworkInfo();
        return (nettverkInfo != null && nettverkInfo.isConnected());
    }

    private class TurMaalOpplaster extends AsyncTask<String, Void, Long>{

        @Override
        protected Long doInBackground(String... params) {
            HttpURLConnection tilkobling = null;
            String URI = MainActivity.DATABASEURL+DBORDRE+"&navn="+params[0]+"&type="+params[1]
                    +"&beskrivelse="+params[2]+"&bilde="+params[3]+"&latitude="+params[4]
                    +"&longitude="+params[5]+"&hoyde="+params[6]+"&bruker="+params[7];

            try {
                URL url = new URL(URI);
                tilkobling = (HttpURLConnection)url.openConnection();
                tilkobling.connect();
                int status = tilkobling.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK){
                    InputStream is = tilkobling.getInputStream();
                    BufferedReader leser = new BufferedReader(new InputStreamReader(is));
                    String svar;
                    StringBuilder sb = new StringBuilder();
                    while ((svar = leser.readLine()) != null){
                        sb = sb.append(svar);
                    }
                    svar = sb.toString();
                    if (svar.equals("true")){
                        return 0l;
                    }
                    return 1l;
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                return 1l;
            }
            catch (IOException e) {
                e.printStackTrace();
                return 1l;
            }
            return 1l;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (aLong == 1l){
                Toast.makeText(getContext(),"Feil under opplasting av nytt turmål",Toast.LENGTH_SHORT).show();
            }
            else {
                TurMaalListeFragment turMaalListeFragment = new TurMaalListeFragment();
                MainActivity.byttFragment(turMaalListeFragment);
            }
        }
    }
}
