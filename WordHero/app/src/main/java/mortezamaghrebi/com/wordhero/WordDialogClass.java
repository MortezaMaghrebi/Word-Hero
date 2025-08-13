package mortezamaghrebi.com.wordhero;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class WordDialogClass extends Dialog {

    public UserActivity userActivity;
    Context context;
    Controller controller;
    Class activity;
    Boolean decreaseheart = false;
    wordItem witem;
    int RESULT_BROWSE_IMG = 111;

    public WordDialogClass(UserActivity a, wordItem witem, Controller controller) {
        super(a);
        // TODO Auto-generated constructor stub
        this.userActivity = a;
        this.witem = witem;
        this.context = a;
        this.controller = controller;
    }

    RelativeLayout btnok;
    TextView txtword, txtcontent;
    ImageView imgword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_word);
        Handler timerHandler = new Handler(Looper.getMainLooper());
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                btnok = (RelativeLayout) findViewById(R.id.lytok);
                txtword = (TextView) findViewById(R.id.txtword);
                txtcontent = (TextView) findViewById(R.id.txtcontent);
                txtword.setText(witem.word);
                txtcontent.setText(witem.pronounce + "\n" + witem.definition + "\n" + witem.persian + "\nExample: " + witem.example.replace("_______", witem.word) + "\n" + witem.examplefa);
                imgword = (ImageView) findViewById(R.id.imgword);
                txtword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        fetchPexelsImageAndShowDialog(witem.word,1);
                    }
                });
                txtword.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String url1 = null;
                        try {
                            url1 = "https://www.google.com/search?tbm=isch&q=" + URLEncoder.encode(witem.word + " meaning", "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
                        userActivity.startActivity(intent1);
                       return false;
                    }
                });
                imgword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserActivity.lastRequestedWord = witem.word;
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        ((Activity) userActivity).startActivityForResult(intent, RESULT_BROWSE_IMG);

                    }
                });
                try {
                    getImage(witem.word);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                try {
                    ((Dialog) WordDialogClass.this).getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                } catch (Exception e) {
                }
                btnok.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                btnok.setBackgroundResource(R.drawable.outline_button1b);
                                return true;
                            case MotionEvent.ACTION_CANCEL:
                                btnok.setBackgroundResource(R.drawable.outline_button1);
                                return true;
                            case MotionEvent.ACTION_UP:
                                btnok.setBackgroundResource(R.drawable.outline_button1);
                                WordDialogClass.this.dismiss();
                                return true;
                        }
                        return false;
                    }
                });
            }
        };
        timerHandler.postDelayed(timerRunnable, 20);
    }

    public void fetchPexelsImageAndShowDialog(String keyword, int page) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String apiKey = "zYWL9R9DssJTKwjxZYK0zZj3oZPXzPK2w2dmSkyFmZOkkTUKZ85LSXH4";
        String url = "https://api.pexels.com/v1/search?query=" + Uri.encode(keyword) + "&per_page=1&page=" + page;
        UserActivity.lastRequestedWord = keyword;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray photos = response.getJSONArray("photos");
                            JSONObject photo = null;

                            if (photos.length() > 1) {
                                // عکس دوم
                                photo = photos.getJSONObject(1);
                            } else if (photos.length() > 0) {
                                // فقط یک عکس بود، عکس اول رو بگیر
                                photo = photos.getJSONObject(0);
                            }

                            if (photo != null) {
                                JSONObject src = photo.getJSONObject("src");
                                String imageUrl = src.getString("medium"); // یا "original" برای کیفیت بالاتر

                                ImageRequest imageRequest = new ImageRequest(imageUrl,
                                        new Response.Listener<Bitmap>() {
                                            @Override
                                            public void onResponse(Bitmap bitmap) {
                                                showImageDialog(bitmap,page);
                                            }
                                        },
                                        0, 0, ImageView.ScaleType.CENTER_INSIDE,
                                        Bitmap.Config.ARGB_8888,
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(context, "Error loading image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                queue.add(imageRequest);
                            } else {
                                Toast.makeText(context, "No images found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "JSON parsing error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", apiKey);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }


    private void showImageDialog(Bitmap bitmap,int page) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);

        int padding = (int) (20 * context.getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);

        builder.setView(imageView);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap bit= controller.resizeImageToFitDatabase(bitmap);
                int min = Math.min(bit.getWidth(),bit.getHeight());
                if(min>=150) {
                    controller.setWordImageFromBase64(UserActivity.lastRequestedWord , controller.bitmapToBase64(bit));
                    Toast.makeText(context, "Confirmed!", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(context, "Image size too small", Toast.LENGTH_LONG).show();

            }
        });
        builder.setNeutralButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fetchPexelsImageAndShowDialog(UserActivity.lastRequestedWord,page+1);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    final String uri_getimage = "http://kingsofleitner.ir/words1100/webservice.php?get_command=getwordimage,";

    boolean getImage(final String word) throws UnsupportedEncodingException {
        Bitmap bit = controller.getWordImage(word);
        if (bit != null) {
            Display display = userActivity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            imgword.getLayoutParams().height = size.x / 2;
            imgword.getLayoutParams().width = size.x / 2;
            imgword.requestLayout();
            imgword.setImageBitmap(bit);
            return true;
        } else {
            // باز کردن گالری برای انتخاب عکس

            // RequestQueue queue = Volley.newRequestQueue(context);
            // StringRequest postRequest = new StringRequest(Request.Method.POST, uri_getimage + word,
            //         new Response.Listener<String>() {
            //             @Override
            //             public void onResponse(String response) {
            //                 try {
            //                     if (response.length() > 200) {
//
            //                         Bitmap bit = controller.setWordImage(word,response);
            //                         if(bit!=null) {
            //                             imgword.setImageBitmap(bit);
            //                             Display display = c.getWindowManager().getDefaultDisplay();
            //                             Point size = new Point();
            //                             display.getSize(size);
            //                             imgword.getLayoutParams().height = size.x / 2;
            //                             imgword.getLayoutParams().width = size.x / 2;
            //                             imgword.requestLayout();
            //                         }
            //                     }
            //                 } catch (Exception e) {
            //                     int a = 1;
            //                 }
            //             }
            //         },
            //         new Response.ErrorListener() {
            //             @Override
            //             public void onErrorResponse(VolleyError error) {
            //                 // error
            //                 //Toast.makeText(SecondActivity.this,"Error\n"+"Could not connect to server",Toast.LENGTH_LONG).show();
            //             }
            //         }
            // );
            // queue.add(postRequest);
        }
        return false;
    }

}