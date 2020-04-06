package fridge02.mysmartfridge;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<LinearLayout> recipesInList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Set temperature on home screen
        setHomeScreenTemperature(75);

        // Set current displayed recipes to an empty list;
        recipesInList = new ArrayList<>();
    }

    public void toWhatsInFridge(View view) {
        setContentView(R.layout.whats_in_fridge);
    }

    public void toShopping(View view) {
        setContentView(R.layout.shopping);
    }

    // Sets up the Recipes screen.
    public void toRecipes(View view) {
        setContentView(R.layout.recipes);
        loadRecipes(""); // Load all recipes

        // Configures the search bar to, every time it's text is changed, clear the recipes list and
        // then re-add all recipes whose names contain the search text as a substring.
        SearchView searchbar = findViewById(R.id.recipeSearchBar);
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                clearRecipes();
                loadRecipes(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                clearRecipes();
                loadRecipes(s);
                return false;
            }
        });
    }

    public void toLookInside(View view) {
        setContentView(R.layout.look_inside);
    }

    public void toHome(View view) {
        setContentView(R.layout.home);
        setHomeScreenTemperature(75);
    }

    // RECIPES METHODS //

    // Goes through the list of recipes in recipes.xml and adds them to the UI.
    public void loadRecipes(String searchString) {
        Resources r = getResources();
        String[] recipeIds = r.getStringArray(R.array.recipe_data);
        for (String recipeId : recipeIds) {
            int id = r.getIdentifier(recipeId, "array", getPackageName());
            String[] recipeInfo = r.getStringArray(id);
            String recipeName = recipeInfo[0];
            if (recipeName.toLowerCase().contains(searchString.toLowerCase())) {
                int imageId = r.getIdentifier(recipeInfo[3], "drawable", getPackageName());
                addRecipe(imageId, recipeName);
            }
        }
    }

    // Removes all recipes from the UI.
    private void clearRecipes() {
        for (LinearLayout layout : recipesInList) {
            layout.setVisibility(View.GONE);
        }

        recipesInList.clear();
    }

    // Adds a recipe to the Recipes UI.
    private void addRecipe(int imageDrawableId, String recipeTitle) {
        LinearLayout recipeScrollLayout = findViewById(R.id.recipesScrollLayout);

        ImageView image = new ImageView(this);
        image.setImageDrawable(getResources().getDrawable(imageDrawableId));
        image.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_image_width), DTP(R.dimen.recipes_image_height)));

        TextView recipeName = new TextView(this);
        recipeName.setText(recipeTitle);
        recipeName.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_name_width), DTP(R.dimen.recipes_name_height)));
        recipeName.setGravity(Gravity.CENTER);
        recipeName.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        Button button = new Button(this);
        button.setText(R.string.recipe_select);
        button.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_button_width), DTP(R.dimen.recipes_button_height)));
        button.setAllCaps(true);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(STP(R.dimen.recipes_button_text_size));

        LinearLayout recipe = new LinearLayout(this);
        recipe.setOrientation(LinearLayout.HORIZONTAL);
        recipe.setGravity(Gravity.CENTER_VERTICAL);
        recipe.addView(image);
        recipe.addView(recipeName);
        recipe.addView(button);

        recipeScrollLayout.addView(recipe);
        recipesInList.add(recipe);
    }

    // Converts from dp to pixels.
    private int DTP(int dimensionId) {
        Resources r = getResources();
        float dpValue = r.getDimension(dimensionId) / r.getDisplayMetrics().density;
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics()));
    }

    // Converts from sp to pixels.
    private int STP(int dimensionId) {
        Resources r = getResources();
        float spValue = r.getDimension(dimensionId) / r.getDisplayMetrics().scaledDensity;
        return Math.round(spValue);
    }

    // Adjusts the temperature on the home screen.
    private void setHomeScreenTemperature(int temperature) {
        TextView uiTemperature = findViewById(R.id.temperature);
        uiTemperature.setText(getString(R.string.temperature, temperature));
    }
}
