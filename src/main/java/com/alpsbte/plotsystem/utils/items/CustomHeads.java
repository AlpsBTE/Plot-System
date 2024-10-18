/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.utils.items;

public enum CustomHeads {
    WHITE_CONCRETE("8614"),
    GREEN_CONCRETE("8621"),
    YELLOW_CONCRETE("8613"),
    RED_CONCRETE("8616"),
    WORKBENCH("24180"),
    ADD_BUTTON("9237"),
    REMOVE_BUTTON("9243"),
    BACK_BUTTON("9226"),
    NEXT_BUTTON("9223"),
    PREVIOUS_BUTTON("9226"),
    INFO_BUTTON("46488"),
    GLOBE_HEAD("49973"),
    PLOT_TYPE_BUTTON("4159"),
    FOCUS_MODE_BUTTON("38199"),
    CITY_INSPIRATION_MODE_BUTTON("38094");

    final String id;

    CustomHeads(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
