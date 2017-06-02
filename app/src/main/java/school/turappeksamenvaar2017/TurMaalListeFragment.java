package school.turappeksamenvaar2017;


import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Fragment for å vise liste over turmål.
 */
public class TurMaalListeFragment extends Fragment {

    ListView turMaalListeSyn;
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

    public void lastListe(){
        sqLiteAdapter = new SQLiteAdapter(getActivity());
        sqLiteAdapter.aapne();
        if (harNett()){
            ListeLaster listeLaster = new ListeLaster();
            listeLaster.execute(MainActivity.DATABASEURL+DBORDRE);
        }
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


    private class ListeLaster extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            HttpURLConnection tilkobling = null;
            try {
                Cursor peker = sqLiteAdapter.hentNye();
                if (peker.moveToFirst()){
                    String[] nyeInlegg = new String[peker.getCount()];
                    int index = 0;
                    do {
                        nyeInlegg[index++] = peker.getString(peker.getColumnIndex(SQLiteAdapter.NAVN));
                    } while (peker.moveToNext());
                    peker.close();
                    peker = sqLiteAdapter.hentNyeTurmaal(nyeInlegg);
                    peker.moveToFirst();
                    do {
                        TurMaal turMaal = TurMaal.hentTurMaalFraPeker(peker);
                        String URI = MainActivity.DATABASEURL+LeggTilTurMaalFragment.DBORDRE
                                +"&navn="+turMaal.navn+"&type="+turMaal.type
                                +"&beskrivelse="+turMaal.beskrivelse+"&bilde="+turMaal.bildeUrl
                                +"&latitude="+turMaal.latitude+"&longitude="+turMaal.longitude
                                +"&hoyde="+turMaal.hoyde+"&bruker="+turMaal.bruker;
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
                }

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

    // Sjekker om nettverkstilgang
    public boolean harNett()
    {
        ConnectivityManager tilkoblingsStyrer = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nettverkInfo = tilkoblingsStyrer.getActiveNetworkInfo();
        return (nettverkInfo != null && nettverkInfo.isConnected());
    }
}
