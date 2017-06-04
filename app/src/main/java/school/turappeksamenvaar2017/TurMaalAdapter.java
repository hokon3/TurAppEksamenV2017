package school.turappeksamenvaar2017;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter klasse for å hvis Turmål i ListView
 */

public class TurMaalAdapter extends BaseAdapter {

    Context kontekst;
    ArrayList<TurMaal> turMaal;
    LayoutInflater oppBlåser;

    public TurMaalAdapter(Context c, ArrayList<TurMaal> t){
        kontekst = c;
        turMaal = t;
        oppBlåser = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return turMaal.size();
    }

    @Override
    public Object getItem(int posisjon) {
        return turMaal.get(posisjon);
    }

    @Override
    public long getItemId(int posisjon) {
        return posisjon;
    }

    @Override
    public View getView(int posisjon, View konversjonsSyn, ViewGroup forelder) {
        ViewHolder synsHolder;
        if (konversjonsSyn == null){
            konversjonsSyn = oppBlåser.inflate(R.layout.tur_maal,null);
            synsHolder = new ViewHolder();
            synsHolder.textViewTurMaalNavn = (TextView) konversjonsSyn.findViewById(R.id.turMaalNavn);
            synsHolder.textViewTurMaalType = (TextView) konversjonsSyn.findViewById(R.id.turMaalType);
            synsHolder.textViewTurMaalHoyde = (TextView) konversjonsSyn.findViewById(R.id.turMaalHoyde);
            konversjonsSyn.setTag(synsHolder);
        }
        else {
            synsHolder = (ViewHolder) konversjonsSyn.getTag();
        }

        final TurMaal detteMaalet = turMaal.get(posisjon);
        synsHolder.textViewTurMaalNavn.setText(detteMaalet.navn);
        synsHolder.textViewTurMaalType.setText(detteMaalet.type);
        synsHolder.textViewTurMaalHoyde.setText(detteMaalet.hoyde+"");

        konversjonsSyn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TurMaalFragment fragment = new TurMaalFragment();
                MainActivity.byttFragment(fragment, detteMaalet);
            }
        });

        return konversjonsSyn;
    }

    private static class ViewHolder{
        public TextView textViewTurMaalNavn;
        public TextView textViewTurMaalType;
        public TextView textViewTurMaalHoyde;
    }
}
