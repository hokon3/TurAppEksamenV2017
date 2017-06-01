package school.turappeksamenvaar2017;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    static FragmentManager fm;
    static FragmentTransaction transaksjon;
    static final String DATABASEURL = "http://itfag.usn.no/~142840/turmaal/api.php";
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
            case R.id.aksjon_instillinger:
                InstillingerFragment instillingerFragment = new InstillingerFragment();
                MainActivity.byttFragment(instillingerFragment);
                break;
            case R.id.aksjon_legg_til:
                LeggTilTurMaalFragment leggTilTurMaalFragment = new LeggTilTurMaalFragment();
                MainActivity.byttFragment(leggTilTurMaalFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void byttFragment(Fragment nyttFragment)
    {
        transaksjon = fm.beginTransaction();
        transaksjon.replace(R.id.fragment_inneholder, nyttFragment, nyttFragment.getClass().getName());
        transaksjon.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaksjon.addToBackStack(null);
        transaksjon.commit();
    }

    public static void byttFragment(Fragment nyttFragment, TurMaal turMaal)
    {
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

    public static void leggTilFragment(Fragment newFragment)
    {
        transaksjon = fm.beginTransaction();
        transaksjon.add(R.id.fragment_inneholder, newFragment, newFragment.getClass().getName());
        transaksjon.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaksjon.addToBackStack(null);
        transaksjon.commit();
    }

    public void sjekkOmLov(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MIN_LOV_KODE);
        }
    }

}
