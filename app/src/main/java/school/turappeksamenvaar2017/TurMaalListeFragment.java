package school.turappeksamenvaar2017;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;


/**
 * Fragment for å vise liste over turmål.
 */
public class TurMaalListeFragment extends Fragment {

    ListView turMaalListeSyn;
    Button knappSortering;
    TurMaalAdapter turMaalAdapter;
    ArrayList<TurMaal> liste;
    private final String DBORDRE = "?aksjon=hent_liste";
    SQLiteAdapter sqLiteAdapter;

    public TurMaalListeFragment() {
        // Nødvendig tom konstruktør
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View syn = inflater.inflate(R.layout.fragment_tur_maal_liste, container, false);
        turMaalListeSyn = (ListView)syn.findViewById(R.id.turMaalListe);
        knappSortering = (Button)syn.findViewById(R.id.knappSorterEtterDistance);

        knappSortering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location lokasjon = hentLokasjon();
                if (lokasjon != null){
                    sorterEtterDistanse(lokasjon.getLatitude(),lokasjon.getLongitude());
                }
            }
        });

        lastListe();

        return syn;
    }

    @Override
    public void onDestroy() {
        if (sqLiteAdapter != null){
            sqLiteAdapter.steng();
        }
        super.onDestroy();
    }

    /**
     * Metode for å laste inn liste over turmål
     */
    public void lastListe(){
        //Åpner adgang til lokal database
        sqLiteAdapter = new SQLiteAdapter(getActivity());
        sqLiteAdapter.aapne();
        //Sjekker om du har nett
        if (harNett()){
            ListeLaster listeLaster = new ListeLaster();
            listeLaster.execute(MainActivity.DATABASEURL+DBORDRE);
        }
        //Hvis du ikke har nett, laster liste fra lokal database
        else {
            Toast.makeText(getActivity(), "Ingen nettverkstilgang. Laster lagret liste.",
                    Toast.LENGTH_SHORT).show();
            liste = new ArrayList<>();
            Cursor peker = sqLiteAdapter.hentTurmaal("navn");
            if (peker.moveToFirst()){
                do{
                    liste.add(TurMaal.hentTurMaalFraPeker(peker));
                } while (peker.moveToNext());
            }
            peker.close();
            sqLiteAdapter.steng();
            oppdaterListe(liste);
        }
    }

    /**
     * Metoden viser listen og passer på at den lokale databasen er oppdatert
     * @param nyListe Listen
     */
    public void oppdaterListe(ArrayList<TurMaal> nyListe){
        turMaalAdapter = new TurMaalAdapter(getContext(),nyListe);
        turMaalListeSyn.setAdapter(turMaalAdapter);
        for(TurMaal t : nyListe){
            ContentValues nyeVerdier = new ContentValues();
            nyeVerdier.put(SQLiteAdapter.NAVN, t.navn);
            nyeVerdier.put(SQLiteAdapter.TYPE, t.type);
            nyeVerdier.put(SQLiteAdapter.BESKRIVELSE, t.beskrivelse);
            nyeVerdier.put(SQLiteAdapter.BILDE, t.bildeUrl);
            nyeVerdier.put(SQLiteAdapter.LATITUDE, t.latitude);
            nyeVerdier.put(SQLiteAdapter.LONGITUDE, t.longitude);
            nyeVerdier.put(SQLiteAdapter.HOYDE, t.hoyde);
            nyeVerdier.put(SQLiteAdapter.BRUKER, t.bruker);
            sqLiteAdapter.settInnTurMaal(nyeVerdier);
        }
    }

    /**
     * Metode for å hente lokasjonen hvis den kan.
     * @return
     */
    public Location hentLokasjon(){
        Location lokasjon = null;
        LocationManager lokasjonsStyrer = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        String lokasjonGiver = LocationManager.GPS_PROVIDER;
        if (lokasjonsStyrer.isProviderEnabled(lokasjonGiver)){
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getActivity(),"Har ikke tilatelse til å hente din posisjon", Toast.LENGTH_LONG);
            }
            else {
                lokasjon = lokasjonsStyrer.getLastKnownLocation(lokasjonGiver);
            }
        }
        return lokasjon;
    }

    /**
     * Metode for å sortere listen etter distanse
     * @param fraLatitude Din latitude
     * @param fraLongitude Din longitude
     */
    public void sorterEtterDistanse(double fraLatitude, double fraLongitude){
        //Finner distanse for hvert turmål
        float[] resultat = new float[1];
        for (int i = 0; i<liste.size(); i++){
            TurMaal t = liste.get(i);
            Location.distanceBetween(fraLatitude,fraLongitude,t.latitude,t.longitude,resultat);
            t.distanse = resultat[0];
        }
        //sorterer
        Collections.sort(liste);

        oppdaterListe(liste);
    }

    /**
     * Sjekker om nettverkstilgang
     * @return Sant hvis du har nett, usant ellers
     */
    public boolean harNett()
    {
        ConnectivityManager tilkoblingsStyrer = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nettverkInfo = tilkoblingsStyrer.getActiveNetworkInfo();
        return (nettverkInfo != null && nettverkInfo.isConnected());
    }

    /**
     * Intern klasse for å laste listen fra den sentrale databasen
     */
    private class ListeLaster extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            HttpURLConnection tilkobling = null;
            try {
                //Laster opp nye turmål som ble opprettet mens du ikke hadde nett
                Cursor peker = sqLiteAdapter.hentNye();
                if (peker.moveToFirst()){ //Sjekker om det er nye turmål
                    //Henter nye turmål fra lokal database
                    String[] nyeInlegg = new String[peker.getCount()];
                    int index = 0;
                    do {
                        nyeInlegg[index++] = peker.getString(peker.getColumnIndex(SQLiteAdapter.NAVN));
                    } while (peker.moveToNext());
                    peker.close();
                    peker = sqLiteAdapter.hentNyeTurmaal(nyeInlegg);
                    peker.moveToFirst();
                    //Laster opp nye turmål
                    do {
                        TurMaal turMaal = TurMaal.hentTurMaalFraPeker(peker);
                        if (!turMaal.bildeUrl.equals("")){
                            //Lager Uri for opplasting av bilde
                            String URI = MainActivity.DATABASEURL + LeggTilTurMaalFragment.DBBILDE;

                            // Laster opp bildet
                            // Måten er hentet fra https://www.simplifiedcoding.net/android-upload-image-to-server/
                            String lastOppId = UUID.randomUUID().toString();
                            new MultipartUploadRequest(getActivity(), lastOppId, URI)
                                    .addFileToUpload(turMaal.bildeUrl, "bilde")
                                    .addParameter("navn",turMaal.navn)
                                    .setMaxRetries(2)
                                    .startUpload();
                            turMaal.bildeUrl = MainActivity.BILDEMAPPEURL+turMaal.navn+".png";
                        }
                        //Lager URI for opplasting av turmål
                        String URI = MainActivity.DATABASEURL+LeggTilTurMaalFragment.DBORDRE
                                +"&navn="+turMaal.navn+"&type="+turMaal.type
                                +"&beskrivelse="+turMaal.beskrivelse+"&bilde="+turMaal.bildeUrl
                                +"&latitude="+turMaal.latitude+"&longitude="+turMaal.longitude
                                +"&hoyde="+turMaal.hoyde+"&bruker="+turMaal.bruker;
                        //Laster opp turmål informasjon
                        URL url = new URL(URI);
                        tilkobling = (HttpURLConnection)url.openConnection();
                        tilkobling.connect();
                        int status = tilkobling.getResponseCode();
                        if (status == HttpURLConnection.HTTP_OK) {
                            InputStream is = tilkobling.getInputStream();
                            BufferedReader leser = new BufferedReader(new InputStreamReader(is));
                            String svar;
                            StringBuilder sb = new StringBuilder();
                            while ((svar = leser.readLine()) != null) {
                                sb = sb.append(svar);
                            }
                            svar = sb.toString();
                            if (svar.equals("false")) {
                                return 1l;
                            }
                        }
                    } while (peker.moveToNext());
                    peker.close();
                    sqLiteAdapter.slettNye(nyeInlegg);
                }

                //Laster ned liste fra sentral database
                URL listeURL = new URL(params[0]);
                tilkobling = (HttpURLConnection) listeURL.openConnection();
                tilkobling.connect();
                int status = tilkobling.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK){
                    InputStream is = tilkobling.getInputStream();
                    BufferedReader leser = new BufferedReader(new InputStreamReader(is));
                    String svar;
                    StringBuilder sb = new StringBuilder();
                    while ((svar = leser.readLine()) != null){
                        sb.append(svar);
                    }
                    String listeData = sb.toString();
                    liste = TurMaal.lagListe(listeData);
                    return 0l;
                }
                return 1l;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                return 1l;
            }
            catch (IOException e) {
                e.printStackTrace();
                return 1l;
            }
            catch (JSONException e) {
                e.printStackTrace();
                return 1l;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (aLong == 0){
                oppdaterListe(liste);
            }
            else {
                Toast.makeText(getContext(),"Feil under lasting fra database",Toast.LENGTH_SHORT).show();
                if (sqLiteAdapter != null) {
                    sqLiteAdapter.steng();
                }
            }
        }
    }
}
