package tm.android.chronos.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import tm.android.chronos.R;
import tm.android.chronos.activity.Chronos;
import tm.android.chronos.audio.CommonMediaPlayer;
import tm.android.chronos.core.*;
import tm.android.chronos.localisation.SpeedCategory;
import tm.android.chronos.sql.DbConstant;
import tm.android.chronos.sql.DbLiveObject;
import tm.android.chronos.sql.TrackFactory;

import java.util.List;
import java.util.Vector;

public class LocationService extends Service {
    private final static String logname = LocationService.class.getSimpleName();

    private enum MODE {WAIT_FIRST_FIX, WHAT_TO_DO_NEXT, SPEED_GRABBING, NEAR_DESTINATION}

    private MODE mode = MODE.WAIT_FIRST_FIX;
    private LocationManager locationManager;
    private Runner runner;
    private TextToSpeech textToSpeech;
    private static LocationService self;
    private Looper looper;

    public static LocationService getSelf() {
        return self;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Chronos.Logdebug(logname, "onCreate");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Chronos.Logdebug(logname, "onStartCommand");
        Notification notification = getNotification();
        if (notification ==null) {
            Toast.makeText(getBaseContext(),"Oups ! got a null notification ! can't start the tracking service",Toast.LENGTH_LONG).show();
            return START_NOT_STICKY;
        }
        int notificationId = (int) (System.currentTimeMillis() / 1000);
        startForeground(notificationId, notification);
        getTextToSpeech();
        self = this;

        locationManager = (LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE);
        long trackId = intent.getLongExtra("trackId", -1);
        int dsp_type = intent.getIntExtra("dsp_type", -1);
        if (trackId == -1 || dsp_type == -1) {
            Toast.makeText(getBaseContext(),"Oups ! missing data ! tarck id or move type",Toast.LENGTH_LONG).show();
            return START_NOT_STICKY;
        } else {
            speak("Bonjour, en attente du gps.");
            mode = MODE.WAIT_FIRST_FIX;
            runner = new Runner(trackId, dsp_type);
            looper = new Handler().getLooper();
            requestLocationUpdate(2000);
        }
        return START_NOT_STICKY;
    }

    public void stop() {
        runner.stop();
    }


