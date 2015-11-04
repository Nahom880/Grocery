package com.raizlabs.android.dbflow.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListenerAdapter;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModel;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelHelper;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.sql.ModelQueriable;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.cache.ModelLruCache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Author: andrewgrosner
 * Description: Operates very similiar to a {@link java.util.List} except its backed by a table cursor. All of
 * the {@link java.util.List} modifications default to the main thread, but it can be set to
 * run on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}. Register a {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener}
 * on this list to know when the results complete. NOTE: any modifications to this list will be reflected
 * on the underlying table.
 */
public class FlowTableList<ModelClass extends Model> extends ContentObserver implements List<ModelClass> {

    /**
     * We use high priority to assume that this list is used in some visual aspect.
     */
    private static DBTransactionInfo MODIFICATION_INFO = DBTransactionInfo.create(BaseTransaction.PRIORITY_HIGH);

    /**
     * Holds the table cursor
     */
    private FlowCursorList<ModelClass> mCursorList;

    private TransactionListener<List<ModelClass>> mTransactionListener;
    private TransactionListener<List<ModelClass>> mInternalTransactionListener = new TransactionListenerAdapter<List<ModelClass>>() {
        @Override
        public void onResultReceived(List<ModelClass> modelClasses) {
            refresh();

            if (mTransactionListener != null) {
                mTransactionListener.onResultReceived(modelClasses);
            }
        }
    };
    /**
     * If true, we will make all modifications on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}, else
     * we will run it on the main thread.
     */
    private boolean transact = false;

    /**
     * Constructs an instance of this list with the specified conditions.
     *
     * @param table      The table to load into this list.
     * @param conditions The set of conditions to use when querying the DB.
     */
    public FlowTableList(Class<ModelClass> table, Condition... conditions) {
        super(null);
        mCursorList = new FlowCursorList<ModelClass>(true, table, conditions) {
            @Override
            protected ModelCache<ModelClass, ?> getBackingCache() {
                return FlowTableList.this.getBackingCache(getCacheSize());
            }
        };
    }

    /**
     * Constructs an instance of this list with the specfied {@link com.raizlabs.android.dbflow.sql.ModelQueriable} object.
     *
     * @param modelQueriable The object that can query from a database.
     */
    public FlowTableList(ModelQueriable<ModelClass> modelQueriable) {
        super(null);
        mCursorList = new FlowCursorList<ModelClass>(transact, modelQueriable) {
            @Override
            protected ModelCache<ModelClass, ?> getBackingCache() {
                return FlowTableList.this.getBackingCache(getCacheSize());
            }
        };
    }

    /**
     * @param count The size of the underlying {@link com.raizlabs.android.dbflow.list.FlowCursorList}
     * @return The cache backing this query. Override to provide a custom {@link com.raizlabs.android.dbflow.structure.cache.ModelCache}
     * instead. If the count is somehow 0, it will default to a size of 50.
     * If you override this method, be careful to call an empty cache to the {@link com.raizlabs.android.dbflow.structure.cache.ModelLruCache}
     */
    public ModelCache<ModelClass, ?> getBackingCache(int count) {
        return new ModelLruCache<>(count);
    }

    /**
     * Called when the count for the underlying cache is needed.
     *
     * @return 50 as default. Override for different.
     */
    public int getCacheSize() {
        return 50;
    }

    /**
     * Registers the list for model change events
     */
    public void registerForContentChanges(Context context) {
        context.getContentResolver().registerContentObserver(SqlUtils.getNotificationUri(mCursorList.getTable(), null), true, this);
    }

    /**
     * Unregisters this list for model change events
     */
    public void unregisterForContentChanges(Context context) {
        context.getContentResolver().unregisterContentObserver(this);
    }

    @Override
    public void onChange(boolean selfChange) {
        mCursorList.refresh();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        mCursorList.refresh();
    }

    /**
     * Register for callbacks when data is changed on this list.
     *
     * @param transactionListener
     */
    public void setModificationReceiver(TransactionListener<List<ModelClass>> transactionListener) {
        mTransactionListener = transactionListener;
    }

