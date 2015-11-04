package com.raizlabs.android.dbflow.runtime.transaction;

/**
 * Author: andrewgrosner
 * Description: Provides the legacy callback wrapper to the ResultReceiver class.
 */
@Deprecated
public class ResultReceiver<ResultClass> implements TransactionListener<ResultClass> {

    @Override
    public void onResultReceived(ResultClass resultClass) {

    }

    @Override
    public boolean onReady(BaseTransaction<ResultClass> transaction) {
        return true;
    }

    @Override
    public boolean hasResult(BaseTransaction<ResultClass> transaction, ResultClass result) {
        return true;
    }
}
