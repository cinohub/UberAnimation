# Umber Car Animation


Here is an example to show car's animation on the Map.
# New Features!
  - Car move from A position to B when user searched.
  - Display a route to connect A to B position 
  - Display window Marker
  
  
you can clone this repo to take a look the code. For this time Uber car's animation is done.
Here is demo : 
[![N|Demo Video](https://github.com/duongnv1996/UberAnimation/raw/master/device-2018-04-26-153807.png) ](https://www.youtube.com/watch?v=069C270LSOY?t=35s)


# Installation
  - Animation Marker on map function
  ```sh
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
                        marker.setPosition(latLngInterpolator.interpolate(animation.getAnimatedFraction(),startPosition, endPosition));
                    } catch (Exception e) {
                    }
                }
            });
            valueAnimator.start();
        }
    }
  ```
  - Rotate Marker function
  ```sh
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
  ```


The main idea to animate the marker smoothly is that run handler in loop with the same delay time. In this example we define the delay time is : 

        public static final long TIME_DELAY=1000;
        
Now is testing animation with sample app, you can look inside MapsActivity.java for full source code : 
```sh
    Runnable runnable =new Runnable() {
        @Override
        public void run() {
            if (index < listRoutes.size() - 1) {
                index++;
                // next = index +1;
            }
            updateMarker(marker, listRoutes.get(index));
            handler.postDelayed(this, MarkerAnimation.TIME_DELAY);
        }
    };
    private void startAnimation() {
        handler.removeCallbacks(runnable);
        marker = mMap.addMarker(new MarkerOptions().position(listRoutes.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_marker_15)));
        marker.setAnchor(0.5f, 0.5f);
        marker.setFlat(true);
        MarkerAnimation.rotateMarker(marker, new Random().nextFloat() * 360.0f);
        MarkerAnimation.fadeInMarker(this, marker, true);
        index = -1;
        handler.postDelayed(runnable,1000);
    }
    public void updateMarker(Marker marker, LatLng newLocaiton) {
        if (marker != null) {
            Location prevLoc = new Location("gps");
            prevLoc.setLatitude(marker.getPosition().latitude);
            prevLoc.setLongitude(marker.getPosition().longitude);
            Location newLoc = new Location("gps");
            newLoc.setLatitude(newLocaiton.latitude);
            newLoc.setLongitude(newLocaiton.longitude);
            MarkerAnimation.animateMarker(newLoc, marker);
            double bearing = MarkerAnimation.getBearing(marker.getPosition(), new LatLng(newLoc.getLatitude(), newLoc.getLongitude()));
            if (bearing > 0.0d) {
                MarkerAnimation.rotateMarker(marker, (float) bearing);
            }
        }
    }
```
when we call startAnimation() function. The animation will be animate and marker will smoothly move from A position to B position. Easy, right?

### Todos

 - Display a route animation to connect A to B position 
 - Display window Marker animation
 
All things are the same with Uber done.



###License
----

MIT



