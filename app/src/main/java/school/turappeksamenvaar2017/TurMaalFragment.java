package school.turappeksamenvaar2017;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * Fragment for å hvis informasjon om et turmål
 */
public class TurMaalFragment extends Fragment {

    TurMaal turMaal;
    TextView tekstVisningNavn, tekstVisningType, tekstVisningBeskrivelse, tekstVisningLatitude, tekstVisningLongitude,
            tekstVisningHoyde, tekstVisningBruker;
    ImageView bildeVisningBilde;
    Button knappVisKart;
    Bitmap bitMap;
    Bundle data;

    public TurMaalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Henter Bundle med data om turmål
        if (savedInstanceState == null) {
            data = getArguments();
        }
        else {
            data = savedInstanceState.getBundle("data");
        }
        //Lager turmål utifra data
        String navn = data.getString("navn");
        String type = data.getString("type");
        String beskrivelse = data.getString("beskrivelse");
        String bildeUrl = data.getString("bilde");
        double latitude = data.getDouble("latitude");
        double longitude = data.getDouble("longitude");
        int hoyde = data.getInt("hoyde");
        String bruker = data.getString("bruker");

        turMaal = new TurMaal(navn, type, beskrivelse, bildeUrl, latitude, longitude, hoyde, bruker);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View syn = inflater.inflate(R.layout.fragment_tur_maal, container, false);

        //Henter referanser
        tekstVisningNavn = (TextView)syn.findViewById(R.id.TvNavn);
        tekstVisningType = (TextView)syn.findViewById(R.id.TvType);
        tekstVisningBeskrivelse = (TextView)syn.findViewById(R.id.TvBeskrivelse);
        bildeVisningBilde = (ImageView)syn.findViewById(R.id.IvBilde);
        tekstVisningLatitude = (TextView)syn.findViewById(R.id.TvLatitude);
        tekstVisningLongitude = (TextView)syn.findViewById(R.id.TvLongitude);
        tekstVisningHoyde = (TextView)syn.findViewById(R.id.TvHoyde);
        tekstVisningBruker = (TextView)syn.findViewById(R.id.TvBruker);
        knappVisKart = (Button)syn.findViewById(R.id.knappVisIKart);

        //Setter innhold
        tekstVisningNavn.setText(turMaal.navn);
        tekstVisningType.setText(turMaal.type);
        tekstVisningBeskrivelse.setText(turMaal.beskrivelse);
        tekstVisningLatitude.setText(turMaal.latitude+"");
        tekstVisningLongitude.setText(turMaal.longitude+"");
        tekstVisningHoyde.setText(turMaal.hoyde+"");
        tekstVisningBruker.setText(turMaal.bruker);

        //Fester lytter til knapp for å vise i kart
        knappVisKart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visKart();
            }
        });

        //Henter bilde hvis turmålet har et bilde
        if(!turMaal.bildeUrl.equals("")){
            if (harNett()){
                new LastBilde().execute(turMaal.bildeUrl);
            }
            else { //Sjekker om bildet finnes lokalt hvis du ikke har nett
                File bildeFil = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), turMaal.navn);
                if (bildeFil.exists()){
                    bitMap = BitmapFactory.decodeFile(bildeFil.getAbsolutePath());
                    visBilde();
                }
            }
        }

        return syn;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle("data",data);
        super.onSaveInstanceState(outState);
    }

    /**
     * Metode for å vise et bilde
     */
    public void visBilde(){
        bildeVisningBilde.setVisibility(View.VISIBLE);
        bildeVisningBilde.setImageBitmap(Bitmap.createScaledBitmap(bitMap,250,250,false));
    }

    /**
     * Metode for å vise turmål på kart
     */
    public void visKart(){
        //Url for kart med markør
        //Endring for å få markør hentet herfra:
        //https://stackoverflow.com/questions/3990110/how-to-show-marker-in-maps-launched-by-geo-uri-intent
        String geoUrl = "geo:" + turMaal.latitude + ","
                + turMaal.longitude + "?q=" + turMaal.latitude + "," + turMaal.longitude + "(" + turMaal.navn + ")" ;
        Uri geoUri = Uri.parse(geoUrl);
        Intent geoKart = new Intent(Intent.ACTION_VIEW,geoUri);
        startActivity(geoKart);
    }

    /**
     * Metoden sjekker om du har nett
     * @return Sant hvis du har nett, ellers usant
     */
    public boolean harNett()
    {
        ConnectivityManager tilkoblingsStyrer = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nettverkInfo = tilkoblingsStyrer.getActiveNetworkInfo();
        return (nettverkInfo != null && nettverkInfo.isConnected());
    }

    /**
     * Asynkron klasse for å laste inn et bilde
     */
    private class LastBilde extends AsyncTask<String, Void, Long> {

        @Override
        protected Long doInBackground(String... params) {
            //Sjekker om bildet finnes lokalt fra før
            File bildeFil = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), turMaal.navn);
            if (bildeFil.exists()){
                bitMap = BitmapFactory.decodeFile(bildeFil.getAbsolutePath());
                return 0l;
            }
            //Laster ned bildet
            try{
                URL bildeUrl = new URL(params[0]);
                URLConnection tilkobling = bildeUrl.openConnection();
                tilkobling.connect();
                InputStream is = tilkobling.getInputStream();
                bitMap = BitmapFactory.decodeStream(is);

                bildeFil.createNewFile();
                OutputStream bildeUtStrom = new BufferedOutputStream(new FileOutputStream(bildeFil));
                bitMap.compress(Bitmap.CompressFormat.PNG,100,bildeUtStrom);
                bildeUtStrom.flush();
                bildeUtStrom.close();
                return 0l;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
                return 1l;
            }
            catch (IOException e) {
                e.printStackTrace();
                return 1l;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (aLong == 0){
                visBilde();
            }
            else {
                Toast.makeText(getContext(),"Feil under lasting av bilde",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
