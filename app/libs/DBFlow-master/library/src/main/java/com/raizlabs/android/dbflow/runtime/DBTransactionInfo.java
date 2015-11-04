package com.raizlabs.android.dbflow.runtime;

import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;

import java.util.UUID;

/**
 * Created by andrewgrosner
 * Date: 2/2/14
 * Contributors:
 * Description: Holds information related to a {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction}
 */
public class DBTransactionInfo {

    private String name;

    private int priority;

    private DBTransactionInfo() {
    }

    /**
     * Creates with a name and default {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction#PRIORITY_NORMAL}
     *
     * @param name
     * @return
     */
    public static DBTransactionInfo create(String name) {
        return create(name, BaseTransaction.PRIORITY_NORMAL);
    }

    /**
     * Creates the Request Information for when running a {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction}
     *
     * @param name     Name of the request (for debugging)
     * @param priority The priority it should be run on from {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction}
     * @return
     */
    public static DBTransactionInfo create(String name, int priority) {
        DBTransactionInfo requestInfo = new DBTransactionInfo();
        requestInfo.name = name;
        requestInfo.priority = priority;
        return requestInfo;
    }

    /**
     * Creates with a priority and name generated from {@link java.util.UUID#randomUUID()}
     *
     * @param priority
     * @return
     */
    public static DBTransactionInfo create(int priority) {
        DBTransactionInfo requestInfo = new DBTransactionInfo();
        requestInfo.name = UUID.randomUUID().toString();
        requestInfo.priority = priority;
        return requestInfo;
    }

    /**
     * Creates with a priority and name generated from {@link java.util.UUID#randomUUID()}
     * and {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction#PRIORITY_LOW}
     *
     * @param priority
     * @return
     */
    public static DBTransactionInfo create() {
        DBTransactionInfo requestInfo = new DBTransactionInfo();
        requestInfo.name = UUID.randomUUID().toString();
        requestInfo.priority = BaseTransaction.PRIORITY_LOW;
        return requestInfo;
    }

    /**
     * Returns a prefilled, fetch request
     *
     * @return
     */
    public static DBTransactionInfo createFetch() {
        DBTransactionInfo requestInfo = new DBTransactionInfo();
        requestInfo.priority = BaseTransaction.PRIORITY_UI;
        requestInfo.name = "fetch " + UUID.randomUUID().toString();
        return requestInfo;
    }

    /**
     * Returns a prefilled, save request. Default is {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction#PRIORITY_NORMAL}
     * since we generally don't need to know right away that it has been saved.
     *
     * @return
     */
    public static DBTransactionInfo createSave() {
        DBTransactionInfo requestInfo = new DBTransactionInfo();
        requestInfo.priority = BaseTransaction.PRIORITY_NORMAL;
        requestInfo.name = "save " + UUID.randomUUID().toString();
        return requestInfo;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }
}
