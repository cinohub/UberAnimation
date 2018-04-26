package com.portalbeanz.testmap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Property;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
public class MarkerAnimation {
    public static final long TIME_DELAY=1000;
    private interface LatLngInterpolator {

        public static class LinearFixed implements LatLngInterpolator {
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = ((b.latitude - a.latitude) * ((double) fraction)) + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                if (Math.abs(lngDelta) > 180.0d) {
                    lngDelta -= Math.signum(lngDelta) * 360.0d;
                }
                return new LatLng(lat, (((double) fraction) * lngDelta) + a.longitude);
            }
        }

        LatLng interpolate(float f, LatLng latLng, LatLng latLng2);
    }

    public static void animateMarkerToGB(Marker marker, LatLng finalPosition, LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final Marker marker2 = marker;
        final LatLngInterpolator latLngInterpolator2 = latLngInterpolator;
        final LatLng latLng = finalPosition;
        handler.post(new Runnable() {
            long elapsed;
            float f40t;
            float f41v;

            public void run() {
                this.elapsed = SystemClock.uptimeMillis() - start;
                this.f40t = ((float) this.elapsed) / 5000.0f;
                this.f41v = interpolator.getInterpolation(this.f40t);
                marker2.setPosition(latLngInterpolator2.interpolate(this.f41v, startPosition, latLng));
                if (this.f40t < 1.0f) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @TargetApi(11)
    static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                marker.setPosition(latLngInterpolator.interpolate(animation.getAnimatedFraction(), startPosition, finalPosition));
            }
        });
        valueAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

    @TargetApi(14)
    static void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, Property.of(Marker.class, LatLng.class, "position"), typeEvaluator, new LatLng[]{finalPosition});
        animator.setDuration(3000);
        animator.start();
    }

    public static double getBearing(LatLng latLng1, LatLng latLng2) {
        double lat1 = (latLng1.latitude * 3.14159d) / 180.0d;
        double lat2 = (latLng2.latitude * 3.14159d) / 180.0d;
        double dLon = ((latLng2.longitude * 3.14159d) / 180.0d) - ((latLng1.longitude * 3.14159d) / 180.0d);
        return (360.0d + Math.toDegrees(Math.atan2(Math.sin(dLon) * Math.cos(lat2), (Math.cos(lat1) * Math.sin(lat2)) - ((Math.sin(lat1) * Math.cos(lat2)) * Math.cos(dLon))))) % 360.0d;
    }

    public static void rotateMarker(Marker marker, float toRotation) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final Interpolator interpolator = new LinearInterpolator();
        final float f = toRotation;
        final Marker marker2 = marker;
        handler.post(new Runnable() {
            public void run() {
                float t = interpolator.getInterpolation(((float) (SystemClock.uptimeMillis() - start)) / 1000.0f);
                float rot = (f * t) + ((1.0f - t) * startRotation);
                Marker marker = marker2;
                if ((-rot) > BitmapDescriptorFactory.HUE_CYAN) {
                    rot /= 2.0f;
                }
                marker.setRotation(rot);
                if (((double) t) < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    public static void animationMoveMarker(Context context, final Marker marker, final LatLng start, final LatLng end) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        valueAnimator.setDuration(5000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                double v = (double) valueAnimator.getAnimatedFraction();
                marker.setPosition(new LatLng((end.latitude * v) + ((1 - v) * start.latitude), (end.longitude * v) + ((1 - v) * start.longitude)));
            }
        });
        valueAnimator.start();
    }

    public static void animateMarker(LatLng destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = destination;
            float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            valueAnimator.setDuration(5000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        marker.setPosition(latLngInterpolator.interpolate(animation.getAnimatedFraction(), startPosition, endPosition));
                    } catch (Exception e) {
                    }
                }
            });
            valueAnimator.start();
        }
    }

    public static void animateMarker(Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());
            float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            valueAnimator.setDuration(TIME_DELAY);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        marker.setPosition(latLngInterpolator.interpolate(animation.getAnimatedFraction(), startPosition, endPosition));
                    } catch (Exception e) {
                    }
                }
            });
            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float rotation;
        float normalizedEndAbs = ((end - start) + 360.0f) % 360.0f;
        if ((normalizedEndAbs > BitmapDescriptorFactory.HUE_CYAN ? -1.0f : 1.0f) > 0.0f) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360.0f;
        }
        return (((fraction * rotation) + start) + 360.0f) % 360.0f;
    }

    public static void animationScaleMarker(GoogleMap googleMap, Marker marker, MarkerOptions markerOptions, float zoomLevel) {
    }

    public static void animatorAddRemoveMarker(final Marker marker, final boolean isAdd, final boolean isRemove) {
        if (marker != null) {
            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            valueAnimator.setDuration(1000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        if (isAdd) {
                            marker.setVisible(true);
                        } else if (isRemove) {
                            marker.remove();
                        } else if (!isAdd) {
                            marker.setVisible(false);
                        }
                    } catch (Exception e) {
                    }
                }
            });
            valueAnimator.start();
        }
    }

    public static void fadeInMarker(Context context, final Marker marker, boolean isIn) {
        int from;
        int to;
        if (isIn) {
            from = 0;
            to = 1;
        } else {
            from = 1;
            to = 0;
        }
        ValueAnimator ani = ValueAnimator.ofFloat(new float[]{(float) from, (float) to});
        ani.setDuration(500);
        ani.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                marker.setAlpha(((Float) animation.getAnimatedValue()).floatValue());
            }
        });
        ani.start();
    }

    public static void animationScale(Context context, GoogleMap map, Bitmap bitmap, Marker marker) {
        final Canvas canvas = new Canvas(bitmap);
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        animator.setDuration(500);
        final Rect originalRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF scaledRect = new RectF();
        final Bitmap bitmap2 = bitmap;
        final Marker marker2 = marker;
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = ((Float) animation.getAnimatedValue()).floatValue();
                scaledRect.set(0.0f, 0.0f, ((float) originalRect.right) * scale, ((float) originalRect.bottom) * scale);
                canvas.drawBitmap(bitmap2, originalRect, scaledRect, null);
                marker2.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap2));
            }
        });
        animator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void animationLoadHome(final View view_root) {
        Animator anim = ViewAnimationUtils.createCircularReveal(view_root, (view_root.getLeft() + view_root.getRight()) / 2, (view_root.getTop() + view_root.getBottom()) / 2, (float) view_root.getWidth(), 0.0f);
        anim.setDuration(1000);
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view_root.setVisibility(View.INVISIBLE);
            }
        });
        anim.start();
    }
}
