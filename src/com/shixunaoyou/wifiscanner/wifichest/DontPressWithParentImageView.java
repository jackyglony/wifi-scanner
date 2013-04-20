/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2010 Sony Ericsson Mobile Communications AB. All rights reserved.
 * This file contains confidential and proprietary information of Sony Ericsson
 * Mobile Communications AB.
 *
 * NOTE: This file contains code from:
 *
 *     /packages/apps/Contacts/src/com/android/contacts/DontPressWithParentImageView.java
 *
 * taken from The Android Open Source Project, which is licensed under the Apache
 * License, Version 2.0, which may be accessed at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Notwithstanding the foregoing, the entire contents of this file is licensed
 * under the Sony Ericsson Mobile Communications AB's End User License Agreement
 * ("EULA"). Any use of this file is subject to the terms of the EULA.
 */

package com.shixunaoyou.wifiscanner.wifichest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Special class to to allow the parent to be pressed without being pressed itself.
 * This way the line of a tab can be pressed, but the image itself is not.
 */
public class DontPressWithParentImageView extends ImageView {

    public DontPressWithParentImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setPressed(boolean pressed) {
        // If the parent is pressed, do not set to pressed.
        if (pressed && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }
}
