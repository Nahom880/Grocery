package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@ContainerAdapter
@Table(databaseName = TestDatabase.NAME)
public class ParentModel extends TestModel1 {
}
