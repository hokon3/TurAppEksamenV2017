package school.turappeksamenvaar2017;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * A simple {@link Fragment} subclass.
 */
public class TurMaalFragment extends Fragment {

    TurMaal turMaal;
    TextView tekstVisningNavn, tekstVisningType, tekstVisningBeskrivelse, tekstVisningLatitude, tekstVisningLongitude,
            tekstVisningHoyde, tekstVisningBruker;
    ImageView bildeVisningBilde;
    Bitmap bitMap;
    Bundle data;

    public TurMaalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            data = getArguments();
        }
        else {
            data = savedInstanceState.getBundle("data");
        }
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
        // Inflate the layout for this fragment
        View syn = inflater.inflate(R.layout.fragment_tur_maal, container, false);

        tekstVisningNavn = (TextView)syn.findViewById(R.id.TvNavn);
        tekstVisningType = (TextView)syn.findViewById(R.id.TvType);
        tekstVisningBeskrivelse = (TextView)syn.findViewById(R.id.TvBeskrivelse);
        bildeVisningBilde = (ImageView)syn.findViewById(R.id.IvBilde);
        tekstVisningLatitude = (TextView)syn.findViewById(R.id.TvLatitude);
        tekstVisningLongitude = (TextView)syn.findViewById(R.id.TvLongitude);
        tekstVisningHoyde = (TextView)syn.findViewById(R.id.TvHoyde);
        tekstVisningBruker = (TextView)syn.findViewById(R.id.TvBruker);

        tekstVisningNavn.setText(turMaal.navn);
        tekstVisningType.setText(turMaal.type);
        tekstVisningBeskrivelse.setText(turMaal.beskrivelse);
        tekstVisningLatitude.setText(turMaal.latitude+"");
        tekstVisningLongitude.setText(turMaal.longitude+"");
        tekstVisningHoyde.setText(turMaal.hoyde+"");
        tekstVisningBruker.setText(turMaal.bruker);

        if(!turMaal.bildeUrl.equals("")){
            if (harNett()){
                new LastBilde().execute(turMaal.bildeUrl);
            }
            else {
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



    public void visBilde(){
        bildeVisningBilde.setVisibility(View.VISIBLE);
        bildeVisningBilde.setImageBitmap(Bitmap.createScaledBitmap(bitMap,250,250,false));
    }

    // Sjekker om nettverkstilgang
    public boolean harNett()
    {
        ConnectivityManager tilkoblingsStyrer = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo nettverkInfo = tilkoblingsStyrer.getActiveNetworkInfo();
        return (nettverkInfo != null && nettverkInfo.isConnected());
    }

    private class LastBilde extends AsyncTask<String, Void, Long> {

        @Override
        protected Long doInBackground(String... params) {
            File bildeFil = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), turMaal.navn);
            if (bildeFil.exists()){
                bitMap = BitmapFactory.decodeFile(bildeFil.getAbsolutePath());
                return 0l;
            }
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