    private void requestLocationUpdate(long minTime) {
        Chronos.Logdebug("LocationService", "requestLocationUpdate: " + minTime, null);
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, runner,looper);

        }
    }


    private TextToSpeech getTextToSpeech() {
        if (textToSpeech == null) {

            textToSpeech = new TextToSpeech(getBaseContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Chronos.Logdebug(logname, "status: " + status);
                    if (status == TextToSpeech.SUCCESS) {
                        speak("Bonjour, en attente du gps");
                    }
                }
            });

            try {
                synchronized (this) {
                    wait(10000);
                }
            } catch (InterruptedException e) {
                // nothing
            }

        }
        return textToSpeech;
    }

    private void speak(String text) {
        Chronos.Logdebug(logname, "Length of Text to speech: " + text.length());
        Chronos.Logdebug(logname, "Text to speech: " + text);
        if (CommonMediaPlayer.Instance().isPlaying())
            CommonMediaPlayer.Instance().pause(500 + 90 * text.length());
        else
            CommonMediaPlayer.Instance().addPause(90 * text.length());
        getTextToSpeech().speak(text, TextToSpeech.QUEUE_ADD, null, String.valueOf(System.currentTimeMillis()));
        getTextToSpeech().playSilentUtterance(500, TextToSpeech.QUEUE_ADD, String.valueOf(System.currentTimeMillis() + 1));
    }


    private Notification getNotification() {
        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null)
            return null;

        String channelId = "channel-02";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = "Chronos Locator Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }


        if (Units.hasNoResources())
            Units.setResources(getBaseContext().getResources());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext(), channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Locator")
                .setContentText("Locator-test")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        return mBuilder.build();
    }


    private class Runner implements LocationListener {
        private final static int accuracyThreshold = 50;
        private List<TrackPart> trackParts;
        private int trackPartIndex;
        private float result[];
        private int SPEED_CATEGORY;
        private Vector<Long> times;
        private Vector<float[]> results;
        private long endTime;
        long minTime;
        float meanSpeed;
        private int initialGoodAccurayCount;

        Runner(long trackId, int dsp_type) {
            SPEED_CATEGORY = dsp_type;
            result = new float[3];
            TrackFactory factory = new TrackFactory(getBaseContext());
            trackParts = factory.getTrack(trackId).getTrackParts();
            trackPartIndex = 0;
            times = new Vector<>(50);
            results = new Vector<>(50);
            minTime = 2000;
            initialGoodAccurayCount = 0;

        }


        @Override
        public void onLocationChanged(Location location) {
            Chronos.Logi("LocationService", "onLocationChanged, accuracy: " + location.getAccuracy());

            switch (mode) {
                case WAIT_FIRST_FIX:
                    if (location.getAccuracy() <= accuracyThreshold) {
//                    Intent intent = new Intent();
//                    intent.setAction(ACTION.READY.name());
//                    sendBroadcast(intent);
                        initialGoodAccurayCount++;
                        if (initialGoodAccurayCount > 5) {
                            mode = MODE.WHAT_TO_DO_NEXT;
                            speak("Gps prêt. Vous pouvez démarrer.");
                        }
                    }
                    break;
                case WHAT_TO_DO_NEXT:
                    whatToDoNext(location);
                    break;
                case NEAR_DESTINATION:
                    nearDestination(location);
                    break;
            }
        }


        private void whatToDoNext(Location currentLocation) {
            long now = System.currentTimeMillis();
            if (System.currentTimeMillis() < endTime) return;

            if (minTime > 2000) {
                minTime = 2000;
                locationManager.removeUpdates(runner);
                requestLocationUpdate(minTime);
                return;
            }
            if (currentLocation.getAccuracy() > accuracyThreshold) {
                return;
            }
            if (mode == MODE.NEAR_DESTINATION) return;

            float closeDistance = SpeedCategory.getCloseDistance(SPEED_CATEGORY);
            if (closeDistance == -1) return;

            TrackPart destination = trackParts.get(trackPartIndex);
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), destination.getLatitude(), destination.getLongitude(), result);
            if (result[0] <= closeDistance) {
                String txt;
                if (destination.isStart())
                    txt = "Départ du parcours ";
                else if (destination.isEnd())
                    txt = "Arrivé du parcours ";
                else txt = "Prochaine destination ";
                speak(txt + destination.getName() + " estimée à " + ((int) result[0]) + " mètres");
                mode = MODE.NEAR_DESTINATION;
                meanSpeed = -1;
                minTime = (SPEED_CATEGORY == SpeedCategory.CAR ? 1000 : 2000);
            } else {
                float speed = getMax(new float[]{meanSpeed, currentLocation.getSpeed(),SpeedCategory.getBaseSpeed(SPEED_CATEGORY)});
                speed = speed*1.2f; // to prevent acceleration that could lead to miss the next destination
                Chronos.Logdebug(logname, "whatToDoNext, speed from location: " + currentLocation.getSpeed() + ", speed" + speed);
                float time = (result[0] - SpeedCategory.getCloseDistance(SPEED_CATEGORY)) / speed;
                minTime = (long) (time * 1000);
                endTime = now + minTime;
                //speak("Prochain fix dans " + (int) time + " secondes");
            }

            locationManager.removeUpdates(runner);
            requestLocationUpdate(minTime);

        }


        private void nearDestination(Location currentLocation) {
            long now = System.currentTimeMillis();
            if (currentLocation.getAccuracy() > accuracyThreshold) {
                speak("Attention mauvaise réception Gps");
                return;
            }
            // we calculate a meanSpeed during the aproach of a point.
            if (currentLocation.hasSpeed() && currentLocation.getSpeed() > SpeedCategory.getMinSpeed(SPEED_CATEGORY)) {
                if (meanSpeed == -1)
                    meanSpeed = currentLocation.getSpeed();
                else
                    meanSpeed = (meanSpeed + currentLocation.getSpeed()) / 2;
            }
            Chronos.Logdebug(logname, "nearDestination");
            TrackPart destination = trackParts.get(trackPartIndex);
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), destination.getLatitude(), destination.getLongitude(), result);
            times.add(now);
            results.add(new float[]{result[0], (result[1] + 360) % 360});
            Chronos.Logdebug(logname, "Distance: " + result[0] + ",  heading: " + results.lastElement()[1]);
            if (result[0] <= SpeedCategory.getCloseDistance(SPEED_CATEGORY)) {
                if (results.size() > 2) { // wee need at least 3 locations
                    int indexOfMin = getMin(results);
                    int i = 1;
                    while (i < 10 && indexOfMin + i < results.size() && indexOfMin - i >= 0) {
                        float[] previous = results.get(indexOfMin - i);
                        float[] min = results.get(indexOfMin);
                        float[] next = results.get(indexOfMin + i);
                        Chronos.Logdebug(logname, "Distance before min: " + previous[0] + ", minDistance: " + min[0] + ",  distance after min: " + next[0]);
                        Chronos.Logdebug(logname, "Heading before min: " + previous[1] + ", heading at min: " + min[1] + ",Heading after min: " + next[1]);
                        if (Math.abs(previous[1] - next[1]) > 50) {
                            String txt;
                            DbLiveObject<Stopwatch> dbLiveObject = new DbLiveObject<>(getBaseContext());
                            List<Stopwatch> lst = dbLiveObject.getRunningLiveObjects(DbConstant.RUNNING_STOPWATCHES_TABLE_NAME);

                            if (destination.isStart()) {
                                txt = "Parcours démarré. Départ: " + destination.getName() + " à " + (Chronos.ftime.format(times.get(indexOfMin)));
                                lst.get(0).start(times.get(indexOfMin));

                            } else {
                                if (destination.isCurrent()) {
                                    lst.get(0).lapTime(times.get(indexOfMin));
                                    txt = "Point " + destination.getName() + " passé à " + (Chronos.ftime.format(times.get(indexOfMin)));
                                } else {
                                    lst.get(0).stopTime(times.get(indexOfMin));
                                    CommonMediaPlayer.Instance().stopAll();
                                    CommonMediaPlayer.Instance().releasePlayer();
                                    txt = "Arrivée: " + destination.getName() + " à " + (Chronos.ftime.format(times.get(indexOfMin)) + ". Parcours terminé.");
                                }
                                speak(txt);
                                
                                StopwatchDataRow dataRow = lst.get(0).getStopwatchData().getTimeList().lastElement();
                                long[] digit = Digit.split(dataRow.getLapTime()).toArray();
                                txt = "Dernière distance: " + trackParts.get(trackPartIndex - 1).getDistanceToNextLocation() + " kilomètres parcourue en " +
                                        digit[2] + " minutes et " + digit[3] + "secondes  à " + dataRow.getSpeed() + " kilomètres heure";
                            }
                            speak(txt);
                            dbLiveObject.clearTable(DbConstant.RUNNING_STOPWATCHES_TABLE_NAME);
                            dbLiveObject.storeLiveObjects(lst, DbConstant.RUNNING_STOPWATCHES_TABLE_NAME);
                            dbLiveObject.close();


                            if (destination.isEnd()) {
                                stop();
                            } else {
                                trackPartIndex++;
                                mode = MODE.WHAT_TO_DO_NEXT;
                                results.clear();
                                times.clear();
                            }
                            break;
                        }
                        i++;
                    }

                }
            } else {
                mode = MODE.WHAT_TO_DO_NEXT;
                times.clear();
                results.clear();
                meanSpeed = -1;
            }
        }


        private int getMin(Vector<float[]> list) {
            float[] min = new float[]{Float.MAX_VALUE};
            for (float[] value : list) {
                if (value[0] < min[0])
                    min = value;
            }
            return min.length == 1 ? -1 : list.indexOf(min);
        }

        private float getMax(float[] list) {
            float max = Float.MIN_VALUE;
            for (float value : list) {
                if (value > max)
                    max = value;
            }
            return max;
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        public void stop() {
            locationManager.removeUpdates(this);
            String message = "Service de localisation terminé. Au revoir.";
            Chronos.Logi("LOCATION SERVICE", message);
            speak(message);
            CommonMediaPlayer.build(getBaseContext());
            CommonMediaPlayer.Instance().startVibrator(1000);
            stopForeground(true);
        }

    }
}
