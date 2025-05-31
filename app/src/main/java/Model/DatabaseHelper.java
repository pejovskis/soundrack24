package Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 189;

    // Singleton
    private static DatabaseHelper instance;
    private static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(4);

    // Table 1: ULOCATION
    public static final String TABLE_ULOCATION = "ulocation";
    public static final String COLUMN_ULOCATION_ID = "_id";
    public static final String COLUMN_ULOCATION_NAME = "name";

    // Table 2: PLOCATION
    public static final String TABLE_PLOCATION = "plocation";
    public static final String COLUMN_PLOCATION_ID = "_id";
    public static final String COLUMN_PLOCATION_NAME = "name";

    // TABLE 33: ILOCATION
    public static final String TABLE_ILOCATION = "ilocation";
    public static final String COLUMN_ILOCATION_ID = "_id";
    public static final String COLUMN_ILOCATION_NAME = "name";

    // Table 4: Fav-Performances
    public static final String TABLE_FAV_PERFORMANCES = "fav_performances";
    public static final String COLUMN_FAV_PERFORMANCE_ID = "_id";
    public static final String COLUMN_FAV_PERFORMANCE_NAME = "name";
    public static final String COLUMN_FAV_PERFORMANCE_ULOCATION = "ulocation_id";
    public static final String COLUMN_FAV_PERFORMANCE_PLOCATION = "plocation_id";
    public static final String COLUMN_FAV_PERFORMANCE_ILOCATION = "ilocation_id";

    // Table 21: Fav-Styles
    public static final String TABLE_FAV_STYLES = "fav_styles";
    public static final String COLUMN_FAV_STYLE_ID = "_id";
    public static final String COLUMN_FAV_STYLE_NAME = "name";
    public static final String COLUMN_FAV_STYLE_ULOCATION = "ulocation_id";
    public static final String COLUMN_FAV_STYLE_PLOCATION = "plocation_id";
    public static final String COLUMN_FAV_STYLE_ILOCATION = "ilocation_id";

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Reset the db TODO
        db.execSQL(getCreateULocationTable());
        db.execSQL(getCreatePLocationTable());
        db.execSQL(getCreateILocationTable());
        db.execSQL(getCreateFavPerformancesTable());
        db.execSQL(getCreateFavStylesTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ULOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ILOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAV_PERFORMANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAV_STYLES);
        onCreate(db);
    }

    public void populateBaseTables() {
        populateUlocationTable();
        populatePlocationTable();
        populateIlocationTable();
        populateFavPerformancesTable();
        populateFavStylesTable();
    }

    private String getCreateULocationTable() {
        return "CREATE TABLE " + TABLE_ULOCATION + " (" +
                COLUMN_ULOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ULOCATION_NAME + " TEXT NOT NULL);";
    }

    private String getCreatePLocationTable() {
        return "CREATE TABLE " + TABLE_PLOCATION + " (" +
                COLUMN_PLOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PLOCATION_NAME + " TEXT NOT NULL);";
    }

    private String getCreateILocationTable() {
        return "CREATE TABLE " + TABLE_ILOCATION + " (" +
                COLUMN_ILOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ILOCATION_NAME + " TEXT NOT NULL);";
    }

    private String getCreateFavPerformancesTable() {
        return "CREATE TABLE " + TABLE_FAV_PERFORMANCES + " (" +
                COLUMN_FAV_PERFORMANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FAV_PERFORMANCE_NAME + " TEXT, " +
                COLUMN_FAV_PERFORMANCE_ULOCATION + " INTEGER, " +
                COLUMN_FAV_PERFORMANCE_PLOCATION + " INTEGER, " +
                COLUMN_FAV_PERFORMANCE_ILOCATION + " INTEGER " + ");";
    }

    private String getCreateFavStylesTable() {
        return "CREATE TABLE " + TABLE_FAV_STYLES + " (" +
                COLUMN_FAV_STYLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FAV_STYLE_NAME + " TEXT, " +
                COLUMN_FAV_STYLE_ULOCATION + " INTEGER, " +
                COLUMN_FAV_STYLE_PLOCATION + " INTEGER, " +
                COLUMN_FAV_STYLE_ILOCATION + " INTEGER " + ");";
    }

    public void populateUlocationTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String value = "U";
        int uNumber = 99;

        for (int i = 1; i <= uNumber; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_ULOCATION_NAME, value + i);
            db.insert(TABLE_ULOCATION, null, contentValues);
        }
    }

    public void populatePlocationTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String value = "P";
        int pNumber = 99;

        for (int i = 1; i <= pNumber; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_PLOCATION_NAME, value + i);
            db.insert(TABLE_PLOCATION, null, contentValues);
        }
    }

    public void populateIlocationTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String value = "I";
        int iNumber = 99;

        for (int i = 1; i <= iNumber; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_ILOCATION_NAME, value + i);
            db.insert(TABLE_ILOCATION, null, contentValues);
        }
    }

    public void populateFavPerformancesTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        for (int i = 1; i <= 24; i++) {
            ContentValues values = new ContentValues();
            values.put("_id", i);
            db.insert(TABLE_FAV_PERFORMANCES, null, values);
        }
    }

    public void populateFavStylesTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 1; i <= 24; i++) {
            ContentValues values = new ContentValues();
            values.put("_id", i);
            db.insert(TABLE_FAV_STYLES, null, values);
        }
    }

    public List<FavPerformance> getAllFavPerformances(Context context) {
        List<FavPerformance> favPerformances = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_FAV_PERFORMANCE_ID,
                COLUMN_FAV_PERFORMANCE_NAME,
                COLUMN_FAV_PERFORMANCE_ULOCATION,
                COLUMN_FAV_PERFORMANCE_PLOCATION,
                COLUMN_FAV_PERFORMANCE_ILOCATION
        };

        Cursor cursor = db.query(
                TABLE_FAV_PERFORMANCES, // table
                columns,                // columns
                null,                   // where
                null,                   // whereArgs
                null,                   // groupBy
                null,                   // having
                null                    // orderBy
        );

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_NAME));
                long uLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ULOCATION));
                long pLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_PLOCATION));
                long iLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ILOCATION));

                Ulocation ulocation = new Ulocation();
                ulocation.loadUlocation(uLocationId, context);
                Plocation plocation = new Plocation();
                plocation.loadPlocation(pLocationId, context);
                Ilocation ilocation = new Ilocation();
                ilocation.loadIlocation(iLocationId, context);

                FavPerformance fav = new FavPerformance(id, name, ulocation, plocation, ilocation);
                favPerformances.add(fav);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return favPerformances;
    }

    public List<FavStyle> getAllFavStyles(Context context) {
        List<FavStyle> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_FAV_STYLE_ID,
                COLUMN_FAV_STYLE_NAME,
                COLUMN_FAV_STYLE_ULOCATION,
                COLUMN_FAV_STYLE_PLOCATION,
                COLUMN_FAV_STYLE_ILOCATION
        };

        Cursor cursor = db.query(TABLE_FAV_STYLES, columns, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_NAME));
                long uId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_ULOCATION));
                long pId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_PLOCATION));
                long iId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_STYLE_ILOCATION));

                Ulocation u = new Ulocation(); u.loadUlocation(uId, context);
                Plocation p = new Plocation(); p.loadPlocation(pId, context);
                Ilocation i = new Ilocation(); i.loadIlocation(iId, context);

                list.add(new FavStyle(id, name, u, p, i));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

}
