package cx.ring.services;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import cx.ring.daemon.IntVect;
import cx.ring.daemon.Ringservice;
import cx.ring.daemon.StringMap;
import cx.ring.daemon.UintVect;
import cx.ring.model.ServiceEvent;
import cx.ring.utils.FutureUtils;
import cx.ring.utils.Log;

/**
 * Created by hdsousa on 17-04-12.
 */

public class HardwareServiceImpl extends HardwareService {

    public static final String TAG = HardwareServiceImpl.class.getName();

    private Context mContext;

    private int cameraFront = 0;
    private int cameraBack = 0;

    private final Map<String, Shm> videoInputs = new HashMap<>();
    private WeakReference<SurfaceHolder> mCameraPreviewSurface = new WeakReference<>(null);
    private Map<String, WeakReference<SurfaceHolder>> videoSurfaces = Collections.synchronizedMap(new HashMap<String, WeakReference<SurfaceHolder>>());
    private VideoParams previewParams = null;
    private Camera previewCamera = null;
    private final HashMap<String, VideoParams> mParams = new HashMap<>();
    private final LongSparseArray<DeviceParams> mNativeParams = new LongSparseArray<>();

    public HardwareServiceImpl(Context mContext) {
        this.mContext = mContext;
    }

    public void initVideo() {
        mNativeParams.clear();
        int number_cameras = Camera.getNumberOfCameras();
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        for (int i = 0; i < number_cameras; i++) {
            addVideoDevice(Integer.toString(i));
            Camera.getCameraInfo(i, camInfo);
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraFront = i;
            } else {
                cameraBack = i;
            }
        }
        setDefaultVideoDevice(Integer.toString(cameraFront));
    }

    public void decodingStarted(String id, String shmPath, int width, int height, boolean isMixer) {
        Log.i(TAG, "DRingService.decodingStarted() " + id + " " + width + "x" + height);
        Shm shm = new Shm();
        shm.id = id;
        shm.path = shmPath;
        shm.w = width;
        shm.h = height;
        shm.mixer = isMixer;
        videoInputs.put(id, shm);
        WeakReference<SurfaceHolder> weakSurfaceHolder = videoSurfaces.get(id);
        if (weakSurfaceHolder != null) {
            SurfaceHolder holder = weakSurfaceHolder.get();
            if (holder != null) {
                shm.window = startVideo(id, holder.getSurface(), width, height);
            }
        }
    }

    @Override
    public void decodingStopped(String id, String shmPath, boolean isMixer) {
        Shm shm = videoInputs.remove(id);
        if (shm != null) {
            stopVideo(shm.id, shm.window);
        }
    }

    @Override
    public void getCameraInfo(String camId, IntVect formats, UintVect sizes, UintVect rates) {
        int id = Integer.valueOf(camId);

        if (id < 0 || id >= Camera.getNumberOfCameras()) {
            return;
        }

        Camera cam;
        try {
            cam = Camera.open(id);
        } catch (Exception e) {
            return;
        }

        Camera.Parameters param = cam.getParameters();
        cam.release();

        for (int fmt : param.getSupportedPreviewFormats()) {
            formats.add(fmt);
        }

        DeviceParams p = new DeviceParams();
        p.size = getSizeToUse(param);
        sizes.add(p.size.x);
        sizes.add(p.size.y);
        sizes.add(p.size.y);
        sizes.add(p.size.x);

        for (int fps[] : param.getSupportedPreviewFpsRange()) {
            int rate = (fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]) / 2;
            rates.add(rate);
        }
        p.rate = rates.get(0);

        p.infos = new Camera.CameraInfo();
        Camera.getCameraInfo(id, p.infos);

        mNativeParams.put(id, p);
    }

    @Override
    public void setParameters(String camId, int format, int width, int height, int rate) {
        int id = Integer.valueOf(camId);
        DeviceParams p = mNativeParams.get(id);
        VideoParams newParams = new VideoParams(id, format, p.size.x, p.size.y, rate);
        newParams.rotWidth = width;
        newParams.rotHeight = height;
        setVideoRotation(newParams, p.infos);
        mParams.put(camId, newParams);
    }

    @Override
    public void startCapture(@Nullable String camId) {
        stopCapture();

        final VideoParams videoParams;

        if (camId == null && previewParams != null) {
            videoParams = previewParams;
        } else if (camId == null && !mParams.isEmpty()) {
            videoParams = mParams.get("0");
        } else {
            videoParams = mParams.get(camId);
        }

        SurfaceHolder surface = mCameraPreviewSurface.get();
        if (surface == null) {
            Log.w(TAG, "Can't start capture: no surface registered.");
            previewParams = videoParams;

            ServiceEvent event = new ServiceEvent(ServiceEvent.EventType.VIDEO_EVENT);
            event.addEventInput(ServiceEvent.EventInput.VIDEO_START, true);
            setChanged();
            notifyObservers(event);
            return;
        }

        if (videoParams == null) {
            Log.w(TAG, "startCapture: no video parameters ");
        }
        Log.d(TAG, "startCapture " + videoParams.id + " " + videoParams.width + "x" + videoParams.height + " rot" + videoParams.rotation);

        final Camera preview;
        try {
            preview = Camera.open(videoParams.id);
            //setCameraDisplayOrientation(videoParams.id, preview);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        try {
            preview.setPreviewDisplay(surface);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        Camera.Parameters parameters = preview.getParameters();
        parameters.setPreviewFormat(videoParams.format);
        parameters.setPreviewSize(videoParams.width, videoParams.height);
        parameters.setRotation(0);

        for (int[] fps : parameters.getSupportedPreviewFpsRange()) {
            if (videoParams.rate >= fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] &&
                    videoParams.rate <= fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]) {
                parameters.setPreviewFpsRange(fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                        fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
            }
        }

        try {
            preview.setParameters(parameters);
        } catch (RuntimeException e) {
            Log.e(TAG, "Error while settings preview parameters", e);
        }

        preview.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                setVideoFrame(data, videoParams.width, videoParams.height, videoParams.rotation);
                preview.addCallbackBuffer(data);
            }
        });

        // enqueue first buffer
        int bufferSize = parameters.getPreviewSize().width * parameters.getPreviewSize().height * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
        preview.addCallbackBuffer(new byte[bufferSize]);

        preview.setErrorCallback(new Camera.ErrorCallback() {
            @Override
            public void onError(int error, Camera cam) {
                Log.w(TAG, "Camera onError " + error);
                if (preview == cam) {
                    stopCapture();
                }
            }
        });
        try {
            preview.startPreview();
        } catch (RuntimeException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        previewCamera = preview;
        previewParams = videoParams;

        ServiceEvent event = new ServiceEvent(ServiceEvent.EventType.VIDEO_EVENT);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_CAMERA, videoParams.id == 1);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_STARTED, true);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_WIDTH, videoParams.rotWidth);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_HEIGHT, videoParams.rotHeight);
        setChanged();
        notifyObservers(event);
    }

    @Override
    public void stopCapture() {
        Log.d(TAG, "stopCapture " + previewCamera);
        if (previewCamera != null) {
            final Camera preview = previewCamera;
            final VideoParams params = previewParams;
            previewCamera = null;
            preview.setPreviewCallback(null);
            preview.setErrorCallback(null);
            preview.stopPreview();
            preview.release();

            ServiceEvent event = new ServiceEvent(ServiceEvent.EventType.VIDEO_EVENT);
            event.addEventInput(ServiceEvent.EventInput.VIDEO_CAMERA, params.id == 1);
            event.addEventInput(ServiceEvent.EventInput.VIDEO_STARTED, true);
            event.addEventInput(ServiceEvent.EventInput.VIDEO_WIDTH, params.width);
            event.addEventInput(ServiceEvent.EventInput.VIDEO_HEIGHT, params.height);
            setChanged();
            notifyObservers(event);
        }
    }

    @Override
    public void addVideoSurface(String id, Object holder) {
        if (!(holder instanceof SurfaceHolder)) {
            return;
        }

        Shm shm = videoInputs.get(id);
        WeakReference<SurfaceHolder> surfaceHolder = new WeakReference<>((SurfaceHolder) holder);
        videoSurfaces.put(id, surfaceHolder);
        if (shm != null && shm.window == 0) {
            shm.window = startVideo(shm.id, surfaceHolder.get().getSurface(), shm.w, shm.h);
        }

        if (shm == null || shm.window == 0) {
            Log.i(TAG, "DRingService.startVideo() no window !");

            ServiceEvent event = new ServiceEvent(ServiceEvent.EventType.VIDEO_EVENT);
            event.addEventInput(ServiceEvent.EventInput.VIDEO_START, true);
            setChanged();
            notifyObservers(event);
            return;
        }

        ServiceEvent event = new ServiceEvent(ServiceEvent.EventType.VIDEO_EVENT);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_CALL, shm.id);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_STARTED, true);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_WIDTH, shm.w);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_HEIGHT, shm.h);
        setChanged();
        notifyObservers(event);

    }

    @Override
    public void addPreviewVideoSurface(Object holder) {
        if (!(holder instanceof SurfaceHolder)) {
            return;
        }

        mCameraPreviewSurface = new WeakReference<>((SurfaceHolder) holder);
    }

    @Override
    public void removeVideoSurface(String id) {
        Log.i(TAG, "DRingService.stopVideo() " + id);
        Shm shm = videoInputs.get(id);
        if (shm == null) {
            return;
        }
        if (shm.window != 0) {
            stopVideo(shm.id, shm.window);
            shm.window = 0;
        }

        ServiceEvent event = new ServiceEvent(ServiceEvent.EventType.VIDEO_EVENT);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_CALL, shm.id);
        event.addEventInput(ServiceEvent.EventInput.VIDEO_STARTED, false);
        setChanged();
        notifyObservers(event);
    }

    @Override
    public void removePreviewVideoSurface() {
        mCameraPreviewSurface.clear();
    }

    @Override
    public void switchInput(String id, boolean front) {
        final int camId = (front ? cameraFront : cameraBack);
        final String uri = "camera://" + camId;
        final cx.ring.daemon.StringMap map = mNativeParams.get(camId).toMap(mContext.getResources().getConfiguration().orientation);
        this.switchInput(id, uri, map);
    }

    @Override
    public void setPreviewSettings() {
        Map<String, StringMap> camSettings = new HashMap<>();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            if (mNativeParams.get(i) != null) {
                camSettings.put(Integer.toString(i), mNativeParams.get(i).toMap(mContext.getResources().getConfiguration().orientation));
            }
        }
        this.setPreviewSettings(camSettings);
    }

    private void setVideoRotation(VideoParams videoParams, Camera.CameraInfo info) {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = rotationToDegrees(windowManager.getDefaultDisplay().getRotation());
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            videoParams.rotation = (info.orientation + rotation + 360) % 360;
        } else {
            videoParams.rotation = (info.orientation - rotation + 360) % 360;
        }
    }

    private void setCameraDisplayOrientation(int camId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(camId, info);
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = rotationToDegrees(windowManager.getDefaultDisplay().getRotation());
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + rotation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - rotation + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private int rotationToDegrees(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private Point getSizeToUse(Camera.Parameters param) {
        final int MIN_WIDTH = 320;
        final Point size = new Point(0, 0);
        /** {@link Camera.Parameters#getSupportedPreviewSizes} :
         * "This method will always return a list with at least one element."
         * Attempt to find the size with width closest (but above) MIN_WIDTH. */
        for (Camera.Size s : param.getSupportedPreviewSizes()) {
            if (s.width < s.height) {
                continue;
            }
            if (size.x < MIN_WIDTH ? s.width > size.x : (s.width >= MIN_WIDTH && s.width < size.x)) {
                size.x = s.width;
                size.y = s.height;
            }
        }
        return size;
    }

    private static class Shm {
        String id;
        String path;
        int w, h;
        boolean mixer;
        long window = 0;
    }

    private static class VideoParams {
        public VideoParams(int id, int format, int width, int height, int rate) {
            this.id = id;
            this.format = format;
            this.width = width;
            this.height = height;
            this.rate = rate;
        }

        public int id;
        public int format;

        // size as captured by Android
        public int width;
        public int height;

        //size, rotated, as seen by the daemon
        public int rotWidth;
        public int rotHeight;

        public int rate;
        public int rotation;
    }

    private static class DeviceParams {
        Point size;
        long rate;
        Camera.CameraInfo infos;

        public StringMap toMap(int orientation) {
            StringMap map = new StringMap();
            boolean rotated = (size.x > size.y) == (orientation == Configuration.ORIENTATION_PORTRAIT);
            map.set("size", Integer.toString(rotated ? size.y : size.x) + "x" + Integer.toString(rotated ? size.x : size.y));
            map.set("rate", Long.toString(rate));
            return map;
        }
    }
}
