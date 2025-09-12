package mortezamaghrebi.com.wordhero;

import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;

import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Boolean entered=false;

    Boolean nouser=true;
    private int i = 0;
    private Handler mHandler = new Handler();
    RelativeLayout lytproga;
    RelativeLayout lytprogb;
    RelativeLayout btnstart;
    int width;
    final String uri = "http://kingsofleitner.ir/words1100/webservice.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnstart = (RelativeLayout)findViewById(R.id.btnstart);
        lytproga=(RelativeLayout)findViewById(R.id.lytproga);
        lytprogb=(RelativeLayout)findViewById(R.id.lytprogb);
        Controller controller=new Controller(MainActivity.this,false);


        try {
            create(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

       
    }

    private void create(Boolean offline) throws IOException {
        workoffline=offline;
         btnstart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnstart.setBackgroundResource(R.drawable.curved3);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        btnstart.setBackgroundResource(R.drawable.curved2);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btnstart.setBackgroundResource(R.drawable.curved2);
                        if(nouser) {
                            CustomDialogClass cdd=new CustomDialogClass(MainActivity.this);
                            cdd.show();
                        }
                        else if(!entered) {
                            Intent secondact = new Intent(MainActivity.this, SecondActivity.class);
                            startActivity(secondact);
                            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                            entered=true;
                            MainActivity.this.finish();
                        }
                        return true;
                }
                return false;
            }
        });

        Controller con= new Controller(MainActivity.this,false);
        if(con.gethearts()<10)con.addheart(10);
        if(con.getExir()<10) con.increaseExir(10);
        if(con.getPassword().equals(""))
        {
            con.setUser("Word Hero");
            con.setPassword("1234");
            con.setAvatar("");
        }else
        {
           int databaseVersion= con.getDatabaseVersion();
           if(databaseVersion==0)
           {
              DatabaseUpdater.recreateDatabase(MainActivity.this);
           }
        }
        con.setDatabaseVersion(3);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x*8/10;
            lytproga.getLayoutParams().width = width;
            lytproga.requestLayout();
            lytprogb.getLayoutParams().width = 0;
            lytprogb.requestLayout();
            progress=0;
            lytproga.setVisibility(View.VISIBLE);
            btnstart.setVisibility(View.INVISIBLE);
            mHandler = new Handler();
            mHandler.postDelayed(mUpdate,100);

    }

    public boolean ismessagesloaded=false;
    public boolean domessagesloaded=false;
    boolean workoffline=false;
    public int progress=0;
    private Runnable mUpdate = new Runnable() {
        public void run() {
            if(progress<=100) {
                try {
                    int wid = width * progress / 100;
                    lytprogb.getLayoutParams().width = wid;
                    lytprogb.requestLayout();
                    lytproga.getLayoutParams().width = width;
                    lytproga.requestLayout();
                    progress+=20;
                    if(progress>20 && !domessagesloaded){
                        try {
                            if(!workoffline) (new Controller(MainActivity.this,false)).loadGetLoading(MainActivity.this);
                            domessagesloaded=true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(!workoffline) if(progress>40 && !ismessagesloaded) progress=40;
                    mHandler = new Handler();
                    mHandler.postDelayed(mUpdate, 100);
                }catch (Exception e){}
            }
            else {
                if(!entered) {
                    Intent secondact = new Intent(MainActivity.this, SecondActivity.class);
                    startActivity(secondact);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    MainActivity.this.finish();
                    entered=true;
                }
            }

        }
    };
}
