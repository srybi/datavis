package de.th.ro.datavis.ui.bottomSheet;


import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class BottomSheetHandler extends GestureDetector.SimpleOnGestureListener {

    private String TAG = "SwipeDetector";
    BottomSheet bottomSheet;
    View visualCue;

    public BottomSheetHandler(BottomSheet bottomSheet, View visualCue){
        this.bottomSheet = bottomSheet;
        this.visualCue = visualCue;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final int SWIPE_MIN_DISTANCE = 120;
        final int SWIPE_MAX_OFF_PATH = 250;
        final int SWIPE_THRESHOLD_VELOCITY = 200;
        if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
            return false;
        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            Log.d(TAG, "Up to Down");
            makeCueVisible(false);
            bottomSheet.showBottomSheetDialog();
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    public void makeCueVisible(boolean visible){
        Log.d(TAG, "makeCueVisible: " + visible);
        if(visible){
            visualCue.setVisibility(View.VISIBLE);
        }else{
            visualCue.setVisibility(View.INVISIBLE);
        }
    }

    public void showBottomSheet(){
        bottomSheet.showBottomSheetDialog();
    }

}
