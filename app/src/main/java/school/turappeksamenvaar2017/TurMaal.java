package school.turappeksamenvaar2017;

/**
 * Created by hakom_000 on 29.05.2017.
 */

public class TurMaal {
    String navn, type, beskrivelse, bildeUrl, bruker;
    int høyde;
    double latitude, longitude;

    public TurMaal(String navn, String type, String beskrivelse, String bildeUrl, String bruker, int høyde, double latitude, double longitude) {
        this.navn = navn;
        this.type = type;
        this.beskrivelse = beskrivelse;
        this.bildeUrl = bildeUrl;
        this.bruker = bruker;
        this.høyde = høyde;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
