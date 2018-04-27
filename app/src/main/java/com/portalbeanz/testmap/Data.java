package com.portalbeanz.testmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public Data() {
        listRoutes = new ArrayList<>();
    }

    @SerializedName("list")
    List<String> listRoutes;

    public List<String> getList() {
        return listRoutes;
    }

    public void setList(List<String> list) {
        this.listRoutes = list;
    }
}
