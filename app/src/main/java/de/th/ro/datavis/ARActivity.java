package de.th.ro.datavis;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

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
    private String TAG = "ARActivity";
    private boolean ffsAvailable;
    private final AppDatabase db = AppDatabase.getInstance(this);

    private LiveData<List<MetaData>> sqlQueryMetadata;
    private Observer<List<MetaData>> sqlMetadataObs;
    //private LiveData<MetaData> HHPBW_deg, VHPBW_deg, Directivity_dBi = new MutableLiveData<>();

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
        List<Double> frequencies = ffsService.FrequenciesForAntenna(antennaId, ffsService.TiltForAntenna(antennaId), InterpretationMode.Logarithmic);
        List<Double> tilts;
        ffsAvailable = frequencies.size() != 0;
        //only initialize bottom sheet, if there is a ffs data to manipulate
        if(ffsAvailable){
            tilts = ffsService.TiltsForAntenna(antennaId, frequencies.get(0), InterpretationMode.Logarithmic);
            bottomSheet = new BottomSheet(this, frequencies, tilts, antennaId);
            bottomSheetHandler = new BottomSheetHandler(bottomSheet, findViewById(R.id.visualCueBottomSheet));
            gestureDetector = new GestureDetector(this, bottomSheetHandler);
        }else{
            Toast.makeText(
                    this, "No ffs data to display!", Toast.LENGTH_LONG).show();
        }


        buildAntennaModel();
        buildSpheres();
        Log.d(TAG, "onCreate: " + renderableList.size());

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
                        throwable.printStackTrace();
                        Log.d(TAG, "buildAntennaModel: Failed to build antenna model" + throwable.getMessage());
                        buildDefaultModel();
                        return null;
                    });
    }

    private void buildDefaultModel(){
            Log.d(TAG, "building default model");
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("models/datavis_antenna_asm.glb"))
                    .setIsFilamentGltf(true)
                    .setAsyncLoadEnabled(true)
                    .build()
                    .thenAccept(model -> {
                        handleCorruptGLB(model);
                        Log.d(TAG, "Antenna model done");
                    }).exceptionally(throwable -> {
                         throwable.printStackTrace();
                         Log.d(TAG, "buildAntennaModel: Failed to build default model" + throwable.getMessage());
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
        Log.d(TAG, "buildSpheres: done");
    }

    private List<Sphere> loadCoordinates(InterpretationMode mode, double frequency, double tilt){
        List<Sphere> coordinates = null;
        Log.d(TAG, "Start coordinate Loading...");

        try {
            AtomicField field = ffsService.getSpheresByPrimaryKey(antennaId, frequency, tilt, mode);
            if(field == null)
                return null;
            maxIntensity = field.maxIntensity;
            coordinates = field.spheres;

        } catch(Exception e){
            Log.e(TAG, "loadCoordinates: " + e.getMessage() );
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
        List<Sphere> list = new ArrayList<>();

        if(ffsAvailable){
            list = loadCoordinates(bottomSheet.getMode(), bottomSheet.getFrequency(), bottomSheet.getTilt());
        }

        //If List is null -> No corresponding atomic field was found
        if(list != null)
            processRenderList(anchorNode, renderableList, list);
        else
            Toast.makeText(this, "No Spheres found", Toast.LENGTH_SHORT).show();


        if(ffsAvailable){
            //Initializes Metadata
            Log.d(TAG, "onTapPlane: Initilizing metadata");
            try { readMetaDataFromDB(); } catch (Exception e) { e.printStackTrace();}
            createMetaDataObserver();
            bottomSheetHandler.makeCueVisible(true);
            bottomSheet.subscribe(this);
        }

    }


    private void processRenderList(AnchorNode anchorNode, Map<String, Renderable> list, List<Sphere> coords){
        Log.d(TAG, "Start processing RenderList");
        attachAntennaToAnchorNode(anchorNode, list.get("antenne"));

        middleNode = new TransformableNode(arFragment.getTransformationSystem());
        middleNode.setParent(anchorNode);

        if(ffsAvailable){
            proccessCoordList(middleNode, list, coords);
        }
        Log.d(TAG, "Processing RenderList Done");
    }

    private void proccessCoordList(TransformableNode middle,  Map<String, Renderable> list, List<Sphere> coords){
        scalingFactor = calcScalingFactor(maxIntensity);
        Log.d(TAG, "ScalingFactor " + scalingFactor);
        int i = 0;

        if (coords == null){
            Toast.makeText(this, "Keine Coordinaten für diese Kombination von Frequenz und Tilt vorhanden" , Toast.LENGTH_LONG).show();
            return;
        }

        for(Sphere s : coords){
            FFSIntensityColor intensityColor = ffsService.mapToColor(s.getIntensity(), maxIntensity);
            attachSphereToAnchorNode(middle, list.get(intensityColor.getName()+"Sphere"), s, scalingFactor);
            i++;
        }
    }

    private void attachAntennaToAnchorNode(AnchorNode anchorNode, Renderable renderable) {
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(0.20f);
        model.getScaleController().setMinScale(0.15f);
        model.setParent(anchorNode);

        try{
            model.setRenderable(renderable)
                    .animate(true).start();
        }catch(IllegalStateException e){
            Log.e(TAG, "attachAntennaToAnchorNode: failed because of corrupt glb file");
            Toast.makeText(this, "Found corrupt .glb file! Displaying default antenna", Toast.LENGTH_LONG).show();
            anchorNode.removeChild(model);
            buildDefaultModel();
        }finally {
            model.select();
        }
    }

    private void handleCorruptGLB(Renderable renderable){
        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.getScaleController().setMaxScale(0.1f);
        model.getScaleController().setMinScale(0.05f);
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
        if(maxIntensity == -1){
            Log.d(TAG, "calcScalingFactor: There is no ffs data");
            return -1 ;
        }

        float target = 0.5f;
        float factor = (float) (target / maxIntensity);

        return factor;
    }

    //Observer Pattern
    @Override
    public void update() {
        Log.d(TAG, "update: the bottomsheet called an update");
        deleteAllSpheres();

        //Metadaten werden neu geladen
        sqlQueryMetadata = db.metadataDao().findAll_Background(antennaId,bottomSheet.getFrequency(),bottomSheet.getTilt());
        sqlQueryMetadata.removeObservers(this);
        createMetaDataObserver();

        processRenderList(anchorNode, renderableList, loadCoordinates(bottomSheet.getMode(), bottomSheet.getFrequency(), bottomSheet.getTilt()));
    }

    private void deleteAllSpheres(){
        anchorNode.removeChild(middleNode);
    }

    //Used for gestureDetection
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(ffsAvailable){
            this.gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
    //Used for gestureDetection
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if(ffsAvailable){
            this.gestureDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Methods to display and update Metadata:
     * readMetaDataFromDB() fetches a List of all available Metadata for a certain AntennaID, frequency and tilt
     * createMetaDataObserver() assigns an Observer to check for updates in the LiveData
     * updateMetaData() on a change goes through the List of available Metadata and for each tries to find a TextView to update
     *      this is done by String matching the [Metadata type] to the [TextView ID]
     */
    private void readMetaDataFromDB(){
        sqlQueryMetadata = db.metadataDao().findAll_Background(antennaId,bottomSheet.getFrequency(),bottomSheet.getTilt());
        Log.d(TAG, "sqlQueryMetadata built "+sqlQueryMetadata.toString());
    }

    private void createMetaDataObserver(){
        sqlMetadataObs  = changeMetaData -> { updateMetadata(changeMetaData);};
        try {
            sqlQueryMetadata.observe(this, sqlMetadataObs);
        } catch (NullPointerException e) {}
    }

    private void updateMetadata(List<MetaData> changeMetaData){
        for(MetaData m: changeMetaData) {
            int resID = this.getResources().getIdentifier(("meta_" + m.getType()), "id", this.getPackageName());
            try {
                TextView textView = findViewById(resID);
                textView.setText(m.getValue());
                Log.d(TAG, "TextView " + textView.toString() + " updated to: " + m.getValue());
            } catch (Exception e) {
            }
        }
    }

}
