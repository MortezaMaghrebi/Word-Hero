package mortezamaghrebi.com.wordhero;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

import androidx.appcompat.app.AlertDialog;

public class Controller {
    final String MY_PREFS_NAME = "PREFS_1100";
    Context context;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    DBAdapter myDB;
    int[] targets = {4,8,10,12,14,16,18,20,22,24,25,26,27,28,29,30,31,32,33,34,35}; //if number of correct answers arrive targets*20 level increases; each level releases 20 words;
    int[] heartOfBox={1,1,4,8,14,24,38,56,80,110,146,189,240,299,360};
    String encode = "پ,A~ر,B~ ,C~خ,D~و,E~،,F~س,G~ی,H~ن,I~ا,J~ذ,K~ب,L~ه,M~ک,N~د,O~ف,P~م,Q~ش,R~ج,S~ت,T~غ,U~ق,V~ز,W~آ,X~ل,Y~ژ,Z~گ,s~ض,b~ح,t~ط,d~چ,r~ث,f~ع,g~ص,h~ظ,i~ئ,j~-,k~p,l~e,m~n,n~a,o~c,p~ء,q";
    public wordItem[] wordItems;
    public ImageItem[] imageItems;
    final String url = "http://kingsofleitner.ir/words1100/webservice.php";
    public final String infourl = "https://github.com/MortezaMaghrebi/Word-Hero/blob/main/Info.md";
    final int DecreasExirEachMin=60; //use functions please: getDecreaseExirTime()
    final int HeartsMaximum=12;
    final int HeartIncreasTime=540; //seconds
    final int InitialExir=60;
    final int NumberOfQuestions=10,RewardBad=3,RewardSoSo=5,RewardExcelent=8;
    ProgressDialog progressDialog;
    ProgressDialog progressDialog2;
    public int Version=3;

    public Controller(Context context,Boolean getwords) {
        this.context = context;
        editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        openDB(context);
        if(getwords) {
            Cursor cursor = myDB.getAllWords();
            getWordItems(cursor);
            cursor.close();
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Getting words online...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog2 = new ProgressDialog(context);
        progressDialog2.setMessage("Getting Saved Progress...");
        progressDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    }

    public void UpdateWordList()
    {
        Cursor cursor = myDB.getAllWords();
        getWordItems(cursor);
        cursor.close();
    }
    private static final String TAG = "DatabaseBackup";
    public boolean backupDatabaseToDocuments(Context context) {
        try {
            File dbFile = context.getDatabasePath("DB1100.db");
            if (!dbFile.exists()) {
                Log.e(TAG, "Database file not found!");
                return false;
            }

            File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!docsDir.exists()) docsDir.mkdirs();

            File backupFile = new File(docsDir, "DB1100_backup.db");

            copyFile(dbFile, backupFile);
            Toast.makeText(context,"Backup saved to Documents folder.",Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Database backup saved in Documents: " + backupFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error backing up database", e);
            return false;
        }
    }
    public boolean deleteAllImages(Context context) {
        try {
            myDB.deleteAllImages();
            return true;
        }catch (Exception e)
        {
            return  false;
        }
    }

