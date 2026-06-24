package jas.school.searchUI;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androix.AbstractSearchUI;
import com.androix.NPersistence;
import com.androix.SearchUIModel;
import com.javax.util.CommonUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import jas.school.app.F;
import jas.school.app.R;


/**
 * Purpose: reusable SearchUI implementation for both database-backed and in-memory lists.
 * Handles registration, item loading, and refresh behavior for the app's searchable screens.
 */
public class ModelSearchUI extends AbstractSearchUI {
    private final static String TAG = "ModelSearchUI";
    protected static Class itemsClazz;
    protected static String query;
    protected static List itemsList;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        super.onCreateView(inflater, container, savedInstanceState);
        Map createParams = allCreateParams.get(thisUITag);
        if (createParams != null) {
            // Restore the registration parameters for the currently opened SearchUI tag.
            ModelSearchUI.itemsClazz = (Class) createParams.get("itemsClazz");
            ModelSearchUI.query = (String) createParams.get("query");
            ModelSearchUI.itemsList = (List) createParams.get("itemsList");
            if (itemsClazz != null) {
                title = itemsClazz.getSimpleName();
            }

        }
        return inflater.inflate(R.layout.search_ui, container, false);
    }

    /**
     * Registers a model-backed SearchUI configuration.
     *
     * <p>Use this when the list should be loaded from the database using the given
     * {@code itemsClazz} and optional {@code query} </p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * ModelSearchUI.registerSearchUI(
     *     "CustomerSearch",
     *     R.layout.search_ui,
     *     R.layout.customer_row,
     *     CustomerRow.class,
     *     Customer.class,
     *     CustomerDetailsUI.class,
     *     "status = 'ACTIVE'",
     *     1
     * );
     * }</pre>
     *
     * @param tag        A unique tag to identify the SearchUI configuration
     * @param searchUIId The layout resource for the SearchUI
     * @param cardLayoutId The layout resource for each row/card
     * @param rowClazz   The row view holder class
     * @param itemsClazz The entity model class to load from DB
     * @param nextUI     The next UI to navigate to on selection
     * @param query      Optional SQL WHERE/fragment to filter DB results (nullable)
     * @param columns    Grid column count (1 for list), 2 for 2 column card like view
     */
    public static void registerDBSearchUI(String tag, int searchUIId, int cardLayoutId, Class rowClazz, Class itemsClazz, Class nextUI, String query, int columns) {
        // All SearchUI-based screens share the same toolbar search menu defined in `menu_search.xml`.
        registerSearchUI(tag, searchUIId, R.menu.menu_search, R.id.txtsearch, R.id.rvRowHolder,
                cardLayoutId, rowClazz, nextUI, columns);
        Map createParams = allCreateParams.get(tag);
        createParams.put("itemsClazz", itemsClazz);
        createParams.put("query", query);
        ModelSearchUI.itemsClazz = itemsClazz;
        ModelSearchUI.query = query;
        ModelSearchUI.itemsList = null; // Reset
    }

    /**
     * Registers a list-backed SearchUI configuration.
     *
     * <p>Use this when the list is already available in memory and you want to display it
     * without hitting the database.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * List<Customer> recentCustomers = ...;
     * ModelSearchUI.registerSearchUI(
     *     "RecentCustomers",
     *     R.layout.search_ui,
     *     R.layout.customer_row,
     *     CustomerRow.class,
     *     recentCustomers,
     *     CustomerDetailsUI.class,
     *     1
     * );
     * }</pre>
     *
     * @param tag        A unique tag to identify the SearchUI configuration
     * @param searchUIId The layout resource for the SearchUI
     * @param cardLayoutId The layout resource for each row/card
     * @param rowClazz   The row view holder class
     * @param itemsList  The in-memory list to display
     * @param nextUI     The next UI to navigate to on selection
     * @param columns    Grid column count (1 for list) 2 for 2 column card like view
     */
    public static void registerListSearchUI(String tag, int searchUIId, int cardLayoutId, Class rowClazz, List itemsList, Class nextUI, int columns) {
        // List-backed SearchUI screens reuse the same SearchView menu as DB-backed ones.
        registerSearchUI(tag, searchUIId, R.menu.menu_search, R.id.txtsearch, R.id.rvRowHolder,
                cardLayoutId, rowClazz, nextUI, columns);
        Map createParams = allCreateParams.get(tag);
        createParams.put("itemsList", itemsList);
        ModelSearchUI.itemsClazz = null;
        ModelSearchUI.query = null;
        ModelSearchUI.itemsList = itemsList;
    }

    public static void registerDBSearchUI(String tag, int searchUIId, int cardLayoutId, Class rowClazz, Class itemsClazz, Class nextUI, int columns) {
        registerDBSearchUI(tag, searchUIId, cardLayoutId, rowClazz, itemsClazz, nextUI, null, columns);
    }


    public void setup() {
        try {
            searchUIModel = new SearchUIModel(this, cardLayoutId, rowClazz);
            recyclerView.setAdapter(searchUIModel);
            if (itemsList != null) {
                // List-backed SearchUIs skip persistence and render the provided items directly.
                setItems(itemsList);
            } else if (query == null) {
                // Fallback: unfiltered "show all" for the model.
                // This is mainly useful for quick demos; real screens usually provide a query or list.
                setItems(F.retriveAll(itemsClazz));
            } else if (itemsClazz == null) {
                // A raw query is used when the result is not a straight entity fetch.
                setItems(NPersistence.retrieve(query).getRows());
            } else {
                // Standard filtered entity load, for example "remainingSlots > 0".
                setItems(NPersistence.retrieve(itemsClazz, query));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Replaces the data source by applying a new persistence query and reloading.
     *
     * <p> It changes the backing data source
     * and triggers a reload from persistence.</p>
     *
     * <p><b>Example scenarios:</b></p>
     * <pre>{@code
     * // 1) Show only active customers (DB-backed)
     * ModelSearchUI ui = ...;
     * ui.setDataSource("status = 'ACTIVE'");
     *
     * // 2) Apply a user-selected date range
     * ui.setDataSource("invoiceDate >= '2026-01-01' AND invoiceDate <= '2026-01-31'");
     * }</pre>
     */
    public void setDataSource(String query) {
        this.query = query;
        // Once a query takes over, the old in-memory list should no longer drive the screen.
        this.itemsList = null;
        reload();
    }

    /**
     * Replaces the data source with a direct list and shows it immediately.
     *
     * <p> It swaps out the data source
     * to a provided list and updates the adapter.</p>
     *
     * <p><b>Example scenarios:</b></p>
     * <pre>{@code
     * // 1) Show a precomputed list (e.g., from an aggregation or custom API)
     * List<Customer> topCustomers = ...;
     * ui.setDataSource(topCustomers);
     *
     * // 2) Show items already loaded in memory (skip DB round-trip)
     * ui.setDataSource(cachedItems);
     * }</pre>
     */
    public void setDataSource(List itemsList) {
        this.query = null;
        this.itemsClazz = null;
        this.itemsList = itemsList;
        // For direct lists we can refresh immediately without another DB round-trip.
        setItems(itemsList);
    }

    /**
     * Reload the items from db or direct list
     */
    public void reload() {
        try {
            if (itemsList != null) {
                setItems(itemsList);
            } else if (query == null) {
                setItems(F.retriveAll(itemsClazz));
            } else if (itemsClazz == null) {
                setItems(NPersistence.retrieve(query).getRows());
            } else {
                long t0 = System.nanoTime();
                setItems(NPersistence.retrieve(itemsClazz, query));
                long t1 = System.nanoTime();
                Log.d(TAG, "Reload time in ms: " + CommonUtils.delayInMs(t0, t1));

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void onShow() {
        Map createParams = allCreateParams.get(thisUITag);
        if (createParams != null) {
            // Refresh static backing fields in case another SearchUI registration ran earlier.
            ModelSearchUI.itemsClazz = (Class) createParams.get("itemsClazz");
            ModelSearchUI.query = (String) createParams.get("query");
            ModelSearchUI.itemsList = (List) createParams.get("itemsList");
        }
        super.onShow();
    }

}
