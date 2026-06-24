package jas.school.searchUI;

import com.androix.model.Identifiable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jas.school.app.F;

/**
 * Purpose: opt-in SearchUI variant that adds multi-selection support on top of ModelSearchUI.
 */
public class SelectableModelSearchUI extends ModelSearchUI {
    // Selection state is stored per SearchUI tag so each selectable search screen keeps its own mapping.
    private static final String SELECTION_STATE_SUFFIX = ".selectionState";

    /**
     * Returns the stable unique key used to track selection for a list item.
     *
     * <p>The default implementation supports entity-backed rows by using the
     * persisted {@code id}. For non-entity list items, override this method
     * and return a key that is both unique and stable for the same logical row.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Override
     * public String getMappingKey(Object item) {
     *     AppMenuOption option = (AppMenuOption) item;
     *     return option.getCode();
     * }
     * }</pre>
     *
     * @param item row item currently shown by the SearchUI
     * @return stable unique key for selection mapping
     */
    public String getMappingKey(Object item) {
        if(item instanceof Identifiable) {
            // Default mapping for entity-backed rows uses the persisted identity.
            return ((Identifiable) item).getId().toString();
        }else{
            // Non-entity search screens must override this with their own stable unique key strategy.
            throw new IllegalArgumentException("Override to provide a unique key for selection mapping");
        }
    }

    /**
     * Replaces adapter items and automatically removes any stale selected keys.
     *
     * <p>This keeps selection consistent when the list is reloaded, filtered by a
     * new data source, or replaced entirely.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * List<Student> students = F.findAll(filter);
     * searchUI.setItems(students);
     * }</pre>
     *
     * @param items latest authoritative items for the SearchUI
     */
    @Override
    public void setItems(List items) {
        super.setItems(items);
        // Whenever the backing items change, prune selection to only keys that still exist.
        retainSelectionOnlyFor(items);
    }

    /**
     * Returns the selection state container for the current SearchUI tag.
     *
     * <p>The state is scoped by UI tag so different SearchUI screens do not share
     * the same selected-key set.</p>
     *
     * <p>Application code usually should not manipulate this object directly.
     * Prefer higher-level methods such as {@link #isItemSelected(Object)},
     * {@link #setItemSelected(Object, boolean)}, and {@link #getSelectedItems()}.</p>
     *
     * @return selection state for the current SearchUI instance
     */
    public SearchUISelectionState getSelectionState() {
        String stateKey = thisUITag + SELECTION_STATE_SUFFIX;
        // The SearchUI tag scopes selection so two search screens do not accidentally share state.
        SearchUISelectionState selectionState = (SearchUISelectionState) F.getFromAppContext(stateKey);
        if (selectionState == null) {
            selectionState = new SearchUISelectionState();
            F.putToAppContext(stateKey, selectionState);
        }
        return selectionState;
    }

    /**
     * Checks whether the given item is currently selected.
     *
     * <p>This is mainly used by row binders when restoring checkbox state.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * checkbox.setChecked(searchUI.isItemSelected(student));
     * }</pre>
     *
     * @param item row item to test
     * @return true if the item's mapping key is currently selected
     */
    public boolean isItemSelected(Object item) {
        return getSelectionState().isSelected(getMappingKey(item));
    }

    /**
     * Marks an item as selected or unselected using its mapping key.
     *
     * <p>Rows should call this instead of updating {@link SearchUISelectionState}
     * directly.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
     *     searchUI.setItemSelected(student, isChecked);
     * });
     * }</pre>
     *
     * @param item row item whose selection should be updated
     * @param checked whether the item should be selected
     */
    public void setItemSelected(Object item, boolean checked) {
        // Rows call this method instead of touching SearchUISelectionState directly.
        getSelectionState().setSelected(getMappingKey(item), checked);
    }

    /**
     * Clears all currently selected items for this SearchUI.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * searchUI.clearSelection();
     * }</pre>
     */
    public void clearSelection() {
        getSelectionState().clear();
    }

    /**
     * Returns the currently selected row objects.
     *
     * <p>This method converts stored selection keys back into actual domain
     * objects by scanning the SearchUI adapter's full item list. It allows
     * actions to work with selected objects directly without knowing how
     * selection keys are tracked.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * List<Student> selectedStudents = studentsOfSectionUI.getSelectedItems();
     * for (Student student : selectedStudents) {
     *     // apply batch action
     * }
     * }</pre>
     *
     * @param <T> expected item type
     * @return selected row objects from the current SearchUI
     */
    public <T> List<T> getSelectedItems() {
        List<T> selectedItems = new ArrayList<>();
        if (searchUIModel == null || searchUIModel.getAllItems() == null) {
            return selectedItems;
        }

        // Use the adapter's full item list so selection survives temporary search filtering.
        Set<String> selectedKeys = getSelectionState().getSelectedKeys();
        // Project the stored key selection back to domain objects so actions can stay selection-agnostic.
        for (Object item : searchUIModel.getAllItems()) {
            if (selectedKeys.contains(getMappingKey(item))) {
                selectedItems.add((T) item);
            }
        }
        return selectedItems;
    }

    private void retainSelectionOnlyFor(List items) {
        if (items == null) {
            clearSelection();
            return;
        }

        Set<String> validSelectionKeys = new LinkedHashSet<>();
        // Build the authoritative key set from the latest items and drop any stale selections.
        for (Object item : items) {
            validSelectionKeys.add(getMappingKey(item));
        }
        getSelectionState().retainOnly(validSelectionKeys);
    }
}
