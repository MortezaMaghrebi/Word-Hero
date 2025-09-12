package mortezamaghrebi.com.wordhero;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListAdapterDatasets extends ArrayAdapter<String> {

    private int resourceLayout;
    private Activity mContext;
    Controller controller;

    public ListAdapterDatasets(Activity context, int resource, List<String> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
        controller = new Controller(context,true);
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }
        TextView txtgroup = (TextView) v.findViewById(R.id.txtgroup);
        TextView txtname = (TextView) v.findViewById(R.id.txtname);
        TextView txtbutton = (TextView) v.findViewById(R.id.txtdatasetbutton);
        String[] parts = getItem(position).split("--");
        RelativeLayout lytbutton= (RelativeLayout) v.findViewById(R.id.lytdatasetbutton);
        RelativeLayout lytimagesbutton= (RelativeLayout) v.findViewById(R.id.lytgetImages);
        if (parts.length >=2) {
            String group = parts[0].trim().split("/")[0];
            String name= parts[0].trim();
            if(parts[0].trim().split("/").length>1)  name = parts[0].trim().split("/")[1];
            String urlwords = parts[1].trim();
            String urlurlimages = parts.length>2? parts[2].trim():"";
            txtgroup.setText(group);
            txtname.setText(name);
            if(urlwords.length()>5) {
                lytbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        try {
                                            GetWordsFromURL(urlwords, urlurlimages);
                                        } catch (UnsupportedEncodingException e) {
                                            throw new RuntimeException(e);
                                        }
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("آیا مطمئنید که میخواهید لغات را دانلود کنید؟").setPositiveButton("بله", dialogClickListener)
                                .setNegativeButton("خیر", dialogClickListener).show();

                    }
                });
                lytbutton.setVisibility(View.VISIBLE);
            }else {
                lytbutton.setVisibility(View.INVISIBLE);
            }
            if(urlurlimages.length()>5) {
                lytimagesbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        try {

                                        GetImagesFromURL(urlurlimages);
                                        } catch (UnsupportedEncodingException e) {
                                            throw new RuntimeException(e);
                                        }break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("آیا مطمئنید که میخواهید تصاویر دانلود کنید؟").setPositiveButton("بله", dialogClickListener)
                                .setNegativeButton("خیر", dialogClickListener).show();

                    }
                });
                lytimagesbutton.setVisibility(View.VISIBLE);
            }else
            {
                lytimagesbutton.setVisibility(View.INVISIBLE);
            }
        }else
        {
            lytbutton.getLayoutParams().height=0;
            lytbutton.requestLayout();
            lytimagesbutton.getLayoutParams().height=0;
            lytimagesbutton.requestLayout();
        }

        return v;
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    int add = 0, update = 0, error = 0;
    public void GetWordsFromURL(String url, String urlimages) throws UnsupportedEncodingException {
        controller = new Controller(mContext, true);
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.getCache().clear();
        add = 0; update = 0; error = 0;
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // وقتی فایل دانلود شد، پردازش شروع میشه

                        // ساخت دیالوگ سفارشی
                        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress, null);
                        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
                        TextView txtPercent = dialogView.findViewById(R.id.txtPercent);
                        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);
                        txtTitle.setText("در حال پردازش لغات...");

                        AlertDialog progressDialog = new AlertDialog.Builder(mContext)
                                .setView(dialogView)
                                .setCancelable(false)
                                .create();
                        progressDialog.show();

                        // حالا پردازش در بک‌گراند
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());

                        executor.execute(() -> {

                            String[] rows = response.split("\n");
                            progressBar.setMax(rows.length);

                            int index = 0;
                            for (String row : rows) {
                                String[] items = row.split("#");
                                if (items.length == 7) {
                                    String word = items[0];
                                    int day = ((index / 5) + 1);
                                    String persian = items[1];
                                    String example = items[2];
                                    String definition = items[3];
                                    String synonyms="";
                                    String cefr_level=items[4].toUpperCase().trim();
                                    String pronounce = items[5];
                                    String sound = "";
                                    String examplefa = items[6];
                                    if (!cefr_level.matches("A[12]|B[12]|C[12]")) {
                                        cefr_level = "B1";
                                    }


                                    if (!controller.myDB.hasWord(word)) {
                                        controller.myDB.insertWord(word, day, persian, definition, pronounce, sound, example, examplefa,synonyms,cefr_level);
                                        add++;
                                    } else {
                                        controller.myDB.updateWordRowFromBackup(word, day, persian, definition, pronounce, sound, example, examplefa,synonyms,cefr_level);
                                        update++;
                                    }
                                } else error++;
                                index++;

                                int finalIndex = index;
                                int finalAdd = add;
                                int finalUpdate = update;
                                int finalError = error;

                                // آپدیت UI
                                handler.post(() -> {
                                    progressBar.setProgress(finalIndex);
                                    int percent = (finalIndex * 100) / rows.length;
                                    txtPercent.setText(percent + "% (" + finalIndex + "/" + rows.length + ")");
                                });
                            }

                            // بعد از اتمام حلقه
                            handler.post(() -> {
                                progressDialog.dismiss();
                                showSummaryDialog(add, update, error, urlimages);
                            });
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, "خطا در دانلود فایل: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
        queue.add(getRequest);
    }

    public void GetImagesFromURL(String url) throws UnsupportedEncodingException {
        ProgressBar progressBar = new ProgressBar(mContext);
        progressBar.setIndeterminate(true);
        AlertDialog progressDialog = new AlertDialog.Builder(mContext)
                .setTitle("در حال دریافت تصاویر")
                .setView(progressBar)
                .setCancelable(true)
                .create();

        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.getCache().clear();

        // Custom Request for binary data
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                response -> {
                    // response is byte[]
                    if (response != null && response.length > 0) {
                        progressDialog.dismiss();

                        controller.LoadImagesFromBytes(response);
                    } else {
                        Toast.makeText(mContext, "Downloaded file is empty", Toast.LENGTH_LONG).show();
                        progressDialog.setView(null); // حذف پروگرس‌بار
                        progressDialog.setCancelable(true);
                        progressDialog.setTitle("خطا");
                        progressDialog.setMessage("فایل دانلود شده خالی است");

                    }
                },
                error -> {
                    Toast.makeText(mContext, "Could not download file: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    progressDialog.setView(null);
                    progressDialog.setCancelable(true);
                    progressDialog.setTitle("خطا");
                    progressDialog.setMessage("خطا در دانلود تصاویر، دوباره تلاش کنید\n" + error.getMessage());

                }
        );

        queue.add(request);
    }
    private void showSummaryDialog(int addCount, int updatedCount, int errorCount,String urlimages) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("لغت ها به درستی دریافت شدند. "+"دوست دارید تصاویر هم دانلود شوند؟");

        String message = "Summary:\n   Added: " + addCount +
                "\n   Updated: " + updatedCount +
                "\n   Errors: " + errorCount;

        builder.setMessage(message);
        builder.setCancelable(false);
        //if(urlimages.length()>0) {
            builder.setPositiveButton("دانلود تصاویر", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    try {
                        //GetImagesFromURL(urlimages);
                        controller.UpdateWordList();
                        controller.getWordImagesFromGithubSequential(mContext);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        //}

        builder.setNeutralButton("تمام", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


            }
        });
        builder.show();
    }


}