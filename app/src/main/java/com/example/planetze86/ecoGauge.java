package com.example.planetze86;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ecoGauge extends AppCompatActivity {

    private PieChart pieChart;
    private LineChart lineChart;
    private DatabaseReference databaseReference;
    private String selectedTab = "Annual";
    private AnnualAnswers annualData;
    private Button annualButton, monthlyButton, weeklyButton, dailyButton, dateButton;
    private TextView displayMessage, dateMessage;
    private String chosenDate;
    private TextView compareText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_gauge);

        
        pieChart = findViewById(R.id.pie_chart);
//        lineChart = findViewById(R.id.line_chart);
        compareText = findViewById(R.id.compare_text);
        displayMessage = findViewById(R.id.emissionMessage);
        dateMessage = findViewById(R.id.dateMessage);
        annualButton = findViewById(R.id.annual_button);
        monthlyButton = findViewById(R.id.monthly_button);
        weeklyButton = findViewById(R.id.weekly_button);
        dailyButton = findViewById(R.id.daily_button);
        dateButton = findViewById(R.id.date_button);
        ImageButton backButton = findViewById(R.id.backButton);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // Format the default date
        chosenDate = day + "-" + (month + 1) + "-" + year;
        dateMessage.setText("Date: " + chosenDate);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String UID = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(UID);
            displayAnnualData(databaseReference); // default

            annualButton.setOnClickListener(view -> {
                selectedTab = "Annual";
                displayAnnualData(databaseReference);
            });
            monthlyButton.setOnClickListener(view -> {
                selectedTab = "Monthly"; // Store selected tab
                displayMonthlyData(databaseReference);
            });
            weeklyButton.setOnClickListener(view -> {
                selectedTab = "Weekly"; // Store selected tab
                displayWeeklyData(databaseReference);
            });
            dailyButton.setOnClickListener(view -> {
                selectedTab = "Daily"; // Store selected tab
                displayDailyData(databaseReference);
            });

        }
        else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_LONG).show();
        }

        dateButton.setOnClickListener(view -> showDatePickerDialog());

        // Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ecoGauge.this, PlanetzeMenu.class);
                startActivity(intent);
            }
        });

    }

    private void displayAnnualData(DatabaseReference databaseReference){
        highlightSelectedButton(annualButton);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getAnnualAnswers() != null) {
                        annualData = user.getAnnualAnswers();
                        compareText.setText(String.format("The average annual footprint in %s is %.4f kg CO2e.",
                                annualData.getCountry(), annualData.getCountryEmission()));
                        displayMessage.setText(String.format("You've emitted %.2f kg CO2e this year", annualData.getAnnualEmission()));
                        float transportation = (float) annualData.getAnnualTransportation();
                        float food = (float) annualData.getAnnualFood();
                        float housing = (float) annualData.getAnnualHousing();
                        float consumption = (float) annualData.getAnnualConsumption();
                        createPieChart(transportation, food, housing, consumption);
                    }
                    else{
                        float transportation = (float) 0.0;
                        float food = (float) 0.0;
                        float housing = (float) 0.0;
                        float consumption = (float) 0.0;
                        createPieChart(transportation, food, housing, consumption);
                        Toast.makeText(ecoGauge.this, "No annual data found.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.e("ecoGauge", "Used data not found.");
                    Toast.makeText(ecoGauge.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ecoGauge", "Database Error: " + error.getMessage());
            }
        });
    }

    private void displayMonthlyData(DatabaseReference databaseReference){
        highlightSelectedButton(monthlyButton);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getAnnualAnswers() != null) {
                        annualData = user.getAnnualAnswers();
                        compareText.setText(String.format("The average monthly footprint in %s is %.4f kg CO2e.",
                                annualData.getCountry(), (annualData.getCountryEmission() / 12.0)));
                        displayMessage.setText(String.format("You've emitted %.2f kg CO2e this year", annualData.getAnnualEmission()));
                        float transportation = (float) annualData.getAnnualTransportation();
                        float food = (float) annualData.getAnnualFood();
                        float housing = (float) annualData.getAnnualHousing();
                        float consumption = (float) annualData.getAnnualConsumption();
                        createPieChart(transportation, food, housing, consumption);
                    }
                    else{
                        float transportation = (float) 0.0;
                        float food = (float) 0.0;
                        float housing = (float) 0.0;
                        float consumption = (float) 0.0;
                        createPieChart(transportation, food, housing, consumption);
                        Toast.makeText(ecoGauge.this, "No annual data found.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.e("ecoGauge", "Used data not found.");
                    Toast.makeText(ecoGauge.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ecoGauge", "Database Error: " + error.getMessage());
            }
        });
    }

    private void displayWeeklyData(DatabaseReference databaseReference){
        highlightSelectedButton(weeklyButton);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getAnnualAnswers() != null) {
                        annualData = user.getAnnualAnswers();
                        compareText.setText(String.format("The average weekly footprint in %s is %.4f kg CO2e.",
                                annualData.getCountry(), ((annualData.getCountryEmission()) * 7.0 / 365.0)));
                        displayMessage.setText(String.format("You've emitted %.2f kg CO2e this year", annualData.getAnnualEmission()));
                        float transportation = (float) annualData.getAnnualTransportation();
                        float food = (float) annualData.getAnnualFood();
                        float housing = (float) annualData.getAnnualHousing();
                        float consumption = (float) annualData.getAnnualConsumption();
                        createPieChart(transportation, food, housing, consumption);
                    }
                    else{
                        float transportation = (float) 0.0;
                        float food = (float) 0.0;
                        float housing = (float) 0.0;
                        float consumption = (float) 0.0;
                        createPieChart(transportation, food, housing, consumption);
                        Toast.makeText(ecoGauge.this, "No annual data found.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.e("ecoGauge", "Used data not found.");
                    Toast.makeText(ecoGauge.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ecoGauge", "Database Error: " + error.getMessage());
            }
        });
    }

    private void displayDailyData(DatabaseReference databaseReference){
        EmissionDataWrapper data = new EmissionDataWrapper();
        highlightSelectedButton(dailyButton);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getAnnualAnswers() != null) {
                        annualData = user.getAnnualAnswers();
                        compareText.setText(String.format("The average daily footprint in %s is %.4f kg CO2e.",
                                annualData.getCountry(), (annualData.getCountryEmission() / 365.0)));
                        displayMessage.setText(String.format("You've emitted %.2f kg CO2e this year", annualData.getAnnualEmission()));
                        float transportation = (float) data.getCategoryEmissions(chosenDate, "Transportation");
                        float food = (float) data.getCategoryEmissions(chosenDate, "FoodConsumption");
                        float housing = (float) annualData.getAnnualHousing();
                        float consumption = (float) data.getCategoryEmissions(chosenDate, "Shopping");
                        createPieChart(transportation, food, housing, consumption);
                    }
                    else{
                        float transportation = (float) 0.0;
                        float food = (float) 0.0;
                        float housing = (float) 0.0;
                        float consumption = (float) 0.0;
                        createPieChart(transportation, food, housing, consumption);
                        Toast.makeText(ecoGauge.this, "No annual data found.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.e("ecoGauge", "Used data not found.");
                    Toast.makeText(ecoGauge.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ecoGauge", "Database Error: " + error.getMessage());
            }
        });
    }

    private void createPieChart(float transportation, float housing, float food, float consumption){
        ArrayList<PieEntry> chartTitles = new ArrayList<>();
        chartTitles.add(new PieEntry(transportation, "transportation"));
        chartTitles.add(new PieEntry(housing, "housing"));
        chartTitles.add(new PieEntry(food, "food"));
        chartTitles.add(new PieEntry(consumption, "consumption"));
        PieDataSet dataStorage = new PieDataSet(chartTitles, "");
        dataStorage.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW);
        dataStorage.setSliceSpace(3f);
        PieData allData = new PieData(dataStorage);
        allData.setValueTextSize(8f);
        allData.setValueTextColor(Color.BLACK);
        pieChart.setData(allData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterTextSize(18f);
        pieChart.animateY(1000);
    }

    private void createLineChart(float transportation, float housing, float food, float consumption){
        ArrayList<Entry> lineTitles = new ArrayList<>();
        lineTitles.add(new Entry(0, transportation));
        lineTitles.add(new Entry(1, housing));
        lineTitles.add(new Entry(2, food));
        lineTitles.add(new Entry(3, consumption));
        LineDataSet dataStorage = new LineDataSet(lineTitles, "Emissions Trend");
        dataStorage.setColor(Color.BLUE);
        dataStorage.setLineWidth(2f);
        dataStorage.setCircleColor(Color.RED);
        dataStorage.setCircleRadius(5f);
        dataStorage.setValueTextSize(10f);
        LineData allData = new LineData(dataStorage);
        lineChart.setData(allData);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(1400);
    }


    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int chosenYear = calendar.get(Calendar.YEAR);
        int chosenMonth = calendar.get(Calendar.MONTH);
        int chosenDay = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    chosenDate = selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear;
                    storeSelectedDate();
                }, chosenYear, chosenMonth, chosenDay);
        datePickerDialog.show();
    }

    private void storeSelectedDate() {
        dateMessage.setText(chosenDate);
        Toast.makeText(this, "Date Selected: " + chosenDate, Toast.LENGTH_SHORT).show();
        switch (selectedTab) {
            case "Annual":
                displayAnnualData(databaseReference);
                break;
            case "Monthly":
                displayMonthlyData(databaseReference);
                break;
            case "Weekly":
                displayWeeklyData(databaseReference);
                break;
            case "Daily":
                displayDailyData(databaseReference);
                break;
        }
    }

    private void highlightSelectedButton(Button btn){

        // Remove every highlighted button
        annualButton.setBackgroundColor(Color.parseColor("#A9BCD0"));
        monthlyButton.setBackgroundColor(Color.parseColor("#A9BCD0"));
        weeklyButton.setBackgroundColor(Color.parseColor("#A9BCD0"));
        dailyButton.setBackgroundColor(Color.parseColor("#A9BCD0"));

        // Highlight current button
        btn.setBackgroundColor(Color.parseColor("#E0E1DD"));

    }

}

