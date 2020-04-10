package fridge02.mysmartfridge;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Space;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<CheckBox> groceriesInList;
    private ArrayList<LinearLayout> recipesInList;
    private String lastRecipeSearch;
    boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Set temperature on home screen
        if (isTablet) {
            setHomeScreenTemperatureDevice(75);
        } else {
            setHomeScreenTemperature(75);
        }

        // Set current displayed recipes to an empty list;
        recipesInList = new ArrayList<>();

        // Set last searched recipe
        lastRecipeSearch = "";

        //Set groceries shopping list to be empty list
        groceriesInList = new ArrayList<>();

    }

    public void toWhatsInFridge(View view) {
        setContentView(R.layout.whats_in_fridge);
    }

    public void toShopping(View view) {
        //hi
        setContentView(R.layout.shopping);
        loadGroceryList(view);

    }

    // Sets up the Recipes screen.
    public void toRecipes(View view) {
        setContentView(R.layout.recipes);
        loadRecipes(lastRecipeSearch); // Load all recipes

        // Configures the search bar to, every time it's text is changed, clear the recipes list and
        // then re-add all recipes whose names contain the search text as a substring.
        SearchView searchbar = findViewById(R.id.recipeSearchBar);
        searchbar.setQuery(lastRecipeSearch, false);
        if (!lastRecipeSearch.equals("")) {
            searchbar.setIconified(false);
            searchbar.requestFocus();
        } else {
            searchbar.setIconified(true);
        }
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                lastRecipeSearch = s;
                clearRecipes();
                loadRecipes(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                lastRecipeSearch = s;
                clearRecipes();
                loadRecipes(s);
                return false;
            }
        });
    }

    public void toLookInside(View view) {
        setContentView(R.layout.look_inside);
    }

    public void toSettings(View view) {
        setContentView(R.layout.settings);
    }

    public void toHome(View view) {
        setContentView(R.layout.home);

        if (isTablet) {
            setHomeScreenTemperatureDevice(75);
        } else {
            setHomeScreenTemperature(75);
        }
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
                addRecipe(recipeInfo);
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
    private void addRecipe(String[] recipeInfo) {
        LinearLayout recipeScrollLayout = findViewById(R.id.recipesScrollLayout);

        ImageView image = new ImageView(this);
        int imageId = getResources().getIdentifier(recipeInfo[3], "drawable", getPackageName());
        image.setImageDrawable(getResources().getDrawable(imageId));
        image.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_image_width), DTP(R.dimen.recipes_image_height)));

        TextView recipeName = new TextView(this);
        recipeName.setText(recipeInfo[0]);
        recipeName.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_name_width), DTP(R.dimen.recipes_name_height)));
        recipeName.setGravity(Gravity.CENTER);
        recipeName.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        Button button = new Button(this);
        button.setText(R.string.recipe_select);
        button.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_select_button_width), DTP(R.dimen.recipes_select_button_height)));
        button.setAllCaps(true);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(STP(R.dimen.recipes_select_button_text_size));
        button.setTag(recipeInfo);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof Button) {
                    String[] recipeInfo = (String[]) view.getTag();
                    int imageId = getResources().getIdentifier(recipeInfo[3], "drawable", getPackageName());
                    createRecipeScreen(view, imageId, recipeInfo[0], recipeInfo[1], recipeInfo[2], recipeInfo[4]);
                }
            }
        });

        LinearLayout recipe = new LinearLayout(this);
        recipe.setOrientation(LinearLayout.HORIZONTAL);
        recipe.setGravity(Gravity.CENTER_VERTICAL);
        recipe.addView(image);
        recipe.addView(recipeName);
        recipe.addView(button);

        recipeScrollLayout.addView(recipe);
        recipesInList.add(recipe);
    }

    private void createRecipeScreen(View view, int imageDrawableId, String recipeName, String ingredientsList, String cookTime, String instructions) {
        setContentView(R.layout.recipes_individual);
        LinearLayout recipeScrollLayout = findViewById(R.id.recipesScrollLayout2);

        TextView recipeTitle = findViewById(R.id.recipesTitle2);
        recipeTitle.setText(recipeName);

        ImageView image = new ImageView(this);
        image.setImageDrawable(getResources().getDrawable(imageDrawableId));
        image.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_indv_image_width), DTP(R.dimen.recipes_indv_image_width)));
        int padding = DTP(R.dimen.recipes_indv_image_padding);
        image.setPadding(padding, padding, padding, padding);
        image.setBackgroundColor(Color.rgb(214, 215, 215)); // The button color! (It's grey.)

        Space space1 = new Space(this);
        space1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.small_vertical_space)));

        TextView cookingTimeHeader = new TextView(this);
        cookingTimeHeader.setText(Html.fromHtml(getString(R.string.recipes_cooking_time_header)));
        cookingTimeHeader.setTextSize(STP(R.dimen.normal_text_size));
        cookingTimeHeader.setTypeface(cookingTimeHeader.getTypeface(), Typeface.BOLD);
        cookingTimeHeader.setGravity(Gravity.CENTER_HORIZONTAL);
        cookingTimeHeader.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView cookingTimeValue = new TextView(this);
        cookingTimeValue.setText(cookTime);
        cookingTimeValue.setTextSize(STP(R.dimen.normal_text_size));
        cookingTimeValue.setGravity(Gravity.CENTER_HORIZONTAL);
        cookingTimeHeader.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Space space2 = new Space(this);
        space2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.small_vertical_space)));

        recipeScrollLayout.addView(image);
        recipeScrollLayout.addView(space1);
        recipeScrollLayout.addView(cookingTimeHeader);
        recipeScrollLayout.addView(cookingTimeValue);
        recipeScrollLayout.addView(space2);

        TextView ingredientsTitle = new TextView(this);
        ingredientsTitle.setText(R.string.recipes_ingredients_header);
        ingredientsTitle.setTextSize(STP(R.dimen.normal_text_size));
        ingredientsTitle.setTypeface(ingredientsTitle.getTypeface(), Typeface.BOLD);
        ingredientsTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        ingredientsTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        recipeScrollLayout.addView(ingredientsTitle);

        String[] ingredientsArray = ingredientsList.split(",");
        for (String i : ingredientsArray) {
            LinearLayout ingredientLayout = new LinearLayout(this);
            ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
            ingredientLayout.setGravity(Gravity.CENTER);

            TextView bullet = new TextView(this);
            bullet.setText("• ");
            bullet.setTextSize(STP(R.dimen.normal_text_size));
            bullet.setGravity(Gravity.CENTER);
            bullet.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView ingredientName = new TextView(this);
            ingredientName.setText(i);
            ingredientName.setTextSize(STP(R.dimen.normal_text_size));
            ingredientName.setGravity(Gravity.START); // Left
            ingredientName.setLayoutParams(new LinearLayout.LayoutParams(Math.round( 0.6f * DTP(R.dimen.recipes_indv_scroll_width)), LinearLayout.LayoutParams.WRAP_CONTENT));

            Button ingredientButton = new Button(this);
            ingredientButton.setText(R.string.recipes_order_button);
            ingredientButton.setTextSize(STP(R.dimen.recipes_order_button_text_size));
            ingredientButton.setLayoutParams(new LinearLayout.LayoutParams(Math.round( 0.2f * DTP(R.dimen.recipes_indv_scroll_width)), DTP(R.dimen.recipes_order_button_height)));

            ingredientLayout.addView(bullet);
            ingredientLayout.addView(ingredientName);
            ingredientLayout.addView(ingredientButton);

            recipeScrollLayout.addView(ingredientLayout);
        }

        Space space3 = new Space(this);
        space3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.small_vertical_space)));

        TextView instructionsTitle = new TextView(this);
        instructionsTitle.setText(R.string.recipes_instructions_header);
        instructionsTitle.setTextSize(STP(R.dimen.normal_text_size));
        instructionsTitle.setTypeface(ingredientsTitle.getTypeface(), Typeface.BOLD);
        instructionsTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        instructionsTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView instructionsText = new TextView(this);
        instructionsText.setText(instructions.replace("\n", "\n\n"));
        instructionsText.setTextSize(STP(R.dimen.normal_text_size));
        instructionsText.setGravity(Gravity.CENTER_HORIZONTAL);
        instructionsText.setLayoutParams(new LinearLayout.LayoutParams(Math.round( 0.8f * DTP(R.dimen.recipes_indv_scroll_width)), LinearLayout.LayoutParams.WRAP_CONTENT));

        recipeScrollLayout.addView(space3);
        recipeScrollLayout.addView(instructionsTitle);
        recipeScrollLayout.addView(instructionsText);
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

    private void setHomeScreenTemperatureDevice(int temperature) {
        TextView uiTemperature = findViewById(R.id.temperatureDevice);
        uiTemperature.setText(getString(R.string.temperature, temperature));
    }

    // Grocery List Methods

    public void addToGroceryList(View view){

        //Check for empty string

        int id = Resources.getSystem().getIdentifier("btn_check_holo_light", "drawable", "android");

        EditText userItem = findViewById(R.id.new_item_input);
        String userItemString =  userItem.getText().toString();
        userItem.getText().clear();

        if (!userItemString.equals("")) {

            LinearLayout linearLay = (LinearLayout) findViewById(R.id.groceryListScrollLayout);

            CheckBox newCheckBox = new CheckBox(getApplicationContext());
            newCheckBox.setText(userItemString);
            newCheckBox.setTextColor(Color.BLACK);

            newCheckBox.setButtonDrawable(id);

            linearLay.addView(newCheckBox);

            groceriesInList.add(newCheckBox);
        }

    }

    public void loadGroceryList(View view){

        LinearLayout linearLay = (LinearLayout) findViewById(R.id.groceryListScrollLayout);

        for (View x: groceriesInList){
            ((ViewGroup)x.getParent()).removeView(x);
            linearLay.addView(x);
        }

    }
}
