package com.portalbeanz.testmap;

import com.google.android.gms.maps.model.LatLng;

public interface LatLngInterpolator {

    public static class Linear implements LatLngInterpolator {
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            return new LatLng(((b.latitude - a.latitude) * ((double) fraction)) + a.latitude, ((b.longitude - a.longitude) * ((double) fraction)) + a.longitude);
        }
    }

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

//    public static class Spherical implements LatLngInterpolator {
//        public LatLng interpolate(float fraction, LatLng from, LatLng to) {
//            double fromLat = Math.toRadians(from.latitude);
//            double fromLng = Math.toRadians(from.longitude);
//            double toLat = Math.toRadians(to.latitude);
//            double toLng = Math.toRadians(to.longitude);
//            double cosFromLat = Math.cos(fromLat);
//            double cosToLat = Math.cos(toLat);
//            double angle = computeAngleBetween(fromLat, fromLng, toLat, toLng);
//            double sinAngle = Math.sin(angle);
//            if (sinAngle < 1.0E-6d) {
//                return from;
//            }
//            double a = Math.sin(((double) (1.0f - fraction)) * angle) / sinAngle;
//            double b = Math.sin(((double) fraction) * angle) / sinAngle;
//            double x = ((a * cosFromLat) * Math.cos(fromLng)) + ((b * cosToLat) * Math.cos(toLng));
//            double y = ((a * cosFromLat) * Math.sin(fromLng)) + ((b * cosToLat) * Math.sin(toLng));
//            return new LatLng(Math.toDegrees(Math.atan2((Math.sin(fromLat) * a) + (Math.sin(toLat) * b), Math.sqrt((x * x) + (y * y)))), Math.toDegrees(Math.atan2(y, x)));
//        }
//
//        private double computeAngleBetween(double fromLat, double fromLng, double toLat, double toLng) {
//            return MediaLoadOptions.PLAYBACK_RATE_MAX * Math.asin(Math.sqrt(Math.pow(Math.sin((fromLat - toLat) / MediaLoadOptions.PLAYBACK_RATE_MAX), MediaLoadOptions.PLAYBACK_RATE_MAX) + ((Math.cos(fromLat) * Math.cos(toLat)) * Math.pow(Math.sin((fromLng - toLng) / MediaLoadOptions.PLAYBACK_RATE_MAX), MediaLoadOptions.PLAYBACK_RATE_MAX))));
//        }
//    }

    LatLng interpolate(float f, LatLng latLng, LatLng latLng2);
}
