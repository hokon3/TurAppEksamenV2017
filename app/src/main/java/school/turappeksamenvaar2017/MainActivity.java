package school.turappeksamenvaar2017;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static FragmentManager fm;
    static FragmentTransaction transaksjon;
    static final String DATABASEURL = "http://itfag.usn.no/~142840/turmaal/api.php";
    static final String BILDEMAPPEURL = "http://itfag.usn.no/~142840/turmaal/bilder/";
    static final int MIN_LOV_KODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getSupportFragmentManager();

        sjekkOmLov();

        if (savedInstanceState == null){
            TurMaalListeFragment turMaalListeFragment = new TurMaalListeFragment();
            leggTilFragment(turMaalListeFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater oppblaser = getMenuInflater();
        oppblaser.inflate(R.menu.bar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.aksjon_instillinger: //Hvis du vil inn i instillinger
                InstillingerFragment instillingerFragment = new InstillingerFragment();
                MainActivity.byttFragment(instillingerFragment);
                break;
            case R.id.aksjon_legg_til: //Hvis du vil legge til turmål
                LeggTilTurMaalFragment leggTilTurMaalFragment = new LeggTilTurMaalFragment();
                MainActivity.byttFragment(leggTilTurMaalFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metode for å bytte ut det aktive fragmentet med et annet fragment
     * @param nyttFragment Fragmentet som blir byttet til
     */
    public static void byttFragment(Fragment nyttFragment)
    {
        transaksjon = fm.beginTransaction();
        transaksjon.replace(R.id.fragment_inneholder, nyttFragment, nyttFragment.getClass().getName());
        transaksjon.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaksjon.addToBackStack(null);
        transaksjon.commit();
    }

    /**
     * Versjon av byttFragment for å gå til hvisning av et turmål
     * @param nyttFragment Fragmentet som blir byttet til
     * @param turMaal Turmålet som skal bli vist.
     */
    public static void byttFragment(Fragment nyttFragment, TurMaal turMaal)
    {
        //data om turmål brytt ned og lagret i en Bundle.
        Bundle data = new Bundle();
        data.putString("navn",turMaal.navn);
        data.putString("type",turMaal.type);
        data.putString("beskrivelse",turMaal.beskrivelse);
        data.putString("bilde",turMaal.bildeUrl);
        data.putDouble("latitude",turMaal.latitude);
        data.putDouble("longitude",turMaal.longitude);
        data.putInt("hoyde",turMaal.hoyde);
        data.putString("bruker",turMaal.bruker);

        transaksjon = fm.beginTransaction();
        nyttFragment.setArguments(data);
        transaksjon.replace(R.id.fragment_inneholder, nyttFragment, nyttFragment.getClass().getName());
        transaksjon.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaksjon.addToBackStack(null);
        transaksjon.commit();
    }

    /**
     * Metode for å legge til et Fragment
     * @param newFragment Fragmentet som blir lagt til
     */
    public static void leggTilFragment(Fragment newFragment)
    {
        transaksjon = fm.beginTransaction();
        transaksjon.add(R.id.fragment_inneholder, newFragment, newFragment.getClass().getName());
        transaksjon.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaksjon.addToBackStack(null);
        transaksjon.commit();
    }

    /**
     * Metoden spør om tilatelse for lokasjon hvis appen ikke allerede har tilatelse
     */
    public void sjekkOmLov(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MIN_LOV_KODE);
        }
    }

}
