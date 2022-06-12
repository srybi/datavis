package de.th.ro.datavis.ui.progressBar;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class ProgressbarHolder {

   private ProgressBar progressBar;
   private final String TAG = "ProgressbarHolder";

   public ProgressbarHolder(View view) {
      initProgressBar(view);
   }

   private void initProgressBar(View view){

      try {
         progressBar = (ProgressBar) view;
         hideProgressBar();

      } catch (ClassCastException | NullPointerException ex){
         Log.e(TAG, ex.getMessage());
         progressBar = null;
      }

   }

   public void showProgressBar(){
      if (progressBar != null){
         progressBar.setVisibility(View.VISIBLE);
      }

   }

   public void hideProgressBar(){
      if (progressBar != null){
         progressBar.setVisibility(View.GONE);
      }
   }

}
