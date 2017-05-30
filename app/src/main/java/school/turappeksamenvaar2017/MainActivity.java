package school.turappeksamenvaar2017;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    static FragmentManager fm;
    static FragmentTransaction transaksjon;
    static final String DATABASEURL = "http://itfag.usn.no/~142840/turmaal/api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getSupportFragmentManager();

        if (savedInstanceState == null){
            TurMaalListeFragment turMaalListeFragment = new TurMaalListeFragment();
            leggTilFragment(turMaalListeFragment);
        }
    }

    public static void byttFragment(Fragment nyttFragment)
    {
        transaksjon = fm.beginTransaction();
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
}
