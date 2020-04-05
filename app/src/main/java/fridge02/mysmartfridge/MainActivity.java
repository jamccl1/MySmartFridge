package fridge02.mysmartfridge;

import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Set temperature on home screen
        setHomeScreenTemperature(75);
    }

    public void toWhatsInFridge(View view) {
        setContentView(R.layout.whats_in_fridge);
    }

    public void toShopping(View view) {
        setContentView(R.layout.shopping);
    }

    public void toRecipes(View view) {
        setContentView(R.layout.recipes);
    }

    public void toLookInside(View view) {
        setContentView(R.layout.look_inside);
    }

    public void toHome(View view) {
        setContentView(R.layout.home);
        setHomeScreenTemperature(75);
    }

    private void setHomeScreenTemperature(int temperature) {
        TextView uiTemperature = findViewById(R.id.temperature);
        uiTemperature.setText(getString(R.string.temperature, temperature));
    }
}
