package com.example.nahom.grocery;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.raizlabs.android.dbflow.config.FlowManager;

import java.util.ArrayList;





public class MainActivity extends Activity implements OnClickListener {
    private String listview_array[] = { "Food", "Gas", "Mortgage",
                                        "Electric", "Water",
                                        "Insurance", "Credit Cards", "Extras"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FlowManager.init(this);
        setContentView(R.layout.activity_main);


        final EditText myEditText = (EditText) findViewById(R.id.editText);
        final ArrayAdapter<String> adapter;
        final ArrayList<String> list = new ArrayList<>();
        final ListView myList;
        myList = (ListView) findViewById(R.id.listView);

        for (int i = 0; i < listview_array.length; i++) {
            list.add(listview_array[i]);
        }


        Button button = (Button) findViewById(R.id.button);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {
                        String Add = myEditText.getText().toString();
                        myEditText.setText("");
                        list.add(Add);
                        adapter.notifyDataSetChanged();
                        myList.setAdapter(adapter);
                    }
                }
        );

        myList.setAdapter(adapter);
        Helper.getListViewSize(myList);


    }
   

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {


        
    }
}


