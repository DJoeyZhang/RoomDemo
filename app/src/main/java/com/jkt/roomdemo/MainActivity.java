package com.jkt.roomdemo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private UserDao mUserDao;
    private TextView mMsgTV;
    private StringBuffer mBuffer;
    private String TAG = "thedata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "roomDemo-database")
                //添加数据库的变动迁移支持(当前状态从version1到version2的变动处理)
                //主要在user里面加入了age字段,大家可以git reset --hard <commit> 到第一个版本
                //然后debug 手动生成一些数据。然后debug 该版本查看数据库升级体现。
                .addMigrations(AppDatabase.MIGRATION_1_2)
                //下面注释表示允许主线程进行数据库操作，但是不推荐这样做。
                //他可能造成主线程lock以及anr
//                .allowMainThreadQueries()
                .build();
        mUserDao = db.userDao();
        mMsgTV = (TextView) findViewById(R.id.tv);
        mBuffer = new StringBuffer();
        //数据库增、删、改都会被观察到 这次观察的所有数据（查询方法不会被观察）
        LiveData<List<User>> all = mUserDao.getAll();
        all.observe(MainActivity.this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                if (users == null) {
                    return;
                }
                Log.i("thedata", "observe all " + users.toString());
            }
        });
        //数据库增、删、改都会被观察到 这次观察的是uid为3的数据（查询方法不会被观察）
        LiveData<User> userLiveData = mUserDao.findByUid(3);
        userLiveData.observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user == null) {
                    Log.i("thedata", "observe  one  " + user);
                    return;
                }
                Log.i("thedata", "observe  one  " + user.toString());

            }
        });

    }

    public void onClick(View view) {
        mBuffer.delete(0, mBuffer.length());
        switch (view.getId()) {
            case R.id.insert_one:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //返回的是插入元素的primary key index
                        Long aLong = mUserDao.insert(new User("t" + System.currentTimeMillis() / 1000, "allen", 18));
                        if (aLong > 0) {
                            String msg = "insert one success, index is " + aLong.toString();
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        } else {
                            String msg = "insert one fail ";
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        }
                        MainActivity.this.setMsg();
                    }
                }).start();
                break;
            case R.id.insert_some:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<User> users = new ArrayList<>();
                        users.add(new User("t" + System.currentTimeMillis() / 1000, "jordan",20));
                        users.add(new User("t" + System.currentTimeMillis() / 1000, "james",21));
                        List<Long> longs = mUserDao.insertAll(users);
                        if (longs != null && longs.size() > 0) {
                            for (Long aLong : longs) {
                                String msg = "insert some success, index is " + aLong;
                                mBuffer.append(msg + "\n");
                                Log.i(TAG, msg);
                            }
                        } else {
                            String msg = "insert some fail ";
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        }
                        MainActivity.this.setMsg();
                    }
                }).start();
                break;
            case R.id.update_one:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int random = new Random().nextInt(9) + 1;
                        int update = mUserDao.update(new User(random, "t" + System.currentTimeMillis() / 1000, "kobe"));
                        if (update > 0) {
                            String msg = "update one success, index is " + random;
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        } else {
                            String msg = "update one fail ,index is " + random + " the user item doesn't exist ";
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        }
                        MainActivity.this.setMsg();
                    }
                }).start();
                break;
            case R.id.delete_one:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int random = new Random().nextInt(9) + 1;
                        int delete = mUserDao.delete(new User(random));
                        if (delete > 0) {
                            //size 表示删除个数
                            String msg = "delete one  success,index is " + random;
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        } else {
                            String msg = "delete  one fail ,index is " + random + " the user item doesn't exist ";
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        }
                        MainActivity.this.setMsg();

                    }
                }).start();

                break;
            case R.id.find_one:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int random = new Random().nextInt(9) + 1;
                        User user = null;
                        try {
                            user = LiveDataUtil.getValue(mUserDao.findByUid(random));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (user != null) {
                            String msg = "find one success , index is " + random + "  user:  " + user.toString();
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        } else {
                            String msg = "find one fail , index is " + random + " the user item doesn't exist ";
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        }
                        MainActivity.this.setMsg();
                    }
                }).start();
                break;
            case R.id.find_all:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<User> all = null;
                        try {
                            all = LiveDataUtil.getValue(mUserDao.getAll());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (all != null && all.size() > 0) {
                            for (User user1 : all) {
                                String msg = "find all success ,item  : " + user1.toString();
                                mBuffer.append(msg + "\n");
                                Log.i(TAG, msg);
                            }
                        } else {
                            String msg = "find all fail , no user item  exist ";
                            mBuffer.append(msg + "\n");
                            Log.i(TAG, msg);
                        }
                        MainActivity.this.setMsg();
                    }
                }).start();
                break;
            case R.id.delete_all:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LiveData<List<User>> all = mUserDao.getAll();
                        List<User> users = null;
                        try {
                            users = LiveDataUtil.getValue(all);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (users != null && users.size() > 0) {
                            mUserDao.deleteAll(users);
                        }
                        MainActivity.this.setMsg();
                    }
                }).start();
                break;

        }
    }

    private void setMsg() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = mBuffer.toString();
                mMsgTV.setText(text);
            }
        });
    }

}
