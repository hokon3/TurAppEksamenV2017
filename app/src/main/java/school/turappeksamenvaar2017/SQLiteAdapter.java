package school.turappeksamenvaar2017;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Klasse for å håndtere SQLite databasen
 */

public class SQLiteAdapter {
    private static final String DATABASE_NAVN = "db142840.db"; //Navnet til databasen lokalt
    private static final int DATABASE_VERSJON = 1; //Versjons nummer
    private static final String TABELL_TURMAAL = "turmaal", TABELL_NYE = "nye"; //Tabelll navn
    //Navn på kolonner
    public static final String NAVN = "navn";
    public static final String TYPE = "type";
    public static final String BESKRIVELSE = "beskrivelse";
    public static final String BILDE = "bilde";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String HOYDE = "hoyde";
    public static final String BRUKER = "bruker";
    //Tabeller over kolonner
    public static final String[] TURMAAL_FELT = new String[]{
            NAVN,
            TYPE,
            BESKRIVELSE,
            BILDE,
            LATITUDE,
            LONGITUDE,
            HOYDE,
            BRUKER
    };
    public static final String[] NYE_FELT = new String[]{
            NAVN
    };
    //SQL for å lage tabellene
    private static final String LAG_TURMAAL_TABELL = "create table " + TABELL_TURMAAL + "("
            + NAVN + " text not null UNIQUE,"
            + TYPE + " text,"
            + BESKRIVELSE + " text,"
            + BILDE + " text,"
            + LATITUDE + " REAL,"
            + LONGITUDE + " REAL,"
            + HOYDE + " INT,"
            + BRUKER + " text"
            +");";
    private static final String LAG_NYE_TABELL = "create table " + TABELL_NYE + "("
            + NAVN + " text not null UNIQUE);";

    private Context kontekst;
    private DatabaseHjelper databaseHjelper;
    private SQLiteDatabase minDB;

    /**
     * Konstruktør
     * @param kontekst konteksten til Activity
     */
    public SQLiteAdapter(Context kontekst){
        this.kontekst = kontekst;
    }

    /**
     * Metoden åpner for skriving i databasen
     * @return Denne klassen
     * @throws SQLException
     */
    public SQLiteAdapter aapne() throws SQLException{
        databaseHjelper = new DatabaseHjelper(kontekst);
        minDB = databaseHjelper.getWritableDatabase();
        return this;
    }

    /**
     * Metoden stenger kobling til databasen
     */
    public void steng(){
        if (minDB != null){
            minDB.close();
        }
    }

    /**
     * Metode for å sette inn et nytt turmål i databasen
     * @param verdier Date for å lage et turmål
     * @return resultat
     */
    public long settInnTurMaal(ContentValues verdier){
        Long resultat = minDB.insertWithOnConflict(TABELL_TURMAAL, null, verdier, SQLiteDatabase.CONFLICT_IGNORE);
        return resultat;
    }

    /**
     * Metode for å registrere nye turmål som ikke er lagret i den sentrale databasen
     * @param verdier Data med navn på nytt turmål
     * @return resultat
     */
    public long settInnNye(ContentValues verdier){
        Long resultat = minDB.insertWithOnConflict(TABELL_NYE, null, verdier, SQLiteDatabase.CONFLICT_IGNORE);
        return resultat;
    }

    /**
     * Metode for å slette registrering av nytt turmål som ikke finnes i sentral database
     * @param valgArgument Tabell med navn på turmål
     * @return Sant hvis alt gikk bra, Ellers usant.
     */
    public boolean slettNye(String[] valgArgument){
        boolean ok = minDB.delete(TABELL_NYE, NAVN +"=?", valgArgument) > 0;
        return ok;
    }

    /**
     * Metode for å hente ut turmål fra Databasen
     * @param sorterEtter Hva turmålene skal bli sortert på
     * @return Cursor
     */
    public Cursor hentTurmaal(String sorterEtter) {
        return minDB.query(TABELL_TURMAAL, TURMAAL_FELT, null, null, null, null, sorterEtter +" DESC");
    }

    /**
     * Henter registreringer av nye turmål fra Nye tabell
     * @return Cursor
     */
    public Cursor hentNye() {
        return minDB.query(TABELL_NYE, NYE_FELT, null, null, null, null, NAVN +" DESC");
    }

    /**
     * Henter nye turmål fra turmål tabell basert på navn fra argumenter
     * @param argumenter Tabell med navn på nye turmål
     * @return
     */
    public Cursor hentNyeTurmaal(String[] argumenter) {
        return minDB.query(TABELL_TURMAAL, TURMAAL_FELT, NAVN+"=?", argumenter, null, null, NAVN +" DESC");
    }

    /**
     * Intern klasse for å hjelpe til med database tilkobling
     */
    private static class DatabaseHjelper extends SQLiteOpenHelper{

        DatabaseHjelper(Context kontekst){
            super(kontekst, DATABASE_NAVN, null, DATABASE_VERSJON);
        }

        //Opretter databsen hvis den ikke finnes
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(LAG_TURMAAL_TABELL);
            db.execSQL(LAG_NYE_TABELL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABELL_TURMAAL);
            db.execSQL("DROP TABLE IF EXISTS " + TABELL_NYE);
            onCreate(db);
        }
    }

}
