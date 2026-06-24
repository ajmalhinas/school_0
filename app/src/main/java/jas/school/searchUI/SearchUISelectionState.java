package jas.school.searchUI;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Purpose: keep UI-owned multi-selection state independent from rendered row models.
 */
public class SearchUISelectionState {
    // Keys represent logical row identity, not RecyclerView position.
    private final Set<String> selectedKeys = new LinkedHashSet<>();

    public boolean isSelected(String key) {
        return selectedKeys.contains(key);
    }

    public void setSelected(String key, boolean checked) {
        // This helper stores only identity keys; it does not know anything about row objects.
        if (checked) {
            selectedKeys.add(key);
        } else {
            selectedKeys.remove(key);
        }
    }

    public void toggle(String key) {
        if (isSelected(key)) {
            selectedKeys.remove(key);
        } else {
            setSelected(key, true);
        }
    }

    public void clear() {
        selectedKeys.clear();
    }

    public void retainOnly(Collection<String> validKeys) {
        // After reload/filter changes, drop any selection whose logical row no longer exists.
        selectedKeys.retainAll(validKeys);
    }

    public int count() {
        return selectedKeys.size();
    }

    public boolean isEmpty() {
        return selectedKeys.isEmpty();
    }

    public Set<String> getSelectedKeys() {
        // Return a copy so callers cannot mutate the stored selection accidentally.
        return new LinkedHashSet<>(selectedKeys);
    }
}