    public void backupImagesToDocumentsWithProgress(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Backing Up");
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        getAllImages(); // Make sure this is thread-safe
        progressDialog.setMax(imageItems.length); // Set max progress
        progressDialog.show();

        new Thread(() -> {
            boolean success = true;
            try {
                File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!docsDir.exists()) docsDir.mkdirs();

                File backupFile = new File(docsDir, "Vocab.txt");
                FileOutputStream outChannel = new FileOutputStream(backupFile);
                OutputStreamWriter writer = new OutputStreamWriter(outChannel);

                getAllImages(); // Make sure this is thread-safe

                for (int i = 0; i < imageItems.length; i++) {
                    String len1 = "" + imageItems[i].word.length();
                    while (len1.length() < 2) len1 = "0" + len1;

                    Bitmap bit = base64ToBitmap(imageItems[i].base64image);
                    String base64;
                    if (bit.getWidth() > 220 || bit.getHeight() > 220) {
                        Bitmap resizedbit = resizeImageToFitDatabase(bit);
                        base64 = bitmapToBase64(resizedbit);
                    } else base64 = bitmapToBase64(bit);

                    String len2 = "" + base64.length();
                    while (len2.length() < 9) len2 = "0" + len2;

                    String line = len1 + "+" + len2 + "~" + imageItems[i].word + "," + base64 + "\n";
                    writer.write(line);

                    int finalI = i;
                    // Update progress on UI thread
                    ((Activity) context).runOnUiThread(() -> progressDialog.setProgress(finalI + 1));
                }

                writer.close();

            } catch (Exception e) {
                Log.e(TAG, "Error backing up database", e);
                success = false;
                String errMsg = e.getMessage();
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error backing up database: " + errMsg, Toast.LENGTH_LONG).show());
            }

            boolean finalSuccess = success;
            ((Activity) context).runOnUiThread(() -> {
                progressDialog.dismiss();
                if (finalSuccess) {
                    Toast.makeText(context, "Backup saved to Documents folder.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    public void backupProgressToDocumentsWithProgress(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Backing Up Progress");
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        getAllImages(); // Make sure this is thread-safe
        progressDialog.setMax(imageItems.length); // Set max progress
        progressDialog.show();

        new Thread(() -> {
            boolean success = true;
            try {
                File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!docsDir.exists()) docsDir.mkdirs();

                File backupFile = new File(docsDir, "Progress.txt");
                FileOutputStream outChannel = new FileOutputStream(backupFile);
                OutputStreamWriter writer = new OutputStreamWriter(outChannel);

                getAllImages(); // Make sure this is thread-safe

                for (int i = 0; i < wordItems.length; i++) {
                    wordItem w = wordItems[i];
                    String line= w.word+"#"+w.review+"#"+w.lastheart+"#"+w.started+"#"+w.finished+"\n";
                    writer.write(line);
                    int finalI = i;
                    ((Activity) context).runOnUiThread(() -> progressDialog.setProgress(finalI + 1));
                }

                writer.close();

            } catch (Exception e) {
                Log.e(TAG, "Error backing up database", e);
                success = false;
                String errMsg = e.getMessage();
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error backing up database: " + errMsg, Toast.LENGTH_LONG).show());
            }

            boolean finalSuccess = success;
            ((Activity) context).runOnUiThread(() -> {
                progressDialog.dismiss();
                if (finalSuccess) {
                    Toast.makeText(context, "Backup saved to Documents folder.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    public boolean backupImagesToDocuments(Context context) {
        try {
            File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!docsDir.exists()) docsDir.mkdirs();

            File backupFile = new File(docsDir, "Vocab.txt");
            FileOutputStream outChannel = new FileOutputStream(backupFile);
            OutputStreamWriter writer = new OutputStreamWriter(outChannel);

            getAllImages();

            for (int i = 0; i < imageItems.length; i++) {
                String len1=""+imageItems[i].word.length();
                while(len1.length()<2)len1="0"+len1;
                Bitmap bit = base64ToBitmap(imageItems[i].base64image);
                String base64;
                if(bit.getWidth()>220 || bit.getHeight()>220) {
                    Bitmap resizedbit = resizeImageToFitDatabase(bit);
                    base64 = bitmapToBase64(resizedbit);
                }else base64=bitmapToBase64(bit);
                String len2=""+base64.length();
                while(len2.length()<9)len2="0"+len2;
                String line =len1+"+"+len2+"~"+ imageItems[i].word + "," + base64 + "\n";
                writer.write(line);
            }

            writer.close();
            Toast.makeText(context,"Backup saved to Documents folder.",Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error backing up database", e);
            Toast.makeText(context,"Error backing up database: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    public void importImagesFromUri(Uri uri) {
        try {
            File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File backupFile = new File(docsDir, "TempImages.txt");


            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(backupFile)) {

                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }


            LoadImagesFromFile(backupFile);

        } catch (Exception e) {
            Toast.makeText(context,"Error restoring images from backup: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error restoring images from backup", e);
        }
    }
    public boolean restoreImagesFromBackupDocuments(Context context) {
        try {
            int newwords=0,duplicatewords=0;
            File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File backupFile = new File(docsDir, "Vocab.txt");
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file does not exist");
                return false;
            }
            LoadImagesFromFile(backupFile);
            return true;

        } catch (Exception e) {
            Toast.makeText(context,"Error restoring images from backup: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error restoring images from backup", e);
            return false;
        }
    }

    public boolean restoreProgressFromBackupDocuments(Context context) {
        try {
            int newwords=0,duplicatewords=0;
            File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File backupFile = new File(docsDir, "Progress.txt");
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file does not exist");
                return false;
            }
            LoadProgressFromFile(backupFile);
            return true;

        } catch (Exception e) {
            Toast.makeText(context,"Error restoring images from backup: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error restoring images from backup", e);
            return false;
        }
    }
    int newwords = 0, duplicatewords = 0;

    public void LoadImagesFromFile(File backupFile) {
        AlertDialog progressDialog;
        TextView progressText;

        // Create and show the progress dialog on the UI thread
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Restoring images...");
        progressText = new TextView(context);
        progressText.setPadding(32, 48, 32, 48);
        progressText.setText("Starting...");
        builder.setView(progressText);
        builder.setCancelable(false);
        progressDialog = builder.create();
        progressDialog.show();

        // Run the file loading process in a separate thread
        new Thread(() -> {
            int total = 0;

            try {
                RandomAccessFile raf = new RandomAccessFile(backupFile, "r");

                while (raf.getFilePointer() < raf.length()) {

                    byte[] lenWordBytes = new byte[2];
                    raf.readFully(lenWordBytes);
                    int lenWord = Integer.parseInt(new String(lenWordBytes, StandardCharsets.UTF_8));

                    byte separator = raf.readByte();
                    if (separator != (byte) '+') throw new IOException("Expected '+' not found.");

                    byte[] lenImageBytes = new byte[9];
                    raf.readFully(lenImageBytes);
                    int lenImage = Integer.parseInt(new String(lenImageBytes, StandardCharsets.UTF_8));

                    separator = raf.readByte();
                    if (separator != (byte) '~') throw new IOException("Expected '~' not found.");

                    byte[] wordBytes = new byte[lenWord];
                    raf.readFully(wordBytes);
                    String word = new String(wordBytes, StandardCharsets.UTF_8);

                    separator = raf.readByte();
                    if (separator != (byte) ',') throw new IOException("Expected ',' not found.");

                    if (hasWordImage(word)) {
                        // Skip base64 image bytes and the newline character
                        raf.seek(raf.getFilePointer() + lenImage + 1);
                        duplicatewords++;
                    } else {
                        byte[] base64Bytes = new byte[lenImage];
                        raf.readFully(base64Bytes);

                        separator = raf.readByte();
                        if (separator != (byte) '\n') throw new IOException("Expected '\\n' not found.");

                        String base64Image = new String(base64Bytes, StandardCharsets.UTF_8);
                        Bitmap bit= base64ToBitmap(base64Image);
                        Bitmap resizedbit=resizeImageToFitDatabase(bit);
                        String base64=bitmapToBase64(resizedbit);
                        setWordImageFromBase64(word, base64);
                        newwords++;
                    }

                    total++;

                    // Update the progress dialog text on the UI thread
                    int finalNew = newwords;
                    int finalDup = duplicatewords;
                    int finalTotal = total;
                    ((Activity) context).runOnUiThread(() -> {
                        progressText.setText(
                                "Total processed: " + finalTotal +
                                        "\nNew images: " + finalNew +
                                        "\nDuplicate images: " + finalDup);
                    });
                }

                raf.close();

                // Show final result dialog on the UI thread
                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Restore Images");
                    alertDialog.setMessage("Backup loaded successfully:\nNew Images: " + newwords + "\nDuplicate Images: " + duplicatewords);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error restoring images from backup", e);
                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Error restoring images: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public void LoadProgressFromFile(File backupFile) {
        if (backupFile == null || !backupFile.exists()) {
            Log.e("LoadProgressFromFile", "File is null or does not exist");
            return;
        }

        StringBuilder textBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(backupFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }

        } catch (IOException e) {
            Log.e("LoadProgressFromFile", "Error reading file: " + e.getMessage(), e);
            return;
        }

        String fileContent = textBuilder.toString();
        String[] lines= fileContent.split("\n");
        int numsuccess=0;
        for(int i=0;i<lines.length;i++)
        {
            String[] items=lines[i].split("#");
            //String line= w.word+"#"+w.review+"#"+w.lastheart+"#"+w.started+"#"+w.finished+"\n";

            if(items.length==5)
            {
                String word=items[0];
                String review=items[1];
                String lastheart=items[2];
                String started=items[3];
                String finished=items[4];
                myDB.UpdateWordProgress(word,review,Integer.parseInt(lastheart),Integer.parseInt(started),Integer.parseInt(finished));
                numsuccess++;
            }
        }
        new AlertDialog.Builder(context)
                .setTitle("نتیجه آپدیت")
                .setMessage(numsuccess + " از " + lines.length + " مورد با موفقیت آپدیت شد")
                .setPositiveButton("باشه", null)
                .show();

        Log.d("LoadProgressFromFile", "File content:\n" + fileContent);
    }
    public void LoadImagesFromBytes(byte[] backupData) {
        AlertDialog progressDialog;
        TextView progressText;

        // Create and show the progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Restoring images...");
        progressText = new TextView(context);
        progressText.setPadding(32, 48, 32, 48);
        progressText.setText("Starting...");
        builder.setView(progressText);
        builder.setCancelable(false);
        progressDialog = builder.create();
        progressDialog.show();

        // Run in background thread
        new Thread(() -> {
            int total = 0;

            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(backupData);
                DataInputStream dis = new DataInputStream(bais);

                while (dis.available() > 0) {

                    // Read word length
                    byte[] lenWordBytes = new byte[2];
                    dis.readFully(lenWordBytes);
                    int lenWord = Integer.parseInt(new String(lenWordBytes, StandardCharsets.UTF_8));

                    // Check separator '+'
                    byte separator = dis.readByte();
                    if (separator != (byte) '+') throw new IOException("Expected '+' not found.");

                    // Read image length
                    byte[] lenImageBytes = new byte[9];
                    dis.readFully(lenImageBytes);
                    int lenImage = Integer.parseInt(new String(lenImageBytes, StandardCharsets.UTF_8));

                    // Check separator '~'
                    separator = dis.readByte();
                    if (separator != (byte) '~') throw new IOException("Expected '~' not found.");

                    // Read word
                    byte[] wordBytes = new byte[lenWord];
                    dis.readFully(wordBytes);
                    String word = new String(wordBytes, StandardCharsets.UTF_8);

                    // Check separator ','
                    separator = dis.readByte();
                    if (separator != (byte) ',') throw new IOException("Expected ',' not found.");

                    if (hasWordImage(word)) {
                        // Skip image and newline
                        dis.skipBytes(lenImage + 1);
                        duplicatewords++;
                    } else {
                        // Read base64 image
                        byte[] base64Bytes = new byte[lenImage];
                        dis.readFully(base64Bytes);

                        // Check newline
                        separator = dis.readByte();
                        if (separator != (byte) '\n') throw new IOException("Expected '\\n' not found.");

                        String base64Image = new String(base64Bytes, StandardCharsets.UTF_8);
                        Bitmap bit = base64ToBitmap(base64Image);
                        Bitmap resizedBit = resizeImageToFitDatabase(bit);
                        String base64 = bitmapToBase64(resizedBit);
                        setWordImageFromBase64(word, base64);
                        newwords++;
                    }

                    total++;

                    // Update progress on UI thread
                    int finalNew = newwords;
                    int finalDup = duplicatewords;
                    int finalTotal = total;
                    ((Activity) context).runOnUiThread(() -> {
                        progressText.setText(
                                "Total processed: " + finalTotal +
                                        "\nNew images: " + finalNew +
                                        "\nDuplicate images: " + finalDup);
                    });
                }

                dis.close();

                // Final result dialog
                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Restore Images");
                    alertDialog.setMessage("Backup loaded successfully:\nNew Images: " + newwords + "\nDuplicate Images: " + duplicatewords);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error restoring images from backup", e);
                ((Activity) context).runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Error restoring images: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public Bitmap resizeImageToFitDatabase(Bitmap img) {
        Bitmap si = img.copy(img.getConfig(), true);
        int width = si.getWidth();
        int height = si.getHeight();
        int min = Math.min(width, height);

        if (min >= 150) {
            int w = Math.min(min, 220);
            double r = 1.0 * min / w;
            int dh = (height - min) / 2;
            int dw = (width - min) / 2;

            Bitmap bit = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < w; j++) {
                    int x = (int)(i * r + dw);
                    if (x < 0) x = 0;
                    else if (x >= width) x = width - 1;

                    int y = (int)(j * r + dh);
                    if (y < 0) y = 0;
                    else if (y >= height) y = height - 1;

                    bit.setPixel(i, j, si.getPixel(x, y));
                }
            }
            return bit;
        }
        return img; // Return original if no resizing was done
    }

    private  void copyFile(File src, File dst) throws Exception {
        try (FileChannel inChannel = new FileInputStream(src).getChannel();
             FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            outChannel.transferFrom(inChannel, 0, inChannel.size());
        }
    }
    public int getExactPercent()
    {
        int wrong=0;
        int correct=0;
        for(int k=0;k<wordItems.length;k++)
        {
            String r = wordItems[k].review;
            for(int i=0;i<r.length();i++)
            {
                String c= String.valueOf(r.charAt(i));
                if(c.equals("w")){wrong++;}
                else if(c.equals("f")) {wrong++;}
                else if(c.equals("e")) {wrong++;}
                else correct++;
            }
        }
        return (correct*100/(correct+wrong));
    }
    public int getProgressPercent()
    {
        double boxes=0;
        for(int k=0;k<wordItems.length;k++)
        {
            boxes+=wordItems[k].box();

        }
        return (int)((boxes*100.0/(wordItems.length*15.0)));
    }
    public void getWordItems(Cursor cursor) {
        wordItems = new wordItem[cursor.getCount()];
        int k = 0;
        if (cursor.moveToFirst()) {
            do {
                wordItems[k] = new wordItem();
                wordItems[k].id = cursor.getInt(DBAdapter.COL_id);
                wordItems[k].word = cursor.getString(DBAdapter.COL_word);
                wordItems[k].example = cursor.getString(DBAdapter.COL_example);
                wordItems[k].day = cursor.getInt(DBAdapter.COL_day);
                wordItems[k].persian= decode(cursor.getString(DBAdapter.COL_persian));
                wordItems[k].examplefa= decode(cursor.getString(DBAdapter.COL_examplefa));
                wordItems[k].definition = cursor.getString(DBAdapter.COL_definition);
                wordItems[k].review= cursor.getString(DBAdapter.COL_review);
                wordItems[k].started = cursor.getInt(DBAdapter.COL_started);
                wordItems[k].finished = cursor.getInt(DBAdapter.COL_finished);
                wordItems[k].lastheart = cursor.getInt(DBAdapter.COL_lastheart);
                wordItems[k].pronounce = cursor.getString(DBAdapter.COL_pronounce);
                k++;
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
    void Dictionary(final String word) throws UnsupportedEncodingException {
        String response ="";
        String search=word.toLowerCase();
        for(int i=0;i<wordItems.length;i++)
        {
            if(wordItems[i].word.toLowerCase().contains(search)) {
                response +=wordItems[i].word+ ": Week: " + (1+(wordItems[i].day-1) / 4) + ", Day: " + (1+(wordItems[i].day-1) % 4) + "\r\n";
            }

        }
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(word);
        alertDialog.setMessage(response);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        //Toast.makeText(context,"result:\n"+response,Toast.LENGTH_SHORT).show();
    }
    public String getSaveString()
    {
        String savestr="[User:"+ getUser()+",Count:"+wordItems.length+"]WORDS(word,review,lastheart,started,finished){";
        for(int i=0;i<wordItems.length;i++)
        {
            String sep="";
            if(i>0) sep="$$";
            savestr+=sep+wordItems[i].word+"**"+wordItems[i].review+"**"+wordItems[i].lastheart+"**"+wordItems[i].started+"**"+wordItems[i].finished;
        }
        savestr+="}";
        return savestr;
    }

    void openDB(Context context)
    {
            myDB = DBAdapter.getInstance(context);
            myDB.open();
    }
    void closeDB()
    {
        try {
            myDB.close();
        }catch (Exception e){}
    }
    int getDBVersion()
    {
        return prefs.getInt("dbversion",0);
    }
    void setDBVersion(int version)
    {
        editor.putInt("dbversion",version);
        editor.commit();
    }
    int getSwipeShown()
    {
        return prefs.getInt("swipeshown",0);
    }
    void setSwipeShown()
    {
        editor.putInt("swipeshown",Math.min(prefs.getInt("swipeshown",0)+1,100));
        editor.commit();
    }
    /////
    int getTimeLearn()
    {
        return prefs.getInt("timelearn",40);
    }
    void setTimeLearn(int time)
    {
        editor.putInt("timelearn",time);
        editor.commit();
    }
    int getTimeTest()
    {
        return prefs.getInt("timetest",55);
    }
    void setTimeTest(int time)
    {
        editor.putInt("timetest",time);
        editor.commit();
    }
    /////
    /////
    int getVolumeGame()
    {
        return prefs.getInt("voulmegame",0);
    }
    void setVolumeGame(int volume)
    {
        editor.putInt("voulmegame",volume);
        editor.commit();
    }
    /////
    int getVolumeButtons()
    {
        return prefs.getInt("voulmebuttons",50);
    }
    void setVolumeButtons(int volume)
    {
        editor.putInt("voulmebuttons",volume);
        editor.commit();
    }/////
    int getVolumeMain()
    {
        return prefs.getInt("voulmemain",0);
    }
    void setVolumeMain(int volume)
    {
        editor.putInt("voulmemain",volume);
        editor.commit();
    }
    /////
    int getTapShown()
    {
        return prefs.getInt("tapshown",0);
    }
    int getImageTapShown()
    {
        return prefs.getInt("imagetapshown",0);
    }
    void setTapShown()
    {
        editor.putInt("tapshown",Math.min(prefs.getInt("tapshown",0)+1,100));
        editor.commit();
    }
    void setImageTapShown()
    {
        editor.putInt("imagetapshown",Math.min(prefs.getInt("imagetapshown",0)+1,100));
        editor.commit();
    }
    /////
    Boolean getInfoShown()
    {
        return prefs.getBoolean("infoshown",false);
    }
    void setInfoShown()
    {
        editor.putBoolean("infoshown",true);
        editor.commit();
    }
    /////
    Boolean getIsLogin()
    {
        return prefs.getBoolean("islogin",false);
    }
    void setIsLogin(Boolean isLogin)
    {
        editor.putBoolean("islogin",isLogin);
        editor.commit();
    }
    ///
    Boolean getIsPronounce()
    {
        return prefs.getBoolean("ispronounce",true);
    }
    void setIsPronounce(Boolean isPronounce)
    {
        editor.putBoolean("ispronounce",isPronounce);
        editor.commit();
    }
    ///
    Boolean getIsUserWordsDownloaded()
    {
        return prefs.getBoolean("userwordsgotten",false);
    }
    void setIsUserWordsDownloaded(Boolean downloaded)
    {
        editor.putBoolean("userwordsgotten",downloaded);
        editor.commit();
    }
    ///
    int getProgWidth()
    {
        Random rnd = new Random();
        return prefs.getInt("progwidth",0);
    }
    void setProgWidth(int width)
    {
        editor.putInt("progwidth",width);
        editor.commit();
    }
    public int getLastHeartidSaved()
    {
        return prefs.getInt("lastsavedid",0);
    }
    public void setLastHeartIdSaved(int heartId)
    {
        editor.putInt("lastsavedid",heartId);
        editor.commit();
    }
    /////
    ///
    String getMessages()
    {
        return prefs.getString("messages","");
    }
    int getMessagesCount()
    {
        return prefs.getInt("messagescount",0);
    }
    int getMessagesShownCount()
    {
        return prefs.getInt("messagesshowncount",0);
    }
    void setMessagesShownCount(int num)
    {
        editor.putInt("messagesshowncount",num);
        editor.commit();
    }
    void setMessages(String messages)
    {
        editor.putString("messages",messages);
        editor.putInt("messagescount",(messages.length()>0?messages.split("\n").length:0));
        editor.commit();
    }
    /////

    ///
    Bitmap getAvatar()
    {
        String imstr= prefs.getString("avatar","");
        if(imstr.length()==0) return BitmapFactory.decodeResource(context.getResources(),
            R.drawable.usericon);
        byte[] decodedString = Base64.decode(imstr, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        int width = Math.min(bitmap.getWidth(), bitmap.getHeight());
        double w = bitmap.getWidth() / 2.0;
        double h = bitmap.getHeight() / 2.0;
        double radius = width / 2.0;
        Bitmap bit = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        try {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (Math.pow(i - w, 2) + Math.pow(j - h, 2) > (radius * radius)) {
                    } else if (Math.pow(i - w, 2) + Math.pow(j - h, 2) > (radius * 0.81 * radius)) {
                        bit.setPixel(i, j, Color.argb(255, 0xB2, 0x66, 0xA5));
                    } else bit.setPixel(i, j, bitmap.getPixel(i, j));
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }return bit;
    }
    void setAvatar(String avatar)
    {
        avatar = "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";
        editor.putString("avatar",avatar);
        editor.putBoolean("avatarsaved",true);
        editor.commit();
    }
    public Boolean isAvatarSaved()
    {
        return prefs.getBoolean("avatarsaved",false);
    }
    public void AvatarChanged()
    {
        editor.putBoolean("avatarsaved",false);
        editor.commit();
    }

    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // Use JPEG
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP); // NO_WRAP = compact
    }

    public Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    private Bitmap StringToWordImage(String imagestr)
    {
        try {

            Bitmap bitmap1 = base64ToBitmap(imagestr);
            Bitmap bitmap = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), Bitmap.Config.ARGB_8888);
            for(int i=0;i<bitmap.getWidth();i++) for (int j=0;j<bitmap.getHeight();j++)bitmap.setPixel(i,j,bitmap1.getPixel(i,j));
            int radius = bitmap.getWidth()/5;
            int width=bitmap.getWidth();int height=bitmap.getHeight();
            int hmr=height-radius;int wmr=width-radius;
            for(int i=0;i<=radius;i++) {
                for (int j = 0; j <= radius; j++)
                    if (((i - radius) * (i - radius) + (j - radius) * (j - radius)) > radius * radius){
                        if ((i <= radius) && (j <= radius)){
                            bitmap.setPixel(i, j, Color.argb(0, 0, 0, 0));
                        }
                    }
            }
            for(int i=wmr;i<bitmap.getWidth();i++) {
                for (int j = 0; j <= radius; j++)
                    if (Math.pow(i-wmr,2) + Math.pow(radius-j,2)  > radius * radius)
                        if (i>= wmr && j <= radius)
                            bitmap.setPixel(i, j, Color.argb(0, 0, 0, 0));
            }
            for(int i=0;i<=radius;i++) {
                for (int j = hmr; j <bitmap.getHeight(); j++)
                    if (((i - radius) * (i - radius) + (j-hmr) * (j-hmr)) > radius * radius)
                        if (i <= radius && j>=hmr)
                            bitmap.setPixel(i, j, Color.argb(0, 0, 0, 0));
            }
            for(int i=wmr;i<width;i++) {
                for (int j = hmr; j <height; j++)
                    if (Math.pow(wmr-i,2) + Math.pow(hmr-j,2) > radius * radius)
                        if (i>=wmr && j>=hmr)
                            bitmap.setPixel(i, j, Color.argb(0, 0, 0, 0));
            }
            return bitmap;
        }catch (Exception e){return null;}
    }
    /////
    Bitmap getWordImage(String word)
    {
        Cursor cursor=myDB.getWordImage(word);
        String imstr="";
        if(cursor.getCount()>0){ if (cursor.moveToFirst()) imstr = cursor.getString(DBAdapter.COL_image);}
        else  return null;
        return StringToWordImage(imstr);
    }

    boolean hasWordImage(String word)
    {
       return myDB.hasWordImage(word);
    }
    Bitmap setWordImageFromBase64(String word,String image)
    {
        if(!myDB.hasWordImage(word)){
            myDB.insertWordImage(word,image);
        }
        else myDB.updateWordImage(word,image);
        return StringToWordImage(image);
    }

    public ImageItem[] getAllImages()
    {
        Cursor cursor=myDB.getAllImages();
        imageItems = new ImageItem[cursor.getCount()];
        int k = 0;
        if (cursor.moveToFirst()) {
            do {
                imageItems[k] = new ImageItem();
                imageItems[k].id = cursor.getInt(DBAdapter.COL_id);
                imageItems[k].word = cursor.getString(DBAdapter.COL_word);
                imageItems[k].base64image = cursor.getString(DBAdapter.COL_image);
                k++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageItems;
    }
    //
    private int getLevelTarget(int level) //if number of correct answers arrive targets level increases; each level releases 20 words;
    {
        int target =80;
        if(level>targets.length) return (35*20);
        else return targets[level-1]*20;
    }
    /////
    public Boolean getBuyEnabled()
    {
        return prefs.getBoolean("potionsactivated",false);
    }
    public void setBuyEnabled(Boolean enabled)
    {
        editor.putBoolean("potionsactivated",enabled);
        editor.commit();
    }
    /////
    /////
    public int getHeartId()
    {
        return prefs.getInt("heartid",1);
    }
    void inceaseHeartId()
    {
        editor.putInt("heartid",(getHeartId()+1));
        editor.commit();
    }
    /////
    public int getNumberOfPlayings()
    {
        return prefs.getInt("numofplays",0);
    }
    public void setNumberOfPlayings(int number)
    {
        editor.putInt("numofplays",number);
        editor.commit();
    }
    public void addPlayResult(double percent)
    {
        int n=getNumberOfPlayings();
        double perc=(double)prefs.getFloat("percent",(float)0);
        double mpercent = (perc*n+percent*10)/(n+1);
        editor.putInt("numofplays",(n+1));
        editor.putFloat("percent",(float)mpercent);
        editor.commit();
    }
    public double getPercent()
    {
        return (prefs.getFloat("percent",(float)0)/10.0);
    }
    public double getPercent10()
    {
        return (prefs.getFloat("percent",(float)0));
    }
    public void setPercent(double percent)
    {
        editor.putFloat("percent",(float)(percent*10));
        editor.commit();
    }
    ///
    public void initialHeartAndExir(int hearts,int heartid,int potions,int level, int progress)
    {
        editor.putInt("heartid",heartid);
        editor.putInt("level",level);
        editor.putInt("hearts",0);
        editor.putInt("heartsbought",0);
        editor.putInt("exir",potions);
        editor.putInt("progress",progress);
        editor.commit();
        addheartsBought(hearts);
        setLevelUpShown(level);
    }
    public void initialHeartAndExirSettings(Boolean online,int heartincreastime,int decreaspotiontime,int heartsmax)
    {
        editor.putInt("heartinctime",heartincreastime);
        editor.putInt("heartsmax",heartsmax);
        editor.putInt("decexirtime",decreaspotiontime);
        editor.putBoolean("settingsonline",online);
        editor.commit();
    }
    /////
    int getHeartsMaximum()
    {
        return prefs.getInt("heartsmax",HeartsMaximum);
    }
    int getHeartIncreaseTime()
    {
        return prefs.getInt("heartinctime",HeartIncreasTime);
    }
    int gethearts()
    {
        return prefs.getInt("hearts",getHeartsMaximum());
    }
    int getheartsBought()
    {
        return prefs.getInt("heartsbought",0);
    }
    void addheart(int number)
    {
        String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm:ss";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String stringDate = sdf.format(date);
        int h=(gethearts()+number);
        if (h>getHeartsMaximum())h=getHeartsMaximum();
        editor.putInt("hearts",h);
        editor.putString("addhearttime",stringDate);
        editor.commit();
    }
    void addheartsBought(int number)
    {
        int h=(gethearts()+number);
        int h1=0;
        if (h>getHeartsMaximum()) {
            h1 = h - getHeartsMaximum();
            editor.putInt("hearts", getHeartsMaximum());
        }
        else editor.putInt("hearts", h);
        editor.putInt("heartsbought",getheartsBought()+h1);
        editor.commit();
    }

    void decreaseheart() {
        int h = gethearts();
        int h1 = getheartsBought();
        if (h > 0) {
            h--;
            editor.putInt("hearts", h);
            if (h == (getHeartsMaximum() - 1)) {
                String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm:ss";
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                String stringDate = sdf.format(date);
                editor.putString("addhearttime", stringDate);
            }
        } else if (h1 > 0) {
            h1--;
            editor.putInt("heartsbought", h1);
        }
        editor.commit();
    }

    Date getHeartAddedTime()
    {
        String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm:ss";
        String str= prefs.getString("addhearttime","");
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        Date date;
        if(str.equals("")){
            date = new Date();
            String stringDate = sdf.format(date);
            editor.putString("addhearttime",stringDate);
            editor.commit();
            return date;
        }
        try {
            date = sdf.parse(str);
            return date;
        } catch(Exception e){
            date = new Date();
            String stringDate = sdf.format(date);
            editor.putString("addhearttime",stringDate);
            editor.commit();
            return date;
        }
    }
    /////
    int getDecreasExirTime() //equals 80
    {
        return prefs.getInt("decexirtime",DecreasExirEachMin);
    }
    int getExir()
    {
        return  prefs.getInt("exir",InitialExir);
    }
    int getDecreasingExir()
    {
        String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm:ss";
        String str= prefs.getString("exirtime","");
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        Date date = new Date();
        if(str.equals("")) date =new Date();
        try {
            date = sdf.parse(str);
        } catch(Exception e){
            date = new Date(); //Exception handling
        }
        long mills = (new Date()).getTime() - date.getTime();
        int mins = (int) (mills/(1000 * 60));
        return (int)(mins/getDecreasExirTime());
    }
    Date getExirChangedTime()
    {
        String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm:ss";
        String str= prefs.getString("exirtime","");
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        Date date;
        if(str.equals("")){
            date = new Date();
            String stringDate = sdf.format(date);
            editor.putString("exirtime",stringDate);
            editor.commit();
            return date;
        }
        try {
            date = sdf.parse(str);
            return date;
        } catch(Exception e){
            date = new Date();
            String stringDate = sdf.format(date);
            editor.putString("exirtime",stringDate);
            editor.commit();
            return date;
        }

    }
    void increaseExir(int number)
    {
        String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm:ss";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String stringDate = sdf.format(date);
        editor.putString("exirtime",stringDate);
        editor.putInt("exir",(getExir()+number));
        editor.commit();
    }
    int decreaseExir(int number)
    {
        int getex=getExir();
        if(getex>12 && number>(getex/2))number=(getex/2);
        int e = (getex-number);
        if(e<0)e=0;
        String DATE_FORMAT_NOW = "yyyy-MM-dd kk:mm:ss";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String stringDate = sdf.format(date);
        editor.putString("exirtime",stringDate);
        editor.putInt("exir",e);
        editor.commit();
        return (getex-e);
    }

    /////
    int getLevel()
    {
        return prefs.getInt("level",1);
    }
    void increaseLevel(int number)
    {
        editor.putInt("level",(getLevel()+number));
        editor.commit();
        increaseExir(1);
        addheartsBought(1);
    }
    int getLevelUpShown()
    {
        return prefs.getInt("levelup",1);
    }
    void setLevelUpShown(int level)
    {
        editor.putInt("levelup",level);
        editor.commit();
    }
    /////
    private int getMaxWordIdReleased()
    {
        int level=getLevel();
        int max=level*20;
        if (max>wordItems.length) max=wordItems.length;
        return max;
    }
    public int getPreviousWordIdReleased()
    {
        int level=getLevel();
        if(level>1)level--;
        int max=level*20;
        if (max>wordItems.length-1) max=wordItems.length-1;
        return max;
    }
    /////
    public int getProgress()
    {
        return prefs.getInt("progress",0);
    }
    Boolean increaseProgress(int number) //correct answers;
    {
        int progress=getProgress();
        int target = getLevelTarget(getLevel());
        if(progress+number>=target)
        {
            increaseLevel(1);
            editor.putInt("progress",(progress+number-target));
            editor.commit();
            return true; //****Level Increased!!!!!****
        }
        else {
            editor.putInt("progress", (getProgress() + number));
            editor.commit();
            return false; //level did not change
        }
    }
    String getProgressString() //correct answers;
    {
        int progress=getProgress();
        int target = getLevelTarget(getLevel());
        return ""+progress+"/"+target;
    }
    int getProgressWidthByRef(int refWidth)
    {
        int progress=getProgress();
        int target = getLevelTarget(getLevel());
        return progress*refWidth/target;
    }

    /////
    String getUser()
    {
        Random rnd = new Random();
        return prefs.getString("user","guest"+ String.valueOf(rnd.nextInt(1000)*rnd.nextInt(1000)));
    }
    void setUser(String user)
    {
        editor.putString("user",user);
        editor.commit();
    }
    String getPassword()
    {
        return prefs.getString("password","");
    }
    void setPassword(String password)
    {
        editor.putString("password",password);
        editor.commit();
    }
    /////
    int[] GetQuestionsIndex (int numOfQuestions)
    {
        int[] questions = new int[numOfQuestions];
        List<Integer> list;
        list = new ArrayList<Integer>();
        int k=0;
        int max=getMaxWordIdReleased();
        int heartid=getHeartId();
        for(int i=0;i<max;i++)
        {
            int box = wordItems[i].box();
            int dheart = heartid-wordItems[i].lastheart;
            if(box>=15) continue;
            else {
                if (dheart >= heartOfBox[box]) {
                    if(!list.contains(i)) list.add(i);
                    k++;
                }
                if (k == numOfQuestions-1) break;
            }
        }
        if(list.size()<numOfQuestions)
        {
            Random rnd=new Random();
            while (list.size()<numOfQuestions){
                int []r= new int[6];
                r[0]=rnd.nextInt(wordItems.length);
                r[1]=rnd.nextInt(Math.min(max*2,wordItems.length));
                r[1]=rnd.nextInt(Math.min(max*2,wordItems.length));
                r[1]=rnd.nextInt(Math.min(max*2,wordItems.length));
                r[2]=rnd.nextInt(Math.min(max*3,wordItems.length));
                r[2]=rnd.nextInt(Math.min(max*3,wordItems.length));
                int rr=r[rnd.nextInt(6)];
                int box = wordItems[rr].box();
                if(box<15) if(!list.contains(rr))list.add(rr);
            }
        }
        List<Integer> qlist;
        qlist = new ArrayList<Integer>();
        Random rnd=new Random();
        while (qlist.size()<numOfQuestions){
            int r= rnd.nextInt(list.size());
            if(!qlist.contains(list.get(r)))qlist.add(list.get(r));
        }
        for(int i=0;i<numOfQuestions;i++) questions[i]=qlist.get(i);
        return questions;
    }

    private String getword(int id)
    {
        return String.valueOf(wordItems.length);
    }

    public void checkVersionAndUpdate()  throws UnsupportedEncodingException
    {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        if(response.contains("VERSION")) {
                            String resp = response.substring(response.indexOf("{") + 1, response.indexOf("}"));
                            try {
                                int version = Integer.parseInt(resp);
                                if(version>getDBVersion())
                                {
                                    progressDialog.show();
                                    updateDatabase(version);
                                }
                                else Toast.makeText(context,"You have the latest version of word database",Toast.LENGTH_SHORT).show();

                            }
                            catch (Exception e){
                                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                        else  Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(context,"Could not connect to server",Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("command", "getdbversion");
                //params.put("domain", "http://itsalif.info");
                return params;
            }
        };
        queue.add(postRequest);

    }

    public  void  updateDatabase(final int version)  throws UnsupportedEncodingException
    {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        if(response.contains("WORDS")) {
                            String params = response.substring(response.indexOf("WORDS")+6,response.indexOf("{")-1);
                            String resp = response.substring(response.indexOf("{") + 1, response.indexOf("}"));
                            if(updateFromString(resp,params,false)>0) setDBVersion(version);
                        }
                        else  Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                        Log.d("Response", response);
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(context,"Could not connect to server",Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("command", "get1100update");
                //params.put("domain", "http://itsalif.info");
                return params;
            }
        };
        queue.add(postRequest);

    }

    public  void  getSavedWordsOnline()  throws UnsupportedEncodingException
    {
        progressDialog2.show();
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        setIsUserWordsDownloaded(true);
                        if(response.contains("WORDS")) {
                            String params = response.substring(response.indexOf("WORDS")+6,response.indexOf("{")-1);
                            String resp = response.substring(response.indexOf("{") + 1, response.indexOf("}"));
                            updateFromString(resp,params,true);
                            Toast.makeText(context,"Saved progress loaded",Toast.LENGTH_SHORT).show();
                        }
                        else  Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                        Log.d("Response", response);
                        progressDialog2.dismiss();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(context,"Could not connect to server",Toast.LENGTH_LONG).show();
                        progressDialog2.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("command", "getSavedWords"+","+getUser());
                //params.put("domain", "http://itsalif.info");
                return params;
            }
        };
        queue.add(postRequest);

    }


    int updateFromString(String file,String params,Boolean isSave) //isSave(true)->loadSavedWordsOnlind  (false)->getUpdateWordsOnline
    {
        String []rows = file.replace("$$","~").split("~");
        String []param = params.split(",");
        String all="";
        int count=0,error=0;
        for(int i=0;i<rows.length;i++)
        {
            String row = rows[i];
            row=row.replace("**","~");
            String[] cells = row.split("~");
            String[] cellvalues = new String[param.length];
            for(int j=0;j<cells.length;j++)
            {
                cellvalues[j] = cells[j];
            }
            String line ="";
            wordItem word=new wordItem();
            for (int j=0;j<cells.length;j++)
            {
                word.setparam(param[j],cellvalues[j]);
            }
            if(isSave) {
                try {
                    if (myDB.UpdateSaveItem(word))
                        count++;
                    else error++;
                }catch (Exception e){}
            }else {
                try {
                    if (myDB.UpdateOrInsert(word))
                        count++;
                    else error ++;
                }catch (Exception e){}

            }
        }
        //Toast.makeText(context,"Your word database updated successfully. Updated: "+count+" , Errors: "+error,Toast.LENGTH_LONG).show();
        Cursor cursor = myDB.getAllWords();
        getWordItems(cursor);
        cursor.close();
        return count;
    }
    public  void  loadGetLoading(final Activity ma)  throws UnsupportedEncodingException {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://raw.githubusercontent.com/MortezaMaghrebi/Datesets-For-Word-Hero-Application/refs/heads/main/Messages.txt";

        // Variable to store the file content
        final String[] fileContent = {""}; // Using array to allow modification in inner class

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Store the response (file content) in the variable
                        setMessages(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        //Toast.makeText(context, "Could not download file: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.getCache().clear();
        queue.add(getRequest);

    }

    String decode (String persian)
    {
        String[] c = encode.split("~");
        String[] c1 = new String[c.length];
        String[] c2 = new String[c1.length];
        for(int j=0;j<c1.length; j++)
        {
            c1[j] = c[j].split(",")[0];
            c2[j] = c[j].split(",")[1];
        }
        for(int j=0;j<c2.length;j++)
        {
            persian=persian.replace(c2[j], c1[j]);
        }
        return persian;
    }

}
