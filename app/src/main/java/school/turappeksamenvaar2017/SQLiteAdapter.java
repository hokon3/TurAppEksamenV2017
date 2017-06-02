package school.turappeksamenvaar2017;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hakom_000 on 02.06.2017.
 */

public class SQLiteAdapter {
    private static final String DATABASE_NAVN = "db142840.db";
    private static final int DATABASE_VERSJON = 1;
    private static final String TABELL_TURMAAL = "turmaal", TABELL_NYE = "nye";
    public static final String NAVN = "navn";
    public static final String TYPE = "type";
    public static final String BESKRIVELSE = "beskrivelse";
    public static final String BILDE = "bilde";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String HOYDE = "hoyde";
    public static final String BRUKER = "bruker";
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

    public SQLiteAdapter(Context kontekst){
        this.kontekst = kontekst;
    }

    public SQLiteAdapter aapne() throws SQLException{
        databaseHjelper = new DatabaseHjelper(kontekst);
        minDB = databaseHjelper.getWritableDatabase();
        return this;
    }

    public void steng(){
        if (minDB != null){
            minDB.close();
        }
    }

    public void oppgrader() throws SQLException{
        databaseHjelper = new DatabaseHjelper(kontekst);
        minDB = databaseHjelper.getWritableDatabase();
        databaseHjelper.onUpgrade(minDB,DATABASE_VERSJON,DATABASE_VERSJON+1);
    }

    public long settInnTurMaal(ContentValues verdier){
        Long resultat = minDB.insertWithOnConflict(TABELL_TURMAAL, null, verdier, SQLiteDatabase.CONFLICT_IGNORE);
        return resultat;
    }

    public long settInnNye(ContentValues verdier){
        Long resultat = minDB.insertWithOnConflict(TABELL_NYE, null, verdier, SQLiteDatabase.CONFLICT_IGNORE);
        return resultat;
    }

    public boolean oppdaterTurMaal(String navn, ContentValues nyeVerdier){
        String[] valgArgument = {navn};
        boolean ok = minDB.update(TABELL_TURMAAL,nyeVerdier, NAVN +"=?", valgArgument) > 0;
        return ok;
    }

    public boolean slettTurmaal(String navn){
        String[] valgArgument = {navn};
        boolean ok = minDB.delete(TABELL_TURMAAL, NAVN +"=?", valgArgument) > 0;
        return ok;
    }

    public boolean slettNye(String navn){
        String[] valgArgument = {navn};
        boolean ok = minDB.delete(TABELL_NYE, NAVN +"=?", valgArgument) > 0;
        return ok;
    }

    public Cursor hentTurmaal(String sorterEtter) {
        return minDB.query(TABELL_TURMAAL, TURMAAL_FELT, null, null, null, null, sorterEtter +" DESC");
    }

    public Cursor hentNye() {
        return minDB.query(TABELL_NYE, NYE_FELT, null, null, null, null, NAVN +" DESC");
    }

    public Cursor hentNyeTurmaal(String[] argumenter) {
        return minDB.query(TABELL_TURMAAL, TURMAAL_FELT, NAVN+"=?", argumenter, null, null, NAVN +" DESC");
    }

    private static class DatabaseHjelper extends SQLiteOpenHelper{

        DatabaseHjelper(Context kontekst){
            super(kontekst, DATABASE_NAVN, null, DATABASE_VERSJON);
        }

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
