package com.tam.winati.ksa;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.balram.library.FotButton;
import com.balram.library.FotTextView;
import com.models.TimingListShowModel;
import com.models.TimingModel;
import com.utils.Functions;
import com.utils.SharePrefsEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DeliveryTimeSelectionActivity extends BaseActivity {

    GridLayout gvDateLayout;
    GridLayout gvTimeLayout, gvTypeLayout;
    FotTextView selectedPreviousDateView = null;
    FotTextView selectedPreviousTimeView = null;
    FotTextView selectedPreviousTypeView = null;
    FotTextView tvNoDateAvailable = null;
    FotTextView tvNoTimeAvailable = null;
    FotTextView tvNoTypeAvailable = null;


    TimingModel currentModel;
    SharePrefsEntry sp;
    Functions function;
    SimpleDateFormat dateFormatter;

    String selectedDate = null;
    String selectedTime = null;
    String selectedType = null;

    HashMap<String, ArrayList<TimingListShowModel>> dateTimeMap = new HashMap<>();

    ArrayList<String> TypeDelivery = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_time_selection);

        gvDateLayout = findViewById(R.id.gvDateLayout);
        gvTimeLayout = findViewById(R.id.gvTimeLayout);
        gvTypeLayout = findViewById(R.id.gvTypeLayout);
        tvNoTypeAvailable = findViewById(R.id.tvNoTypeAvailable);
        tvNoDateAvailable = findViewById(R.id.tvNoDateAvailable);
        tvNoTimeAvailable = findViewById(R.id.tvNoTimeAvailable);
        TypeDelivery.add(" Delivery Now ");
        TypeDelivery.add(" Delivery Later ");
        TypeDelivery.add(" Pickup ");


        initialize();
        parseDateTimeData();
        parseTimeData();
        drawType();
        checkDateAndTimeFilledOrNot();


    }


    private void checkDateAndTimeFilledOrNot() {

        if (gvTypeLayout.getChildCount() > 0)
            tvNoTypeAvailable.setVisibility(View.GONE);
        else
            tvNoTypeAvailable.setVisibility(View.VISIBLE);

        if(gvDateLayout.getChildCount() > 0)
            tvNoDateAvailable.setVisibility(View.GONE);
        else
            tvNoDateAvailable.setVisibility(View.VISIBLE);

        if(gvTimeLayout.getChildCount() > 0)
            tvNoTimeAvailable.setVisibility(View.GONE);
        else
            tvNoTimeAvailable.setVisibility(View.VISIBLE);

    }

    private void initialize(){
        currentModel        = (TimingModel) getIntent().getSerializableExtra("currentTimeObject");
        sp                  = new SharePrefsEntry(this);
        function            = new Functions(this);
//        dateFormatter       = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        dateFormatter       = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH);

    }


    private void parseDateTimeData() {

        for (int index = 0;index < currentModel.getDateList().size(); index++) {

            String formattedDate        = dateFormatter.format(currentModel.getDateList().get(index).getTime());
            String splittedTime[]       = formattedDate.split(" ", 2);

            if(!dateTimeMap.containsKey(splittedTime[0])){
                ArrayList<TimingListShowModel> timeArrayList = new ArrayList<>();
                timeArrayList.add(new TimingListShowModel(splittedTime[1], splittedTime[1]));
                dateTimeMap.put(splittedTime[0], timeArrayList);
            }else {
                ArrayList<TimingListShowModel> timeArrayList = dateTimeMap.get(splittedTime[0]);
                timeArrayList.add(new TimingListShowModel(splittedTime[1], splittedTime[1]));
                dateTimeMap.put(splittedTime[0], timeArrayList);
            }
        }
    }

    private void parseTimeData() {
        for (Map.Entry<String, ArrayList<TimingListShowModel>> entry : dateTimeMap.entrySet()) {

            final String key = entry.getKey();
            final ArrayList<TimingListShowModel> timeList = entry.getValue();

            for (int index = 0, index2 = 1; index < timeList.size(); index++, index2++) {

                if(index2 == timeList.size())
                    timeList.get(index).setShowTime(timeList.get(index).getActualTime() + " - " + "12:00 PM");
                else
                    timeList.get(index).setShowTime(timeList.get(index).getActualTime() + " - " + timeList.get(index2).getActualTime());

            }
        }
    }

    private void drawType(){
        for (int index = 0; index < TypeDelivery.size(); index++) {

            final FotTextView fotTextView = createTextLabel(TypeDelivery.get(index));
            final int finalIndex = index;
            fotTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gvDateLayout.removeAllViews();
                    gvTimeLayout.removeAllViews();
                    if(selectedPreviousTypeView != null)
                        selectedPreviousTypeView.setBackground(ContextCompat.getDrawable(DeliveryTimeSelectionActivity.this, R.drawable.delivery_date_unselected));

                    fotTextView.setBackground(ContextCompat.getDrawable(DeliveryTimeSelectionActivity.this, R.drawable.delivery_date_selected));
                    selectedPreviousTypeView = fotTextView;
                    selectedType = TypeDelivery.get(finalIndex);

                    if (selectedType.equals(" Delivery Later ")){
                        drawDate();
                        checkDateAndTimeFilledOrNot();
                    }
                    else{
                        tvNoTimeAvailable.setVisibility(View.VISIBLE);
                        tvNoDateAvailable.setVisibility(View.VISIBLE);
                        tvNoDateAvailable.setText("Not Applicable");
                        tvNoTimeAvailable.setText("Not Applicable");
                        selectedDate=null;
                        selectedTime=null;
                    }
                }
            });

            gvTypeLayout.addView(fotTextView);
        }
    }



    private void drawDate() {


        for (Map.Entry<String, ArrayList<TimingListShowModel>> entry : dateTimeMap.entrySet()) {

            final String key = entry.getKey();
            final ArrayList<TimingListShowModel> value = entry.getValue();
            final FotTextView fotTextView = createTextLabel(key);
            fotTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedPreviousDateView != null)
                        selectedPreviousDateView.setBackground(ContextCompat.getDrawable(DeliveryTimeSelectionActivity.this, R.drawable.delivery_date_unselected));

                    fotTextView.setBackground(ContextCompat.getDrawable(DeliveryTimeSelectionActivity.this, R.drawable.delivery_date_selected));

                    selectedPreviousDateView = fotTextView;
                    selectedDate = fotTextView.getText().toString();
                    drawTime(value);
                    checkDateAndTimeFilledOrNot();

                }
            });

            gvDateLayout.addView(fotTextView);
        }

    }


    private void drawTime(final ArrayList<TimingListShowModel> timeList) {


        gvTimeLayout.removeAllViews();
        for (int index = 0; index < timeList.size(); index++) {

            final FotTextView fotTextView = createTextLabel(timeList.get(index).getShowTime());

            final int finalIndex = index;
            fotTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(selectedPreviousTimeView != null)
                        selectedPreviousTimeView.setBackground(ContextCompat.getDrawable(DeliveryTimeSelectionActivity.this, R.drawable.delivery_date_unselected));

                    fotTextView.setBackground(ContextCompat.getDrawable(DeliveryTimeSelectionActivity.this, R.drawable.delivery_date_selected));
//                    selectedTime = fotTextView.getText().toString();
                    selectedTime = timeList.get(finalIndex).getActualTime();
                    selectedPreviousTimeView = fotTextView;
                    checkDateAndTimeFilledOrNot();
                }
            });

            gvTimeLayout.addView(fotTextView);
        }
    }


    public FotTextView createTextLabel(String text){

        final FotTextView fotTextView = new FotTextView(this);


        fotTextView.setWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 95, getResources().getDisplayMetrics()));
        fotTextView.setGravity(Gravity.CENTER);
        fotTextView.setSingleLine();
        fotTextView.setTextColor(ContextCompat.getColor(this, R.color.profile_hint));
        fotTextView.setCustomFont(this, "Exo2-Regular.otf");
        int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        fotTextView.setPadding(0, padding, 0, padding);
        //int textSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 7, getResources().getDisplayMetrics());
        fotTextView.setTextSize(10);
        fotTextView.setBackground(ContextCompat.getDrawable(this, R.drawable.delivery_date_unselected));
        GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams();

        int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
        gridParam.setMargins(margin,margin,margin,margin);
        fotTextView.setLayoutParams(gridParam);
        fotTextView.setClickable(true);
        fotTextView.setText(text);

        return fotTextView;
    }


    public void backtoCart(View v){
        finish();
    }

    public void saveSetting(View v){

        if(selectedDate != null && selectedTime != null && selectedType != null){
            String formattedDate = null;
            try {
                SimpleDateFormat inFormat = new SimpleDateFormat("hh:mm aa");
                SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm:ss");
                String time24 = outFormat.format(inFormat.parse(selectedTime));

                formattedDate = selectedDate + " " +time24;
            } catch (ParseException e) {
                e.printStackTrace();
            }



            ViewCart.deliveryTime.put(function.generateCartKey(currentModel.getStoreID() , currentModel.getLocID()), formattedDate);
            try
            {
                String delTime     = ViewCart.deliveryTime.get(function.generateCartKey(currentModel.getStoreID() ,currentModel.getLocID()));
                String splittedTime[]       = delTime.split(" ");
                ViewCart.deliverytmtxt.setText(splittedTime[1]);
//                ViewCart.price_dl.setText(getString(R.string.free));
//                ViewCart.delivery_cst.setText(getString(R.string.free));
                ViewCart.deliverytimetxt.setText(splittedTime[0]);
            }catch (Exception ee){
                ee.printStackTrace();
            }
            globalLog("dated :: " + formattedDate);
//            ViewCart.delivery_Cost  = Float.parseFloat("0");
            ViewCart.delivery_Time  = formattedDate;
            ViewCart.delivery_Type  = 1;
            function.loadQty(SpecificStore.STOREID,SpecificStore.LOCID);
            finish();
        }else if(selectedType != null){
            ViewCart.deliverytmtxt.setText("");
            ViewCart.deliverytimetxt.setText(selectedType);
            if (selectedType.equals(" Delivery Now ")){
                ViewCart.delivery_Time  = "Deliver Now";
                ViewCart.delivery_Type  = 2;
            }
            else{
                ViewCart.delivery_Time  = "Pickup";
                ViewCart.delivery_Type  = 3;
            }
            finish();
        }
        else {
            Toast.makeText(this, getString(R.string.please_select_date_time), Toast.LENGTH_SHORT).show();
        }
    }
}
