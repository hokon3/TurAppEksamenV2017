package school.turappeksamenvaar2017;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hakom_000 on 29.05.2017.
 */

public class TurMaalAdapter extends BaseAdapter {

    Context kontekst;
    TurMaal[] turMaal;
    LayoutInflater oppBlåser;

    public TurMaalAdapter(Context c, TurMaal[] t){
        kontekst = c;
        turMaal = t;
        oppBlåser = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return turMaal.length;
    }

    @Override
    public Object getItem(int posisjon) {
        return turMaal[posisjon];
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
            synsHolder.textViewTurMaalHøyde = (TextView) konversjonsSyn.findViewById(R.id.turMaalHøyde);
            konversjonsSyn.setTag(synsHolder);
        }
        else {
            synsHolder = (ViewHolder) konversjonsSyn.getTag();
        }

        TurMaal detteMaalet = turMaal[posisjon];
        synsHolder.textViewTurMaalNavn.setText(detteMaalet.navn);
        synsHolder.textViewTurMaalType.setText(detteMaalet.type);
        synsHolder.textViewTurMaalHøyde.setText(detteMaalet.høyde);

        return konversjonsSyn;
    }

    private static class ViewHolder{
        public TextView textViewTurMaalNavn;
        public TextView textViewTurMaalType;
        public TextView textViewTurMaalHøyde;
    }
}
