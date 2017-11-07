package com.example.justin.draganddrop.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.justin.draganddrop.R;
import com.example.justin.draganddrop.adapter.ContactListAdapter;
import com.example.justin.draganddrop.data.ContactData;
import com.example.justin.draganddrop.util.ContactUtil;
import com.example.justin.draganddrop.adapter.ItemAdapter;
import com.example.justin.draganddrop.data.CopyData;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnDragListener, View.OnLongClickListener {

    @BindView(R.id.contactlist)
    ListView mListView;

    @BindView(R.id.move)
    DragListView mCopyListView;

    ContactListAdapter mAdapter;

    ArrayList<Pair<Long, String>> mItemArray;
    ItemAdapter mItemAdapter;

    int mID = 0; // 리스트의 ID Pair 형으로 받기에 ID 부여
    int mPosition = -1; // Copy Drag&Drop을 하였을 때 Drop 되는 Position

    boolean mCheck = true;
    boolean mDragOk = false;
    boolean mSavePoint = true;
    boolean mCreateEmptyItem = true;
    boolean mOverlap = true;

    String mName;
    String mPhone;
    float mSaveX, mSaveY;

    //모션들
    MotionEvent mDown;
    MotionEvent mMove;
    MotionEvent mUp;
    MotionEvent mCancel;
    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            // 아이템 추가가 되었을때 들어온다. 뷰가 그려졌을때라고 설명이지만 실제로 어떨지 모르겠음
            // Postion 을 잡기위해 임의로 드래그 이벤트 발생하는데 걔가 완료되면 얘 호출함
            mCopyListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        cleardata();
        GetPermission();
        mAdapter = new ContactListAdapter(this);
        mListView.setAdapter(mAdapter);

        addEvents();
        mCopyListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mCopyListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                // 드래그가 시작되었을때 호출되는 콜백 메소드
                mPosition = position;
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {
                // 드래그 중에 position 을 바꿔서 제대로된 위치에 드롭
                mPosition = itemPosition;
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                // 드래그가 끝났을때 호출되는 콜백 메소드
                // 아이템 업데이트가 완료되면 이게 호출이된다는거.
                /*if (mCreateEmptyItem) {
                    createEmptyItem();
                    mCreateEmptyItem = false;
                    //여기서는 터치이벤트 발생
                    dragevent(mSaveX, mSaveY);
                }*/
                if (mCheck || mOverlap) {
                    removeEmpty();
                    mOverlap = false;
                }


                mDragOk = true;
                mPosition = -1;
                mCopyListView.getRecyclerView().getRecycledViewPool().clear();

            }
        });

        mItemArray = new ArrayList<>();
        setUpListView();
    }

    public void GetPermission() {
        //퍼미션을 얻는데 사용자의 확인을 받아야지만 권한이 획득 된다. 앱 실행중에 권한을 획득한다는 것.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                //권한이 부여가 되었을 경우
                ContactUtil.GetContacts(this);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1); // 1은 그냥 넣은 것. 해당 권한 획득이 제대로 된다면 콜백 메소드에 1이 전달
                }
            }
        } else {
            ContactUtil.GetContacts(this);
        }
    }


    // 주소 가져오기 위한 퍼미션
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContactUtil.GetContacts(this);
    }

    // 롱클릭 = 드래그로 인식해서 투명한뷰 생성
    @Override
    public boolean onLongClick(View view) {
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(data, shadowBuilder, view, 0);
        return true;
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        int action = dragEvent.getAction();

        // DRAG_STARTED : 드래그가 시작될 때 호출
        // DRAG_ENTERED : 드래그가 올바른 뷰에 들어왔을때 호출
        // DRAG_LOCATIOM : 드래그 위치가 변할 때 호출
        // DRAG_EXIT : 올바른 뷰에 들어왔다가 다른 뷰로 나갔을 때
        // DROP : 드랍을 하였을때
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED: {
                if (dragEvent.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return true;
                }
                return false;
            }
            case DragEvent.ACTION_DRAG_ENTERED: {

                return true;
            }
            case DragEvent.ACTION_DRAG_LOCATION: {

                if (view.getTag().equals("copylist")) {
                    // 클릭이벤트로 mPostion 변경
                    //영역에 들어왔을때 하나의 가상의 뷰 생성
                    if (mCheck) {
                        //x,y에 드래그 이벤트 발생 --> mPosition 변경을 위하여
                        mCreateEmptyItem = true;
                        mPosition = mCopyListView.getPosition(dragEvent.getX(), dragEvent.getY());
                        mSaveX = dragEvent.getX();
                        mSaveY = dragEvent.getY();
                        mSavePoint = true;
                        createEmptyItem();
                        mCheck = false;
                        mCopyListView.setDragEnabled(false);

                        mCopyListView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCopyListView.setDragEnabled(true);
                            }
                        }, 250);
                    } else {
                        // 드래그 이벤트 발생
                        if (mSavePoint) {
                            dragging(mSaveX, mSaveY);
                            mSavePoint = false;
                        } else {
                            dragging(dragEvent.getX(), dragEvent.getY());
                        }
                    }
                } else {
                    mUp = MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP,
                            dragEvent.getX(),
                            dragEvent.getY(),
                            0
                    );

                    mCopyListView.dispatchTouchEvent(mUp);
                    if (mDragOk) {
                        mCheck = true;
                        mDragOk = false;
                    }
                }
                return true;
            }
            case DragEvent.ACTION_DRAG_EXITED: {
                mUp = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        dragEvent.getX(),
                        dragEvent.getY(),
                        0
                );

                mCopyListView.dispatchTouchEvent(mUp);
                removeEmpty();
                return true;
            }
            case DragEvent.ACTION_DROP: {
                View v = (View) dragEvent.getLocalState();
                if (view.getTag().equals("copylist")) {
                    TextView name = (TextView) v.findViewById(R.id.name);
                    TextView phone = (TextView) v.findViewById(R.id.phone);

                    mUp = MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP,
                            dragEvent.getX(),
                            dragEvent.getY(),
                            0
                    );

                    mCopyListView.dispatchTouchEvent(mUp);

                    // 중복된 데이터가 있는지 검사
                    if (overlapdata(name.getText() + "", phone.getText() + "")) {
                        removeEmpty();
                        mOverlap = true;
                        return false;
                    }
                    mName = name.getText() + "";
                    mPhone = phone.getText() + "";

                    mCheck = true;

                    if (mPosition == -1) {
                        removeEmpty();
                        addItem();
                    } else {
                        removeAddItem();
                    }
                }
                return true;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                return true;
            }
        }
        return false;
    }

    public void cleardata() {
        ContactData.Id.clear();
        ContactData.Name.clear();
        ContactData.PhoneNumber.clear();
        CopyData.PhoneNumber.clear();
        CopyData.Name.clear();
        CopyData.mDrag = false;
    }

    // 뷰에 이벤트 처리를 달아줌
    public void addEvents() {
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData("", mimeTypes, item);
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                return true;
            }
        });

        findViewById(R.id.contact).setOnDragListener(this);
        findViewById(R.id.move).setOnDragListener(this);
    }

    // 빈 뷰를 지움
    public void removeEmpty() {
        for (int i = 0; i < mItemArray.size(); i++) {
            if (mItemArray.get(i).second.equals("")) {
                mItemArray.remove(i);
                mItemAdapter.notifyDataSetChanged();
                mCheck = true;
            }
        }
    }

    public boolean overlapdata(String name, String Phone) {
        for (int i = 0; i < CopyData.Name.size(); i++) {
            if (CopyData.Name.get(i).equals(name) && CopyData.PhoneNumber.get(i).equals(Phone)) {
                return true;

            }
        }
        return false;
    }

    // Item 추가
    public void addItem() {
        mItemArray.add(mItemArray.size(), (new Pair<>((long) mID++, mName + "/" + mPhone)));
        CopyData.Name.add(mName);
        CopyData.PhoneNumber.add(mPhone);
        mItemAdapter.notifyDataSetChanged();
        mName = "";
        mPhone = "";
    }

    // 빈 뷰를 지우고 Item 추가
    public void removeAddItem() {
        mItemArray.remove(mPosition);
        mItemArray.add(mPosition, (new Pair<>((long) mID++, mName + "/" + mPhone)));
        CopyData.Name.add(mName);
        CopyData.PhoneNumber.add(mPhone);
        mItemAdapter.notifyDataSetChanged();
        mName = "";
        mPhone = "";
    }

    // 빈 뷰 추가
    public void createEmptyItem() {
        if (mPosition == -1) {
            mItemArray.add(mItemArray.size(), (new Pair<>((long) mID++, "")));
            mCopyListView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
            mItemAdapter.notifyDataSetChanged();
            mCheck = false;
        } else {
            mItemArray.add(mPosition, (new Pair<>((long) mID++, "")));
            mCopyListView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
            mItemAdapter.notifyDataSetChanged();
            mCheck = false;
        }
    }

    // DragListView 속성 초기화
    public void setUpListView() {
        mCopyListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mItemAdapter = new ItemAdapter(mItemArray, R.layout.list_copylist, R.id.copylist, false);
        mCopyListView.setAdapter(mItemAdapter, true);
        mCopyListView.setCustomDragItem(new MyDragItem(getApplicationContext(), R.layout.list_copylist));
        mCopyListView.setScrollingEnabled(true);
        mCopyListView.setDragEnabled(true);
    }

    // 드래그 이벤트 발생 ( 처음부터 끝까지 )
    public void dragevent(float x, float y) {
        mCheck = false;
        // 맨 뒤 혹은 제대로된 위치에 가상의 뷰 생성
        mSaveX = x;
        mSaveY = y;
        mSavePoint = true;

        mDown = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                x,
                y,
                0
        );

        mMove = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE,
                x,
                y,
                0
        );
        mUp = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                x,
                y,
                0
        );
        mCancel = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_CANCEL,
                x,
                y,
                0
        );


        mCopyListView.dispatchTouchEvent(mDown);
        mCopyListView.dispatchTouchEvent(mMove);
        mCopyListView.dispatchTouchEvent(mUp);
        mCopyListView.dispatchTouchEvent(mCancel);
    }

    // 드래그 중인 이벤트 발생
    public void dragging(float x, float y) {
        mDown = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                x,
                y,
                0
        );

        mMove = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE,
                x,
                y,
                0
        );

        mCopyListView.dispatchTouchEvent(mDown);
        mCopyListView.dispatchTouchEvent(mMove);
    }


    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text1 = ((TextView) clickedView.findViewById(R.id.copyname)).getText();
            CharSequence text2 = ((TextView) clickedView.findViewById(R.id.copyphone)).getText();
            ((TextView) dragView.findViewById(R.id.copyname)).setText(text1);
            ((TextView) dragView.findViewById(R.id.copyphone)).setText(text2);

            dragView.findViewById(R.id.copylist).setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
        }
    }
}
