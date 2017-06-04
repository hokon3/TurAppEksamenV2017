package school.turappeksamenvaar2017;

import android.database.Cursor;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Klasse for Turmål
 */

public class TurMaal implements Comparable<TurMaal>{

    //Konstranter som tilsvarer kolonne navn i den sentrale databasen
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
    float distanse;

    public TurMaal(){

    }

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

    /**
     * Metoden henter ut et turmål fra et Cursor objekt
     * @param peker Cursor objekt
     * @return Turmål
     */
    public static TurMaal hentTurMaalFraPeker(Cursor peker){
        TurMaal turMaal = new TurMaal();
        turMaal.navn = peker.getString(peker.getColumnIndex(SQLiteAdapter.NAVN));
        turMaal.type = peker.getString(peker.getColumnIndex(SQLiteAdapter.TYPE));
        turMaal.beskrivelse = peker.getString(peker.getColumnIndex(SQLiteAdapter.BESKRIVELSE));
        turMaal.bildeUrl = peker.getString(peker.getColumnIndex(SQLiteAdapter.BILDE));
        turMaal.latitude = peker.getDouble(peker.getColumnIndex(SQLiteAdapter.LATITUDE));
        turMaal.longitude = peker.getDouble(peker.getColumnIndex(SQLiteAdapter.LONGITUDE));
        turMaal.hoyde = peker.getInt(peker.getColumnIndex(SQLiteAdapter.HOYDE));
        turMaal.bruker = peker.getString(peker.getColumnIndex(SQLiteAdapter.BRUKER));
        return turMaal;
    }

    /**
     * Metode for å generere en liste over turmål utifra JSON data
     * @param data JSON data
     * @return Liste over turmål
     * @throws JSONException
     */
    public static ArrayList<TurMaal> lagListe(String data) throws JSONException{
        ArrayList<TurMaal> liste = new ArrayList<>();
        JSONArray jsonTabell = new JSONArray(data);

        for (int i = 0; i < jsonTabell.length(); i++){
            JSONObject jsonTM = (JSONObject) jsonTabell.get(i);
            liste.add(new TurMaal(jsonTM));
        }

        return liste;
    }

    /**
     * Implementasjon av compareTo, brukes for sortering på distanse
     * @param t Turmålet som skal bli samenlignet med
     * @return posetivt hvis t er mindre, 0 hvis t er likt og negativt hvis t er større
     */
    @Override
    public int compareTo(TurMaal t) {
        return (int)(distanse-t.distanse);
    }
}
