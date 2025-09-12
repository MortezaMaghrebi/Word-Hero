package mortezamaghrebi.com.wordhero;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

public class DatabaseUpdater {

    public static void recreateDatabase(Context context) {
        try {
            DBAdapter dbAdapter = DBAdapter.getInstance(context);
            dbAdapter.close();

            String dbPath = context.getDatabasePath(DBAdapter.DATABASE_NAME).getPath();
            File dbFile = new File(dbPath);

            if (dbFile.exists()) {
                boolean deleted = dbFile.delete();
                if (!deleted) {
                    Toast.makeText(context, "Failed to delete database", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File[] dbFiles = new File(dbFile.getParent()).listFiles((dir, name) ->
                    name.startsWith(DBAdapter.DATABASE_NAME));
            if (dbFiles != null) {
                for (File file : dbFiles) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        Log.w("DatabaseUpdater", "Failed to delete file: " + file.getName());
                    }
                }
            }

            SQLiteDatabase newDb = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
            newDb.execSQL(DBAdapter.DATABASE_CREATE_SQL);
            newDb.execSQL(DBAdapter.DATABASE_CREATE_SQL_IMAGE);
            newDb.close();

            Toast.makeText(context, "New database created with updated structure", Toast.LENGTH_SHORT).show();

            dbAdapter.createDatabaseIfNeeded();
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("DatabaseUpdater", "Error recreating database: ", e);
        }
    }
}