package com.jkt.roomdemo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Allen on 2018/4/16/016.
 */

@Dao
public interface UserDao {
    //所有的CURD根据primary key进行匹配
    //------------------------query------------------------
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND "
            + "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);

    @Query("SELECT * FROM user WHERE uid = :uid")
    User findByUid(int uid);

    //-----------------------insert----------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<User> users);

    //---------------------update------------------------
    @Update()
    int update(User user);

    @Update()
    int updateAll(User... user);

    @Update()
    int updateAll(List<User> user);

    //-------------------delete-------------------
    @Delete
    int delete(User user);

    @Delete
    int deleteAll(List<User> users);

    @Delete
    int deleteAll(User... users);
}
