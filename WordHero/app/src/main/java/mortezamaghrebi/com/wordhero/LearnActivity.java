package mortezamaghrebi.com.wordhero;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;

import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class LearnActivity extends AppCompatActivity {

    RelativeLayout lytques,lytans1,lytans2,lytans3,lytans4,lytproga,lytprogb,btnhelp1,btnhelp2,btnhelp3,btnnext,btneasy,lytactionbuttons,p1,p2,p3;
    LinearLayout lytwait,lytchoices,lytcontent,lythelps;
    TextView txtnum,txtques,txtweek,txtans1,txtans2,txtans3,txtans4,txtscore,txtanswer,txtdefinition,txtexample,txtnext,txtprogress;
    ImageView imgpron,imgwordimage,imgtap;
    Controller controller;
    int imagesize=0;
    int[] questionsIndex;
    String[] results;
    int currentQuestionIndex=0;
    int currentAnswerIndex=0;
    int corrects=0;
    int width=110;
    private Handler mHandler = new Handler();
    MediaPlayer mPlayer;
    boolean isclicked=false;
    Boolean used_help_a=false,help_a_used=false;
    Boolean used_help_b=false;
    Boolean used_help_c=false;
    int NumberOfQuestions=10;
    TextToSpeech tts;
    boolean ttsinit= false;
    boolean isproun=true;
    int timeperquestion;
    MediaPlayer mp_correct;
    MediaPlayer mp_wrong;
    MediaPlayer mp_win;
    MediaPlayer mp_lose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        int[] images = {
                R.drawable.theme1,
                R.drawable.theme2,
                R.drawable.theme3,
                R.drawable.theme4,
                R.drawable.theme5,
                R.drawable.theme6,
                R.drawable.theme7,
                R.drawable.theme8
        };
        int randomIndex = new Random().nextInt(images.length);
        backgroundImage.setImageResource(images[randomIndex]);

        Handler timerHandler = new Handler(Looper.getMainLooper());
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                initcontrols();
                getquestions();
                mp_correct = MediaPlayer.create(LearnActivity.this, R.raw.correct);
                mp_wrong = MediaPlayer.create(LearnActivity.this, R.raw.wrong);
                mp_win = MediaPlayer.create(LearnActivity.this, R.raw.win);
                mp_lose = MediaPlayer.create(LearnActivity.this, R.raw.lose);
                mp_correct.setVolume((float)(controller.getVolumeButtons()*0.7/100.0),(float)(controller.getVolumeButtons()*0.7/100.0));
                mp_wrong.setVolume((float)(controller.getVolumeButtons()*0.7/100.0),(float)(controller.getVolumeButtons()*0.7/100.0));
                mp_win.setVolume((float)(controller.getVolumeButtons()/100.0),(float)(controller.getVolumeButtons()/100.0));
                mp_lose.setVolume((float)(controller.getVolumeButtons()/100.0),(float)(controller.getVolumeButtons()/100.0));
                setPlayer();
                musiccreated=true;
                resumeMusic();
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    double getAngle(int dx,int dy) {
        double angle = 0;
        if (dy < 0 && dx == 0) angle = 0;
        else if (dx > 0 && dy == 0) angle = 90;
        else if (dy > 0 && dx == 0) angle = 180;
        else if (dx < 0 && dy == 0) angle = 270;
        else {
            angle = Math.atan(Math.abs(dx*1.0/dy)) * 180 / Math.PI;
            if (dy < 0 && dx > 0) angle = angle;
            else if (dy > 0 && dx > 0) angle = -angle + 180;
            else if (dy > 0 && dx < 0) angle = angle + 180;
            else if (dy < 0 && dx < 0) angle = -angle + 360;
        }
        return angle;
    }
    int percentt=0;
    void showResult()
    {
        lythelps.setVisibility(View.INVISIBLE);
        LinearLayout lytparent = (LinearLayout)findViewById(R.id.lytparent);
        LinearLayout lytresult = (LinearLayout)findViewById(R.id.lytresult);
        LinearLayout lytresultinner = (LinearLayout) findViewById(R.id.lytresultinner);
        final ImageView imgpercent = (ImageView)findViewById(R.id.imgpercent);
        final TextView txtpercent = (TextView) findViewById(R.id.txtpercent);
        final TextView txtreward = (TextView) findViewById(R.id.txtreward);
        RelativeLayout btnback =(RelativeLayout)findViewById(R.id.btnback);
        controller.increaseProgress(corrects);
        controller.addPlayResult(corrects*100.0/NumberOfQuestions);
        Bitmap bit = Bitmap.createBitmap(401, 401, Bitmap.Config.ARGB_8888);
        double z = 50/Math.PI;
        double percent=corrects*100/NumberOfQuestions;
        int r=200;
        int r1=130,r2=140,r3=190,r4=200;
        int r12=r1*r1,r22=r2*r2,r32=r3*r3,r42=r4*r4;
        int color;
        if(corrects>=controller.RewardExcelent) color=Color.argb(255,254,192,9);
        else if(corrects>=controller.RewardSoSo) color=Color.argb(255,95,186,107);
        else if(corrects<controller.RewardBad) color=Color.argb(255,231,115,115);
        else color=Color.argb(255, 101, 179, 245);
        for(int i=0;i<401;i++)
        {
            for(int j=0;j<401;j++)
            {
                double radius2 = (i-201)*(i-201)+(j-201)*(j-201);
                if(radius2>r12&&radius2<=r22||radius2>r32&&radius2<=r42) {
                    bit.setPixel(i,j, Color.argb(180, 255, 255, 255));
                }
                else if(radius2>r22&&radius2<r32) {
                    int dx = i - 201;
                    int dy = j - 201;
                    double per=getAngle(dx,dy);
                    if(per<=percent*36/10)
                    {
                        bit.setPixel(i,j, color);
                    }
                }
            }
        }
        imgpercent.setImageBitmap(bit);
        txtpercent.setText(""+percent+"%");
        txtpercent.setTextColor(color);
        mPlayer.setVolume((float)(controller.getVolumeGame()/300.0),(float)(controller.getVolumeGame()/300.0));
        if (corrects >= controller.RewardExcelent) {
            txtreward.setText("You won 2 drops of potion");
            txtreward.setTextColor(Color.argb(255,255,230,175));
            controller.increaseExir(2);
            mp_win.seekTo(0);mp_win.start();
        }
        else if (corrects >= controller.RewardSoSo) {
            txtreward.setText("You won one drop of potion");
            txtreward.setTextColor(Color.argb(255,201,230,202));
            controller.increaseExir(1);
        }
        else if(corrects<controller.RewardBad) {
            txtreward.setTextColor(Color.argb(255,255,205,210));
            txtreward.setText("You lost one drop of potion");
            controller.decreaseExir(1);
            mp_lose.seekTo(0);mp_lose.start();
        }
        else {
            txtreward.setText("Your potion didn't change");
            txtreward.setTextColor(Color.argb(255, 187, 222, 251));
        }
        int height = lytparent.getMeasuredHeight();
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.resultregular_grow);
        lytresult.getLayoutParams().height=height;
        lytresult.requestLayout();
        lytresultinner.startAnimation(animation);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LearnActivity.this.finish();
            }
        });
    }
    void setPlayer()
    {
        mPlayer = MediaPlayer.create(LearnActivity.this,R.raw.royalty_regularplay);
        if(mPlayer!= null)
        {
            mPlayer.setLooping(true);
            mPlayer.setVolume((float)(controller.getVolumeGame()/100.0),(float)(controller.getVolumeGame()/100.0));
        }
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            public boolean onError(MediaPlayer mp, int what, int
                    extra){

                onError(mPlayer, what, extra);
                return true;
            }
        });
    }
    int length=0;
    public void pauseMusic()
    {
        if(mPlayer.isPlaying())
        {
            mPlayer.pause();
            length=mPlayer.getCurrentPosition();

        }
    }
    Boolean paused=false;
    public void resumeMusic()
    {
        if(mPlayer.isPlaying()==false)
        {
            mPlayer.seekTo(length);
            mPlayer.start();
        }
    }
    public void stopMusic()
    {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    @Override
    protected void onPause() {
        paused=true;
        pauseMusic();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        paused=false;
        super.onPostResume();
    }

    Boolean musiccreated=false;
    @Override
    protected void onStart() {
        if(musiccreated) resumeMusic();
        super.onStart();
    }

    int[] pronsrc = {R.drawable.pron2,R.drawable.pron1};
    @SuppressLint("ClickableViewAccessibility")
    void initcontrols()
    {
        lytans1 = (RelativeLayout)findViewById(R.id.lytans1);
        lytans2 = (RelativeLayout)findViewById(R.id.lytans2);
        lytans3 = (RelativeLayout)findViewById(R.id.lytans3);
        lytans4 = (RelativeLayout)findViewById(R.id.lytans4);
        lytques = (RelativeLayout)findViewById(R.id.lytques);
        lytproga = (RelativeLayout)findViewById(R.id.lytproga);
        lytprogb = (RelativeLayout)findViewById(R.id.lytprogb);
        btnhelp1 = (RelativeLayout)findViewById(R.id.btnhelp1);
        btnhelp2 = (RelativeLayout)findViewById(R.id.btnhelp2);
        btnhelp3 = (RelativeLayout)findViewById(R.id.btnhelp3);
        btnnext = (RelativeLayout)findViewById(R.id.lytnext);
        btneasy = (RelativeLayout)findViewById(R.id.lyteasy);
        lytactionbuttons = (RelativeLayout)findViewById(R.id.lytactionbuttons);
        lytchoices = (LinearLayout) findViewById(R.id.lytchoices);
        lytwait = (LinearLayout) findViewById(R.id.lytwait);
        lytcontent = (LinearLayout) findViewById(R.id.lytcontent);
        lythelps = (LinearLayout) findViewById(R.id.lythelps);
        txtnum = (TextView)findViewById(R.id.txtnum);
        txtques= (TextView)findViewById(R.id.txtques);
        txtweek= (TextView)findViewById(R.id.txtweek);
        txtans1= (TextView)findViewById(R.id.txtans1);
        txtans2= (TextView)findViewById(R.id.txtans2);
        txtans3= (TextView)findViewById(R.id.txtans3);
        txtans4= (TextView)findViewById(R.id.txtans4);
        txtscore= (TextView)findViewById(R.id.txtscore);
        txtanswer= (TextView)findViewById(R.id.txtanswer);
        txtdefinition= (TextView)findViewById(R.id.txtdefinitoin);
        txtexample= (TextView)findViewById(R.id.txtexample);
        txtprogress = (TextView) findViewById(R.id.txtprogress);
        p1 = (RelativeLayout) findViewById(R.id.lytwordprogres1);
        p2 = (RelativeLayout) findViewById(R.id.lytwordprogres2);
        p3 = (RelativeLayout) findViewById(R.id.lytwordprogres3);
        txtnext= (TextView)findViewById(R.id.txtnext);
        imgpron = (ImageView)findViewById(R.id.imgpron);
        imgwordimage = (ImageView)findViewById(R.id.imgwordimage);
        imgtap = (ImageView)findViewById(R.id.imgtap);
        lytans1.setTag("0");
        lytans2.setTag("1");
        lytans3.setTag("2");
        lytans4.setTag("3");
        controller = new Controller(LearnActivity.this,true);
        NumberOfQuestions=controller.NumberOfQuestions;
        timeperquestion=controller.getTimeLearn();
        isproun=controller.getIsPronounce();
        imgpron.setImageResource(isproun?pronsrc[0]:pronsrc[1]);
        if(controller.wordItems.length>0 && controller.getIsLogin())
        {
            if (!controller.getIsUserWordsDownloaded()) {
                try {
                    controller.getSavedWordsOnline();
                } catch (Exception e) {
                    Toast.makeText(LearnActivity.this, "Could not connect to internet\nWe need to get your progress online", Toast.LENGTH_LONG).show();
                }
            }
        }
        if(controller.wordItems.length==0)
        {
            controller.setDBVersion(0);
            try {
                controller.checkVersionAndUpdate();
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(LearnActivity.this,"You didn't get words, please connect to internet and try again",Toast.LENGTH_LONG).show();
                LearnActivity.this.finish();
                e.printStackTrace();
            }
        }
        initTts();
        helpButtonsInit();
        answerButtonsInit();
        btnnext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnnext.setBackgroundResource(R.drawable.nextbuttonb);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        btnnext.setBackgroundResource(R.drawable.nextbutton);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btnnext.setBackgroundResource(R.drawable.nextbutton);
                        goNextQuestion();
                        return true;
                }
                return false;
            }
        });
        btneasy.setOnTouchListener(new View.OnTouchListener() {
            private boolean isLongPress = false;
            private GestureDetector gestureDetector = new GestureDetector(btneasy.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (!isLongPress) {
                        btneasy.setBackgroundResource(R.drawable.easybutton);
                        itsEasyQuestion();
                        goNextQuestion();
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    isLongPress = true;
                    btneasy.setBackgroundResource(R.drawable.easybutton);
                    itsVeryEasyFinishIt();
                    goNextQuestion();
                    //Toast.makeText(btneasy.getContext(), "Finish it!", Toast.LENGTH_SHORT).show();
                }
            });

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isLongPress = false;
                        btneasy.setBackgroundResource(R.drawable.easybuttonb);
                        gestureDetector.onTouchEvent(motionEvent);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        btneasy.setBackgroundResource(R.drawable.easybutton);
                        isLongPress = false;
                        return true;
                    case MotionEvent.ACTION_UP:
                        gestureDetector.onTouchEvent(motionEvent);
                        if (!isLongPress) {
                            // اگر لانگ کلیک نبود، خودتان اقدامات لازم را انجام دهید
                        }
                        return true;
                }
                return false;
            }
        });

        imgpron.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //isproun=!isproun;
                //controller.setIsPronounce(isproun);
                //imgpron.setImageResource(isproun?pronsrc[0]:pronsrc[1]);
                if(ttsinit&&isproun) tts.speak(controller.wordItems[questionsIndex[currentQuestionIndex]].word + "!?", TextToSpeech.QUEUE_ADD, null);
            }
        });
       try {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x*88/100;
            //lytactionbuttons.getLayoutParams().height = 134 * size.x / 720;
            //lytactionbuttons.requestLayout();
            //lytques.getLayoutParams().height = size.y * 386 / 1184;
            //lytques.requestLayout();
            //lytproga.getLayoutParams().width = width;
            //lytproga.requestLayout();
            //lytprogb.getLayoutParams().width = 0;
            //lytprogb.requestLayout();
        }catch (Exception e){}
        imgwordimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                fetch_current_word=controller.wordItems[questionsIndex[currentQuestionIndex]].word;
                fetchPexelsImageAndShowDialog(fetch_current_word,fetch_current_page,false);
                fetch_current_page++;

            }
        });
        imgwordimage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(LearnActivity.this,"Reverting Image...",Toast.LENGTH_SHORT).show();
                fetch_current_word=controller.wordItems[questionsIndex[currentQuestionIndex]].word;
                fetch_current_page=1;
                fetchPexelsImageAndShowDialog(fetch_current_word,fetch_current_page,false);
                return false;
            }
        });
    }

    void itsEasyQuestion()
    {
        try {
            controller.wordItems[questionsIndex[currentQuestionIndex]].review += "z";
            wordItem wi = controller.wordItems[questionsIndex[currentQuestionIndex]];
            controller.myDB.UpdateWordReview(wi.id, wi.review, wi.lastheart);
        } catch (Exception e) {
            Toast.makeText(LearnActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    void itsVeryEasyFinishIt()
    {
        try {

            controller.wordItems[questionsIndex[currentQuestionIndex]].review += "k";
            wordItem wi = controller.wordItems[questionsIndex[currentQuestionIndex]];
            controller.myDB.UpdateWordReview(wi.id, wi.review, wi.lastheart);
        } catch (Exception e) {
            Toast.makeText(LearnActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    void goNextQuestion()
    {
        try {
            currentQuestionIndex++;
            if (currentQuestionIndex == NumberOfQuestions) {
                showResult();
            } else {
                if(helpheight>0) {
                    lythelps.getLayoutParams().height = helpheight;
                    lythelps.requestLayout();
                }
                lythelps.setVisibility(View.VISIBLE);
                int height = lytcontent.getMeasuredHeight()-helpheight;
                lytwait.getLayoutParams().height =1;
                lytwait.requestLayout();
                lytchoices.getLayoutParams().height = height;
                lytchoices.requestLayout();
                showNextQuestion();
            }
        }catch (Exception e){
            Toast.makeText(LearnActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    void getquestions()
    {
        if(controller.wordItems.length==0)
        {
            controller.addheart(1);
            AlertDialog alertDialog = new AlertDialog.Builder(LearnActivity.this).create();
            alertDialog.setMessage("You didn't get words,if download didn't start automatically, please connect to internet and try again.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else {

            questionsIndex = controller.GetQuestionsIndex(controller.NumberOfQuestions);
            results = new String[NumberOfQuestions];
            controller.inceaseHeartId();
            currentQuestionIndex = 0;
            (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                @Override
                public void run() {
                    showNextQuestion();
                }
            },700);
        }
    }

    int time=timeperquestion*100;
    private Runnable mUpdate = new Runnable() {
        public void run() {
            if(time>1)
            {
                lytprogb.getLayoutParams().width = (time*width/(timeperquestion*100));
                lytprogb.requestLayout();
                time-=2;
                mHandler = new Handler();
                mHandler.postDelayed(mUpdate, 10);}
            else
            {
                lytprogb.getLayoutParams().width = 0;
                lytprogb.requestLayout();
                if(currentAnswerIndex==0) lytans1.setBackgroundResource(R.drawable.curve_answrb1);
                else if(currentAnswerIndex==1) lytans2.setBackgroundResource(R.drawable.curve_answrb1);
                else if(currentAnswerIndex==2) lytans3.setBackgroundResource(R.drawable.curve_answrb1);
                else if(currentAnswerIndex==3) lytans4.setBackgroundResource(R.drawable.curve_answrb1);
                isclicked=true;
                HandleNextQuestion(500);
            }

        }
    };

    void showNextQuestion()
    {
            Random rnd = new Random();
            wordItem item=controller.wordItems[questionsIndex[currentQuestionIndex]];

            //txtques.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].word + "?");
            List<Integer> list;
            list = new ArrayList<Integer>();
            list.add(questionsIndex[currentQuestionIndex]);
            while(list.size()<2)
            {
                int r= rnd.nextInt(questionsIndex.length);
                if(!list.contains(questionsIndex[r]))list.add(questionsIndex[r]);
            }
            while(list.size()<4)
            {
                int r= rnd.nextInt(controller.wordItems.length);
                if(!list.contains(r))list.add(r);
            }
            List<Integer> alist;
            alist = new ArrayList<Integer>();
            while(alist.size()<4)
            {
                int r= rnd.nextInt(4);
                if(!alist.contains(list.get(r)))alist.add(list.get(r));
            }
            imgwordimage.getLayoutParams().height=0;
            imgwordimage.requestLayout();
            showimage=false;
            currentAnswerIndex = alist.indexOf(questionsIndex[currentQuestionIndex]);
            if(item.box()<7)
            {
                txtques.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].word + "?");
                txtans1.setText(controller.wordItems[alist.get(0)].persian);
                txtans2.setText(controller.wordItems[alist.get(1)].persian);
                txtans3.setText(controller.wordItems[alist.get(2)].persian);
                txtans4.setText(controller.wordItems[alist.get(3)].persian);
                if(item.box()<3)
                {
                    txtques.setTextColor(Color.parseColor("#4169E1"));
                    try {
                        showimage=true;
                        getImage(controller.wordItems[questionsIndex[currentQuestionIndex]].word,true);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else
                    txtques.setTextColor(Color.parseColor("#000000"));
                if(ttsinit&&isproun) tts.speak(controller.wordItems[questionsIndex[currentQuestionIndex]].word + "!?", TextToSpeech.QUEUE_ADD, null);

            }else if(item.box()<12)
            {
                txtques.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].persian + "؟");
                txtans1.setText(controller.wordItems[alist.get(0)].word);
                txtans2.setText(controller.wordItems[alist.get(1)].word);
                txtans3.setText(controller.wordItems[alist.get(2)].word);
                txtans4.setText(controller.wordItems[alist.get(3)].word);

            }else if(item.box()<13)
            {
                txtques.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].word + "?");
                txtans1.setText(controller.wordItems[alist.get(0)].definition);
                txtans2.setText(controller.wordItems[alist.get(1)].definition);
                txtans3.setText(controller.wordItems[alist.get(2)].definition);
                txtans4.setText(controller.wordItems[alist.get(3)].definition);
                if(ttsinit&&isproun) tts.speak(controller.wordItems[questionsIndex[currentQuestionIndex]].word + "!?", TextToSpeech.QUEUE_ADD, null);

            }else if(item.box()<14)
            {
                txtques.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].persian + "؟");
                txtans1.setText(controller.wordItems[alist.get(0)].definition);
                txtans2.setText(controller.wordItems[alist.get(1)].definition);
                txtans3.setText(controller.wordItems[alist.get(2)].definition);
                txtans4.setText(controller.wordItems[alist.get(3)].definition);
            }else if(item.box()<14)
            {
                txtques.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].definition + "?");
                txtans1.setText(controller.wordItems[alist.get(0)].word);
                txtans2.setText(controller.wordItems[alist.get(1)].word);
                txtans3.setText(controller.wordItems[alist.get(2)].word);
                txtans4.setText(controller.wordItems[alist.get(3)].word);
                if(ttsinit&&isproun) tts.speak(controller.wordItems[questionsIndex[currentQuestionIndex]].definition + "!?", TextToSpeech.QUEUE_ADD, null);

            }else if (item.box()<15)
            {
                String question=controller.wordItems[questionsIndex[currentQuestionIndex]].example.toLowerCase();
                String[] words = controller.wordItems[questionsIndex[currentQuestionIndex]].word.toLowerCase().split(" ");
                for (String word:words) {
                    question= question.replace(word,"#");
                }
                String[] questionwords = question.split(" ");
                String[] realQuestionwords=controller.wordItems[questionsIndex[currentQuestionIndex]].example.split(" ");
                for(int i=0;i<questionwords.length;i++) if(questionwords[i].contains("#")) questionwords[i]="_____";
                question="";
                for(int i=0;i<questionwords.length;i++)
                {
                    if(questionwords[i].contains("____"))question+=questionwords[i]+" ";
                    else question+=realQuestionwords[i]+" ";
                }
                while (question.contains("_____ _____"))question=question.replace("_____ _____","_____");
                txtques.setText(question.trim() + "?");
                txtans1.setText(controller.wordItems[alist.get(0)].word);
                txtans2.setText(controller.wordItems[alist.get(1)].word);
                txtans3.setText(controller.wordItems[alist.get(2)].word);
                txtans4.setText(controller.wordItems[alist.get(3)].word);
                if(ttsinit&&isproun) tts.speak(question + "!?", TextToSpeech.QUEUE_ADD, null);

            }

            int day=controller.wordItems[questionsIndex[currentQuestionIndex]].day;
            int dayis=((day-1)%4)+1;
            int weekis=(int)((day-1)/4)+1;
            txtweek.setText("هفته: "+weekis+"، روز: "+dayis);
            txtnum.setText("سوال: "+(currentQuestionIndex+1)+"/"+NumberOfQuestions);
            lytans1.setBackgroundResource(R.drawable.curve_answra1);
            lytans2.setBackgroundResource(R.drawable.curve_answra1);
            lytans3.setBackgroundResource(R.drawable.curve_answra1);
            lytans4.setBackgroundResource(R.drawable.curve_answra1);
            lytans1.setVisibility(View.VISIBLE);
            lytans2.setVisibility(View.VISIBLE);
            lytans3.setVisibility(View.VISIBLE);
            lytans4.setVisibility(View.VISIBLE);
            isclicked = false;
            used_help_a = used_help_b = used_help_c = help_a_used = false;
            btnhelp1.setEnabled(true);
            btnhelp2.setEnabled(true);
            btnhelp3.setEnabled(true);
            btnhelp1.setBackgroundResource(R.drawable.helpx2);
            btnhelp2.setBackgroundResource(R.drawable.help2r);
            btnhelp3.setBackgroundResource(R.drawable.helpshow);
            time=(timeperquestion*100)-1;
            mHandler = new Handler();
            mHandler.postDelayed(mUpdate, 100);
    }

    void initTts()
    {
        try {
            tts = new TextToSpeech(LearnActivity.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = tts.setLanguage(Locale.US);
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        } else ttsinit = true;
                    }
                }
            });
        }catch (Exception e){ttsinit=false;}
    }

    void answerButtonsInit()
    {
        View.OnClickListener lytansClick= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if ((!isclicked) || (used_help_a && help_a_used)) {
                        if (currentAnswerIndex == Integer.parseInt(view.getTag().toString())) {
                            String s = "";
                            ((RelativeLayout) view).setBackgroundResource(R.drawable.curve_answrb1);
                            if (used_help_a) s = "a";
                            else if (used_help_b) s = "b";
                            else if (used_help_c) s = "c";
                            else s = "t";
                            controller.wordItems[questionsIndex[currentQuestionIndex]].review += s;
                            controller.wordItems[questionsIndex[currentQuestionIndex]].lastheart = controller.getHeartId();
                            wordItem wi = controller.wordItems[questionsIndex[currentQuestionIndex]];
                            controller.myDB.UpdateWordReview(wi.id, wi.review, wi.lastheart);
                            corrects++;
                            mp_correct.seekTo(0);
                            mp_correct.start();
                            HandleNextQuestion(100);
                            used_help_a = false;
                        } else {
                            ((RelativeLayout) view).setBackgroundResource(R.drawable.curve_answrc1);
                            if (used_help_a && help_a_used) used_help_a = false;
                            if (used_help_a) help_a_used = true;
                            else {
                                String s = "f";
                                controller.wordItems[questionsIndex[currentQuestionIndex]].review += s;
                                controller.wordItems[questionsIndex[currentQuestionIndex]].lastheart = controller.getHeartId();
                                wordItem wi = controller.wordItems[questionsIndex[currentQuestionIndex]];
                                controller.myDB.UpdateWordReview(wi.id, wi.review, wi.lastheart);
                                if(currentAnswerIndex==0) lytans1.setBackgroundResource(R.drawable.curve_answrb1);
                                else if(currentAnswerIndex==1) lytans2.setBackgroundResource(R.drawable.curve_answrb1);
                                else if(currentAnswerIndex==2) lytans3.setBackgroundResource(R.drawable.curve_answrb1);
                                else if(currentAnswerIndex==3) lytans4.setBackgroundResource(R.drawable.curve_answrb1);
                                mp_wrong.seekTo(0);
                                mp_wrong.start();
                                HandleNextQuestion(500);
                            }
                        }
                        isclicked = true;
                    }
                }catch (Exception e){
                    Toast.makeText(LearnActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        };
        lytans1.setOnClickListener(lytansClick);
        lytans2.setOnClickListener(lytansClick);
        lytans3.setOnClickListener(lytansClick);
        lytans4.setOnClickListener(lytansClick);
    }

    int helpheight=0;
    void HandleNextQuestion (int delay)
    {
        mHandler.removeCallbacks(mUpdate);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView shown = (TextView) findViewById(R.id.txtshown);


                int height = lytcontent.getMeasuredHeight();
                helpheight= lythelps.getMeasuredHeight();
                lythelps.getLayoutParams().height=0;
                lythelps.requestLayout();
                lythelps.setVisibility(View.INVISIBLE);
                lytwait.getLayoutParams().height = height+helpheight;
                lytwait.requestLayout();
                lytchoices.getLayoutParams().height = 1;
                lytchoices.requestLayout();
                txtscore.setText("تعداد پاسخ صحیح: "+corrects);
                wordItem item =controller.wordItems[questionsIndex[currentQuestionIndex]];
                if(item.box()<7)
                {
                    txtanswer.setText(item.persian);
                }else if(item.box()<12)
                {
                    txtanswer.setText(item.word);
                    if(ttsinit&&isproun) tts.speak(item.word + "!?", TextToSpeech.QUEUE_ADD, null);
                }else if(item.box()<13)
                {
                    txtanswer.setText(item.definition+"\n"+item.persian);
                }else if(item.box()<14)
                {
                    txtanswer.setText(item.definition+"\n"+item.persian);
                }else if(item.box()<14)
                {
                    txtanswer.setText(item.word);
                    if(ttsinit&&isproun) tts.speak(item.word + "!?", TextToSpeech.QUEUE_ADD, null);
                }else if (item.box()<15)
                {
                    txtanswer.setText(item.word+"\n"+item.persian);
                }
                txtanswer.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].persian);
                txtdefinition.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].definition);
                txtexample.setText(controller.wordItems[questionsIndex[currentQuestionIndex]].example+"\n"+controller.wordItems[questionsIndex[currentQuestionIndex]].examplefa);
                //txtanswer.setTextSize(25);
                txtprogress.setText("پیشرفت لغت: "+(controller.wordItems[questionsIndex[currentQuestionIndex]].box()*100/15)+"%");
                shown.setText(""+controller.wordItems[questionsIndex[currentQuestionIndex]].review.length()+" times shown");
                int maxwidth=p1.getMeasuredWidth();
                int greenwitdh=maxwidth*controller.wordItems[questionsIndex[currentQuestionIndex]].box()/15;
                int redwidth =greenwitdh*controller.wordItems[questionsIndex[currentQuestionIndex]].wrongpercent()/1000;
                p2.getLayoutParams().width =greenwitdh;
                p3.getLayoutParams().width =redwidth;
                p1.requestLayout();p2.requestLayout();p3.requestLayout();

                try {
                    showimage=true;
                    getImage(controller.wordItems[questionsIndex[currentQuestionIndex]].word,false);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if(currentQuestionIndex==NumberOfQuestions-1)txtnext.setText("نتیجه");
                fetch_current_page=2;


            }
        }, delay);
    }

    String fetch_current_word="";
    int fetch_current_page=0;
    void helpButtonsInit()
    {
        btnhelp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnhelp1.setEnabled(false);
                btnhelp2.setEnabled(false);
                btnhelp3.setEnabled(false);
                btnhelp1.setBackgroundResource(R.drawable.helpx2b);
                used_help_a=true;
                help_a_used=false;
            }
        });
        btnhelp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnhelp1.setEnabled(false);
                btnhelp2.setEnabled(false);
                btnhelp3.setEnabled(false);
                Random rnd = new Random();
                int r=rnd.nextInt(3);
                int y=-1;
                for(int i=0;i<=r;i++) {
                    if (i == currentAnswerIndex) {y++;}
                    y++;
                }
                if(!(y==0 ^ currentAnswerIndex==0))
                    lytans1.setVisibility(View.INVISIBLE);
                if(!(y==1 ^ currentAnswerIndex==1))
                    lytans2.setVisibility(View.INVISIBLE);
                if(!(y==2 ^ currentAnswerIndex==2))
                    lytans3.setVisibility(View.INVISIBLE);
                if(!(y==3 ^ currentAnswerIndex==3))
                    lytans4.setVisibility(View.INVISIBLE);
                btnhelp2.setBackgroundResource(R.drawable.help2rb);
                used_help_b=true;
            }
        });
        btnhelp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnhelp1.setEnabled(false);
                btnhelp2.setEnabled(false);
                btnhelp3.setEnabled(false);
                if(currentAnswerIndex==0) lytans1.setBackgroundResource(R.drawable.curve_answrd1);
                else if(currentAnswerIndex==1) lytans2.setBackgroundResource(R.drawable.curve_answrd1);
                else if(currentAnswerIndex==2) lytans3.setBackgroundResource(R.drawable.curve_answrd1);
                else if(currentAnswerIndex==3) lytans4.setBackgroundResource(R.drawable.curve_answrd1);
                btnhelp3.setBackgroundResource(R.drawable.helpshowb);
                used_help_c=true;
            }
        });
    }


    @Override
    protected void onDestroy() {
        try {
            mp_correct.stop();
            mp_wrong.stop();
            mp_lose.stop();
            mp_win.stop();
            mp_correct.release();
            mp_wrong.release();
            mp_lose.release();
            mp_win.release();
        }catch (Exception e){}
        try {
            tts.shutdown();
        }catch (Exception e){}
        stopMusic();
        super.onDestroy();
    }
    final String uri_getimage = "http://kingsofleitner.ir/words1100/webservice.php?get_command=getwordimage,";
    Boolean showimage=false;
    boolean imagetapshown=false;
    void getImage(final String word,boolean isquestion) throws UnsupportedEncodingException {
        Bitmap bit =controller.getWordImage(word);
        if(bit!=null && showimage) {
            imgwordimage.setImageBitmap(bit);
            if(isquestion) imgwordimage.getLayoutParams().height=(int)(imgwordimage.getMeasuredWidth()*0.8);
            else imgwordimage.getLayoutParams().height=imgwordimage.getMeasuredWidth();
            imgwordimage.requestLayout();
            if(!imagetapshown) {
                imagetapshown=true;
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (controller.getImageTapShown() % 5 == 0) {
                            Animation connectingAnimation = AnimationUtils.loadAnimation(LearnActivity.this, R.anim.tap);
                            imgtap.setVisibility(View.VISIBLE);
                            imgtap.startAnimation(connectingAnimation);
                            imgtap.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    imgtap.setVisibility(View.INVISIBLE);
                                }
                            });
                            (new Handler()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    imgtap.setVisibility(View.INVISIBLE);
                                }
                            }, 1500);
                        }
                        controller.setImageTapShown();
                    }
                }, 700);
            }
        }else {
            fetch_current_page=1;
            fetch_current_word=word;
            fetchPexelsImageAndShowDialog(word,1,isquestion);
        }
    }

    public void fetchPexelsImageAndShowDialog(String keyword, int page,boolean isqestion) {

        RequestQueue queue = Volley.newRequestQueue(LearnActivity.this);
        String apiKey = "zYWL9R9DssJTKwjxZYK0zZj3oZPXzPK2w2dmSkyFmZOkkTUKZ85LSXH4";
        String url = "https://api.pexels.com/v1/search?query=" + Uri.encode(keyword) + "&per_page=1&page=" + page;
        UserActivity.lastRequestedWord = keyword;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            errortimefetch=0;
                            JSONArray photos = response.getJSONArray("photos");
                            JSONObject photo = null;

                            if (photos.length() > 1) {

                                photo = photos.getJSONObject(1);
                            } else if (photos.length() > 0) {

                                photo = photos.getJSONObject(0);
                            }

                            if (photo != null) {
                                JSONObject src = photo.getJSONObject("src");
                                String imageUrl = src.getString("medium"); // یا "original" برای کیفیت بالاتر

                                ImageRequest imageRequest = new ImageRequest(imageUrl,
                                        new Response.Listener<Bitmap>() {
                                            @Override
                                            public void onResponse(Bitmap bitmap) {
                                                showImageDialog(bitmap,page,isqestion);
                                            }
                                        },
                                        0, 0, ImageView.ScaleType.CENTER_INSIDE,
                                        Bitmap.Config.ARGB_8888,
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                               // Toast.makeText(LearnActivity.this, "Error loading image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                queue.add(imageRequest);
                            } else {
                                //Toast.makeText(LearnActivity.this, "No images found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                           // Toast.makeText(LearnActivity.this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                       // Toast.makeText(LearnActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();

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
    int errortimefetch=0;
    private void showImageDialog(Bitmap bitmap,int page,boolean isquestion) {
        String word = fetch_current_word;
        Bitmap bit = controller.resizeImageToFitDatabase(bitmap);
        int min = Math.min(bit.getWidth(), bit.getHeight());
        if (min >= 150) {
            controller.setWordImageFromBase64(UserActivity.lastRequestedWord, controller.bitmapToBase64(bit));
            Bitmap bit1 =controller.getWordImage(word);
            if(bit1!=null && showimage) {
                //controller.getWordImage(fetch_current_word);
                imgwordimage.setImageBitmap(controller.getWordImage(fetch_current_word));
                if(isquestion) imgwordimage.getLayoutParams().height=(int)(imgwordimage.getMeasuredWidth()*0.8);
                else imgwordimage.getLayoutParams().height=imgwordimage.getMeasuredWidth();
                imgwordimage.requestLayout();
                //Toast.makeText(LearnActivity.this,"Image changed",Toast.LENGTH_SHORT).show();

            }
        } else
            Toast.makeText(LearnActivity.this, "Image size too small", Toast.LENGTH_LONG).show();


    }
}
