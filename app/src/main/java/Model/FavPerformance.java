package Model;

import static Model.DatabaseHelper.COLUMN_FAV_PERFORMANCE_ID;
import static Model.DatabaseHelper.COLUMN_FAV_PERFORMANCE_ILOCATION;
import static Model.DatabaseHelper.COLUMN_FAV_PERFORMANCE_NAME;
import static Model.DatabaseHelper.COLUMN_FAV_PERFORMANCE_PLOCATION;
import static Model.DatabaseHelper.COLUMN_FAV_PERFORMANCE_ULOCATION;
import static Model.DatabaseHelper.TABLE_FAV_PERFORMANCES;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;
import java.util.Objects;

public class FavPerformance implements Serializable {

    private long id;
    private String name;
    private Ulocation ulocation;
    private Plocation plocation;
    private Ilocation ilocation;
    private DatabaseHelper dbHelper;

    public FavPerformance() {}

    public FavPerformance(long id, String name, Ulocation ulocation, Plocation plocation, Ilocation ilocation) {
        this.id = id;
        this.name = name;
        this.ulocation = ulocation;
        this.plocation = plocation;
        this.ilocation = ilocation;
    }

    public FavPerformance(String name, Ulocation ulocation, Plocation plocation, Ilocation ilocation) {
        this.name = name;
        this.ulocation = ulocation;
        this.plocation = plocation;
        this.ilocation = ilocation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return (name != null && name.length() > 1) ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public Ulocation getUlocation() {
        return ulocation;
    }

    public void setUlocation(Ulocation ulocation) {
        this.ulocation = ulocation;
    }

    public Plocation getPlocation() {
        return plocation;
    }

    public void setPlocation(Plocation plocation) {
        this.plocation = plocation;
    }

    public Ilocation getIlocation() {
        return ilocation;
    }

    public void setIlocation(Ilocation ilocation) {
        this.ilocation = ilocation;
    }

    public void loadFavPerformance(long favPerformanceId, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define the columns to retrieve for the style
        String[] performanceColumns = {
                COLUMN_FAV_PERFORMANCE_ID,
                COLUMN_FAV_PERFORMANCE_NAME,
                COLUMN_FAV_PERFORMANCE_PLOCATION,
                COLUMN_FAV_PERFORMANCE_ULOCATION,
                COLUMN_FAV_PERFORMANCE_ILOCATION
        };

        // Define the WHERE clause for the style
        String selection = COLUMN_FAV_PERFORMANCE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(favPerformanceId) };

        // Query the database to get the style
        Cursor cursor = db.query(
                TABLE_FAV_PERFORMANCES,     // The table to query
                performanceColumns,     // The columns to return
                selection,        // The WHERE clause
                selectionArgs,    // The values for the WHERE clause
                null,             // Group by rows
                null,             // Having (filter by row groups)
                null              // The sort order
        );

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_NAME));
            long uLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ULOCATION));
            long pLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_PLOCATION));
            long iLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ILOCATION));

            // Load related objects by ID
            Ulocation ulocation = new Ulocation();
            ulocation.loadUlocation(uLocationId, context);
            Plocation plocation = new Plocation();
            plocation.loadPlocation(pLocationId, context);
            Ilocation ilocation = new Ilocation();
            ilocation.loadIlocation(iLocationId, context);

            this.setId(id);
            this.setName(name);
            this.setUlocation(ulocation);
            this.setPlocation(plocation);
            this.setIlocation(ilocation);
        }

        // Close the cursor and database
        cursor.close();
    }

    public void update(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FAV_PERFORMANCE_NAME, this.name != null ? this.name : "");

        contentValues.put(COLUMN_FAV_PERFORMANCE_ULOCATION,
                this.ulocation != null ? this.ulocation.getId() : null);
        contentValues.put(COLUMN_FAV_PERFORMANCE_PLOCATION,
                this.plocation != null ? this.plocation.getId() : null);
        contentValues.put(COLUMN_FAV_PERFORMANCE_ILOCATION,
                this.ilocation != null ? this.ilocation.getId() : null);

        db.update(TABLE_FAV_PERFORMANCES, contentValues, COLUMN_FAV_PERFORMANCE_ID + " = ?", new String[]{String.valueOf(this.id)});
    }

    public void update(Context context, int id) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FAV_PERFORMANCE_NAME, "");
        contentValues.put(COLUMN_FAV_PERFORMANCE_ULOCATION, "");
        contentValues.put(COLUMN_FAV_PERFORMANCE_PLOCATION, "");
        contentValues.put(COLUMN_FAV_PERFORMANCE_ILOCATION, "");

        db.update(TABLE_FAV_PERFORMANCES, contentValues, COLUMN_FAV_PERFORMANCE_ID + " = ?", new String[]{String.valueOf(this.id)});
    }

    public void remove(Context context, int id) {
        this.update(context, id);
    }

    public boolean isFpEmpty() {
        return (name == null || name.isEmpty()) &&
                (ulocation == null || ulocation.getName() == null || ulocation.getName().isEmpty()) &&
                (plocation == null || plocation.getName() == null || plocation.getName().isEmpty()) &&
                (ilocation == null || ilocation.getName() == null || ilocation.getName().isEmpty());
    }

}
