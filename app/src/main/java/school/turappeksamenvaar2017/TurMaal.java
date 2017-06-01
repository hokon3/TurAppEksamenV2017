package school.turappeksamenvaar2017;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hakom_000 on 29.05.2017.
 */

public class TurMaal {

    static final String KOL_NAVN_NAVN = "navn";
    static final String KOL_NAVN_TYPE = "type";
    static final String KOL_NAVN_BESKRIVELSE = "beskrivelse";
    static final String KOL_NAVN_BILDE = "bilde";
    static final String KOL_NAVN_LAT = "latitude";
    static final String KOL_NAVN_LONG = "longitude";
    static final String KOL_NAVN_HOYDE = "hoyde";
    static final String KOL_NAVN_BRUKER = "bruker";

    String navn, type, beskrivelse, bildeUrl, bruker;
    int hoyde;
    double latitude, longitude;

    public TurMaal(String navn, String type, String beskrivelse, String bildeUrl, double latitude, double longitude, int hoyde, String bruker) {
        this.navn = navn;
        this.type = type;
        this.beskrivelse = beskrivelse;
        this.bildeUrl = bildeUrl;
        this.bruker = bruker;
        this.hoyde = hoyde;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public TurMaal(JSONObject jsonTM){
        navn = jsonTM.optString(KOL_NAVN_NAVN);
        type = jsonTM.optString(KOL_NAVN_TYPE);
        beskrivelse = jsonTM.optString(KOL_NAVN_BESKRIVELSE);
        bildeUrl = jsonTM.optString(KOL_NAVN_BILDE);
        latitude = jsonTM.optDouble(KOL_NAVN_LAT);
        longitude = jsonTM.optDouble(KOL_NAVN_LONG);
        hoyde = jsonTM.optInt(KOL_NAVN_HOYDE);
        bruker = jsonTM.optString(KOL_NAVN_BRUKER);
    }

    public static ArrayList<TurMaal> lagListe(String data) throws JSONException{
        ArrayList<TurMaal> liste = new ArrayList<>();
        JSONArray jsonTabell = new JSONArray(data);

        for (int i = 0; i < jsonTabell.length(); i++){
            JSONObject jsonTM = (JSONObject) jsonTabell.get(i);
            liste.add(new TurMaal(jsonTM));
        }

        return liste;
    }
}
