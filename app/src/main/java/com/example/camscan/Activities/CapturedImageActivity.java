package com.example.camscan.Activities;
import  androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import  android.graphics.Bitmap;
import  android.graphics.BitmapFactory;
import  android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import  android.widget.ImageView;
import android.widget.Toast;

import com.example.camscan.ObjectClass.BitmapObject;
import com.example.camscan.R;
import com.squareup.picasso.Picasso;

public class CapturedImageActivity extends AppCompatActivity
{
    CardView top_card_view;
    ImageView flash_mode;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing_layout);
        getSupportActionBar().hide();
        top_card_view=findViewById(R.id.top_card_view);
        flash_mode=findViewById(R.id.flash_mode);
        flash_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view,true,R.style.MyPopupOtherStyle);
            }
        });

      //  top_card_view.setBackgroundResource(R.drawable.card_view_background);
    }
    private void showPopupMenu(View anchor, boolean isWithIcons, int style) {
        //init the wrapper with style
        Context wrapper = new ContextThemeWrapper(this, style);
        PopupMenu popup = new PopupMenu(wrapper, anchor);
        if (isWithIcons) {
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        assert menuPopupHelper != null;
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        popup.getMenuInflater().inflate(R.menu.flash_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.flash_auto:
                        Toast.makeText(CapturedImageActivity.this, "Flash Auto !", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.flash_on:
                        Toast.makeText(CapturedImageActivity.this, "Flash On !", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.flash_off:
                        Toast.makeText(CapturedImageActivity.this, "Flash Off !", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}