    /**
     * If true, we will transact all modifications on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     *
     * @param transact true to transact all modifications in the background.
     */
    public void setTransact(boolean transact) {
        this.transact = transact;
    }

    /**
     * @return a mutable list that does not reflect changes on the underlying DB.
     */
    public List<ModelClass> getCopy() {
        return mCursorList.getAll();
    }

    /**
     * @return The {@link com.raizlabs.android.dbflow.list.FlowCursorList} that backs this table list.
     */
    public FlowCursorList<ModelClass> getCursorList() {
        return mCursorList;
    }

    /**
     * Refreshes the content backing this list.
     */
    public void refresh() {
        mCursorList.refresh();
    }

    /**
     * Adds an item to this table, but does not allow positonal insertion. Same as calling
     * {@link #add(com.raizlabs.android.dbflow.structure.Model)}
     *
     * @param location Not used.
     * @param model    The model to save
     */
    @Override
    public void add(int location, ModelClass model) {
        add(model);
    }

    /**
     * Internal helper method for constructing {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}
     *
     * @param modelClasses
     * @return
     */
    @SafeVarargs
    protected final ProcessModelInfo<ModelClass> getProcessModelInfo(ModelClass... modelClasses) {
        return ProcessModelInfo.withModels(modelClasses).result(mInternalTransactionListener).info(MODIFICATION_INFO);
    }

    /**
     * Helper method for constructing {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}. Override
     * for custom processing.
     *
     * @param modelCollection The list of models to add to the {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}
     * @return The shared info for this table.
     */
    protected final ProcessModelInfo<ModelClass> getProcessModelInfo(Collection<ModelClass> modelCollection) {
        return ProcessModelInfo.withModels(modelCollection).result(mInternalTransactionListener).info(MODIFICATION_INFO);
    }

    /**
     * Adds an item to this table
     *
     * @param model The model to save
     * @return always true
     */
    @Override
    public boolean add(ModelClass model) {
        if (transact) {
            TransactionManager.getInstance().save(getProcessModelInfo(model));
        } else {
            model.save(false);
            mInternalTransactionListener.onResultReceived(Arrays.asList(model));
        }
        return true;
    }

    /**
     * Adds all items to this table, but
     * does not allow positional insertion. Same as calling {@link #addAll(java.util.Collection)}
     *
     * @param location   Not used.
     * @param collection The list of items to add to the table
     * @return always true
     */
    @Override
    public boolean addAll(int location, Collection<? extends ModelClass> collection) {
        return addAll(collection);
    }

    /**
     * Adds all items to this table.
     *
     * @param collection The list of items to add to the table
     * @return always true
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection<? extends ModelClass> collection) {
        // cast to normal collection, we do not want subclasses of this table saved
        final Collection<ModelClass> tmpCollection = (Collection<ModelClass>) collection;
        if (transact) {
            TransactionManager.getInstance().save(getProcessModelInfo(tmpCollection));
        } else {
            ProcessModelHelper.process(mCursorList.getTable(), tmpCollection, new ProcessModel<ModelClass>() {
                @Override
                public void processModel(ModelClass model) {
                    model.save(false);
                }
            });
            mInternalTransactionListener.onResultReceived((List<ModelClass>) tmpCollection);
        }
        return true;
    }

    /**
     * Deletes all items from the table. Be careful as this will clear data!
     */
    @Override
    public void clear() {
        if (transact) {
            TransactionManager.getInstance().delete(MODIFICATION_INFO, mCursorList.getTable());
        } else {
            Delete.table(mCursorList.getTable());
        }
        mInternalTransactionListener.onResultReceived(null);
    }

