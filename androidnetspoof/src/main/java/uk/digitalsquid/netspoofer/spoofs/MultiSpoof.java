/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.netspoofer.spoofs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

import uk.digitalsquid.netspoofer.MultiSpoofDialogRunner;
import uk.digitalsquid.netspoofer.SpoofSelector;
import uk.digitalsquid.netspoofer.config.LogConf;
import uk.digitalsquid.netspoofer.proxy.HttpRequest;
import uk.digitalsquid.netspoofer.proxy.HttpResponse;

/**
 * A spoof which runs multiple other spoofs.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class MultiSpoof extends Spoof implements LogConf {

    public MultiSpoof() {
        // TODO: Localise
        super("Multiple spoofs", "Run multiple spoofs at once. May run slowly.");
    }
    
    private ArrayList<Spoof> selectedSpoofs;
    private ArrayList<Spoof> finalSpoofs;

    private static final long serialVersionUID = -848683524539301592L;

    @Override
    public Intent activityForResult(Context context) {
        Intent ret = new Intent(context, SpoofSelector.class);
        ret.setAction(Intent.ACTION_PICK);
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean activityFinished(Context context, Intent intent) {
        selectedSpoofs = (ArrayList<Spoof>) intent.getSerializableExtra("uk.digitalsquid.netspoof.SpoofSelector.spoofs");
        return true;
    }

    @Override
    public Intent activityForResult2(Context context) {
        Intent ret = new Intent(context, MultiSpoofDialogRunner.class);
        ret.putExtra(MultiSpoofDialogRunner.SPOOF_LIST, selectedSpoofs);
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean activityFinished2(Context context, Intent result) {
        finalSpoofs = (ArrayList<Spoof>) result.getSerializableExtra(MultiSpoofDialogRunner.SPOOF_LIST);
        return true;
    }
    
    @Override public Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone) { return null; }
    
    public ArrayList<Spoof> getSpoofs() {
        return finalSpoofs;
    }
    
    // These functions do nothing; MultiSpoof is never actually used (its inner
    // spoofs are expanded before runtime)

    @Override
    public void modifyRequest(HttpRequest request) {
        Log.e(TAG, "MultiSpoof.modifyRequest called!");
    }

    @Override
    public void modifyResponse(HttpResponse response, HttpRequest request) {
        Log.e(TAG, "MultiSpoof.modifyResponse called!");
    }
}
