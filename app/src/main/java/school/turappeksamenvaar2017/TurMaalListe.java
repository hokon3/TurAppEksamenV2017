package school.turappeksamenvaar2017;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class TurMaalListe extends Fragment {


    public TurMaalListe() {
        // Nødvendig tom konstruktør
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tur_maal_liste, container, false);
    }

}
