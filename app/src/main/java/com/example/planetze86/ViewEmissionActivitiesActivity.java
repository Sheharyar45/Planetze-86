package com.example.planetze86;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewEmissionActivitiesActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private ListView listView;
    private String selectedDate;
    private ArrayList<String> activityDetails;
    private ArrayList<EmissionActivityElement> activityObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_emission_activities);

        firebaseManager = new FirebaseManager();
        listView = findViewById(R.id.activities_list_view);
        selectedDate = getIntent().getStringExtra("SELECTED_DATE");

        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "No date selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.bringToFront();
        fetchActivities();
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewEmissionActivitiesActivity.this, EcoTracker.class);
            startActivity(intent);
        });
    }

    private void fetchActivities() {
        firebaseManager.retrieveAllActivitiesForDate(selectedDate, activities -> {
            activityDetails = new ArrayList<>();
            activityObjects = new ArrayList<>();

            for (EmissionActivityElement activity : activities) {
                activityDetails.add(activity.getDetails());
                activityObjects.add(activity); // Keep track of the actual objects
            }

            if (activityDetails.isEmpty()) {
                activityDetails.add("No activities logged for this date.");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activityDetails);
            listView.setAdapter(adapter);

            // Set up long-press listener for edit/delete options
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                if (position >= activityObjects.size()) {
                    return false; // Ignore if it's the "No activities" message
                }
                showEditDeleteDialog(position);
                return true;
            });
        });
    }

    private void showEditDeleteDialog(int position) {
        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an action")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit selected
                        editActivity(position);

                    } else if (which == 1) {
                        // Delete selected
                        deleteActivity(position);
                    }
                })
                .show();
    }


    private void deleteActivity(int position) {
        EmissionActivityElement selectedActivity = activityObjects.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this activity?")
                .setPositiveButton("Delete", (dialog, which) -> firebaseManager.deleteActivity(
                        selectedDate,
                        selectedActivity.getType(),
                        selectedActivity.getId(),
                        success -> {
                            if (success) {
                                Toast.makeText(this, "Activity deleted successfully!", Toast.LENGTH_SHORT).show();
                                fetchActivities(); // Refresh the list
                            } else {
                                Toast.makeText(this, "Failed to delete activity.", Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void editActivity(int position) {
        EmissionActivityElement selectedActivity = activityObjects.get(position);

        Intent intent = getIntent(selectedActivity);

        if (intent != null) {
            // Pass the activity ID and type to the tracking activity for editing
            intent.putExtra("ACTIVITY_ID", selectedActivity.getId());
            intent.putExtra("ACTIVITY_TYPE", selectedActivity.getType());
            intent.putExtra("SELECTED_DATE_UPDATE", selectedDate);
            startActivity(intent);
            fetchActivities();
        } else {
            Toast.makeText(this, "Edit not supported for this activity type.", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Intent getIntent(EmissionActivityElement selectedActivity) {
        Intent intent = null;

        // Check the type of the selected activity and route to the correct tracking activity
        if (selectedActivity instanceof TransportationActivityElement) {
            intent = new Intent(ViewEmissionActivitiesActivity.this, TransportationTracking.class);
        } else if (selectedActivity instanceof ShoppingActivityElement) {
            intent = new Intent(ViewEmissionActivitiesActivity.this, ShoppingTracking.class);
        } else if (selectedActivity instanceof FoodConsumptionActivityElement) {
            intent = new Intent(ViewEmissionActivitiesActivity.this, FoodConsumptionTracking.class);
        }
        return intent;
    }

}
