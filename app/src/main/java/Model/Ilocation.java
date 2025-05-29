package Model;

import static Model.DatabaseHelper.COLUMN_ILOCATION_ID;
import static Model.DatabaseHelper.COLUMN_ILOCATION_NAME;
import static Model.DatabaseHelper.TABLE_ILOCATION;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;

public class Ilocation implements Serializable {

    private long id;
    private String name;
    private DatabaseHelper dbHelper;

    public Ilocation() {}

    public Ilocation(long id, String name) {
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

    public void loadIlocation(long iLocationId, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                COLUMN_ILOCATION_ID,
                COLUMN_ILOCATION_NAME
        };
        String selection = COLUMN_ILOCATION_ID + " = ?";
        String[] selectionArgs = { String.valueOf(iLocationId) };

        Cursor cursor = db.query(
                TABLE_ILOCATION,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ILOCATION_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ILOCATION_NAME));
            this.setId(id);
            this.setName(name);
        }

        cursor.close();
    }

    public void loadIlocationByName(String name, Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {COLUMN_ILOCATION_ID, COLUMN_ILOCATION_NAME};
        String selection = COLUMN_ILOCATION_NAME + " = ?";
        String[] selectionArgs = {name};

        Cursor cursor = db.query(TABLE_ILOCATION, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ILOCATION_ID));
            this.setId(id);
            this.setName(name);
        }

        cursor.close();
    }
}
