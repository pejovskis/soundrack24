package Model;

import static Model.DatabaseHelper.COLUMN_FAV_PERFORMANCE_FAV_INDEX;
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

public class FavPerformance implements Serializable {

    private long id;
    private String name;
    private Ulocation ulocation;
    private Plocation plocation;
    private Ilocation ilocation;
    private int favIndex;
    private DatabaseHelper dbHelper;

    public FavPerformance() {}

    public FavPerformance(long id, String name, Ulocation ulocation, Plocation plocation, Ilocation ilocation, int favIndex) {
        this.id = id;
        this.name = name;
        this.ulocation = ulocation;
        this.plocation = plocation;
        this.ilocation = ilocation;
        this.favIndex = favIndex;
    }

    public FavPerformance(String name, Ulocation ulocation, Plocation plocation, Ilocation ilocation, int favIndex) {
        this.name = name;
        this.ulocation = ulocation;
        this.plocation = plocation;
        this.ilocation = ilocation;
        this.favIndex = favIndex;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
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

    public int getFavIndex() {
        return favIndex;
    }

    public void setFavIndex(int favIndex) {
        this.favIndex = favIndex;
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
                COLUMN_FAV_PERFORMANCE_ILOCATION,
                COLUMN_FAV_PERFORMANCE_FAV_INDEX
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
            int favIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_FAV_INDEX));

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
            this.setFavIndex(favIndex);
        }

        // Close the cursor and database
        cursor.close();
    }

    public void loadFavPerformanceByFavIndex(int favIndex, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                COLUMN_FAV_PERFORMANCE_ID,
                COLUMN_FAV_PERFORMANCE_NAME,
                COLUMN_FAV_PERFORMANCE_ULOCATION,
                COLUMN_FAV_PERFORMANCE_PLOCATION,
                COLUMN_FAV_PERFORMANCE_ILOCATION,
                COLUMN_FAV_PERFORMANCE_FAV_INDEX
        };

        String selection = COLUMN_FAV_PERFORMANCE_FAV_INDEX + " = ?";
        String[] selectionArgs = { String.valueOf(favIndex) };

        Cursor cursor = db.query(
                TABLE_FAV_PERFORMANCES,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_NAME));
            long uLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ULOCATION));
            long pLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_PLOCATION));
            long iLocationId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_ILOCATION));
            int index = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAV_PERFORMANCE_FAV_INDEX));

            Ulocation ulocation = new Ulocation();
            Plocation plocation = new Plocation();
            Ilocation ilocation = new Ilocation();

            ulocation.loadUlocation(uLocationId, context);
            plocation.loadPlocation(pLocationId, context);
            ilocation.loadIlocation(iLocationId, context);

            this.setId(id);
            this.setName(name);
            this.setUlocation(ulocation);
            this.setPlocation(plocation);
            this.setIlocation(ilocation);
            this.setFavIndex(index);
        }

        cursor.close();
    }

    public void saveOrUpdate(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_FAV_PERFORMANCE_NAME, this.getName());
        contentValues.put(COLUMN_FAV_PERFORMANCE_ULOCATION, this.getUlocation().getId());
        contentValues.put(COLUMN_FAV_PERFORMANCE_PLOCATION, this.getPlocation().getId());
        contentValues.put(COLUMN_FAV_PERFORMANCE_ILOCATION, this.getIlocation().getId());
        contentValues.put(COLUMN_FAV_PERFORMANCE_FAV_INDEX, this.getFavIndex());

        long existingId = this.id;

        if (existingId == 0) {
            // Try to load ID from favIndex if not present
            Cursor findCursor = db.rawQuery("SELECT _id FROM " + TABLE_FAV_PERFORMANCES + " WHERE " + COLUMN_FAV_PERFORMANCE_FAV_INDEX + "=?", new String[]{String.valueOf(this.favIndex)});
            if (findCursor.moveToFirst()) {
                existingId = findCursor.getLong(0);
                this.id = existingId;
            }
            findCursor.close();
        }

        if (existingId > 0) {
            db.update(TABLE_FAV_PERFORMANCES, contentValues, "_id=?", new String[]{String.valueOf(existingId)});
        } else {
            long newId = db.insert(TABLE_FAV_PERFORMANCES, null, contentValues);
            this.id = newId;
        }
    }

    public static void deleteByFavIndex(Context context, int favIndex) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(TABLE_FAV_PERFORMANCES, COLUMN_FAV_PERFORMANCE_FAV_INDEX + " = ?", new String[]{String.valueOf(favIndex)});

        db.close();
    }

}
