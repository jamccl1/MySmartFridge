package fridge02.mysmartfridge;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Space;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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

    public void toOrderOnline(View view) {
        setContentView(R.layout.order_online);
    }

    public void toLookInside(View view) {
        setContentView(R.layout.look_inside);
        showInsideFridge(null);
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

            ingredientButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toOrderOnline(view);
                    populateOrderableItems(view);
                }
            });

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



// Look Inside METHODS //

    public void showInsideFridge(View view) {

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        ImageView lookInsideImage = (ImageView) findViewById(R.id.lookInsideImage);

        switch(radioButtonID) {
            case R.id.topShelfRadio:
                lookInsideImage.setImageResource(R.drawable.top_fridge_image);
                break;

            case R.id.middleShelfRadio:
                lookInsideImage.setImageResource(R.drawable.middle_fridge_image);
                break;

            case R.id.bottomSelfRadio:
                lookInsideImage.setImageResource(R.drawable.bottom_fridge_image);
                break;

            case R.id.drawer_one:
                lookInsideImage.setImageResource(R.drawable.drawer1_fridge_image);
                break;

            case R.id.drawer_two:
                lookInsideImage.setImageResource(R.drawable.drawer2_fridge_image);
                break;
        }


    }

    // Order Online Methods //
    private void populateOrderableItems(View view) {
        LinearLayout scrollLayout = findViewById(R.id.orderScrollLayout);

        String[] foods = getResources().getStringArray(R.array.food_list);

        for (String foodId : foods) {
            int id = getResources().getIdentifier(foodId, "array", getPackageName());
            String[] foodInfo = getResources().getStringArray(id);

            LinearLayout orderableItem = new LinearLayout(this);
            orderableItem.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.recipes_name_height) + 10));
            orderableItem.setOrientation(LinearLayout.HORIZONTAL);
            orderableItem.setGravity(Gravity.CENTER_VERTICAL);

            ImageView itemPicture = new ImageView(this);
            String imageIdString = foodInfo[0].toLowerCase().replaceAll(" ", "_") + "_image";
            int imageId = getResources().getIdentifier(imageIdString, "drawable", getPackageName());
            itemPicture.setImageDrawable(getResources().getDrawable(imageId));
            itemPicture.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.recipes_image_height), 0.14f));

            TextView itemName = new TextView(this);
            itemName.setText(foodInfo[0]);
            itemName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.34f));
            itemName.setGravity(Gravity.CENTER);
            itemName.setTextSize(STP(R.dimen.normal_text_size));

            TextView itemPrice = new TextView(this);
            itemPrice.setText(getString(R.string.order_price, foodInfo[1]));
            itemPrice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.26f));
            itemPrice.setGravity(Gravity.CENTER);
            itemPrice.setTextSize(STP(R.dimen.normal_text_size));

            ImageButton itemAdd = new ImageButton(this);
            itemAdd.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_input_add));
            itemAdd.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.recipes_order_button_height), 0.12f));
            itemAdd.setTag(foodInfo);

            itemAdd.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createDialog(view, (String[]) view.getTag());
                }
            });

            orderableItem.addView(itemPicture);
            orderableItem.addView(itemName);
            orderableItem.addView(itemPrice);
            orderableItem.addView(itemAdd);

            scrollLayout.addView(orderableItem);
        }
    }

    public void createDialog(View view, String[] foodInfo) {
        final TextView blur = findViewById(R.id.orderBlur);
        blur.setBackgroundColor(Color.parseColor("#BF424242"));

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            View dialogView = inflater.inflate(R.layout.order_online_dialog, null);
            int width = DTP(R.dimen.add_dialog_width);
            int height = DTP(R.dimen.add_dialog_height);
            final PopupWindow dialog = new PopupWindow(dialogView, width, height, true);
            dialog.showAtLocation(view, Gravity.CENTER, 0, 0);

            TextView dialogTitle = dialogView.findViewById(R.id.orderDialogTitle);
            dialogTitle.setText(getString(R.string.dialog_title, foodInfo[0]));

            TextView dialogUnit = dialogView.findViewById(R.id.orderDialogUnit);
            dialogUnit.setText(getString(R.string.order_unit, foodInfo[1], foodInfo[2]));

            Button decButton = dialogView.findViewById(R.id.orderDialogDecButton);
            decButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText input = dialog.getContentView().findViewById(R.id.orderDialogNumberField);
                    int inputNumber = Integer.parseInt(input.getText().toString());
                    if (inputNumber > 0) {
                        // It complains if I don't add the locale, so...yeah.
                        input.setText(String.format(Locale.ENGLISH, "%d", --inputNumber));
                    }
                }
            });

            Button incButton = dialogView.findViewById(R.id.orderDialogIncButton);
            incButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText input = dialog.getContentView().findViewById(R.id.orderDialogNumberField);
                    int inputNumber = Integer.parseInt(input.getText().toString());
                    // It complains if I don't add the locale, so...yeah.
                    input.setText(String.format(Locale.ENGLISH, "%d", ++inputNumber));
                }
            });

            Button cancelButton = dialogView.findViewById(R.id.orderDialogCancelButton);
            cancelButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    blur.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
            });
        }
    }

}



