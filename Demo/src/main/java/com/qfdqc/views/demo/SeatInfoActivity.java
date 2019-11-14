package com.qfdqc.views.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.qfdqc.views.seattable.Room;
import com.qfdqc.views.seattable.Seat;
import com.qfdqc.views.seattable.SeatTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SeatInfoActivity extends AppCompatActivity {
    public SeatTable seatTableView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_seatinfo);

        seatTableView = (SeatTable) findViewById(R.id.seatView);

        //seatTableView.setScreenName("8号厅荧幕");//设置屏幕名称
        //seatTableView.setMaxSelected(3);//设置最多选中, 若不设置最多，则可以无限选择座位数

        seatTableView.setSeatChecker(new SeatTable.SeatChecker() {

            @Override
            public boolean isValidSeat(int row, int column) {//设置位置是否可见 可以设置一整列或一整行不可见 或就某一个位置不可见
                //return false;     //当前位置不可见
                return true;        //当前位置可见
            }

            @Override
            public boolean isSold(int row, int column) {
                //return true;      //当前位置已有人
                return false;       //当前位置可用
            }

            @Override
            public void checked(int row, int column) {
            }

            @Override
            public void unCheck(int row, int column) {
            }

            @Override
            public String[] checkedSeatTxt(int row, int column) {
                return null;
            }

        });

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("seatsData");
        String result = bundle.getString("result");
        String roomData = bundle.getString("roomData");
        /*在这里把 List<Seat> 类型的数据进行解释并,向新的 Intent 传送数据 */
        List<Seat> seatsList = new ArrayList<>();
        Seat seat;
        try {
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0;i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                seat = new Seat();
                seat.setId(jsonObject.getString("id"));
                seat.setX(Integer.parseInt(jsonObject.getString("X")));
                seat.setY(Integer.parseInt(jsonObject.getString("Y")));
                seat.setAngle(Integer.parseInt(jsonObject.getString("angle")));
                seat.setState(Integer.parseInt(jsonObject.getString("state")));
                seatsList.add(seat);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Room room = new Room();
        try {
            JSONArray jsonArray = new JSONArray(roomData);
            for(int i = 0;i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                room.setId(jsonObject.getString("room_id"));
                room.setName(jsonObject.getString("room_name"));
                room.setNo(Integer.parseInt(jsonObject.getString("room_no")));
                room.setFlooer(Integer.parseInt(jsonObject.getString("flooer")));
                room.setCountx(Integer.parseInt(jsonObject.getString("countx")));
                room.setCounty(Integer.parseInt(jsonObject.getString("county")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("--->>>",room.toString());
        seatTableView.setSeatList(seatsList);                           //设置座位表数据
        seatTableView.setData(room.getCountx(),room.getCounty());       //设置的座位数
    }
}
