package mortezamaghrebi.com.wordhero;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DatasetsDialogClass extends Dialog  {

    Context context;
    Controller controller;

    public DatasetsDialogClass(Context c,Controller controller) {
        super(c);
        // TODO Auto-generated constructor stub
        this.context = c;
        this.context= c;
        this.controller=controller;
    }
     RelativeLayout btnok;
    ListView list;
    EditText search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_datasets);
        try {
            ((Dialog) DatasetsDialogClass.this).getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }catch (Exception e){}
        btnok = (RelativeLayout)findViewById(R.id.lytok);
        list = (ListView) findViewById(R.id.listdatasets);
        search=(EditText) findViewById(R.id.etSearch);
        controller.datasets_ListAdapter=new ListAdapterDatasets((Activity) context, R.layout.dataset_item,controller.datasets_List);
        list.setAdapter(controller.datasets_ListAdapter);


        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if(search.getText().toString().length()==0){
                    list.setAdapter(controller.datasets_ListAdapter);
                }else {
                    ArrayList<String> newlist = new ArrayList<String>();
                    for (int i = 0; i < controller.datasets_List.size(); i++) {
                        try {
                            String title = controller.datasets_List.get(i).split("--")[0];
                            if (title.toLowerCase().contains(search.getText().toString().toLowerCase())) {
                                newlist.add(controller.datasets_List.get(i));
                            }
                        } catch (Exception e) {
                        }
                    }
                    ListAdapterDatasets listAdapterDatasets = new ListAdapterDatasets((Activity) context, R.layout.dataset_item, newlist);
                    list.setAdapter(listAdapterDatasets);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });



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
                        DatasetsDialogClass.this.dismiss();
                        return true;
                }
                return false;
            }
        });
    }

}