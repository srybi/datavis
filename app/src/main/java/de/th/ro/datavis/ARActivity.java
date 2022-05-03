package de.th.ro.datavis;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.ar.sceneform.rendering.Renderable;

import java.util.List;

import de.th.ro.datavis.util.activity.BaseARActivity;
import de.th.ro.datavis.util.renderable.DataVisRenderableUtil;

public class ARActivity extends BaseARActivity {

    @Override
    public List<Renderable> getRenderableList() {
        DataVisRenderableUtil dataVisRenderableUtil = new DataVisRenderableUtil();
        return dataVisRenderableUtil.loadSphereTower(this);
    }

    @Override
    public int getARFragment() {
        return R.id.arFragment;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }


}
