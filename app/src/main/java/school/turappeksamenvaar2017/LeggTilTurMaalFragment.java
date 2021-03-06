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
import android.support.v4.content.FileProvider;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * Fragment for å legge til turmål
 */
public class LeggTilTurMaalFragment extends Fragment {

    final static String DBORDRE = "?aksjon=legg_til_turmaal";
    final static String DBBILDE = "?aksjon=legg_til_bilde";
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
        //Henter brukernavn fra lagrede instillinger
        SharedPreferences foretrekninger = PreferenceManager.getDefaultSharedPreferences(getContext());
        bruker = foretrekninger.getString("bruker","anonym");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View syn = inflater.inflate(R.layout.fragment_legg_til_tur_maal, container, false);

        //Henter referanser
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

        //Hvis det finnes lagret data så blir den gjennopprettet.
        if (savedInstanceState != null){
            eTNavn.setText(savedInstanceState.getString("navn"));
            eTType.setText(savedInstanceState.getString("type"));
            eTBeskrivelse.setText(savedInstanceState.getString("beskrivelse"));
            bildeSti = savedInstanceState.getString("bilde","");
            if (!bildeSti.equals("")){
                visBilde();
            }
        }

        //Henter lokasjons info
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

    /**
     * Lagrer data for å håndtere rotasjon.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (bildeSti != null){
            outState.putString("bilde",bildeSti);
        }
        outState.putString("navn",eTNavn.getText()+"");
        outState.putString("type",eTType.getText()+"");
        outState.putString("beskrivelse",eTBeskrivelse.getText()+"");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (sqLiteAdapter != null){
            sqLiteAdapter.steng();
        }
        super.onDestroy();
    }

    /**
     * Metoden takler hva som skjer når du vil ta et bilde med innebygget kamera
     */
    public void taBilde(){
        //Lager bildefil og intensjon
        File bildeFil = null;
        Intent taBildeIntensjon = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (taBildeIntensjon.resolveActivity(getActivity().getPackageManager()) != null){
            try {
                bildeFil = lagBildeFil();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            //hvis bildefilen som bildet skal bli lagret i ble opprettet, blir Uri laget
            // og intensjon startet
            if (bildeFil != null){
                Uri bildeUri = FileProvider.getUriForFile(getActivity(),"filgiver",bildeFil);
                taBildeIntensjon.putExtra(MediaStore.EXTRA_OUTPUT,bildeUri);
                startActivityForResult(taBildeIntensjon, AKSJON_TA_BILDE);
            }
        }
    }

    /**
     * Metoden oppretter en fil for lagring av bilde
     * @return Bildefil
     * @throws IOException
     */
    private File lagBildeFil() throws IOException{
        String tid = new SimpleDateFormat("yyyymmdd_HHmmss").format(new Date());
        File bildeFil = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),tid+".png");
        bildeSti = bildeFil.getAbsolutePath();
        return bildeFil;
    }

    /**
     * Metoden håndterer hva som skjer når du vil legge til et bilde fra telefonen
     */
    public void leggTilBilde(){
        Intent velgBildeIntensjon = new Intent();
        velgBildeIntensjon.setType("image/*");
        velgBildeIntensjon.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(velgBildeIntensjon,AKSJON_VELG_BILDE);
    }

    /**
     * Metoden håndterer hva som skjer når du vil legge til et turmål
     */
    public void leggTilTurmaal(){
        String navn = eTNavn.getText()+"";
        String type = eTType.getText()+"";
        String beskrivelse = eTBeskrivelse.getText()+"";
        String latitude = tVLatitude.getText()+"";
        String longitude = tVLongitude.getText()+"";
        String hoyde = tVHoyde.getText()+"";
        //Sjekker om noen felt er tomme.
        if (navn.equals("") || type.equals("") || beskrivelse.equals("") || latitude.equals("") || longitude.equals("") || hoyde.equals("")){
            Toast.makeText(getContext(),"Tomme felt er ikke tillat",Toast.LENGTH_SHORT).show();
        }
        else {
            if (bildeSti == null){
                bildeSti = "";
            }
            //Lagrer turmål i SQLite databasen
            sqLiteAdapter = new SQLiteAdapter(getActivity());
            sqLiteAdapter.aapne();
            double dLatitude = Double.parseDouble(latitude);
            double dLongitude = Double.parseDouble(longitude);
            int iHoyde = (int)Double.parseDouble(hoyde);
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

            //Hvis du harNett blir også dataen lagret i den sentrale databasen
            if (harNett()) {
                TurMaalOpplaster turMaalOpplaster = new TurMaalOpplaster();
                turMaalOpplaster.execute(navn,type,beskrivelse,bildeSti,latitude,longitude,hoyde,bruker);
            }
            //Hvis du ikke har nett blir dette registrert i SQLite databasen.
            else {
                nyeVerdier = new ContentValues();
                nyeVerdier.put(SQLiteAdapter.NAVN, navn);
                sqLiteAdapter.settInnNye(nyeVerdier);
            }
            sqLiteAdapter.steng();
        }
    }

    /**
     * Metoden henter ut lokasjonen hvis den kan.
     * @return lokasjonen, lokasjon er null hvis noe gikk galt
     */
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

    /**
     * Metoden takler hva som skjer når en av intensjonene for bilde returnerer
     * @param henteKode Tilsvarer koden for intensjonen
     * @param resultatKode  Kode for hvordan det gikk
     * @param data Intensjonen som ble returnert
     */
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

    /**
     * Viser bilde utifra bildeSti variabelen, bildet blir skalert til 250x250
     */
    private void visBilde(){
        iVBilde.setVisibility(View.VISIBLE);
        int maalW = 250;
        int maalH = 250;

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
    }

    /**
     * Sjekker om nettverkstilgang
     * @return Sant hvis du har nett eller usant hvis du ikke har nett
     */
    public boolean harNett()
    {
        ConnectivityManager tilkoblingsStyrer = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nettverkInfo = tilkoblingsStyrer.getActiveNetworkInfo();
        return (nettverkInfo != null && nettverkInfo.isConnected());
    }

    /**
     * Intern klasse for å asynkront laste opp data til Sentral database
     */
    private class TurMaalOpplaster extends AsyncTask<String, Void, Long>{

        @Override
        protected Long doInBackground(String... params) {
            HttpURLConnection tilkobling = null;
            //Hvis bildesti finnes, blir bildet lastet opp
            //Måten er hentet fra https://www.simplifiedcoding.net/android-upload-image-to-server/
            if (!bildeSti.equals("")) {
                String URI = MainActivity.DATABASEURL + DBBILDE; //Nett adresse til api
                try {
                    String lastOppId = UUID.randomUUID().toString();

                    new MultipartUploadRequest(getActivity(), lastOppId, URI)
                            .addFileToUpload(params[3], "bilde")
                            .addParameter("navn",params[0])
                            .setMaxRetries(2)
                            .startUpload();
                    params[3] = MainActivity.BILDEMAPPEURL+params[0]+".png";
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return 1l;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return 1l;
                }
            }

            //Lager Uri til api med Get parametre
            String URI = MainActivity.DATABASEURL+DBORDRE+"&navn="+params[0]+"&type="+params[1]
                    +"&beskrivelse="+params[2]+"&bilde="+params[3]+"&latitude="+params[4]
                    +"&longitude="+params[5]+"&hoyde="+params[6]+"&bruker="+params[7];

            //Laster opp data til databasen
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
