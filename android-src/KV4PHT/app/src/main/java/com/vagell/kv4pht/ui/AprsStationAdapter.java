/*
kv4p HT (see http://kv4p.com)
Copyright (C) 2024 Vance Vagell
Modified 2026 by Atley LLC: heard-station list.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package com.vagell.kv4pht.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vagell.kv4pht.R;
import com.vagell.kv4pht.aprs.AprsOperator;
import com.vagell.kv4pht.data.APRSMessage;

import java.util.ArrayList;
import java.util.List;

public class AprsStationAdapter extends RecyclerView.Adapter<AprsStationAdapter.Holder> {
    public interface StationListener {
        void onStationClick(APRSMessage station);
    }

    private final List<APRSMessage> stations = new ArrayList<>();
    private final StationListener listener;

    public AprsStationAdapter(StationListener listener) {
        this.listener = listener;
    }

    public void setStations(List<APRSMessage> next) {
        stations.clear();
        if (next != null) {
            stations.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.aprs_station_row, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        APRSMessage station = stations.get(position);
        String name = station.objName != null && !station.objName.isEmpty()
                ? station.fromCallsign + " · " + station.objName
                : station.fromCallsign;
        holder.callsign.setText(name);
        String grid = AprsOperator.maidenhead(station.positionLat, station.positionLong);
        holder.grid.setText(grid.isEmpty()
                ? ""
                : grid.toUpperCase() + "  " + station.positionLat + ", " + station.positionLong);
        holder.comment.setText(station.comment == null ? "" : station.comment);
        holder.itemView.setOnClickListener(v -> listener.onStationClick(station));
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView callsign;
        final TextView grid;
        final TextView comment;

        Holder(@NonNull View itemView) {
            super(itemView);
            callsign = itemView.findViewById(R.id.stationCallsign);
            grid = itemView.findViewById(R.id.stationGrid);
            comment = itemView.findViewById(R.id.stationComment);
        }
    }
}
