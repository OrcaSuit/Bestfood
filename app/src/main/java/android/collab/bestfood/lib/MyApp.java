package android.collab.bestfood.lib;


import android.app.Application;
import android.collab.bestfood.item.FoodInfoItem;
import android.collab.bestfood.item.MemberInfoItem;
import android.os.StrictMode;

//앱 전역에서 사용할 수 있는 클래스
public class MyApp extends Application {
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
