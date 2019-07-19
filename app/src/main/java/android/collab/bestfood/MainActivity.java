package android.collab.bestfood;

import android.collab.bestfood.item.MemberInfoItem;
import android.collab.bestfood.lib.GoLib;
import android.collab.bestfood.lib.StringLib;
import android.collab.bestfood.remote.RemoteService;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = getClass().getSimpleName();

    MemberInfoItem memberInfoItem;
    DrawerLayout drawer;
    View headerLayout;

    CircleImageView profileIconImage;

    //액티비티 & 네비게이션 뷰설정, BestFoodListFragment를 화면출력 @param savedInstanceState 액티비티가 새로 생성되었을때 이전 상태 값을 가지는 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        memberInfoItem = ((IndexActivity.MyApp)getApplication()).getMemberInfoItem();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerLayout = navigationView.getHeaderView(0);

       // GoLib.getInstance().goFragment(getSupportFragmentManager(), R.id.content_main,BestFoodListFragment.newInstance());
    }

    //프로필 정보는 별도 액티비티에서 변경 될수 있음, 변경을 바로 감지하기 위해 화면이 새로 보여질 때마다 setProfileView 호출.


    @Override
    protected void onResume() {
        super.onResume();;
        setProfileView();
    }

    private void setProfileView(){
        profileIconImage = (CircleImageView) headerLayout.findViewById(R.id.profile_icon);
        profileIconImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
                GoLib.getInstance().goProfileActivity(MainActivity.this);
            }
        });

        if(StringLib.getInstance().isBlank(memberInfoItem.memberIconFilename)){
            Picasso.get().load(R.drawable.ic_person).into(profileIconImage);
        } else {
            Picasso.get().load(RemoteService.MEMBER_ICON_URL + memberInfoItem.memberIconFilename).into(profileIconImage);
        }

        TextView nameText = (TextView) headerLayout.findViewById(R.id.name);

        if(memberInfoItem.name == null || memberInfoItem.name.equals("")){
            nameText.setText(R.string.name_need);
        } else {
            nameText.setText(memberInfoItem.name);
        }
    }

    //폰에서 하드웨어 버튼 클릭시 호출 메소드 네비게이션 메뉴가 보인 상태라면 닫음.


    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //네비게이션 메뉴를 클릭했을 때 호출되는 메소드 @param item  메뉴 아이템 객체 @return 메뉴 클릭 이벤트의 처리 여부
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_list) {
            //GoLib.getInstance().goFragment(getSupportFragmentManager(),
                  //  R.id.content_main, BestFoodListFragment.newInstance());

        } else if (id == R.id.nav_map) {
            //GoLib.getInstance().goFragment(getSupportFragmentManager(),
                   // R.id.content_main, BestFoodMapFragment.newInstance());

        } else if (id == R.id.nav_keep) {
           // GoLib.getInstance().goFragment(getSupportFragmentManager(),
                  //  R.id.content_main, BestFoodKeepFragment.newInstance());

        } else if (id == R.id.nav_register) {
            //GoLib.getInstance().goBestFoodRegisterActivity(this);

        } else if (id == R.id.nav_profile) {
          //  GoLib.getInstance().goProfileActivity(this);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
