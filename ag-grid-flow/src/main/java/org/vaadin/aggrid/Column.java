package org.vaadin.aggrid;

/*
 * #%L
 * ag-grid-flow
 * %%
 * Copyright (C) 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.internal.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jcgueriaud
 */
public class Column<T> extends AbstractColumn<T, Column<T>> {

    private static final Logger log = LoggerFactory.getLogger(Column.class);

    public enum FilterColumn {
        text("agTextColumnFilter"),
        number("agNumberColumnFilter"),
        date("agDateColumnFilter");

        private String value;

        FilterColumn(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    @JsonProperty("field")
    private String columnKey; // defined and used by the user

    private boolean sortingEnabled;

    private SerializableComparator<T> comparator;

    private ValueProvider<T, ?> valueProvider;

    /// todo JCG String is hardcoded, to change
    private Setter<T, String> setter;

    private SerializableFunction<T, String> classNameGenerator = item -> null;

    private DataGenerator<T> dataGenerator;

    private Integer width;

    private String cellRenderer;

    private String cellRendererFramework;

    private String cellEditor;
    private Boolean editable = false;

    private boolean resizable;
    private String cellClass;
    private Map<String, String> cellClassRules = new HashMap<>();

    private String filter;

    private Map<String, RendererActionHandler<T>> actionHandlers = new HashMap<>();


    public Column(String columnKey, ValueProvider<T, ?> valueProvider) {
        this.columnKey = columnKey;
        this.valueProvider = valueProvider;
        dataGenerator = (item, data) -> data.put(columnKey,
                JsonSerializer.toJson(valueProvider.apply(item)));
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public String getCellClass() {
        return cellClass;
    }

    public void setCellClass(String cellClass) {
        this.cellClass = cellClass;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getCellRenderer() {
        return cellRenderer;
    }

    public Column<T> setCellRenderer(String cellRenderer) {
        this.cellRenderer = cellRenderer;
        return this;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public boolean isSortingEnabled() {
        return sortingEnabled;
    }

    public void setSortingEnabled(boolean sortingEnabled) {
        this.sortingEnabled = sortingEnabled;
    }

    public SerializableComparator<T> getComparator(
            SortDirection sortDirection) {
        Objects.requireNonNull(comparator,
                "No comparator defined for sorted column.");
        setSortable(true);
        boolean reverse = sortDirection != SortDirection.ASCENDING;
        return reverse ? comparator.reversed()::compare : comparator;
    }

    public <V extends Comparable<? super V>> Column<T> setComparator(
            ValueProvider<T, V> keyExtractor) {
        Objects.requireNonNull(keyExtractor,
                "Key extractor must not be null");
        setComparator(Comparator.comparing(keyExtractor,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return this;
    }

    public Column<T> setComparator(Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "Comparator must not be null");
        setSortable(true);
        this.comparator = comparator::compare;
        return this;
    }

    public ValueProvider<T, ?> getValueProvider() {
        return valueProvider;
    }

    public void setValueProvider(ValueProvider<T, ?> valueProvider) {
        this.valueProvider = valueProvider;
    }

    public SerializableFunction<T, String> getClassNameGenerator() {
        return classNameGenerator;
    }

    public void setClassNameGenerator(SerializableFunction<T, String> classNameGenerator) {
        this.classNameGenerator = classNameGenerator;
    }

    public DataGenerator<T> getDataGenerator() {
        return dataGenerator;
    }

    public void setCellClassRules(String cellClass, String cellClassRules) {
        this.cellClassRules.put(cellClass, cellClassRules);
    }

    public Map<String, String> getCellClassRules() {
        return cellClassRules;
    }

    public String getFilter() {
        return filter;
    }

    public Column<T> setFilter(String filter) {
        this.filter = filter.toString();
        return this;
    }

    public String getCellEditor() {
        return cellEditor;
    }

    //* TODO JCG Requires a Setter to set the value on the server side
    // when the cell value has been edited **/
    public Column<T> setCellEditor(String cellEditor) {
        this.cellEditor = cellEditor;
        if (cellEditor != null) {
            editable = true;
        }
        return this;
    }

    public Boolean getEditable() {
        return editable;
    }

    public Setter<T, String> getSetter() {
        return setter;
    }

    public Column<T> setSetter(Setter<T, String> setter) {
        this.setter = setter;
        return this;
    }

    public String getCellRendererFramework() {
        return cellRendererFramework;
    }

    public Column<T> setCellRendererFramework(String cellRendererFramework) {
        this.cellRendererFramework = cellRendererFramework;
        return this;
    }

    public SerializableComparator<T> getComparator() {
        return comparator;
    }

    /**
     * Link an action actionName to a handler
     *
     * @param actionName name of the action used in the client side cellRendererFrameworkCallback
     * @param actionHandler action to run
     *
     * @return the column
     */
    public Column<T> withActionHandler(String actionName, RendererActionHandler<T> actionHandler) {
        actionHandlers.put(actionName, actionHandler);
        return this;
    }

    protected void runActionHandler(String actionName, T rowItem) {
        if (actionHandlers.containsKey(actionName)) {
            actionHandlers.get(actionName).execute(rowItem);
        } else {
            log.warn("ActionName {} does not exist for the column {}", actionName, columnKey);
        }
    }
}