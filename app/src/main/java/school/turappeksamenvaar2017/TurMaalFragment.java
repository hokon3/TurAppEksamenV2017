package school.turappeksamenvaar2017;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TurMaalFragment extends Fragment {

    TurMaal turMaal;
    TextView tekstVisningNavn, tekstVisningType, tekstVisningBeskrivelse, tekstVisningLatitude, tekstVisningLongitude,
            tekstVisningHoyde, tekstVisningBruker;
    ImageView bildeVisningBilde;

    public TurMaalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle data = getArguments();
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

        return syn;
    }

}
