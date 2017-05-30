package school.turappeksamenvaar2017;


import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
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

    public TurMaalListeFragment() {
        // Nødvendig tom konstruktør
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View syn = inflater.inflate(R.layout.fragment_tur_maal_liste, container, false);
        turMaalListeSyn = (ListView)syn.findViewById(R.id.turMaalListe);

        lastListe();

        return syn;
    }

    public void lastListe(){
        if (harNett()){
            ListeLaster listeLaster = new ListeLaster();
            listeLaster.execute(MainActivity.DATABASEURL+DBORDRE);
        }
        else {
            Toast.makeText(getActivity(), "Ingen nettverkstilgang. Laster lagret liste.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void oppdaterListe(ArrayList<TurMaal> nyListe){
        TurMaalAdapter turMaalAdapter = new TurMaalAdapter(getContext(),nyListe);
        turMaalListeSyn.setAdapter(turMaalAdapter);
    }


    private class ListeLaster extends AsyncTask<String,Void,Long>{

        @Override
        protected Long doInBackground(String... params) {
            HttpURLConnection tilkobling = null;
            try {
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
                Toast.makeText(getContext(),"ERROR during load from database",Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Sjekker om nettverkstilgang
    public boolean harNett()
    {
        ConnectivityManager tilkoblingsStyrer = (ConnectivityManager) getContext().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nettverkInfo = tilkoblingsStyrer.getActiveNetworkInfo();
        return (nettverkInfo != null && nettverkInfo.isConnected());
    }
}
