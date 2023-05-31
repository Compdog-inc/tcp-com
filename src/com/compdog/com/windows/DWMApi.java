package com.compdog.com.windows;

import com.eaio.nativecall.*;

public class DWMApi {

    private static final System.Logger logger = System.getLogger("DWMApi");

    public static int DwmSetWindowAttribute(long hwnd, DwmWindowAttribute attribute, int value, int size){
        try {
            NativeCall.init();
        } catch (Exception e){
            logger.log(System.Logger.Level.ERROR, "Error initializing NativeCall: "+e.getMessage());
        }

        IntCall ic = new IntCall("DwmSetWindowAttribute");
        int r = ic.executeCall(new Object[] {
                hwnd, attribute.getValue(), value, size });
        ic.destroy();
        return r;
    }

}
