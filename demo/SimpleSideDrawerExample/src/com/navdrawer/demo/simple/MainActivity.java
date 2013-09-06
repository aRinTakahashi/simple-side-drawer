package com.navdrawer.demo.simple;

import java.util.ArrayList;

import com.navdrawer.SimpleSideDrawer;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SimpleSideDrawer mNav;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNav = new SimpleSideDrawer(this);
        mNav.setLeftBehindContentView(R.layout.activity_behind_left_simple);
        findViewById(R.id.leftBtn).setOnClickListener(new OnClickListener() {
            @Override 
            public void onClick(View v) {
                mNav.toggleLeftDrawer();
            }
        });
        
        
        mNav.setRightBehindContentView(R.layout.activity_behind_right_simple);
        
        findViewById(R.id.rightBtn).setOnClickListener(new OnClickListener() {
            @Override 
            public void onClick(View v) {
                mNav.toggleRightDrawer();
            }
        });
        
        CheckBox check = (CheckBox) findViewById(android.R.id.checkbox);
        mNav.setDragOpenEnabled(check.isChecked());
        check.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                mNav.setDragOpenEnabled(checked);
            }
        });
        
        ArrayList<String> list = new ArrayList<String>();
        char c = '1';
        for (int i = 0; i < 20; i++) {
            list.add(Character.toString(c++));
        }
        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, list.toArray());
        ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(getApplicationContext(), parent.getAdapter().getItem(position).toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mNav.dispatchActivityTouchEvent(ev)) {
			return true;
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}

}