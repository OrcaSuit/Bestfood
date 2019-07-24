package android.collab.bestfood;


import android.Manifest;
import android.collab.bestfood.custom.WorkaroundMapFragment;
import android.collab.bestfood.item.FoodInfoItem;
import android.collab.bestfood.lib.DialogLib;
import android.collab.bestfood.lib.EtcLib;
import android.collab.bestfood.lib.MyLog;
import android.collab.bestfood.lib.StringLib;
import android.collab.bestfood.remote.RemoteService;
import android.collab.bestfood.remote.ServiceGenerator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 맛집 정보를 보는 액티비티
public class BestFoodInfoActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private  final String TAG = this.getClass().getSimpleName();
    public  static final String INFO_SEQ = "INFO_SEQ";

    Context context;

    int memberSeq;
    int foodInfoSeq;

    FoodInfoItem item;
    GoogleMap map;

    View loadingText;
    ScrollView scrollView;
    ImageView keepImage;

    //맛집 정보를 보여주기 위해 사용자 시퀀스와 맛집 정보를 시퀀스를 얻고 이를 기반으로 서버에서 맛집 정보를 조회하는 메소드를 호출한다.
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bestfood_info);

        context = this;

        loadingText = findViewById(R.id.loading_layout);

        memberSeq = ((MyApp)getApplication()).getMemberSeq();
        foodInfoSeq = getIntent().getIntExtra(INFO_SEQ, 0);
        selectFoodInfo(foodInfoSeq, memberSeq);

        setToolbar();
    }

    //툴바를 설정햐ㅏㄴ다.
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
    }

    //오른쪽 상단 메뉴를 구성한다. 닫기 메뉴만이 설정되어 있는 menu_close.xml을 지정한다.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }

    //왼쪽 화살표 메뉴(android.R.id.home)을 클릭했을 때와 오른쪽 상단 닫기 메뉴를 클릭했을 때의 동작을 지정한다. 여기서는 모든 버튼이 액티비티를 종료한다.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                finish();
                break;
            case R.id.action_close :
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //서버에서 맛집 정보를 조회한다. @param foodInfoSeq 맛집 정보 시퀀스 @param memberSeq 사용자 시퀀스
    private  void selectFoodInfo(int foodInfoSeq, int memberSeq) {
        RemoteService remoteService = ServiceGenerator. createService(RemoteService.class);

        Call<FoodInfoItem> call = remoteService.selectFoodInfo(foodInfoSeq, memberSeq);

        call.enqueue(new Callback<FoodInfoItem>() {
            @Override
            public void onResponse(Call<FoodInfoItem> call, Response<FoodInfoItem> response) {
                FoodInfoItem infoItem = response.body();

                if(response.isSuccessful() && infoItem != null && infoItem.seq > 0){
                    item = infoItem;
                    setView();
                    loadingText.setVisibility(View.GONE);
                } else {
                    loadingText.setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.loading_text)).setText(R.string.loading_not);
                }
            }
            @Override
            public void onFailure(Call<FoodInfoItem> call, Throwable t) {
                MyLog.d(TAG, "no internet connectivity");
                MyLog.d(TAG, t.toString());
            }
        });
    }
    private void setView() {
        getSupportActionBar().setTitle(item.name);

        ImageView infoImage = (ImageView) findViewById(R.id.info_image);
        setImage(infoImage, item.imageFilename);

        TextView location = (TextView) findViewById(R.id.location);
        location.setOnClickListener(this);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        FragmentManager fm = getSupportFragmentManager();
        WorkaroundMapFragment fragment = (WorkaroundMapFragment) fm.findFragmentById(R.id.map);

        if (fragment == null) {
            fragment = (WorkaroundMapFragment) SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.content_main, fragment).commit();
        }
        fragment.getMapAsync(this);

        fragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        TextView nameText = (TextView) findViewById(R.id.name);
        if (!StringLib.getInstance().isBlank(item.name)) {
            nameText.setText(item.name);
        }

        keepImage = (ImageView) findViewById(R.id.keep);
        keepImage.setOnClickListener(this);
        if (item.isKeep) {
            keepImage.setImageResource(R.drawable.ic_keep_on);
        } else {
            keepImage.setImageResource(R.drawable.ic_keep_off);
        }

        TextView address = (TextView) findViewById(R.id.address);
        if (!StringLib.getInstance().isBlank(item.address)) {
            address.setText(item.address);
        } else {
            address.setVisibility(View.GONE);
        }

        TextView tel = (TextView) findViewById(R.id.tel);
        if (!StringLib.getInstance().isBlank(item.tel)) {
            tel.setText(EtcLib.getInstance().getPhoneNumberText(item.tel));
            tel.setOnClickListener(this);
        } else {
            tel.setVisibility(View.GONE);
        }

        TextView description = (TextView) findViewById(R.id.description);
        if (!StringLib.getInstance().isBlank(item.description)) {
            description.setText(item.description);
        } else {
            description.setText(R.string.no_text);
        }
    }

    //구글 맵이 보옂ㄹ 준비가 되었을 때 호출되는 메소드이며 서벵서 조회한 맛집 위도와 경도를 기반으로 지도를 표시한다.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return ;

        map.setMyLocationEnabled(true);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);
        setting.setMapToolbarEnabled(true);

        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(item.latitude, item.longitude));
        marker.draggable(false);

        map.addMarker(marker);

        movePosition(new LatLng(item.latitude, item.longitude), Constant.MAP_ZOOM_LEVEL_DETAIL);
    }

    //즐겨찾기 버튼과 위치보기 버튼을 클릭했을 떄의 동작을 정의한다. @param v 클릭한 뷰에 대한 저옵


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.keep) {
            if(item.isKeep) {
                DialogLib.getInstance().showKeepDeleteDialog(context, keepHandler, memberSeq, item.seq);

                keepImage.setImageResource(R.drawable.ic_keep_off);
            } else {
                DialogLib.getInstance().showKeepInsertDialog(context, keepHandler, memberSeq, item.seq);

                keepImage.setImageResource(R.drawable.ic_keep_on);
            }
        } else if (v.getId() == R.id.location) {
            movePosition(new LatLng(item.latitude, item.longitude), Constant.MAP_ZOOM_LEVEL_DETAIL);
        }
    }
    Handler keepHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            item.isKeep = !item.isKeep;

            if(item.isKeep){
                keepImage.setImageResource(R.drawable.ic_keep_on);
            } else {
                keepImage.setImageResource(R.drawable.ic_keep_off);
            }
        }
    };

    private  void movePosition(LatLng latLng, float zoomLevel) {
        CameraPosition cp = new CameraPosition.Builder().target((latLng)).zoom(zoomLevel).build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    private void setImage(ImageView imageView, String fileName) {
        if(StringLib.getInstance().isBlank(fileName)){
            Picasso.get().load(R.drawable.bg_bestfood_drawer).into(imageView);
        } else {
            Picasso.get().load(RemoteService.IMAGE_URL + fileName).into(imageView);
        }
    }

    //화면이 일시 정지 상태로 될 때 호출되며 ,현재 아이템의 변경 사항을 저장한다. 이는 BestFoodListFragment BestFoodKeppFragment에서 변경된 즐겨찾기 상태를 반영하는 용도로 사용된다.
    @Override
    protected void onPause() {
        super.onPause();
        ((MyApp) getApplication()).setFoodInfoItem(item);
    }
}
