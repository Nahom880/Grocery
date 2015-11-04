package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.transaction.BaseResultTransaction;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Provides a {@link ModelClass}-list backed implementation on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 * and allows for specific method calling on a model.
 */
public abstract class ProcessModelTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>> implements ProcessModel<ModelClass> {

    protected ProcessModelInfo<ModelClass> mModelInfo;

    /**
     * Constructs this transaction with a single model enabled.
     *
     * @param modelInfo Holds information about this process request
     */
    public ProcessModelTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo.getInfo(), modelInfo.mTransactionListener);
        mModelInfo = modelInfo;
    }

    @Override
    public boolean onReady() {
        return mModelInfo.hasData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ModelClass> onExecute() {
        mModelInfo.processModels(this);
        return mModelInfo.mModels;
    }

    /**
     * Called when we are on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} and looping
     * through the models. Run a specific {@link com.raizlabs.android.dbflow.structure.Model} method here.
     *
     * @param model
     */
    @Override
    public abstract void processModel(ModelClass model);

}
