package com.example.nahom.grocery;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


@Table (databaseName = AppDatabase.dbname)
/** Creating Table for the database
 * Created by Nahom on 4/12/15.
 */
public class TestModel extends BaseModel {
    //setting
    @Column (columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    int id;

    @Column
    String catagory;

}
