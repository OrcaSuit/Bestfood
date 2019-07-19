package android.collab.bestfood;

import android.app.Application;
import android.collab.bestfood.item.FoodInfoItem;
import android.collab.bestfood.item.MemberInfoItem;
import android.collab.bestfood.lib.EtcLib;
import android.collab.bestfood.lib.GeoLib;
import android.collab.bestfood.lib.MyLog;
import android.collab.bestfood.lib.RemoteLib;
import android.collab.bestfood.remote.RemoteService;
import android.collab.bestfood.remote.ServiceGenerator;
import android.collab.bestfood.lib.StringLib;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IndexActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    Context context;

    //레이아웃 설정 후 인터넷 연결 확인 안됬으면 showNoServe 메소드 호출 . @param saveInstanceState 액티비티가 새로 생성 되었을 경우 이전 상태값을 가지는 객체


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        context = this;

        if(!RemoteLib.getInstance().isConnected(context)) {
            showNoService();
            return;
        }
    }

    //일정 시간 후에 startTask() 메소드 호출 서버사용자 정보 조회
    @Override
    protected void onStart() {
        super.onStart();

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTask();
            }
        }, 1200);
    }

    // 인터넷 접속 상태 메세지 , 화면 종료 버튼
    private void showNoService() {
        TextView messageText = (TextView)findViewById(R.id.message);
        messageText.setVisibility(View.VISIBLE);

        Button closeButton = (Button)findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        closeButton.setVisibility((View.VISIBLE));
    }

    //현재 폰의 전화번호와 동일한 사요자 정보 조회 selectMemberInfo() 메소드 호출 setLastKnownLocation 메소드 호출 해서 현재 위치 정보 설정.
    public void startTask() {
        String phone = EtcLib.getInstance().getPhoneNumber(this);

        selectMemberInfo(phone);
        GeoLib.getInstance().setLastKnownLocation(this);
    }

    //레트로핏을 활용해서 서버로부터 사용자 정보 조회. 사용자 정보 조회후 setmemberinfoitem 호출 그렇지 않으면 goprofileactivity 호출 @param phone 폰의 전화번호.
    public void selectMemberInfo(String phone){
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<MemberInfoItem> call = remoteService.selectMemberInfo(phone);
        call.enqueue(new Callback<MemberInfoItem>() {
            @Override
            public void onResponse(Call<MemberInfoItem> call, Response<MemberInfoItem> response) {

                MemberInfoItem item = response.body();

                if(response.isSuccessful() && !StringLib.getInstance().isBlank(item.name)){
                    MyLog.d(TAG, "success" + response.body().toString());
                    setMemberInfoItem(item);
                } else {
                    MyLog.d(TAG, "not success");
                    goProfileActivity(item);
                }
            }

            @Override
            public void onFailure(Call<MemberInfoItem> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }

    //전달받은 MemverInfoItem을 Application 객체에 저장한다. 그리고 StatMain() 메소드를 호출한다. @param item 사용자 정보
    private void setMemberInfoItem(MemberInfoItem item){
        ((MyApp)getApplicationContext()).setMemberInfoItem(item);
        startMain();
    }

    public void startMain(){
        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    //사용자 정보를 조회하지 못했다면 insertMemverPhone() 메소드를 통해 전화번호를 서버에 저장, MainActivity를 실행한 후에 ProfileActivity를 실행 그리고 현재 액티비티 종료ㅕ. @param item 사용자 정보

    private  void goProfileActivity(MemberInfoItem item){
        if(item == null || item.seq <= 0 ) {
            insertMemberPhone();
        }

        Intent intent = new Intent(IndexActivity.this, MainActivity.class);
        startActivity(intent);
    }

    //폰의 전화번호를 서버에 저장.
    private void insertMemberPhone() {
        String phone = EtcLib.getInstance().getPhoneNumber(context);
        RemoteService remoteService = ServiceGenerator.createService(RemoteService.class);

        Call<String> call = remoteService.insertMemberPhone(phone);
        call.enqueue(new Callback<String>(){
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    MyLog.d(TAG, "success insert id " + response.body().toString());
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
            }
        });
    }

    //앱 전역에서 사용할 수 있는 클래스
    public static class MyApp extends Application {
        private MemberInfoItem memberInfoItem;
        private FoodInfoItem foodInfoItem;

        @Override
        public void onCreate() {
            super.onCreate();

            //FileUriExposedException 문제를 해결하기 위한 코드
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        public MemberInfoItem getMemberInfoItem(){
            if(memberInfoItem == null) memberInfoItem = new MemberInfoItem();

            return memberInfoItem;
        }

        public void setMemberInfoItem(MemberInfoItem item){
            this.memberInfoItem = item;
        }

        public int getMemberSeq() {
            return memberInfoItem.seq;
        }

        public void setFoodInfoItem(FoodInfoItem foodInfoItem){
            this.foodInfoItem = foodInfoItem;
        }

        public FoodInfoItem getFoodInfoItem() {
            return foodInfoItem;
        }
    }
}
