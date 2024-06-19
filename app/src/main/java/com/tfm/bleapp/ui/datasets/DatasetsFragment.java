package com.tfm.bleapp.ui.datasets;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tfm.bleapp.AppState;
import com.tfm.bleapp.MainViewModel;
import com.tfm.bleapp.R;
import com.tfm.bleapp.databinding.FragmentDatasetsBinding;
import com.tfm.bleapp.rest.AcquisitionSample;
import com.tfm.bleapp.rest.Campaign;
import com.tfm.bleapp.rest.IServiceAPI;
import com.tfm.bleapp.rest.AcquisitionPoint;
import com.tfm.bleapp.rest.RestClient;
import com.tfm.bleapp.scanner.Scanner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatasetsFragment extends Fragment {

    private FragmentDatasetsBinding binding;
    private Spinner datasetSel;
    private Spinner pointSel;
    private ImageView blueprintView;
    private Button captureButton;
    private Bitmap blueprint;
    private AppState state;
    private Map<String, List<AcquisitionPoint>> campaignPointsMap;
    private Map<String, AcquisitionPoint> pointMap;
    private List<AcquisitionSample> sampleBuffer;
    private String[] beaconNames;
    private Scanner scanner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beaconNames = getResources().getStringArray(com.tfm.bleapp.R.array.beacon_names);
        campaignPointsMap = new HashMap<>();
        sampleBuffer = new ArrayList<>();
        scanner = new Scanner(new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                AcquisitionSample sample = new AcquisitionSample();
                sample.setBeacon(result.getDevice().getName());
                sample.setRssi(result.getRssi());
                sample.setRxTimestamp(String.valueOf(result.getTimestampNanos()));
                sampleBuffer.add(sample);
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDatasetsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        datasetSel = view.findViewById(R.id.datasetSpinner);
        pointSel = view.findViewById(R.id.pointSpinner);
        captureButton = view.findViewById(R.id.captureButton);
        blueprintView = view.findViewById(R.id.blueprintView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        state = viewModel.getState();

        datasetSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (state != null) {
                    String campaignName = (String) datasetSel.getItemAtPosition(position);
                    updatePointSpinner(campaignName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        pointSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (state != null) {
                    String pointName = (String) pointSel.getItemAtPosition(position);
                    AcquisitionPoint acqPoint = pointMap.get(pointName);

                    if (acqPoint != null) {
                        updateImageView(acqPoint);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        updateView();

        captureButton.setOnClickListener(_view -> {
            String pointName = (String) pointSel.getSelectedItem();
            if (pointName == null) return;

            AcquisitionPoint acqPoint = pointMap.get(pointName);
            if (acqPoint == null) return;

            sampleBuffer.clear();

            long countdownTimeInSec = acqPoint.getAcquisitionTime() + acqPoint.getPreambleTime();

            AlertDialog captureDialog = new AlertDialog.Builder(view.getContext())
                    .setNegativeButton("Cancel", (dialog, id) -> {
                        scanner.stop();
                        dialog.dismiss();
                    })
                    .create();

            captureDialog.setTitle("Capturing...");
            captureDialog.setMessage(String.valueOf(countdownTimeInSec));
            captureDialog.show();

            new CountDownTimer(countdownTimeInSec * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long remainingTimeInSec = millisUntilFinished / 1000;

                    if (remainingTimeInSec <= acqPoint.getAcquisitionTime()) {
                        scanner.scanLowLatency(beaconNames);
                    }

                    captureDialog.setMessage(String.valueOf(remainingTimeInSec));
                }

                @Override
                public void onFinish() {
                    scanner.stop();
                    postAcquisitionSamples(
                            acqPoint.getCampaignName(),
                            acqPoint.getId(),
                            sampleBuffer
                    );
                    captureDialog.hide();
                }
            }.start();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateView() {
        if (state == null || !state.isConnected()) return;

        IServiceAPI api = new RestClient(state.getHost(), state.getPort())
                .createServiceApiHandler();

        Call<List<Campaign>> campaignsCall = api.listCampaigns();

        campaignsCall.enqueue(new Callback<List<Campaign>>() {
            @Override
            public void onResponse(@NonNull Call<List<Campaign>> call,
                                   @NonNull Response<List<Campaign>> response) {
                if (response.code() == 200 && response.body() != null) {
                    ArrayList<String> campaignNames = new ArrayList<>();

                    for (Campaign campaign : response.body()) {
                        // filter out campaigns whose scenario is not the selected one
                        if (campaign.getScenario().equals(state.getScenario())) {
                            campaignNames.add(campaign.getName());
                        }
                    }

                    String[] arrCampaignNames = campaignNames.toArray(new String[0]);

                    ArrayAdapter<String> arrAdapter = new ArrayAdapter<>(
                            requireContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                            arrCampaignNames
                    );

                    datasetSel.setAdapter(arrAdapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Campaign>> call, @NonNull Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Error fetching campaigns")
                        .setMessage(t.getMessage())
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        });

        Call<ResponseBody> blueprintCall = api.getBlueprint(state.getScenario());

        blueprintCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.code() == 200 && response.body() != null) {
                    InputStream inStream = response.body().byteStream();
                    blueprint = BitmapFactory.decodeStream(inStream);
                    return;
                }

                blueprint = null;
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                blueprint = null;
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Error fetching blueprint")
                        .setMessage(t.getMessage())
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        });
    }

    private void updatePointSpinner(String campaignName) {
        if (state == null || campaignPointsMap == null) return;

        if (campaignPointsMap.containsKey(campaignName)) {
            ArrayList<String> pointNames = new ArrayList<>();
            pointMap = new HashMap<>();

            for (AcquisitionPoint acqPoint :
                    Objects.requireNonNull(campaignPointsMap.get(campaignName))) {
                pointNames.add(acqPoint.getName());
                pointMap.put(acqPoint.getName(), acqPoint);
            }

            String[] arrPointNames = pointNames.toArray(new String[0]);

            ArrayAdapter<String> arrAdapter = new ArrayAdapter<>(
                    requireContext(),
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    arrPointNames
            );

            pointSel.setAdapter(arrAdapter);
            return;
        }

        IServiceAPI api = new RestClient(state.getHost(), state.getPort())
                .createServiceApiHandler();

        Call<List<AcquisitionPoint>> pointsCall = api.listPoints(campaignName);
        pointsCall.enqueue(new Callback<List<AcquisitionPoint>>() {
            @Override
            public void onResponse(@NonNull Call<List<AcquisitionPoint>> call,
                                   @NonNull Response<List<AcquisitionPoint>> response) {
                if (response.code() == 200 && response.body() != null) {
                    campaignPointsMap.put(campaignName, response.body());
                    updatePointSpinner(campaignName); // recursive call
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AcquisitionPoint>> call,
                                  @NonNull Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Error fetching points")
                        .setMessage(t.getMessage())
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        });
    }

    private void updateImageView(AcquisitionPoint acqPoint) {
        if (blueprint != null) {
            Bitmap bmOverlay = Bitmap.createBitmap(
                    blueprint.getWidth(),
                    blueprint.getHeight(),
                    blueprint.getConfig()
            );

            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(blueprint, new Matrix(), null);

            Paint paint = new Paint();
            paint.setColor(Color.BLUE);

            for (AcquisitionPoint auxPoint : pointMap.values()) {
                if (!auxPoint.getName().equals(acqPoint.getName())) {
                    canvas.drawCircle(auxPoint.getX(), auxPoint.getY(), 10, paint);
                }
            }

            paint.setColor(Color.RED);
            canvas.drawCircle(acqPoint.getX(), acqPoint.getY(), 15, paint);

            blueprintView.setImageBitmap(bmOverlay);
        }
    }

    private void postAcquisitionSamples(String campaignName,
                                        int pointId,
                                        List<AcquisitionSample> sampleList) {
        if (sampleList.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Beacons not found")
                    .setMessage("No samples were captured!")
                    .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                    .show();
            return;
        }

        IServiceAPI api = new RestClient(state.getHost(), state.getPort())
                .createServiceApiHandler();

        Call<Void> samplesCall = api.createSamples(campaignName, pointId, sampleList);

        samplesCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                String title, message;

                if (response.isSuccessful()) {
                    title = "POST request successful";
                    message = "Captured samples: " + sampleList.size();

                    // select automatically the next point in the campaign
                    int nextPos = pointSel.getSelectedItemPosition() + 1;
                    if (nextPos < pointSel.getCount()) pointSel.setSelection(nextPos);
                } else {
                    title = "POST request rejected";
                    message = response.message();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Error uploading captured samples")
                        .setMessage(t.getMessage())
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        });
    }
}