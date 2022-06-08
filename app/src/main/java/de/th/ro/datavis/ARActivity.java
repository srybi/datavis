package de.th.ro.datavis;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interfaces.IObserver;
import de.th.ro.datavis.interpreter.ffs.FFSIntensityColor;
import de.th.ro.datavis.interpreter.ffs.FFSInterpreter;
import de.th.ro.datavis.interpreter.ffs.FFSService;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.ui.bottomSheet.BottomSheet;
import de.th.ro.datavis.ui.bottomSheet.BottomSheetHandler;
import de.th.ro.datavis.util.activity.BaseActivity;
import de.th.ro.datavis.util.enums.InterpretationMode;

public class ARActivity extends BaseActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener,
        IObserver {


    private ArFragment arFragment;
    private Map<String, Renderable> renderableList = new HashMap<>();

    private IInterpreter ffsInterpreter;
    private FFSService ffsService;

    private BottomSheet bottomSheet;
    private BottomSheetHandler bottomSheetHandler;
    private GestureDetector gestureDetector;
    private AnchorNode anchorNode;
    private TransformableNode middleNode;
    private int antennaId;
    private String antennaURI;
    private InterpretationMode interpretationMode;
    private String TAG = "myTag";
    private final AppDatabase db = AppDatabase.getInstance(this);

    private LiveData<MetaData> HHPBW_deg, VHPBW_deg, Directivity_dBi = new MutableLiveData<>();

    private double maxIntensity = -1;
    private float scalingFactor = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "StartUp ARActivity");
        setContentView(R.layout.activity_ar);
        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        ffsInterpreter = new FFSInterpreter();
        ffsService = new FFSService(ffsInterpreter, this);

        Bundle b = getIntent().getExtras();
        antennaId = b.getInt("antennaId");
        antennaURI = b.getString("antennaURI");
        String modeString = b.getString("interpretationMode");
        if(modeString.equals("Linear")){
            interpretationMode = InterpretationMode.Linear;
        }else{
            interpretationMode = InterpretationMode.Logarithmic;
        }
        List<Double> frequencies = ffsService.FrequenciesForAntenna(antennaId, 2, InterpretationMode.Logarithmic);
        bottomSheet = new BottomSheet(this, frequencies);
        bottomSheetHandler = new BottomSheetHandler(bottomSheet, findViewById(R.id.visualCueBottomSheet));
        gestureDetector = new GestureDetector(this, bottomSheetHandler);

        //TODO
        try {
            readMetaDataFromDB();
        } catch (Exception e) { e.printStackTrace();}
        //Create Observer for Metadata

        createMetaDataObserver();

        buildAntennaModel();
        buildSpheres();
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        arSceneView.getPlaneRenderer().setShadowReceiver(false);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
    }

    private void buildAntennaModel(){
        Log.d(TAG, "buildAntennaModel: "+ antennaURI);
        ModelRenderable.builder()
                .setSource(this, Uri.parse(antennaURI))
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    renderableList.put("antenne", model);
                    Log.d(TAG, "Antenna model done");
                })
                .exceptionally(throwable -> {
                    Log.d(TAG, "buildAntennaModel: Failed to build antenna model" + throwable.getMessage());
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    private void buildSpheres(){
        for(FFSIntensityColor intensityColor : FFSIntensityColor.values()){
            MaterialFactory.makeTransparentWithColor(this, intensityColor.getColor())
                    .thenAccept(
                            material -> {
                                ModelRenderable sphere = ShapeFactory.makeSphere(0.0065f, new Vector3(), material);
                                renderableList.put(intensityColor.getName() + "Sphere", sphere);
                            });
        }
    }

    private List<Sphere> loadCoordinates(InterpretationMode mode, double frequency, int tilt){
        List<Sphere> coordinates = null;
        Log.d(TAG, "Start coordinate Loading...");

        try {
            AtomicField field = ffsService.getSpheresByPrimaryKey(antennaId, frequency, tilt, mode);
            maxIntensity = field.maxIntensity;
            coordinates = field.spheres;

        } catch(Exception e){
            Toast.makeText(this, "Unable to load the coordinates from the database.", Toast.LENGTH_SHORT).show();
            return new ArrayList<Sphere>();
        }

        Log.d(TAG, "Coordinate Loading done");
        Log.d(TAG, "Coordinate List Size: " + coordinates.size());
        return coordinates;
    }


    private List<Renderable> getRenderList(){
        return new ArrayList<Renderable>();
    }



    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        Log.d(TAG, "Plane Tab");

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        //saving the anchorNode for removing later
        this.anchorNode = anchorNode;
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // AntennaId hardcoded, since its is currently not possible to choose an antenna
        List<Sphere> list = loadCoordinates(bottomSheet.getMode(), bottomSheet.getFrequency(), bottomSheet.getTilt());
        processRenderList(anchorNode, renderableList, list);
        bottomSheetHandler.makeCueVisible(true);
        bottomSheet.subscribe(this);
    }


    private void processRenderList(AnchorNode anchorNode, Map<String, Renderable> list, List<Sphere> coords){
        Log.d(TAG, "Start processing RenderList");
        attachAntennaToAnchorNode(anchorNode, list.get("antenne"));

        middleNode = new TransformableNode(arFragment.getTransformationSystem());
        middleNode.setParent(anchorNode);

        scalingFactor = calcScalingFactor(maxIntensity);
        Log.d(TAG, "ScalingFactor " + scalingFactor);
        int i = 0;
        for(Sphere s : coords){
            FFSIntensityColor intensityColor = ffsService.mapToColor(s.getIntensity(), maxIntensity);
            attachSphereToAnchorNode(middleNode, list.get(intensityColor.getName()+"Sphere"), s, scalingFactor);
            i++;
            //Log.d(TAG, "processRenderList: proccessing #" + i);
        }
        Log.d(TAG, "Processing RenderList Done");
    }

    private void attachAntennaToAnchorNode(AnchorNode anchorNode, Renderable renderable) {
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(0.20f);
        model.getScaleController().setMinScale(0.15f);
        model.setParent(anchorNode);
        model.setRenderable(renderable)
                .animate(true).start();
        model.select();
    }


    private void attachSphereToAnchorNode(TransformableNode middleNode, Renderable renderable, Sphere sphere, float scalingFactor){
        float yOffset = 0.1f;
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setLocalPosition(new Vector3((float) sphere.getX() * scalingFactor, (float) sphere.getY() * scalingFactor + yOffset, (float) sphere.getZ() * scalingFactor));
        model.setParent(middleNode);
        model.setRenderable(renderable)
                .animate(true).start();
        model.select();
    }

    /**
     * This calculates a Scaling Factor in reference to the given maxIntensity.
     * @param maxIntensity maxIntensity of the SphereList
     * @return a ScalingFactor based on the maxIntensity
     */
    private float calcScalingFactor(double maxIntensity){

        float target = 0.5f;
        float factor = (float) (target / maxIntensity);

        return factor;
    }

    //Observer Pattern
    @Override
    public void update() {
        Log.d(TAG, "update: the bottomsheet called an update");

        deleteAllSheres();
        processRenderList(anchorNode, renderableList, loadCoordinates(bottomSheet.getMode(), bottomSheet.getFrequency(), bottomSheet.getTilt()));
    }

    private void deleteAllSheres(){
        anchorNode.removeChild(middleNode);
    }

    //Used for gestureDetection
    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    //Used for gestureDetection
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        this.gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    //Metadata reading
    private void readMetaDataFromDB(){
        HHPBW_deg = db.metadataDao().findByMetadata_Background(antennaId, bottomSheet.getFrequency(), bottomSheet.getTilt(), "HHPBW_deg");
        VHPBW_deg = db.metadataDao().findByMetadata_Background(antennaId, bottomSheet.getFrequency(), bottomSheet.getTilt(), "VHPBW_deg");
        Directivity_dBi = db.metadataDao().findByMetadata_Background(antennaId, bottomSheet.getFrequency(), bottomSheet.getTilt(), "Directivity_dBi");
    }

    private void createMetaDataObserver(){
        Observer<MetaData> nullFillObs = changeMetaData -> { updateHHPBW_degView(changeMetaData);};
        HHPBW_deg.observe(this, nullFillObs);
        Observer<MetaData> squintObs = changeMetaData -> { updateVHPBW_degView(changeMetaData);};
        VHPBW_deg.observe(this, squintObs);
        Observer<MetaData> tiltObs = changeMetaData -> { updateDirectivity_dBiView(changeMetaData);};
        Directivity_dBi.observe(this, tiltObs);
    }
    private void updateHHPBW_degView(MetaData changeMetaData){
        TextView HHPBW_deg = findViewById(R.id.meta_HHPBW_deg);
        try {
            HHPBW_deg.setText("HHPBW: " + changeMetaData.getValue());
        } catch (Exception e){ Log.d(TAG, "couldn't find hhpbw"); }
    }
    private void updateVHPBW_degView(MetaData changeMetaData){
        TextView VHPBW_deg = findViewById(R.id.meta_VHPBW_deg);
        try {
            VHPBW_deg.setText("VHPBW: " + changeMetaData.getValue());
        } catch (Exception e){ Log.d(TAG, "couldn't find vhpbw"); }
    }
    private void updateDirectivity_dBiView(MetaData changeMetaData){
        TextView Directivity_dBi = findViewById(R.id.meta_Directivity_dBi);
        try {
            Directivity_dBi.setText("Directivity_dBi: " + changeMetaData.getValue());
        } catch (Exception e){ Log.d(TAG, "couldn't find directivity"); }
    }

}
