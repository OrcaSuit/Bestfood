package android.collab.bestfood;

import android.app.Activity;
import android.collab.bestfood.item.ImageItem;
import android.collab.bestfood.lib.BitmapLib;
import android.collab.bestfood.lib.FileLib;
import android.collab.bestfood.lib.GoLib;
import android.collab.bestfood.lib.MyLog;
import android.collab.bestfood.lib.MyToast;
import android.collab.bestfood.lib.RemoteLib;
import android.collab.bestfood.lib.StringLib;
import android.collab.bestfood.remote.RemoteService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.io.File;

public class BestFoodRegisterImageFragment extends Fragment implements View.OnClickListener {

    private  final String TAG = this.getClass().getSimpleName();
    public static final String INFO_SEQ = "INFO_SEQ";

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;

    Activity context;
    int infoSeq;

    File imageFile;
    String imageFilename;

    EditText imageMemoEdit;
    ImageView infoImage;

    ImageItem imageItem;

    boolean isSavingImage = false;


    //FoodInfoItem 객체를 인자로 저장하는 BestFoodRegisterInputFragment 인스턴스를 생성해서 반환한다.
    public static BestFoodRegisterImageFragment newInstance(int infoSeq){
        Bundle bundle = new Bundle();
        bundle.putInt(INFO_SEQ, infoSeq);

        BestFoodRegisterImageFragment f = new BestFoodRegisterImageFragment();
        f.setArguments(bundle);

        return f;
    }

    //프래그먼트가 생성될 때 호출되며, 인자에 저장된 INFO_SEQ를 멤버변수 infoSeq에 저장
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            infoSeq = getArguments().getInt(INFO_SEQ);
        }
    }

    //fragment_bestfood_register_image.xml 기반으로 뷰를 생성한다.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = this.getActivity();
        View v = inflater.inflate(R.layout.fragment_bestfood_register_image, container, false);

        return v;
    }

    //onCreateView() 메소드 뒤에 호출되며, 기본정보 생성과 화면 처리
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageItem = new ImageItem();
        imageItem.infoSeq = infoSeq;

        imageFilename = infoSeq + "_" + String.valueOf(System.currentTimeMillis());
        imageFile = FileLib.getInstance().getImageFile(context, imageFilename);

        infoImage = (ImageView) view.findViewById(R.id.bestfood_image);
        imageMemoEdit = (EditText) view.findViewById(R.id.register_image_memo);

        ImageView imageRegister = (ImageView) view.findViewById(R.id.bestfood_image_register);
        imageRegister.setOnClickListener(this);

        view.findViewById(R.id.prev).setOnClickListener(this);
        view.findViewById(R.id.complete).setOnClickListener(this);
    }

    /**
     * 이미지를 촬영하고 그 결과를 받을 수 있는 액티비티를 시작한다.
     */
    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        context.startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    /**
     * 앨범으로부터 이미지를 선택할 수 있는 액티비티를 시작한다.
     */
    private void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        context.startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    /**
     * 클릭이벤트를 처리한다.
     * @param v 클릭한 뷰에 대한 정보
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bestfood_image_register) {
            showImageDialog(context);
        } else if (v.getId() == R.id.complete) {
            saveImage();
        } else if (v.getId() == R.id.prev) {
            GoLib.getInstance().goBackFragment(getFragmentManager());
        }
    }

    /**
     * 다른 액티비티를 실행한 결과를 처리하는 메소드
     * @param requestCode 액티비티를 실행하면서 전달한 요청 코드
     * @param resultCode 실행한 액티비티가 설정한 결과 코드
     * @param data 결과 데이터
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA) {
                Picasso.get().load(imageFile).into(infoImage);

            } else if (requestCode == PICK_FROM_ALBUM && data != null) {
                Uri dataUri = data.getData();

                if (dataUri != null) {
                    Picasso.get().load(dataUri).into(infoImage);

                    Picasso.get().load(dataUri).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            BitmapLib.getInstance().saveBitmapToFileThread(imageUploadHandler,
                                    imageFile, bitmap);
                            isSavingImage = true;
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                }
            }
        }
    }

    /**
     * 사용자가 선택한 이미지와 입력한 메모를 ImageItem 객체에 저장한다.
     */
    private  void setImageItem() {
        String imageMemo = imageMemoEdit.getText().toString();
        if (StringLib.getInstance().isBlank(imageMemo)) {
            imageMemo = "";
        }

        imageItem.imageMemo = imageMemo;
        imageItem.fileName = imageFilename + ".png";
    }

    /**
     * 이미지를 서버에 업로드한다.
     */
    private void saveImage() {
        if (isSavingImage) {
            MyToast.s(context, R.string.no_image_ready);
            return;
        }
        MyLog.d(TAG, "imageFile.length() " + imageFile.length());

        if (imageFile.length() == 0) {
            MyToast.s(context, R.string.no_image_selected);
            return;
        }

        setImageItem();

        RemoteLib.getInstance().uploadFoodImage(infoSeq,
                imageItem.imageMemo, imageFile, finishHandler);
        isSavingImage = false;
    }

    /**
     * 이미지를 어떤 방식으로 선택할지에 대해 다이얼로그를 보여준다.
     * @param context 컨텍스트 객체
     */
    public void showImageDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_bestfood_image_register)
                .setSingleChoiceItems(R.array.camera_album_category, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    getImageFromCamera();
                                } else {
                                    getImageFromAlbum();
                                }

                                dialog.dismiss();
                            }
                        }).show();
    }

    Handler imageUploadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isSavingImage = false;
            setImageItem();
            Picasso.get().invalidate(RemoteService.IMAGE_URL + imageItem.fileName);
        }
    };

    Handler finishHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            context.finish();
        }
    };
}
