package mortezamaghrebi.com.wordhero;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {
    public static Dictionary instance = null;
    private Context context;
    private ProgressDialog progressDialog;

    public static Dictionary getInstance(Context context) {
        if (instance == null) {
            instance = new Dictionary(context);
            instance.loadDictionary(new Dictionary.LoadDictionaryListener() {
                @Override
                public void onComplete() {
                    // دیالوگ به صورت خودکار بسته می‌شود
                    Toast.makeText(context,
                            "دیکشنری با موفقیت بارگذاری شد (" +
                                    instance.getVocabularyCount() + " کلمه)",
                            Toast.LENGTH_LONG).show();

                    // تست جستجو
                    // if (found != null) {
                    //Log.d("Dictionary", "کلمه یافت شد: " + found.persian);
                    //}
                }

                @Override
                public void onError(String error) {
                    // دیالوگ به صورت خودکار بسته می‌شود
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                }
            });
        }
        return instance;
    }

    public Dictionary(Context context) {
        this.context = context;
        this.vocabularies = new ArrayList<>();
    }

    public class Vocabulary {
        public String word;
        public String persian;
        public String example;
        public String synonym;
        public String level;
        public String pronounciation;
        public String examplefa;
    }

    List<Vocabulary> vocabularies;

    public interface LoadDictionaryListener {
        void onComplete();
        void onError(String error);
    }

    public void loadDictionary(LoadDictionaryListener listener) {
        // ایجاد و نمایش دیالوگ پروگرس
        showProgressDialog("در حال بارگذاری دیکشنری", "لطفا منتظر بمانید...");

        new Thread(() -> {
            try {
                String asset = loadFromAsset("my_dictionary.txt");
                if (asset == null || asset.length() == 0) {
                    hideProgressDialog();
                    if (listener != null) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                listener.onError("فایل دیکشنری یافت نشد"));
                    }
                    return;
                }

                String[] lines = asset.replace("\r\n", "\n").split("\n");
                int totalLines = lines.length;

                vocabularies.clear();

                for (int i = 0; i < totalLines; i++) {
                    String line = lines[i];
                    String[] params = line.split("#");
                    if (params.length != 7) continue;

                    Vocabulary vocabulary = new Vocabulary();
                    vocabulary.word = params[0].trim().toLowerCase();
                    vocabulary.persian = params[1].trim();
                    vocabulary.example = params[2].trim();
                    vocabulary.synonym = params[3].trim();
                    vocabulary.level = params[4].trim();
                    vocabulary.pronounciation = params[5].trim();
                    vocabulary.examplefa = params[6].trim();

                    vocabularies.add(vocabulary);

                    // آپدیت پروگرس
                    final int progress = i + 1;
                    final int finalTotal = totalLines;
                    updateProgressDialog(progress, finalTotal);
                }

                hideProgressDialog();

                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(listener::onComplete);
                }

            } catch (Exception e) {
                e.printStackTrace();
                hideProgressDialog();
                if (listener != null) {
                    final String error = e.getMessage();
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onError(error != null ? error : "خطا در بارگذاری دیکشنری"));
                }
            }
        }).start();
    }

    private void showProgressDialog(String title, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.show();
        });
    }

    private void updateProgressDialog(int current, int total) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                int progressPercent = (int) ((current * 100.0f) / total);
                progressDialog.setProgress(progressPercent);
                progressDialog.setMessage("در حال پردازش: " + current + " از " + total);
            }
        });
    }

    private void hideProgressDialog() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        });
    }

    public void find(String vocabulary) {
        if (vocabularies == null || vocabularies.isEmpty() || vocabulary == null) {
            return ;
        }
        String response ="";

        String searchWord = vocabulary.trim().toLowerCase();

        for (Vocabulary vocab : vocabularies) {
            if (vocab.word != null && vocab.word.contains(searchWord) && vocab.word.length()>2) {
                response +=
                        vocab.word.substring(0,1).toUpperCase()+ vocab.word.substring(1).toLowerCase()+ ":"+ "\r\nPronounciation: "+vocab.pronounciation+ "\r\nDefinition: "+vocab.synonym+"\r\nExample: "+vocab.example
                                + "\r\nمعنی: "+vocab.persian+"\r\n"+"مثال: "+vocab.examplefa+"\r\n-------------------------------\r\n";

            }
        }

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(vocabulary);
        alertDialog.setMessage(response);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public List<Vocabulary> search(String query) {
        List<Vocabulary> results = new ArrayList<>();
        if (vocabularies == null || vocabularies.isEmpty() || query == null || query.trim().isEmpty()) {
            return results;
        }

        String searchQuery = query.trim().toLowerCase();

        for (Vocabulary vocab : vocabularies) {
            if (vocab.word != null && vocab.word.toLowerCase().contains(searchQuery)) {
                results.add(vocab);
            }
        }

        return results;
    }





    public boolean isLoaded() {
        return vocabularies != null && !vocabularies.isEmpty();
    }

    public int getVocabularyCount() {
        return vocabularies != null ? vocabularies.size() : 0;
    }

    public String loadFromAsset(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        InputStream inputStream = null;

        try {
            inputStream = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return stringBuilder.toString();
    }
}