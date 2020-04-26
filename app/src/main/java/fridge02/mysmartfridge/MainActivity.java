package fridge02.mysmartfridge;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ArrayList<LinearLayout> recipesInList, orderableItemsInList;
    private ArrayList<String> itemsInCart;
    private String[] paymentInfo;
    private String lastRecipeSearch, lastOnlineOrderSearch;
    private int checkoutFieldsComplete;
    private float cartTotal;
    private boolean isTablet, saveCheckoutInfo, useExpressShipping;
    private HashMap<String, String> itemsInFridge;

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
        orderableItemsInList = new ArrayList<>();
        itemsInCart = new ArrayList<>();
        paymentInfo = new String[9];
        itemsInFridge = new HashMap<>();

        // Set last searched recipe/online order
        lastRecipeSearch = "";
        lastOnlineOrderSearch = "";

        // Set booleans
        saveCheckoutInfo = false;
        useExpressShipping = false;

        // Set numbers
        cartTotal = 0;
        checkoutFieldsComplete = 0;
    }


    //FRIDGE STUFF:

    public void toWhatsInFridge(View view) {
        setContentView(R.layout.whats_in_fridge);

        for (String name: itemsInFridge.keySet()) {
            addFridgeItem(name, itemsInFridge.get(name));
        }

    }

    public void addFridgeItem(String name, String expiration) {
        LinearLayout fridgeScrollLayout = findViewById(R.id.fridgeScrollLayout);

        TextView itemName = new TextView(this);
        itemName.setText(name);
        itemName.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_image_width), DTP(R.dimen.recipes_image_height)));
        itemName.setGravity(Gravity.CENTER);
        itemName.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        TextView expirationDate = new TextView(this);
        expirationDate.setText(expiration);
        expirationDate.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_name_width), DTP(R.dimen.recipes_name_height)));
        expirationDate.setGravity(Gravity.CENTER);
        expirationDate.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        Button button = new Button(this);
        button.setText(R.string.fridge_item_remove);
        button.setLayoutParams(new LinearLayout.LayoutParams(DTP(R.dimen.recipes_select_button_width), DTP(R.dimen.recipes_select_button_height)));
        button.setAllCaps(true);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(STP(R.dimen.what_in_fridge_remove));

        LinearLayout inFridge = new LinearLayout(this);
        inFridge.setOrientation(LinearLayout.HORIZONTAL);
        inFridge.setGravity(Gravity.CENTER_VERTICAL);
        inFridge.addView(itemName);
        inFridge.addView(expirationDate);
        inFridge.addView(button);

        //value to be passed into remove method
        final LinearLayout inFridgeFinal = inFridge;
        final String key = name;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view instanceof Button) {
                    //String[] recipeInfo = (String[]) view.getTag();
                    //int imageId = getResources().getIdentifier(recipeInfo[3], "drawable", getPackageName());
                    remove(inFridgeFinal, key);
                }
            }
        });

        fridgeScrollLayout.addView(inFridge);
    }

    private void remove(LinearLayout inFridge, String key) {
        LinearLayout fridgeScrollLayout = findViewById(R.id.fridgeScrollLayout);
        fridgeScrollLayout.removeView(inFridge);

        itemsInFridge.remove(key);

    }

    public void toAddItem(View view) {
        setContentView(R.layout.add_food_item);
    }

    public void addNewItem(View view) {
        EditText nameText = findViewById(R.id.itemNameText);
        EditText countText = findViewById(R.id.itemCountText);
        EditText dayText = findViewById(R.id.itemDayText);
        EditText monthText = findViewById(R.id.itemMonthText);
        EditText yearText = findViewById(R.id.itemYearText);

        String name = nameText.getText().toString();
        int count = Integer.parseInt(countText.getText().toString());
        int day = Integer.parseInt(dayText.getText().toString());
        int month = Integer.parseInt(monthText.getText().toString());
        int year = Integer.parseInt(yearText.getText().toString());

        //TODO: CHECK TO SEE THAT ALL EDITTEXTS ARE FILLED IN WITH THE CORRECT INFORMATION; MAYBE ADD TO THE HINT WHAT IT SHOULD LOOK LIKE, MAYBE CHANGE THE WAY YOU ENTER DATE

        String key = name + " (" + count + ")";
        String value = month + "/" + day + "/" + year;
        itemsInFridge.put(key, value);
        toWhatsInFridge(view);

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
        populateOrderableItems(lastOnlineOrderSearch);

        final ScrollView scrollView = findViewById(R.id.orderScrollView);
        final ImageView canScrollUp = findViewById(R.id.orderCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.orderCanScrollDown);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });

        SearchView searchbar = findViewById(R.id.orderSearchBar);

        searchbar.setQuery(lastOnlineOrderSearch, false);
        if (!lastOnlineOrderSearch.equals("")) {
            searchbar.setIconified(false);
            searchbar.requestFocus();
        } else {
            searchbar.setIconified(true);
        }

        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                lastOnlineOrderSearch = s;
                clearOrderableItems();
                populateOrderableItems(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                lastOnlineOrderSearch = s;
                clearOrderableItems();
                populateOrderableItems(s);

                return false;
            }
        });
    }

    public void toCart(View view) {
        setContentView(R.layout.order_online_cart);
        populateCart();
    }

    public void toCheckout(View view) {
        setContentView(R.layout.order_online_checkout);

        // Input Fields
        final EditText firstName = findViewById(R.id.chktFirstNameField);
        final EditText middleInitial = findViewById(R.id.chktMiddleInitialField);
        final EditText lastName = findViewById(R.id.chktLastNameField);
        final EditText address = findViewById(R.id.chktAddressField);
        final EditText city = findViewById(R.id.chktCityField);
        final Spinner state = findViewById(R.id.chktStateSpinner);
        final EditText zipCode = findViewById(R.id.chktZipCodeField);
        final EditText creditCardNumber = findViewById(R.id.chktCreditCardField);
        final EditText svcNumber = findViewById(R.id.chktSvcField);

        if (isTablet) {
            // Make spinner text size normal
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.checkout_states, R.layout.custom_spinner);
            adapter.setDropDownViewResource(R.layout.custom_spinner);
            state.setAdapter(adapter);
        }

        final EditText[] fieldArray = {firstName, middleInitial, lastName, address, city, zipCode, creditCardNumber, svcNumber};
        final TextView[] labelArray = {findViewById(R.id.chktFirstNameLabel), findViewById(R.id.chktMiddleInitialLabel), findViewById(R.id.chktLastNameLabel),
                findViewById(R.id.chktAddressLabel), findViewById(R.id.chktCityLabel), findViewById(R.id.chktZipCodeLabel), findViewById(R.id.chktCreditCardLabel),
                findViewById(R.id.chktSvcLabel)};

        final Button payButton = findViewById(R.id.chktPayButton);
        payButton.setText(getString(R.string.chkt_pay_button_text, getPriceString(cartTotal * (useExpressShipping ? 1.05f : 1))));
        payButton.setEnabled(checkoutFieldsComplete >= 8);

        CheckBox saveInfo = findViewById(R.id.chktSaveCheckbox);
        saveInfo.setChecked(saveCheckoutInfo);
        saveInfo.setTextColor(saveCheckoutInfo ? Color.BLACK : Color.parseColor("#737373"));

        if (saveCheckoutInfo) {
            firstName.setText(paymentInfo[0]);
            middleInitial.setText(paymentInfo[1]);
            lastName.setText(paymentInfo[2]);
            address.setText(paymentInfo[3]);
            city.setText(paymentInfo[4]);

            String[] states = getResources().getStringArray(R.array.checkout_states);
            for (int i = 0; i < states.length; i++) {
                if (states[i].equals(paymentInfo[5])) {
                    state.setSelection(i);
                }
            }

            zipCode.setText(paymentInfo[6]);
            creditCardNumber.setText(paymentInfo[7]);
            svcNumber.setText(paymentInfo[8]);
        }

        saveInfo.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    paymentInfo[0] = firstName.getText().toString();
                    paymentInfo[1] = middleInitial.getText().toString();
                    paymentInfo[2] = lastName.getText().toString();
                    paymentInfo[3] = address.getText().toString();
                    paymentInfo[4] = city.getText().toString();
                    paymentInfo[5] = state.getSelectedItem().toString();
                    paymentInfo[6] = zipCode.getText().toString();
                    paymentInfo[7] = creditCardNumber.getText().toString();
                    paymentInfo[8] = svcNumber.getText().toString();

                    saveCheckoutInfo = true;

                    compoundButton.setTextColor(Color.BLACK);
                } else {
                    paymentInfo = new String[9];
                    saveCheckoutInfo = false;

                    compoundButton.setTextColor(Color.parseColor("#737373"));
                }
            }
        });

        final CheckBox expressShipping = findViewById(R.id.chktShippingCheckbox);
        expressShipping.setChecked(useExpressShipping);
        expressShipping.setText(getString(R.string.chkt_express_option, getPriceString(cartTotal * 0.05f)));
        expressShipping.setTextColor(useExpressShipping ? Color.BLACK : Color.parseColor("#737373"));
        expressShipping.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    payButton.setText(getString(R.string.chkt_pay_button_text, getPriceString(cartTotal * 1.05f)));
                    useExpressShipping = true;
                    compoundButton.setTextColor(Color.BLACK);
                } else {
                    payButton.setText(getString(R.string.chkt_pay_button_text, getPriceString(cartTotal)));
                    useExpressShipping = false;
                    compoundButton.setTextColor(Color.parseColor("#737373"));
                }
            }
        });

        Button backButton = findViewById(R.id.chktBackButton);
        backButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveCheckoutInfo) {
                    paymentInfo[0] = firstName.getText().toString();
                    paymentInfo[1] = middleInitial.getText().toString();
                    paymentInfo[2] = lastName.getText().toString();
                    paymentInfo[3] = address.getText().toString();
                    paymentInfo[4] = city.getText().toString();
                    paymentInfo[5] = state.getSelectedItem().toString();
                    paymentInfo[6] = zipCode.getText().toString();
                    paymentInfo[7] = creditCardNumber.getText().toString();
                    paymentInfo[8] = svcNumber.getText().toString();
                } else {
                    checkoutFieldsComplete = 0;
                }

                toCart(view);
            }
        });

        TextWatcher textChangedListener = new TextWatcher() {
            private String prevText;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevText = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currStr = charSequence.toString();
                if (prevText.equals("") && !currStr.equals("")) {
                    checkoutFieldsComplete++;

                    if (checkoutFieldsComplete >= 8) {
                        payButton.setEnabled(true);
                    }
                } else if (!prevText.equals("") && currStr.equals("")) {
                    checkoutFieldsComplete--;
                    payButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        for (EditText field : fieldArray) {
            field.addTextChangedListener(textChangedListener);
        }

        final TextView thanksBackground = findViewById(R.id.chktBlur);
        final TextView thanksText = findViewById(R.id.chktThankYou);
        final ProgressBar thanksBar = findViewById(R.id.chktThinkingBar);
        payButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveCheckoutInfo) {
                    paymentInfo[0] = firstName.getText().toString();
                    paymentInfo[1] = middleInitial.getText().toString();
                    paymentInfo[2] = lastName.getText().toString();
                    paymentInfo[3] = address.getText().toString();
                    paymentInfo[4] = city.getText().toString();
                    paymentInfo[5] = state.getSelectedItem().toString();
                    paymentInfo[6] = zipCode.getText().toString();
                    paymentInfo[7] = creditCardNumber.getText().toString();
                    paymentInfo[8] = svcNumber.getText().toString();
                } else {
                    checkoutFieldsComplete = 0;
                }

                thanksBackground.setVisibility(View.VISIBLE);
                thanksText.setVisibility(View.VISIBLE);
                thanksBar.setVisibility(View.VISIBLE);
                final View holdView = view; // So we can use it in the runnable call

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        thanksText.setText(R.string.chkt_complete_text);
                    }
                }, 1500);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cartTotal = 0;
                        itemsInCart.clear();
                        toHome(holdView);
                    }
                }, 2500);
            }
        });
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
            String tag = i.split(" [(]")[0]; // Name of the ingredient, in a string
            ingredientButton.setTag(tag);

            ingredientButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastOnlineOrderSearch = ((String) view.getTag()).toLowerCase();
                    toOrderOnline(view);
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
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics()));
    }

    // Converts from dp to pixels. Takes in a value directly, rather than a dimension.
    private int DTPDirect(int value) {
        float dpValue = value / getResources().getDisplayMetrics().density;
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
        TextView uiTemperature = findViewById(R.id.temperature);
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

    private void clearOrderableItems() {
        for (LinearLayout layout : orderableItemsInList) {
            layout.setVisibility(View.GONE);
        }

        orderableItemsInList.clear();
    }

    // Order Online Methods //
    private void populateOrderableItems(String searchString) {
        LinearLayout scrollLayout = findViewById(R.id.orderScrollLayout);
        Resources r = getResources();
        String[] foods = r.getStringArray(R.array.food_list);

        // Weights for the image, item name, item price, and add button (respectively) in each layout
        float[] phoneWeights = {1.4f, 3.4f, 2.6f, 1.2f};
        float[] tabletWeights = {1.4f, 3.8f, 3f, 1.2f};
        float[] weights = isTablet ? tabletWeights : phoneWeights;

        for (String foodId : foods) {
            int id = r.getIdentifier(foodId, "array", getPackageName());
            String[] foodInfo = r.getStringArray(id);

            if (foodInfo[0].toLowerCase().contains(searchString.toLowerCase())) {
                LinearLayout orderableItem = new LinearLayout(this);
                orderableItem.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.order_item_height)));
                orderableItem.setOrientation(LinearLayout.HORIZONTAL);
                orderableItem.setGravity(Gravity.CENTER_VERTICAL);

                ImageView itemPicture = new ImageView(this);
                String imageIdString = foodInfo[0].toLowerCase().replaceAll(" ", "_") + "_image";
                int imageId = r.getIdentifier(imageIdString, "drawable", getPackageName());
                itemPicture.setImageDrawable(r.getDrawable(imageId));
                itemPicture.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_image_height), weights[0]));

                TextView itemName = new TextView(this);
                itemName.setText(foodInfo[0]);
                itemName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[1]));
                itemName.setGravity(Gravity.CENTER);
                itemName.setTextSize(STP(R.dimen.normal_text_size));

                orderableItem.addView(itemPicture);
                orderableItem.addView(itemName);

                if (isTablet) {
                    // For whether the item is in the fridge, is expired, or isn't in, resp.
                    float[] fridgeIconWeights = {1f, 1.1f, 0.7f};
                    float [] fridgeTextWeights = {2.2f, 2.1f, 2.5f};

                    ImageView inFridgeIcon = new ImageView(this);
                    inFridgeIcon.setScaleType(ImageView.ScaleType.FIT_END);
                    inFridgeIcon.setImageDrawable(r.getDrawable(android.R.drawable.presence_online));
                    inFridgeIcon.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_fridge_icon_height), fridgeIconWeights[0]));

                    TextView inFridgeText = new TextView(this);
                    inFridgeText.setText(getString(R.string.order_in_fridge_text, " "));
                    inFridgeText.setTextSize(STP(R.dimen.normal_text_size));
                    inFridgeText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, fridgeTextWeights[0]));

                    orderableItem.addView(inFridgeIcon);
                    orderableItem.addView(inFridgeText);
                }

                TextView itemPrice = new TextView(this);
                if (isTablet) {
                    String priceString = getString(R.string.order_price_t, foodInfo[1], foodInfo[2]);
                    priceString = priceString.replace("[", "<b>").replace("]", "</b>").replace("\n", "<br>");
                    itemPrice.setText(Html.fromHtml(priceString));
                } else {
                    itemPrice.setText(getString(R.string.order_price, foodInfo[1]));
                }
                itemPrice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[2]));
                itemPrice.setGravity(Gravity.CENTER);
                itemPrice.setTextSize(STP(R.dimen.normal_text_size));

                ImageButton itemAdd = new ImageButton(this);
                itemAdd.setScaleType(ImageView.ScaleType.FIT_CENTER);
                itemAdd.setImageDrawable(r.getDrawable(R.drawable.cart));
                itemAdd.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.order_button_height), weights[3]));
                itemAdd.setTag(foodInfo);

                itemAdd.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createDialog(view, (String[]) view.getTag());
                    }
                });

                orderableItem.addView(itemPrice);
                orderableItem.addView(itemAdd);

                scrollLayout.addView(orderableItem);
                orderableItemsInList.add(orderableItem);
            }
        }

        final ScrollView scrollView = findViewById(R.id.orderScrollView);
        final ImageView canScrollUp = findViewById(R.id.orderCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.orderCanScrollDown);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });
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

            if (isTablet) {
                // Increase offset between edges of dialog and content
                ConstraintLayout dialogLayout = dialogView.findViewById(R.id.orderDialogLayout);
                ConstraintSet test = new ConstraintSet();
                test.clone(dialogLayout);
                test.setMargin(R.id.orderDialogButtonsLayout, ConstraintSet.BOTTOM, DTPDirect(96));
                test.setMargin(R.id.orderDialogTitle, ConstraintSet.TOP, DTPDirect(96));
                test.applyTo(dialogLayout);
            }

            TextView dialogTitle = dialogView.findViewById(R.id.orderDialogTitle);
            dialogTitle.setText(getString(R.string.dialog_title, foodInfo[0]));

            TextView dialogUnit = dialogView.findViewById(R.id.orderDialogUnit);
            String unit = foodInfo[2].replace("one ", "");
            dialogUnit.setText(getString(R.string.order_unit, foodInfo[1], unit));

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

            Button addButton = dialogView.findViewById(R.id.orderDialogOkButton);
            String foodInfoStr = foodInfo[0] + "," + foodInfo[1]; // Name and price, resp.
            addButton.setTag(foodInfoStr);
            addButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String foodString = (String) view.getTag();
                    EditText input = dialog.getContentView().findViewById(R.id.orderDialogNumberField);
                    int inputNumber = Integer.parseInt(input.getText().toString());
                    int numberInCart = 0; // How many of this item are already in the cart
                    int indexInCart = -1; // Position of this item that's already in the cart list

                    // Scan the cart to see if this item is already in it.
                    for (String item : itemsInCart) {
                        if (item.contains(foodString)) {
                            indexInCart = itemsInCart.indexOf(item);

                            String numInCartStr = item.split(",")[2];
                            numberInCart = Integer.parseInt(numInCartStr);
                        }
                    }

                    if (indexInCart != -1) {
                        // Some items already in the cart, so add them together
                        itemsInCart.set(indexInCart, foodString + "," + (inputNumber + numberInCart));
                    } else {
                        // Item is not in the cart yet, so add it
                        itemsInCart.add(foodString + "," + inputNumber);
                    }

                    dialog.dismiss();
                    blur.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                }
            });
        }
    }

    private void populateCart() {
        LinearLayout cartScrollLayout = findViewById(R.id.cartScrollLayout);
        float total = 0;
        Collections.sort(itemsInCart);

        // Gives the items in each listing different weights depending on layout
        float[] phoneWeights = {4.5f, 1, 2, 1};
        float[] tabletWeights = {4.2f, 0.7f, 2, 0.7f};
        float[] weights = isTablet ? tabletWeights : phoneWeights;

        // This is only used for the tablet
        Button minusButton = null;

        for (String item : itemsInCart) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DTP(R.dimen.cart_item_height)));

            String[] itemInfo = item.split(",");

            TextView itemName = new TextView(this);
            itemName.setText(itemInfo[0]);
            itemName.setTextSize(STP(R.dimen.normal_text_size));
            itemName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[0]));
            itemLayout.addView(itemName);

            TextView itemNum = new TextView(this);
            itemNum.setText(itemInfo[2]);
            itemNum.setTextSize(STP(R.dimen.normal_text_size));
            itemNum.setTag(item);
            itemNum.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[1]));

            if (isTablet) {
                minusButton = new Button(this);
                minusButton.setText(R.string.minus);
                minusButton.setTextSize(STP(R.dimen.small_text_size));
                minusButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));
                minusButton.setTag(itemNum);

                minusButton.setEnabled(Integer.parseInt(itemInfo[2]) != 1);

                minusButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView itemNum = (TextView) view.getTag();
                        int num = Integer.parseInt(itemNum.getText().toString());

                        itemNum.setText(getString(R.string.number, --num));

                        String cartElement = (String) itemNum.getTag();
                        int index = itemsInCart.indexOf(cartElement);
                        String[] cartElements = cartElement.split(",");
                        itemsInCart.set(index, cartElements[0] + "," + cartElements[1] + "," + num);

                        if (num == 1) {
                            // If there's only one item left on our checkout list, don't let us remove any more this way
                            view.setEnabled(false);
                        }
                    }
                });

                itemLayout.addView(minusButton);
            }

            // Had to add the minus button before we could do this
            itemLayout.addView(itemNum);

            if (isTablet) {
                itemNum.setGravity(Gravity.CENTER);

                Button plusButton = new Button(this);
                plusButton.setText(R.string.plus);
                plusButton.setTextSize(STP(R.dimen.small_text_size));
                plusButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));
                plusButton.setTag(minusButton);

                plusButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button minusButton = (Button) view.getTag();
                        TextView itemNum = (TextView) minusButton.getTag();
                        int num = Integer.parseInt(itemNum.getText().toString());

                        itemNum.setText(getString(R.string.number, ++num));

                        String cartElement = (String) itemNum.getTag();
                        int index = itemsInCart.indexOf(cartElement);
                        String[] cartElements = cartElement.split(",");
                        itemsInCart.set(index, cartElements[0] + "," + cartElements[1] + "," + num);

                        if (num > 1) {
                            // If we increased from one item, we can subtract now
                            minusButton.setEnabled(true);
                        }
                    }
                });

                TextView priceEachText = new TextView(this);
                priceEachText.setText(getString(R.string.order_price, itemInfo[1]));
                priceEachText.setTextSize(STP(R.dimen.normal_text_size));
                priceEachText.setGravity(Gravity.END);
                priceEachText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

                itemLayout.addView(plusButton);
                itemLayout.addView(priceEachText);
            }

            TextView itemPrice = new TextView(this);
            float price = Float.parseFloat(itemInfo[1]) * Integer.parseInt(itemInfo[2]);
            total += price;
            itemPrice.setText(getResources().getString(R.string.order_price, getPriceString(price)));
            itemPrice.setTextSize(STP(R.dimen.normal_text_size));
            itemPrice.setGravity(Gravity.END); // right
            itemPrice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[2]));

            Space space = new Space(this);
            space.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0.5f));

            ImageButton removeButton = new ImageButton(this);
            removeButton.setScaleType(ImageButton.ScaleType.CENTER);
            removeButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_delete));
            removeButton.setLayoutParams(new LinearLayout.LayoutParams(0, DTP(R.dimen.cart_subitem_height), weights[3]));
            removeButton.setTag(item);

            removeButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemsInCart.remove((String) view.getTag());

                    for (LinearLayout layout : orderableItemsInList) {
                        layout.setVisibility(View.GONE);
                    }

                    orderableItemsInList.clear();
                    populateCart();
                }
            });

            itemLayout.addView(itemPrice);
            itemLayout.addView(space);
            itemLayout.addView(removeButton);

            cartScrollLayout.addView(itemLayout);
            orderableItemsInList.add(itemLayout);
        }

        TextView totalText = findViewById(R.id.cartTotalAmount);
        totalText.setText(getString(R.string.order_price, getPriceString(total)));
        cartTotal = total;

        Button checkoutButton = findViewById(R.id.cartCheckoutButton);
        checkoutButton.setEnabled(!itemsInCart.isEmpty());

        findViewById(R.id.cartEmptyLabel).setVisibility(itemsInCart.isEmpty() ? View.VISIBLE : View.INVISIBLE);

        final ScrollView scrollView = findViewById(R.id.cartScrollView);
        final ImageView canScrollUp = findViewById(R.id.cartCanScrollUp);
        final ImageView canScrollDown = findViewById(R.id.cartCanScrollDown);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                canScrollDown.setAlpha(scrollView.canScrollVertically(1) ? 1f : 0.25f);
                canScrollUp.setAlpha(scrollView.canScrollVertically(-1) ? 1f : 0.25f);
            }
        });
    }

    private String getPriceString(float price) {
        if (price == 0) {
            return "0.00";
        } else {
            int temp = Math.round(price * 100f);
            boolean evenDime = temp % 10 == 0;
            price = temp / 100f;

            return price + (evenDime ? "0" : "");
        }
    }

}