    /**
     * Checks to see if the table contains the object only if its a {@link ModelClass}
     *
     * @param object A model class. For interface purposes, this must be an Object.
     * @return always false if its anything other than the current table. True if {@link com.raizlabs.android.dbflow.structure.Model#exists()} passes.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object object) {
        boolean contains = false;
        if (mCursorList.getTable().isAssignableFrom(object.getClass())) {
            ModelClass model = ((ModelClass) object);
            contains = model.exists();
        }

        return contains;
    }

    /**
     * If the collection is null or empty, we return false.
     *
     * @param collection The collection to check if all exist within the table.
     * @return true if all items exist in table, false if at least one fails.
     */
    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        boolean contains = !(collection.isEmpty());
        if (contains) {
            for (Object o : collection) {
                if (!contains(o)) {
                    contains = false;
                    break;
                }
            }
        }
        return contains;
    }

    /**
     * Returns the item from the backing {@link com.raizlabs.android.dbflow.list.FlowCursorList}. First call
     * will load the model from the cursor, while subsequent calls will use the cache.
     *
     * @param row the row from the internal {@link com.raizlabs.android.dbflow.list.FlowCursorList} query that we use.
     * @return A model converted from the internal {@link com.raizlabs.android.dbflow.list.FlowCursorList}. For
     * performance improvements, ensure caching is turned on.
     */
    @Override
    public ModelClass get(int row) {
        return mCursorList.getItem(row);
    }

    @Override
    public int indexOf(Object object) {
        throw new UnsupportedOperationException("We cannot determine which index in the table this item exists at efficiently");
    }

    @Override
    public boolean isEmpty() {
        return mCursorList.isEmpty();
    }

    /**
     * @return An iterator from {@link #getAll(com.raizlabs.android.dbflow.sql.builder.Condition...)}.
     * Be careful as this method will convert all data under this table into a list of {@link ModelClass} in the UI thread.
     */
    @NonNull
    @Override
    public Iterator<ModelClass> iterator() {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        throw new UnsupportedOperationException("We cannot determine which index in the table this item exists at efficiently");
    }

    /**
     * @return A list iterator from the {@link #getAll(com.raizlabs.android.dbflow.sql.builder.Condition...)}.
     * Be careful as this method will convert all data under this table into a list of {@link ModelClass} in the UI thread.
     */
    @NonNull
    @Override
    public ListIterator<ModelClass> listIterator() {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.listIterator();
    }

    /**
     * @param location The index to start the iterator.
     * @return A list iterator from the {@link #getAll(com.raizlabs.android.dbflow.sql.builder.Condition...)}.
     * Be careful as this method will convert all data under this table into a list of {@link ModelClass} in the UI thread.
     */
    @NonNull
    @Override
    public ListIterator<ModelClass> listIterator(int location) {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.listIterator(location);
    }

    /**
     * Removes the {@link ModelClass} from its table on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} .
     * If {@link #transact} is true, the delete does not happen immediately.
     *
     * @param location The location within the table to remove the item from
     * @return The removed item.
     */
    @Override
    public ModelClass remove(int location) {
        ModelClass model = mCursorList.getItem(location);
        if (transact) {
            TransactionManager.getInstance().delete(getProcessModelInfo(model));
        } else {
            model.delete(false);
            mInternalTransactionListener.onResultReceived(Arrays.asList(model));
        }
        return model;
    }

    /**
     * Removes an item from this table on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} if
     * {@link #transact} is true.
     *
     * @param object A model class. For interface purposes, this must be an Object.
     * @return true if the item was removed. Always false if the object is not from the same table as this list.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object object) {
        boolean removed = false;

        // if its a ModelClass
        if (mCursorList.getTable().isAssignableFrom(object.getClass())) {
            ModelClass model = ((ModelClass) object);
            if (transact) {
                TransactionManager.getInstance().delete(getProcessModelInfo(model));
            } else {
                model.delete(false);
                mInternalTransactionListener.onResultReceived(Arrays.asList(model));
            }
            removed = true;
        }

        return removed;
    }

    /**
     * Removes all items from this table in one transaction based on the list passed. This may happen in the background
     * if {@link #transact} is true.
     *
     * @param collection The collection to remove.
     * @return Always true. Will cause a {@link ClassCastException} if the collection is not of type {@link ModelClass}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {

        // if its a ModelClass
        Collection<ModelClass> modelCollection = (Collection<ModelClass>) collection;
        if (transact) {
            TransactionManager.getInstance().delete(getProcessModelInfo(modelCollection));
        } else {
            ProcessModelHelper.process(mCursorList.getTable(), modelCollection, new ProcessModel<ModelClass>() {
                @Override
                public void processModel(ModelClass model) {
                    model.delete(false);
                }
            });
            mInternalTransactionListener.onResultReceived((List<ModelClass>) modelCollection);

        }

        return true;
    }

    /**
     * Retrieves the full list of {@link ModelClass} items from the table, removes these from the list, and
     * then deletes the remaining members. This is not that efficient.
     *
     * @param collection The collection if models to keep in the table.
     * @return Always true.
     */
    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        List<ModelClass> tableList = mCursorList.getAll();
        tableList.removeAll(collection);
        if (transact) {
            TransactionManager.getInstance().delete(getProcessModelInfo(tableList));
        } else {
            ProcessModelHelper.process(mCursorList.getTable(), tableList, new ProcessModel<ModelClass>() {
                @Override
                public void processModel(ModelClass model) {
                    model.delete(false);
                }
            });
            mInternalTransactionListener.onResultReceived(tableList);
        }
        return true;
    }

    /**
     * Will not use the index, rather just call a {@link com.raizlabs.android.dbflow.structure.Model#update(boolean)}
     *
     * @param location Not used.
     * @param object   The object to update
     * @return the updated model.
     */
    @Override
    public ModelClass set(int location, ModelClass object) {
        return set(object);
    }

    /**
     * Updates a Model {@link com.raizlabs.android.dbflow.structure.Model#update(boolean)} . If {@link #transact}
     * is true, this update happens in the BG, otherwise it happens immediately.
     *
     * @param object The object to update
     * @return The updated model.
     */
    public ModelClass set(ModelClass object) {
        if (transact) {
            TransactionManager.getInstance().update(getProcessModelInfo(object));
        } else {
            object.update(false);
            mInternalTransactionListener.onResultReceived(Arrays.asList(object));
        }
        return object;
    }

    @Override
    public int size() {
        return mCursorList.getCount();
    }

    @NonNull
    @Override
    public List<ModelClass> subList(int start, int end) {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        List<ModelClass> tableList = mCursorList.getAll();
        return tableList.toArray(array);
    }

    /**
     * Gets a {@link ModelClass} based on a list of {@link com.raizlabs.android.dbflow.sql.builder.Condition}
     *
     * @param conditions The list of conditions to retrieve a model from
     * @return A model from this table based on the conditions passed.
     */
    public ModelClass get(Condition... conditions) {
        return new Select().from(mCursorList.getTable()).where(conditions).querySingle();
    }

    /**
     * Fetches a list of all items in this table based on current queries.
     *
     * @param transactionListener The callback that will receive the list.
     */
    public void fetchAll(TransactionListener<List<ModelClass>> transactionListener) {
        mCursorList.fetchAll(transactionListener);
    }

    /**
     * Removes all {@link ModelClass} from the table based on the {@link com.raizlabs.android.dbflow.sql.builder.Condition}
     *
     * @param conditions The list of conditions to delete models with
     */
    public void removeAll(Condition... conditions) {
        if (transact) {
            TransactionManager.getInstance().delete(getProcessModelInfo(getAll(conditions)));
        } else {
            Delete.table(mCursorList.getTable(), conditions);
            mInternalTransactionListener.onResultReceived(null);
        }
    }

    /**
     * Returns a list of {@link ModelClass} based on the list of {@link com.raizlabs.android.dbflow.sql.builder.Condition}
     *
     * @param conditions The list of conditions to retrieve a model from
     * @return
     */
    public List<ModelClass> getAll(Condition... conditions) {
        return new Select().from(mCursorList.getTable()).where(conditions).queryList();
    }

}
