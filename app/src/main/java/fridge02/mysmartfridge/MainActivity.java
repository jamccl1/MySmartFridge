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
    }

    public void toRecipes(View view) {
        setContentView(R.layout.recipes);
    }

    public void goHome(View view) {
        setContentView(R.layout.home);
    }
}
