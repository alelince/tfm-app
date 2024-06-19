package com.tfm.bleapp.ui.home;

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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tfm.bleapp.AppState;
import com.tfm.bleapp.MainViewModel;
import com.tfm.bleapp.R;
import com.tfm.bleapp.databinding.FragmentHomeBinding;
import com.tfm.bleapp.rest.IServiceAPI;
import com.tfm.bleapp.rest.RestClient;
import com.tfm.bleapp.scanner.Scanner;

import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EditText hostView;
    private EditText portView;
    private ImageView blueprintView;
    private Button saveButton;
    private TextView connStatus;
    private Spinner scenarioSel;
    private Bitmap blueprint;
    private MainViewModel viewModel;
    private RealTimeLocationClient locationClient;
    private Handler locationViewHandler;
    private int x = -999;
    private int y = -999;
    private Paint locationPaint;
    private String[] beaconNames;
    private Scanner scanner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        beaconNames = getResources().getStringArray(com.tfm.bleapp.R.array.beacon_names);

        hostView = view.findViewById(R.id.ipAddressEntry);
        portView = view.findViewById(R.id.tcpPortEntry);
        saveButton = view.findViewById(R.id.pingButton);
        connStatus = view.findViewById(R.id.connectionStatus);
        scenarioSel = view.findViewById(R.id.scenarioSpinner);
        blueprintView = view.findViewById(R.id.blueprintView);

        locationPaint = new Paint();
        locationPaint.setStyle(Paint.Style.FILL);
        locationPaint.setColor(Color.BLUE);
        locationPaint.setAlpha(150);

        locationViewHandler = new Handler();
        locationViewHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocationOnImageView();
                locationViewHandler.postDelayed(this,1000);
            }
        },1000);

        scanner = new Scanner(new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                locationClient.addRssiSample(result.getDevice().getName(), result.getRssi());
            }
        });

        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        AppState state = viewModel.getState();

        hostView.setText(state.getHost(), TextView.BufferType.EDITABLE);
        portView.setText(Integer.toString(state.getPort()), TextView.BufferType.EDITABLE);

        scenarioSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (viewModel != null) {
                    state.setScenario((String) scenarioSel.getItemAtPosition(position));
                    locationClient = new RealTimeLocationClient(
                            state.getHost(),
                            state.getPort(),
                            state.getScenario()
                    );
                    scanner.scanLowLatency(beaconNames);
                    updateImageView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        saveButton.setOnClickListener(_view -> {
            String host = String.valueOf(hostView.getText());
            int port = Integer.parseInt(String.valueOf(portView.getText()));

            if (viewModel != null) {
                AppState auxState = viewModel.getState();
                auxState.setHost(host);
                auxState.setPort(port);
            }

            updateView(host, port);
        });

        // update from server (it will populate the drop-down list of scenarios)
        updateView(state.getHost(), state.getPort());
    }

    @Override
    public void onDestroyView() {
        scanner.stop();
        super.onDestroyView();
        binding = null;
    }

    private void updateView(String host, int port) {
        IServiceAPI api = new RestClient(host, port).createServiceApiHandler();

        Call<Void> heartbeatCall = api.heartbeat();

        heartbeatCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.code() == 200 && viewModel != null) {
                    AppState state = viewModel.getState();
                    state.setConnected(true);
                    connStatus.setText(R.string.server_status_connected);
                    updateScenarios(api);
                } else {
                    connStatus.setText(R.string.server_status_disconnected);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                connStatus.setText(R.string.server_status_disconnected);
            }
        });
    }

    private void updateScenarios(IServiceAPI api) {
        Call<List<String>> scenariosCall = api.listScenarios();

        scenariosCall.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.code() == 200 && response.body() != null) {
                    String[] arrScenarios = response.body().toArray(new String[0]);

                    ArrayAdapter<String> arrAdapter = new ArrayAdapter<>(
                            requireContext(),
                            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                            arrScenarios
                    );

                    scenarioSel.setAdapter(arrAdapter);
                    updateSpinner();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Error")
                        .setMessage(t.getMessage())
                        .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        });
    }

    private void updateSpinner() {
        if (viewModel == null) return;

        AppState state = viewModel.getState();
        SpinnerAdapter scenarioAdapter = scenarioSel.getAdapter();

        if (scenarioAdapter != null) {
            // workaround to initialize the drop-down list of scenarios
            // to the previously selected one
            for (int i = 0; i < scenarioAdapter.getCount(); ++i) {
                String item = (String) scenarioAdapter.getItem(i);

                if (item.equals(state.getScenario())) {
                    scenarioSel.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateImageView() {
        if (viewModel == null) return;

        AppState state = viewModel.getState();
        IServiceAPI api = new RestClient(state.getHost(), state.getPort()).createServiceApiHandler();
        Call<ResponseBody> blueprintCall = api.getBlueprint(state.getScenario());

        blueprintCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.code() == 200 && response.body() != null) {
                    InputStream inStream = response.body().byteStream();
                    blueprint = BitmapFactory.decodeStream(inStream);

                    Bitmap bmOverlay = Bitmap.createBitmap(
                            blueprint.getWidth(),
                            blueprint.getHeight(),
                            blueprint.getConfig()
                    );

                    Canvas canvas = new Canvas(bmOverlay);
                    canvas.drawBitmap(blueprint, new Matrix(), null);

                    blueprintView.setImageBitmap(bmOverlay);
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

    private void updateLocationOnImageView() {
        if (locationClient == null) return;

        int dx = x - locationClient.getX();
        int dy = y - locationClient.getY();

        if (Math.sqrt(dx*dx + dy*dy) > 100.) {
            x = locationClient.getX();
            y = locationClient.getY();

            Bitmap bmOverlay = Bitmap.createBitmap(
                    blueprint.getWidth(),
                    blueprint.getHeight(),
                    blueprint.getConfig()
            );

            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(blueprint, new Matrix(), null);
            canvas.drawCircle(x, y, 50.f, locationPaint);

            blueprintView.setImageBitmap(bmOverlay);
        }
    }
}