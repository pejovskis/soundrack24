package Model;

import static Model.DatabaseHelper.COLUMN_ULOCATION_ID;
import static Model.DatabaseHelper.COLUMN_ULOCATION_NAME;
import static Model.DatabaseHelper.TABLE_ULOCATION;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;

public class Ulocation implements Serializable {

    private long id;
    private String name;
    private DatabaseHelper dbHelper;

    public Ulocation() {}

    public Ulocation(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return (name != null && name.length() > 1) ? name.substring(1) : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void loadUlocation(long uLocationId, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                COLUMN_ULOCATION_ID,
                COLUMN_ULOCATION_NAME
        };
        String selection = COLUMN_ULOCATION_ID + " = ?";
        String[] selectionArgs = { String.valueOf(uLocationId) };

        Cursor cursor = db.query(
                TABLE_ULOCATION,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ULOCATION_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ULOCATION_NAME));
            this.setId(id);
            this.setName(name);
        }

        cursor.close();
    }

    public void loadUlocationByName(String name, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {COLUMN_ULOCATION_ID, COLUMN_ULOCATION_NAME};
        String selection = COLUMN_ULOCATION_NAME + " = ?";
        String[] selectionArgs = {name};

        Cursor cursor = db.query(TABLE_ULOCATION, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ULOCATION_ID));
            this.setId(id);
            this.setName(name);
        }

        cursor.close();
    }
}
