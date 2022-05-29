package de.th.ro.datavis.ui.bottomSheet;


import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class BottomSheetHandler extends GestureDetector.SimpleOnGestureListener {

    private String TAG = "SwipeDetector";
    BottomSheet bottomSheet;

    public BottomSheetHandler(BottomSheet bottomSheet){
        this.bottomSheet = bottomSheet;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final int SWIPE_MIN_DISTANCE = 120;
        final int SWIPE_MAX_OFF_PATH = 250;
        final int SWIPE_THRESHOLD_VELOCITY = 200;
        try {
            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(TAG, "Up to Down");
                bottomSheet.showBottomSheetDialog();
            }
        } catch (Exception e) {
            // nothing
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }


}
